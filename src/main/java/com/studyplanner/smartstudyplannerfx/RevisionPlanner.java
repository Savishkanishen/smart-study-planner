package com.studyplanner.smartstudyplannerfx;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;  // ADD THIS IMPORT
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

class Topic {
    int id, score;
    String name;
    Topic(int id, String name, int score){ this.id=id; this.name=name; this.score=score; }
}

public class RevisionPlanner {
    private PriorityQueue<Topic> heap = new PriorityQueue<>(Comparator.comparingInt(t->t.score));
    private List<Topic> weakTopics = new ArrayList<>();

public void loadPerformance(int studentId) throws Exception {
    heap.clear();
    weakTopics.clear();
    Connection con = DBConnection.getConnection();
    
    // Only load topic scores (original logic - works with existing DB)
    PreparedStatement ps = con.prepareStatement(
        "SELECT p.topic_id, s.topic_name, p.score FROM performance p " +
        "JOIN syllabus s ON p.topic_id = s.topic_id WHERE p.student_id=?"
    );
    ps.setInt(1, studentId);
    ResultSet rs = ps.executeQuery();
    
    while(rs.next()){
        Topic t = new Topic(rs.getInt("topic_id"), rs.getString("topic_name"), rs.getInt("score"));
        heap.add(t);
        if(t.score < 60) weakTopics.add(t);
    }
}



// Add these methods to RevisionPlanner class:

public int getWeakTopicsCount() {
    return weakTopics.size();
}

public PriorityQueue<Topic> getWeakTopics() {
    // Return a copy of weak topics sorted by score (lowest first)
    PriorityQueue<Topic> weak = new PriorityQueue<>(Comparator.comparingInt(t->t.score));
    weak.addAll(weakTopics);
    return weak;
}

public void addPerformance(int studentId, int topicId, int score) throws Exception {
    Connection con = DBConnection.getConnection();
    PreparedStatement ps = con.prepareStatement(
        "INSERT INTO performance(student_id, topic_id, score) VALUES(?,?,?) " +
        "ON DUPLICATE KEY UPDATE score=?, last_updated=NOW()"
    );
    ps.setInt(1, studentId);
    ps.setInt(2, topicId);
    ps.setInt(3, score);
    ps.setInt(4, score);
    ps.executeUpdate();
    
    // Refresh local data
    loadPerformance(studentId);
}

public List<Topic> getAllTopics() {
    List<Topic> all = new ArrayList<>();
    all.addAll(heap);
    return all;
}
    
    
    
    public VBox getRevisionView(int studentId) {
        
        
        if(heap.isEmpty()) {
    VBox emptyState = new VBox(20);
    emptyState.setAlignment(Pos.CENTER);
    emptyState.setPadding(new Insets(50));
    
    Label icon = new Label("📝");
    icon.setFont(Font.font("System", 72));
    
    Label msg = new Label("No performance data yet!");
    msg.setFont(Font.font("System", FontWeight.BOLD, 24));
    msg.setTextFill(Color.web("#7f8c8d"));
    
    Label hint = new Label("Click '➕ Add Performance' to enter your marks\nand see your weak points automatically.");
    hint.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
    hint.setTextFill(Color.web("#95a5a6"));
    
    emptyState.getChildren().addAll(icon, msg, hint);
    return emptyState;
}
        
        
        
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffecd2, #fcb69f);");
        
        Label title = new Label("🔥 Priority Revision Plan");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#c0392b"));
        
        // Stats cards
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        int avgScore = heap.isEmpty() ? 0 : (int) heap.stream().mapToInt(t->t.score).average().getAsDouble();
        
        VBox weakCard = createStatCard("⚠️ Weak Topics", String.valueOf(weakTopics.size()), "#e74c3c");
        VBox avgCard = createStatCard("📊 Average Score", avgScore + "%", "#f39c12");
        VBox totalCard = createStatCard("📝 Total Topics", String.valueOf(heap.size()), "#27ae60");
        
        statsBox.getChildren().addAll(weakCard, avgCard, totalCard);
        
        // Priority list
        VBox listBox = new VBox(10);
        listBox.setPadding(new Insets(15));
        listBox.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 10;");
        
        Label listTitle = new Label("Weakest Topics First - Study These!");
        listTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        listTitle.setTextFill(Color.web("#2c3e50"));
        
        listBox.getChildren().add(listTitle);
        
        PriorityQueue<Topic> tempHeap = new PriorityQueue<>(heap);
        int count = 0;
        while(!tempHeap.isEmpty() && count < 10) {
            Topic t = tempHeap.poll();
            HBox topicRow = new HBox(15);
            topicRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            topicRow.setPadding(new Insets(10));
            topicRow.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
            
            String emoji = t.score < 50 ? "🔴" : (t.score < 70 ? "🟡" : "🟢");
            Label nameLbl = new Label(emoji + " " + t.name);
            nameLbl.setFont(Font.font("System", 14));
            
            Label scoreLbl = new Label(t.score + "%");
            scoreLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            scoreLbl.setTextFill(t.score < 50 ? Color.web("#e74c3c") : (t.score < 70 ? Color.web("#f39c12") : Color.web("#27ae60")));
            
            ProgressBar bar = new ProgressBar(t.score / 100.0);
            bar.setPrefWidth(200);
            bar.setStyle("-fx-accent: " + (t.score < 50 ? "#e74c3c" : (t.score < 70 ? "#f39c12" : "#27ae60")) + ";");
            
            topicRow.getChildren().addAll(nameLbl, bar, scoreLbl);
            HBox.setHgrow(nameLbl, javafx.scene.layout.Priority.ALWAYS);
            listBox.getChildren().add(topicRow);
            count++;
        }
        
        // Study Guide Button
        Button guideBtn = new Button("📋 Generate Study Guide");
        guideBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 25; -fx-cursor: hand;");
        guideBtn.setOnMouseEntered(e -> guideBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(142,68,173,0.4), 10, 0, 0, 0);"));
        guideBtn.setOnMouseExited(e -> guideBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 25; -fx-cursor: hand;"));
        guideBtn.setOnAction(e -> showStudyGuide());
        
        container.getChildren().addAll(title, statsBox, listBox, guideBtn);
        return container;
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(150);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        
        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 28));
        valLbl.setTextFill(Color.web(color));
        
        Label titLbl = new Label(title);
        titLbl.setFont(Font.font("System", 12));
        titLbl.setTextFill(Color.web("#7f8c8d"));
        
        card.getChildren().addAll(valLbl, titLbl);
        return card;
    }
    
    
    
    
    private void showStudyGuide() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Personalized Study Guide");
        alert.setHeaderText("📚 Your Custom Study Plan");
        
        StringBuilder guide = new StringBuilder();
        guide.append("Based on your performance analysis:\n\n");
        
        if(weakTopics.isEmpty()) {
            guide.append("✅ Great job! You have no weak topics. Keep maintaining your scores!\n");
        } else {
            guide.append("🎯 PRIORITY ACTIONS:\n");
            guide.append("1. Focus on these weak areas first (score < 60%):\n");
            // FIXED LINE HERE - Using Collectors.toList() instead of toList()
            for(Topic t : weakTopics.stream().limit(5).collect(Collectors.toList())) {
                guide.append("   • ").append(t.name).append(" (").append(t.score).append("%) - Suggested: 2-3 hours revision\n");
            }
            guide.append("\n⏰ RECOMMENDED SCHEDULE:\n");
            guide.append("• Week 1: Cover bottom 3 topics (Intensive revision)\n");
            guide.append("• Week 2: Practice tests on weak areas\n");
            guide.append("• Week 3: Review medium performance topics\n");
            guide.append("\n💡 TIPS:\n");
            guide.append("• Use active recall for topics below 50%\n");
            guide.append("• Create mind maps for complex subjects\n");
            guide.append("• Take breaks every 45 minutes\n");
        }
        
        alert.setContentText(guide.toString());
        alert.getDialogPane().setStyle("-fx-background-color: #f8f9fa;");
        alert.showAndWait();
    }
}