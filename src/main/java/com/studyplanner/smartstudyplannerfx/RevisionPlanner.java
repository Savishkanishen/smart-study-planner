package com.studyplanner.smartstudyplannerfx;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Consumer; // Import for callback
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
        
        // Load topic scores with subject info for display
        PreparedStatement ps = con.prepareStatement(
            "SELECT p.topic_id, s.topic_name, p.score, p.subject_id, sub.subject_name as subject_name " +
            "FROM performance p " +
            "JOIN syllabus s ON p.topic_id = s.topic_id " +
            "JOIN subjects sub ON p.subject_id = sub.subject_id " +
            "WHERE p.student_id=?"
        );
        ps.setInt(1, studentId);
        ResultSet rs = ps.executeQuery();
        
        while(rs.next()){
            int topicId = rs.getInt("topic_id");
            String topicName = rs.getString("topic_name");
            int score = rs.getInt("score");
            
            // Check if this is an "Overall Score" dummy topic
            if(topicName.contains("📊 Overall Score")) {
                String subjectName = rs.getString("subject_name");
                topicName = "📚 " + subjectName + " (Full Syllabus)";
            }
            
            Topic t = new Topic(topicId, topicName, score);
            heap.add(t);
            if(t.score < 60) weakTopics.add(t);
        }
    }

    public int getWeakTopicsCount() {
        return weakTopics.size();
    }

    public PriorityQueue<Topic> getWeakTopics() {
        PriorityQueue<Topic> weak = new PriorityQueue<>(Comparator.comparingInt(t->t.score));
        weak.addAll(weakTopics);
        return weak;
    }

    public List<Topic> getAllTopics() {
        List<Topic> all = new ArrayList<>();
        all.addAll(heap);
        return all;
    }
    
    // Modified to accept a refresh callback
    public VBox getRevisionView(int studentId, Runnable refreshCallback) {
        if(heap.isEmpty()) {
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(50));
            
            Label icon = new Label("📝");
            icon.setFont(Font.font("System", 72));
            
            Label msg = new Label("No performance data yet!");
            msg.setFont(Font.font("System", FontWeight.BOLD, 24));
            msg.setTextFill(Color.web("#7f8c8d"));
            
            Label hint = new Label("Click '➕ Add Score' to enter your marks\nand see your weak points automatically.");
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
        statsBox.setAlignment(Pos.CENTER);
        
        int avgScore = heap.isEmpty() ? 0 : (int) heap.stream().mapToInt(t->t.score).average().getAsDouble();
        long weakCount = heap.stream().filter(t -> t.score < 60).count();
        
        VBox weakCard = createStatCard("⚠️ Weak Areas", String.valueOf(weakCount), "#e74c3c");
        VBox avgCard = createStatCard("📊 Average Score", avgScore + "%", "#f39c12");
        VBox totalCard = createStatCard("📝 Total Scored", String.valueOf(heap.size()), "#27ae60");
        
        statsBox.getChildren().addAll(weakCard, avgCard, totalCard);
        
        // ScrollPane for the list to handle many items
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox listBox = new VBox(10);
        listBox.setPadding(new Insets(15));
        listBox.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 10;");
        
        Label listTitle = new Label("Weakest Areas First - Study These!");
        listTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        listTitle.setTextFill(Color.web("#2c3e50"));
        
        listBox.getChildren().add(listTitle);
        
        PriorityQueue<Topic> tempHeap = new PriorityQueue<>(heap);
        int count = 0;
        
        while(!tempHeap.isEmpty() && count < 10) {
            Topic t = tempHeap.poll();
            HBox topicRow = createTopicRow(t, studentId, refreshCallback);
            listBox.getChildren().add(topicRow);
            count++;
        }
        
        scrollPane.setContent(listBox);
        
        // Clear All Button
        HBox clearBox = new HBox();
        clearBox.setAlignment(Pos.CENTER_RIGHT);
        Button clearAllBtn = new Button("🗑 Clear All My Scores");
        clearAllBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5;");
        clearAllBtn.setOnAction(e -> clearAllScores(studentId, refreshCallback));
        clearBox.getChildren().add(clearAllBtn);
        
        // Study Guide Button
        Button guideBtn = new Button("📋 Generate Study Guide");
        guideBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 25; -fx-cursor: hand;");
        guideBtn.setOnAction(e -> showStudyGuide());
        
        container.getChildren().addAll(title, statsBox, scrollPane, clearBox, guideBtn);
        return container;
    }
    
    // Helper to create each row with delete button
    private HBox createTopicRow(Topic t, int studentId, Runnable refreshCallback) {
        HBox topicRow = new HBox(15);
        topicRow.setAlignment(Pos.CENTER_LEFT);
        topicRow.setPadding(new Insets(10));
        topicRow.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        
        String emoji = t.score < 50 ? "🔴" : (t.score < 70 ? "🟡" : "🟢");
        
        Label nameLbl = new Label(emoji + " " + t.name);
        nameLbl.setFont(Font.font("System", 14));
        
        Label scoreLbl = new Label(t.score + "%");
        scoreLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        scoreLbl.setTextFill(t.score < 50 ? Color.web("#e74c3c") : (t.score < 70 ? Color.web("#f39c12") : Color.web("#27ae60")));
        
        ProgressBar bar = new ProgressBar(t.score / 100.0);
        bar.setPrefWidth(150);
        bar.setStyle("-fx-accent: " + (t.score < 50 ? "#e74c3c" : (t.score < 70 ? "#f39c12" : "#27ae60")) + ";");
        
        // Delete button for this specific entry
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3;");
        deleteBtn.setOnAction(e -> deleteSingleEntry(studentId, t.id, t.name, refreshCallback));
        
        HBox.setHgrow(nameLbl, Priority.ALWAYS);
        topicRow.getChildren().addAll(nameLbl, bar, scoreLbl, deleteBtn);
        
        return topicRow;
    }
    
    // Delete single entry with real-time refresh
    private void deleteSingleEntry(int studentId, int topicId, String displayName, Runnable refreshCallback) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Score");
        confirm.setHeaderText("Delete this score?");
        confirm.setContentText("Remove: " + displayName + "\n\nAre you sure?");
        
        confirm.showAndWait().ifPresent(response -> {
            if(response.getText().equals("OK")) {
                try {
                    Connection con = DBConnection.getConnection();
                    
                    // Check if it's an overall score dummy topic
                    PreparedStatement checkPs = con.prepareStatement(
                        "SELECT s.topic_name FROM performance p JOIN syllabus s ON p.topic_id = s.topic_id WHERE p.topic_id=?"
                    );
                    checkPs.setInt(1, topicId);
                    ResultSet rs = checkPs.executeQuery();
                    boolean isOverallScore = false;
                    if(rs.next()) {
                        isOverallScore = rs.getString("topic_name").contains("📊 Overall Score");
                    }
                    
                    // Delete performance record
                    PreparedStatement ps = con.prepareStatement("DELETE FROM performance WHERE student_id=? AND topic_id=?");
                    ps.setInt(1, studentId);
                    ps.setInt(2, topicId);
                    int deleted = ps.executeUpdate();
                    
                    if(deleted > 0) {
                        // If it was an overall score dummy topic, delete it from syllabus too
                        if(isOverallScore) {
                            ps = con.prepareStatement("DELETE FROM syllabus WHERE topic_id=?");
                            ps.setInt(1, topicId);
                            ps.executeUpdate();
                        }
                        
                        // RELOAD DATA
                        loadPerformance(studentId);
                        
                        // REAL-TIME REFRESH - Call the callback to update UI
                        if(refreshCallback != null) {
                            refreshCallback.run();
                        }
                        
                        // Show success notification
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Deleted");
                        success.setHeaderText(null);
                        success.setContentText("✅ Deleted successfully!");
                        success.showAndWait();
                    }
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setContentText("Error deleting: " + e.getMessage());
                    error.showAndWait();
                }
            }
        });
    }
    
    private void clearAllScores(int studentId, Runnable refreshCallback) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Scores");
        confirm.setHeaderText("⚠️ Are you sure?");
        confirm.setContentText("This will delete ALL your performance data. This cannot be undone!");
        
        ButtonType clearBtn = new ButtonType("Clear All", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(clearBtn, cancelBtn);
        
        confirm.showAndWait().ifPresent(response -> {
            if(response == clearBtn) {
                try {
                    Connection con = DBConnection.getConnection();
                    
                    // Get all dummy topic IDs first
                    PreparedStatement ps = con.prepareStatement(
                        "SELECT p.topic_id FROM performance p JOIN syllabus s ON p.topic_id = s.topic_id " +
                        "WHERE p.student_id=? AND s.topic_name='📊 Overall Score'"
                    );
                    ps.setInt(1, studentId);
                    ResultSet rs = ps.executeQuery();
                    
                    List<Integer> dummyTopicIds = new ArrayList<>();
                    while(rs.next()) {
                        dummyTopicIds.add(rs.getInt("topic_id"));
                    }
                    
                    // Delete all performance records
                    ps = con.prepareStatement("DELETE FROM performance WHERE student_id=?");
                    ps.setInt(1, studentId);
                    ps.executeUpdate();
                    
                    // Clean up dummy topics
                    for(int id : dummyTopicIds) {
                        ps = con.prepareStatement("DELETE FROM syllabus WHERE topic_id=?");
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                    
                    // RELOAD DATA
                    loadPerformance(studentId);
                    
                    // REAL-TIME REFRESH
                    if(refreshCallback != null) {
                        refreshCallback.run();
                    }
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setContentText("✅ All scores cleared!");
                    success.showAndWait();
                    
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setContentText("Error clearing scores: " + e.getMessage());
                    error.showAndWait();
                }
            }
        });
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
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