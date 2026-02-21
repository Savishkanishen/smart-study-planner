package com.studyplanner.smartstudyplannerfx;

import java.sql.*;
import java.util.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StudyGraph {
    private Map<Integer, List<Integer>> graph = new HashMap<>();
    private Map<Integer, String> subjectNames = new HashMap<>();

    public void loadSubjects() throws Exception {
        Connection con = DBConnection.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM subjects");
        while(rs.next()){
            int id = rs.getInt("subject_id");
            String name = rs.getString("subject_name");
            graph.putIfAbsent(id, new ArrayList<>());
            subjectNames.put(id, name);
        }
        rs = st.executeQuery("SELECT * FROM prerequisites");
        while(rs.next()){
            int s = rs.getInt("subject_id");
            int pre = rs.getInt("prerequisite_id");
            graph.get(s).add(pre);
        }
    }

    public VBox getStudyPathView() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f7fa, #c3cfe2);");
        
        Label title = new Label("📚 Recommended Study Path");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        Set<Integer> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        
        for(int id : graph.keySet()){
            dfs(id, visited, path);
        }
        
        VBox pathBox = new VBox(8);
        pathBox.setPadding(new Insets(15));
        
        for(int i = 0; i < path.size(); i++) {
            HBox step = new HBox(10);
            step.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label arrow = new Label("➡");
            arrow.setStyle("-fx-font-size: 18px; -fx-text-fill: #3498db;");
            
            Label subject = new Label((i+1) + ". " + path.get(i));
            subject.setFont(Font.font("System", 16));
            subject.setTextFill(Color.web("#2c3e50"));
            subject.setStyle("-fx-background-color: white; -fx-padding: 10 20; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            step.getChildren().addAll(arrow, subject);
            pathBox.getChildren().add(step);
        }
        
        ScrollPane scroll = new ScrollPane(pathBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        
        container.getChildren().addAll(title, scroll);
        return container;
    }

    
    private void dfs(int id, Set<Integer> visited, List<String> path){
        if(visited.contains(id)) return;
        visited.add(id);
        for(int pre : graph.get(id)){
            dfs(pre, visited, path);
        }
        path.add(subjectNames.get(id));
    }
    
    public Map<Integer, String> getSubjectMap() { return subjectNames; }
}