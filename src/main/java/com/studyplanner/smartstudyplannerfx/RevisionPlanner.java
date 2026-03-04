package com.studyplanner.smartstudyplannerfx;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

class Topic {
    int id, score;
    String name;
    String trend = "Stable";  // default value

    Topic(int id, String name, int score){
        this.id = id;
        this.name = name;
        this.score = score;
    }
}

public class RevisionPlanner {
    private PriorityQueue<Topic> heap = new PriorityQueue<>(Comparator.comparingInt(t->t.score));
    private Map<Integer, String> trendMap = new HashMap<>();
    private List<Topic> weakTopics = new ArrayList<>();

    public void setTrend(int topicId, String trend) {
        trendMap.put(topicId, trend);
    }

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
            String subjectName = rs.getString("subject_name");

            // LOGIC FIX: Check if this is an "Entire Subject" dummy topic
            if(topicName.equals(subjectName)) {
                topicName = "📚 " + subjectName + " (Full Syllabus)";
            }

            Topic t = new Topic(topicId, topicName, score);
            if(trendMap.containsKey(topicId)) {
                t.trend = trendMap.get(topicId);
            }
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
            emptyState.setStyle("-fx-background-color: #f1f5f9;");

            Label icon = new Label("📝");
            icon.setFont(Font.font("System", 72));

            Label msg = new Label("No performance data yet!");
            msg.setFont(Font.font("System", FontWeight.BOLD, 24));
            msg.setTextFill(Color.web("#0f172a"));

            Label hint = new Label("Click ' Add Score' to enter your marks\nand see your weak points automatically.");
            hint.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            hint.setTextFill(Color.web("#64748b"));

            emptyState.getChildren().addAll(icon, msg, hint);
            return emptyState;
        }

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color:#e0d2c5;");

        Label title = new Label(" Priority Revision Plan");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#0f172a"));

        // Stats cards
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        int avgScore = heap.isEmpty() ? 0 : (int) heap.stream().mapToInt(t->t.score).average().getAsDouble();
        long weakCount = heap.stream().filter(t -> t.score < 60).count();


        VBox weakCard = createStatCard("Weak Areas", String.valueOf(weakCount), "#ef4444");
        VBox avgCard = createStatCard("Average Score", avgScore + "%", "#f59e0b");
        VBox totalCard = createStatCard("Total Scored", String.valueOf(heap.size()), "#3b82f6");

      


        statsBox.getChildren().addAll(weakCard, avgCard, totalCard);

        // ScrollPane for the list to handle many items
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        VBox listBox = new VBox(12);
        listBox.setPadding(new Insets(20));
        listBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label listTitle = new Label("Weakest Areas First - Study These!");
        listTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        listTitle.setTextFill(Color.web("#1e293b"));

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

        // Buttons Bottom Box
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button clearAllBtn = new Button("Clear All Scores");
        clearAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-border-color: #ef4444; -fx-border-radius: 6; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-weight: bold;");
        clearAllBtn.setOnAction(e -> clearAllScores(studentId, refreshCallback));

        Button guideBtn = new Button(" Generate Study Guide");
        guideBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;");
        guideBtn.setOnAction(e -> showStudyGuide());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actionBox.getChildren().addAll(clearAllBtn, spacer, guideBtn);

        container.getChildren().addAll(title, statsBox, scrollPane, actionBox);
        return container;
    }

    // Helper to create each row with delete button
    private HBox createTopicRow(Topic t, int studentId, Runnable refreshCallback) {
        HBox topicRow = new HBox(15);
        topicRow.setAlignment(Pos.CENTER_LEFT);
        topicRow.setPadding(new Insets(12, 15, 12, 15));
        topicRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        String emoji = t.score < 50 ? "🔴" : (t.score < 70 ? "🟡" : "🟢");

        Label nameLbl = new Label(emoji + " " + t.name);
        nameLbl.setFont(Font.font("System", 14));
        nameLbl.setStyle("-fx-text-fill: #1e293b;"); // CSS fix for correct coloring

        Label scoreLbl = new Label(t.score + "% (" + t.trend + ")");
        scoreLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        String scoreColor = t.score < 50 ? "#ef4444" : (t.score < 70 ? "#f59e0b" : "#10b981");
        scoreLbl.setStyle("-fx-text-fill: " + scoreColor + ";"); // CSS fix for correct coloring

        ProgressBar bar = new ProgressBar(t.score / 100.0);
        bar.setPrefWidth(150);
        bar.setStyle("-fx-accent: " + scoreColor + ";");

        // Delete button for this specific entry
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-border-color: #fecaca; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-border-color: #fecaca; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;"));
        deleteBtn.setOnAction(e -> deleteSingleEntry(studentId, t.id, t.name, refreshCallback));

        HBox.setHgrow(nameLbl, Priority.ALWAYS);

        Button progressBtn = new Button("Progress");

        progressBtn.setStyle(
                "-fx-background-color: #e0f2fe;" +
                        "-fx-text-fill: #0284c7;" +
                        "-fx-border-color: #bae6fd;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
        );

        progressBtn.setOnMouseEntered(e ->
                progressBtn.setStyle(
                        "-fx-background-color: #0284c7;" +
                                "-fx-text-fill: white;" +
                                "-fx-border-color: #0284c7;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-radius: 5;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-weight: bold;"
                )
        );

        progressBtn.setOnMouseExited(e ->
                progressBtn.setStyle(
                        "-fx-background-color: #e0f2fe;" +
                                "-fx-text-fill: #0284c7;" +
                                "-fx-border-color: #bae6fd;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-radius: 5;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-weight: bold;"
                )
        );

        progressBtn.setOnAction(e -> showProgressGraph(t.id, t.name, studentId));

        HBox.setHgrow(nameLbl, Priority.ALWAYS);

        topicRow.getChildren().addAll(nameLbl, bar, scoreLbl, progressBtn, deleteBtn);

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

                    // LOGIC FIX: Check if it's an "Entire Subject" dummy topic
                    PreparedStatement checkPs = con.prepareStatement(
                            "SELECT s.topic_name, sub.subject_name FROM performance p " +
                                    "JOIN syllabus s ON p.topic_id = s.topic_id " +
                                    "JOIN subjects sub ON p.subject_id = sub.subject_id " +
                                    "WHERE p.topic_id=?"
                    );
                    checkPs.setInt(1, topicId);
                    ResultSet rs = checkPs.executeQuery();
                    boolean isOverallScore = false;
                    if(rs.next()) {
                        isOverallScore = rs.getString("topic_name").equals(rs.getString("subject_name"));
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
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will delete ALL your performance data. This cannot be undone!");

        ButtonType clearBtn = new ButtonType("Clear All", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(clearBtn, cancelBtn);

        confirm.showAndWait().ifPresent(response -> {
            if(response == clearBtn) {
                try {
                    Connection con = DBConnection.getConnection();

                    // LOGIC FIX: Get all dummy topic IDs first using the proper name matching
                    PreparedStatement ps = con.prepareStatement(
                            "SELECT p.topic_id FROM performance p " +
                                    "JOIN syllabus s ON p.topic_id = s.topic_id " +
                                    "JOIN subjects sub ON p.subject_id = sub.subject_id " +
                                    "WHERE p.student_id=? AND s.topic_name = sub.subject_name"
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
        card.setPrefWidth(160);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 32));
        valLbl.setTextFill(Color.web(color));

        Label titLbl = new Label(title);
        titLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        titLbl.setTextFill(Color.web("#64748b"));

        card.getChildren().addAll(valLbl, titLbl);
        return card;
    }

    private void showStudyGuide() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Personalized Study Guide");
        alert.setHeaderText("Your Smart Adaptive Study Plan");

        alert.getDialogPane().setPrefWidth(480);
        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        StringBuilder guide = new StringBuilder();

        guide.append("🎯 SMART STUDY GUIDE\n\n");

        if(weakTopics.isEmpty()) {

            guide.append("✅ Excellent work!\n\n");
            guide.append("You currently have no weak topics.\n");
            guide.append("Maintain your performance with light revision.\n");

        } else {

            guide.append("🔥 PRIORITY TOPICS\n");
            guide.append("────────────────────────\n\n");

            int totalTime = 0;
            int rank = 1;

            for(Topic t : weakTopics.stream().limit(5).collect(Collectors.toList())) {

                int score = t.score;

                int time;

                if(score < 40) time = 60;
                else if(score < 60) time = 40;
                else if(score < 80) time = 20;
                else time = 10;

                totalTime += time;

                guide.append(rank).append(". ").append(t.name)
                        .append(" — ").append(score).append("%")
                        .append(" (").append(t.trend).append(")\n");

                guide.append("   ⏱ Suggested Study Time: ")
                        .append(time).append(" minutes\n\n");

                rank++;
            }

            guide.append("📅 TODAY'S STUDY PLAN\n");
            guide.append("────────────────────────\n");

            for(Topic t : weakTopics.stream().limit(3).collect(Collectors.toList())) {

                int score = t.score;

                int time;

                if(score < 40) time = 60;
                else if(score < 60) time = 40;
                else time = 20;

                guide.append("📘 ")
                        .append(t.name)
                        .append(" → ")
                        .append(time)
                        .append(" minutes\n");
            }

            guide.append("\n⏳ TOTAL STUDY TIME TODAY\n");
            guide.append(totalTime).append(" minutes\n");

            guide.append("\n💡 STUDY TIPS\n");
            guide.append("• Use active recall for topics below 50%\n");
            guide.append("• Practice past exam questions\n");
            guide.append("• Review improving topics briefly\n");
            guide.append("• Take breaks every 45 minutes\n");
        }

        alert.setContentText(guide.toString());
        alert.showAndWait();
    }
    private void showProgressGraph(int topicId, String topicName, int studentId) {

        Stage stage = new Stage();
        stage.setTitle("Progress - " + topicName);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Attempt");

        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Score (%)");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Performance Trend");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(topicName);

        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps;

            // 🔹 If it is a "Full Syllabus" subject row
            if (topicName.contains("(Full Syllabus)")) {

                PreparedStatement subPs = con.prepareStatement(
                        "SELECT subject_id FROM syllabus WHERE topic_id=?"
                );
                subPs.setInt(1, topicId);

                ResultSet subRs = subPs.executeQuery();
                int subjectId = -1;

                if (subRs.next()) {
                    subjectId = subRs.getInt("subject_id");
                }

                ps = con.prepareStatement(
                        "SELECT score FROM performance_log " +
                                "WHERE subject_id=? AND student_id=? " +
                                "ORDER BY recorded_at ASC"
                );

                ps.setInt(1, subjectId);
                ps.setInt(2, studentId);

            } else {
                // 🔹 Topic progress
                ps = con.prepareStatement(
                        "SELECT score FROM performance_log " +
                                "WHERE topic_id=? AND student_id=? " +
                                "ORDER BY recorded_at ASC"
                );

                ps.setInt(1, topicId);
                ps.setInt(2, studentId);
            }

            ResultSet rs = ps.executeQuery();

            int attempt = 1;

            while (rs.next()) {
                int score = rs.getInt("score");
                series.getData().add(new XYChart.Data<>(attempt++, score));
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error loading progress data: " + e.getMessage());
            alert.showAndWait();
        }

        lineChart.getData().add(series);

        VBox layout = new VBox(lineChart);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 700, 450);
        stage.setScene(scene);
        stage.show();
    }
}