package com.studyplanner.smartstudyplannerfx;

import java.sql.*;
import java.util.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
    private int currentSubjectId;
    private String currentSubjectName;
    private VBox currentContentArea;

    public void loadSyllabus() throws Exception {
        Connection con = DBConnection.getConnection();
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
        
        // load the data
        
        try {
            Connection con = DBConnection.getConnection();
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
                    showAlert("Please enter a topic name!", Alert.AlertType.WARNING);
                    return null;
                }
                
                try {
                    Connection con = DBConnection.getConnection();
                    int parentId = 0;
                    
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
                    
                    PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO syllabus(subject_id, parent_topic_id, topic_name) VALUES(?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, subjectId);
                    ps.setInt(2, parentId);
                    ps.setString(3, topicName);
                    ps.executeUpdate();
                    
                    showAlert("Added topic: " + topicName, Alert.AlertType.INFORMATION);
                    refreshSyllabusView(subjectId, subjectName, parentContainer);
                    
                } catch (Exception ex) {
                    showAlert("Error adding topic: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }

   //here update topics
    
    private void showUpdateTopicDialog(int topicId, String currentName, int subjectId, VBox parentContainer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Topic");
        dialog.setHeaderText("Edit topic name");
        
        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);
        
        VBox form = new VBox(10);
        form.setPadding(new Insets(15));
        
        TextField topicField = new TextField(currentName);
        topicField.setPromptText("New Topic Name");
        
        form.getChildren().addAll(
            new Label("Current Name: " + currentName),
            new Label("New Name:"), topicField
        );
        
        dialog.getDialogPane().setContent(form);
        
        dialog.setResultConverter(btn -> {
            if(btn == updateBtn) {
                String newName = topicField.getText().trim();
                
                if(newName.isEmpty()) {
                    showAlert("Please enter a topic name!", Alert.AlertType.WARNING);
                    return null;
                }
                
                if(newName.equals(currentName)) {
                    showAlert("No changes made!", Alert.AlertType.INFORMATION);
                    return null;
                }
                
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE syllabus SET topic_name=? WHERE topic_id=?"
                    );
                    ps.setString(1, newName);
                    ps.setInt(2, topicId);
                    
                    int rows = ps.executeUpdate();
                    if(rows > 0) {
                        showAlert("Updated successfully!", Alert.AlertType.INFORMATION);
                        refreshSyllabusView(subjectId, currentSubjectName, parentContainer);
                    } else {
                        showAlert("Update failed!", Alert.AlertType.ERROR);
                    }
                    
                } catch (Exception ex) {
                    showAlert("Error updating topic: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }

  //delete
    
    private void deleteTopic(int topicId, String topicName, int subjectId, VBox parentContainer) {
        // Check if topic has children
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement checkPs = con.prepareStatement(
                "SELECT COUNT(*) as child_count FROM syllabus WHERE parent_topic_id=?"
            );
            checkPs.setInt(1, topicId);
            ResultSet rs = checkPs.executeQuery();
            
            boolean hasChildren = false;
            if(rs.next() && rs.getInt("child_count") > 0) {
                hasChildren = true;
            }
            
            String confirmMsg = hasChildren 
                ? "⚠️ '" + topicName + "' has sub-topics!\n\nDeleting this will also delete ALL sub-topics.\n\nAre you sure?"
                : "Delete topic '" + topicName + "'?\n\nThis action cannot be undone.";
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText(null);
            confirm.setContentText(confirmMsg);
            
            Optional<ButtonType> result = confirm.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK) {
                // Delete all children recursively first, then delete the topic
                deleteTopicRecursive(topicId);
                
                showAlert("Deleted: " + topicName, Alert.AlertType.INFORMATION);
                refreshSyllabusView(subjectId, currentSubjectName, parentContainer);
            }
            
        } catch (Exception ex) {
            showAlert("Error deleting topic: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void deleteTopicRecursive(int topicId) throws SQLException {
        Connection con = DBConnection.getConnection();
        
        // First delete all children
        PreparedStatement findChildren = con.prepareStatement(
            "SELECT topic_id FROM syllabus WHERE parent_topic_id=?"
        );
        findChildren.setInt(1, topicId);
        ResultSet rs = findChildren.executeQuery();
        
        while(rs.next()) {
            deleteTopicRecursive(rs.getInt("topic_id"));
        }
        
        // Then delete this topic
        PreparedStatement deletePs = con.prepareStatement(
            "DELETE FROM syllabus WHERE topic_id=?"
        );
        deletePs.setInt(1, topicId);
        deletePs.executeUpdate();
    }


    
    private void refreshSyllabusView(int subjectId, String subjectName, VBox contentArea) {
        this.currentSubjectId = subjectId;
        this.currentSubjectName = subjectName;
        this.currentContentArea = contentArea;
        
        contentArea.getChildren().clear();
        
        try {
            Connection con = DBConnection.getConnection();
            
            Label subjTitle = new Label("📖 " + subjectName);
            subjTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
            subjTitle.setTextFill(Color.web("#2c3e50"));
            
            Button addTopicBtn = new Button("➕ Add Topic");
            addTopicBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15;");
            addTopicBtn.setOnAction(ev -> showAddTopicDialog(subjectId, subjectName, contentArea));
            
            HBox header = new HBox(20);
            header.setAlignment(Pos.CENTER_LEFT);
            header.getChildren().addAll(subjTitle, addTopicBtn);
            
            contentArea.getChildren().add(header);
            
            // Build interactive tree with edit/delete buttons
            VBox treeContainer = new VBox(5);
            treeContainer.setPadding(new Insets(10));
            
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM syllabus WHERE subject_id=? ORDER BY parent_topic_id, topic_id"
            );
            ps.setInt(1, subjectId);
            ResultSet rs = ps.executeQuery();
            
            Map<Integer, HBox> topicRows = new HashMap<>();
            Map<Integer, Integer> parentMap = new HashMap<>();
            List<Integer> rootTopics = new ArrayList<>();
            
            // First pass: collect all topics
            while(rs.next()) {
                int topicId = rs.getInt("topic_id");
                String topicName = rs.getString("topic_name");
                int parentId = rs.getInt("parent_topic_id");
                
                parentMap.put(topicId, parentId);
                
                HBox row = createTopicRow(topicId, topicName, parentId, subjectId, contentArea);
                topicRows.put(topicId, row);
                
                if(parentId == 0) {
                    rootTopics.add(topicId);
                }
            }
            
            // Second pass: build hierarchy
            for(int topicId : rootTopics) {
                addTopicToContainer(treeContainer, topicId, topicRows, parentMap, 0);
            }
            
            if(rootTopics.isEmpty()) {
                Label emptyLabel = new Label("No topics yet. Click 'Add Topic' to create your syllabus!");
                emptyLabel.setTextFill(Color.web("#7f8c8d"));
                emptyLabel.setFont(Font.font("System", 14));
                contentArea.getChildren().add(emptyLabel);
            } else {
                ScrollPane scroll = new ScrollPane(treeContainer);
                scroll.setFitToWidth(true);
                scroll.setStyle("-fx-background-color: transparent;");
                contentArea.getChildren().add(scroll);
            }
            
        } catch (Exception ex) {
            contentArea.getChildren().add(new Label("Error: " + ex.getMessage()));
        }
    }
    
    private HBox createTopicRow(int topicId, String topicName, int parentId, int subjectId, VBox contentArea) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));
        row.setId("topic-row-" + topicId);
        
        // Topic label with icon
        Label label = new Label("📂 " + topicName);
        label.setFont(Font.font("System", 14));
        label.setTextFill(Color.web("#2c3e50"));
        label.setStyle("-fx-background-color: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #bdc3c7; -fx-border-width: 1;");
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);
        
        // Edit button
        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10;");
        editBtn.setTooltip(new Tooltip("Edit Topic"));
        editBtn.setOnAction(e -> showUpdateTopicDialog(topicId, topicName, subjectId, contentArea));
        
        // Delete button
        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");
        deleteBtn.setTooltip(new Tooltip("Delete Topic"));
        deleteBtn.setOnAction(e -> deleteTopic(topicId, topicName, subjectId, contentArea));
        
        // Add sub-topic button
        Button addSubBtn = new Button("➕");
        addSubBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10;");
        addSubBtn.setTooltip(new Tooltip("Add Sub-Topic"));
        addSubBtn.setOnAction(e -> showAddSubTopicDialog(topicId, topicName, subjectId, contentArea));
        
        row.getChildren().addAll(label, addSubBtn, editBtn, deleteBtn);
        return row;
    }
    
    private void addTopicToContainer(VBox container, int topicId, Map<Integer, HBox> topicRows, 
                                     Map<Integer, Integer> parentMap, int level) {
        HBox row = topicRows.get(topicId);
        if(row != null) {
            // Apply indentation
            row.setPadding(new Insets(5, 0, 5, level * 30));
            container.getChildren().add(row);
            
            // Find and add children
            for(Map.Entry<Integer, Integer> entry : parentMap.entrySet()) {
                if(entry.getValue() == topicId) {
                    addTopicToContainer(container, entry.getKey(), topicRows, parentMap, level + 1);
                }
            }
        }
    }
    
    //
    
    private void showAddSubTopicDialog(int parentTopicId, String parentName, int subjectId, VBox contentArea) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Sub-Topic");
        dialog.setHeaderText("Add sub-topic to: " + parentName);
        
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        
        VBox form = new VBox(10);
        form.setPadding(new Insets(15));
        
        TextField topicField = new TextField();
        topicField.setPromptText("Sub-Topic Name");
        
        form.getChildren().addAll(
            new Label("Parent: " + parentName),
            new Label("Sub-Topic Name:"), topicField
        );
        
        dialog.getDialogPane().setContent(form);
        
        dialog.setResultConverter(btn -> {
            if(btn == addBtn) {
                String topicName = topicField.getText().trim();
                
                if(topicName.isEmpty()) {
                    showAlert("Please enter a sub-topic name!", Alert.AlertType.WARNING);
                    return null;
                }
                
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO syllabus(subject_id, parent_topic_id, topic_name) VALUES(?,?,?)"
                    );
                    ps.setInt(1, subjectId);
                    ps.setInt(2, parentTopicId);
                    ps.setString(3, topicName);
                    ps.executeUpdate();
                    
                    showAlert("Added sub-topic: " + topicName, Alert.AlertType.INFORMATION);
                    refreshSyllabusView(subjectId, currentSubjectName, contentArea);
                    
                } catch (Exception ex) {
                    showAlert("Error adding sub-topic: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }


    
    public Map<Integer, String> getTopicMap() {
        Map<Integer, String> topics = new HashMap<>();
        for(Map.Entry<Integer, SyllabusNode> entry : nodes.entrySet()) {
            topics.put(entry.getKey(), entry.getValue().name);
        }
        return topics;
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : 
                      type == Alert.AlertType.WARNING ? "Warning" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}