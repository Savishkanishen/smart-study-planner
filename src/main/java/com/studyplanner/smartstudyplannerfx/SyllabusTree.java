package com.studyplanner.smartstudyplannerfx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

class SyllabusNode {
    int id;
    String name;
    List<SyllabusNode> children = new ArrayList<>();
    SyllabusNode(int id, String name){ this.id = id; this.name = name; }
    void addSubTopic(SyllabusNode node){ children.add(node); }
}

public class SyllabusTree {
    private Map<Integer, SyllabusNode> nodes = new HashMap<>();
    private SyllabusNode root = new SyllabusNode(0,"Root");

    public void loadSyllabus() throws Exception {
       Connection con = SQLiteConnection.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM syllabus");
        while(rs.next()){
            int id = rs.getInt("topic_id");
            String name = rs.getString("topic_name");
            int parent = rs.getInt("parent_topic_id");
            SyllabusNode node = new SyllabusNode(id,name);
            nodes.put(id,node);
            if(parent==0) root.addSubTopic(node);
            else nodes.get(parent).addSubTopic(node);
        }
    }

    public VBox getTreeView() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);");
        
        Label title = new Label("Syllabus Structure");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        VBox treeBox = new VBox(5);
        treeBox.setPadding(new Insets(10));
        
        for(SyllabusNode child : root.children) {
            buildTreeUI(child, treeBox, 0);
        }
        
        ScrollPane scroll = new ScrollPane(treeBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        
        container.getChildren().addAll(title, scroll);
        return container;
    }
    
    private void buildTreeUI(SyllabusNode node, VBox parent, int level) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, level * 30));
        
        String indent = "  ".repeat(level);
        Label label = new Label(indent + "📂 " + node.name);
        label.setFont(Font.font("System", 14));
        label.setTextFill(Color.web("#34495e"));
        label.setStyle("-fx-background-color: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #bdc3c7; -fx-border-width: 1;");
        
        row.getChildren().add(label);
        parent.getChildren().add(row);
        
        for(SyllabusNode child : node.children) {
            buildTreeUI(child, parent, level + 1);
        }
    }
    
    
    
    
    
    private void showAddTopicDialog(int subjectId, String subjectName, VBox parentContainer) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Add Topic to " + subjectName);
    dialog.setHeaderText("Create new topic or sub-topic");
    
    ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
    
    VBox form = new VBox(10);
    form.setPadding(new Insets(15));
    
    TextField topicField = new TextField();
    topicField.setPromptText("Topic Name (e.g., Chapter 1: Basics)");
    
    ComboBox<String> parentCombo = new ComboBox<>();
    parentCombo.setPromptText("Parent Topic (optional - for sub-topics)");
    parentCombo.setMaxWidth(Double.MAX_VALUE);
    
    // potential parents
    try {
       Connection con = SQLiteConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "SELECT topic_id, topic_name FROM syllabus WHERE subject_id=? AND parent_topic_id=0"
        );
        ps.setInt(1, subjectId);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            parentCombo.getItems().add(rs.getString("topic_name"));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    Label hint = new Label("Leave 'Parent Topic' empty to create a main chapter");
    hint.setFont(Font.font("System", 11));
    hint.setTextFill(Color.web("#7f8c8d"));
    
    form.getChildren().addAll(
        new Label("Topic Name:"), topicField,
        new Label("Parent Topic (Optional):"), parentCombo, hint
    );
    
    dialog.getDialogPane().setContent(form);
    
    
    dialog.setResultConverter(btn -> {
        if(btn == addBtn) {
            String topicName = topicField.getText().trim();
            String parentName = parentCombo.getValue();
            
            if(topicName.isEmpty()) {
                showAlert("Please enter a topic name!");
                return null;
            }
            
            try {
                Connection con = SQLiteConnection.getConnection();
                int parentId = 0; 
                
                // If parent selected, get its ID
                if(parentName != null && !parentName.isEmpty()) {
                    PreparedStatement ps = con.prepareStatement(
                        "SELECT topic_id FROM syllabus WHERE topic_name=? AND subject_id=?"
                    );
                    ps.setString(1, parentName);
                    ps.setInt(2, subjectId);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        parentId = rs.getInt("topic_id");
                    }
                }
                
                //topic
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO syllabus(subject_id, parent_topic_id, topic_name) VALUES(?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setInt(1, subjectId);
                ps.setInt(2, parentId);
                ps.setString(3, topicName);
                ps.executeUpdate();
                
                showAlert("Added topic: " + topicName);
                
                
                refreshSyllabusView(subjectId, subjectName, parentContainer);
                
            } catch (Exception ex) {
                showAlert("Error adding topic: " + ex.getMessage());
            }
        }
        return null;
    });
    
    dialog.showAndWait();
}


private void refreshSyllabusView(int subjectId, String subjectName, VBox contentArea) {
    contentArea.getChildren().clear();
    
    try {
        Connection con = SQLiteConnection.getConnection();
        
        Label subjTitle = new Label("📖 " + subjectName);
        subjTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        subjTitle.setTextFill(Color.web("#2c3e50"));
        
        Button addTopicBtn = new Button("➕ Add Topic to this Subject");
        addTopicBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15;");
        addTopicBtn.setOnAction(ev -> showAddTopicDialog(subjectId, subjectName, contentArea));
        
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(subjTitle, addTopicBtn);
        
        contentArea.getChildren().add(header);
        
        

        TreeView<String> treeView = new TreeView<>();
        TreeItem<String> rootItem = new TreeItem<>("Topics");
        rootItem.setExpanded(true);
        
        PreparedStatement ps = con.prepareStatement("SELECT * FROM syllabus WHERE subject_id=?");
        ps.setInt(1, subjectId);
        ResultSet rs = ps.executeQuery();
        
        Map<Integer, TreeItem<String>> itemMap = new HashMap<>();
        List<Integer[]> relations = new ArrayList<>();
        
        while(rs.next()) {
            int topicId = rs.getInt("topic_id");
            String topicName = rs.getString("topic_name");
            int parentId = rs.getInt("parent_topic_id");
            
            TreeItem<String> item = new TreeItem<>("📂 " + topicName);
            item.setExpanded(true);
            itemMap.put(topicId, item);
            
            if(parentId == 0) {
                rootItem.getChildren().add(item);
            } else {
                relations.add(new Integer[]{topicId, parentId});
            }
        }
        
        
        
        for(Integer[] rel : relations) {
            TreeItem<String> child = itemMap.get(rel[0]);
            TreeItem<String> parent = itemMap.get(rel[1]);
            if(parent != null && child != null) {
                parent.getChildren().add(child);
            }
        }
        
        treeView.setRoot(rootItem);
        treeView.setStyle("-fx-font-size: 14px;");
        
        if(rootItem.getChildren().isEmpty()) {
            contentArea.getChildren().add(new Label("No topics yet. Click 'Add Topic' to create your syllabus!"));
        } else {
            contentArea.getChildren().add(treeView);
        }
        
    } catch (Exception ex) {
        contentArea.getChildren().add(new Label("Error refreshing: " + ex.getMessage()));
    }
}
    
    
    
    public Map<Integer, String> getTopicMap() {
        Map<Integer, String> topics = new HashMap<>();
        for(Map.Entry<Integer, SyllabusNode> entry : nodes.entrySet()) {
            topics.put(entry.getKey(), entry.getValue().name);
        }
        return topics;
    }

    private void showAlert(String please_enter_a_topic_name) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}