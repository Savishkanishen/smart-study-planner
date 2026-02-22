package com.studyplanner.smartstudyplannerfx;

import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseButton;



public class StudyToolApp extends Application {
    private Stage primaryStage;
    private Student currentStudent;
    private StudyGraph graph = new StudyGraph();
    private SyllabusTree tree = new SyllabusTree();
    private RevisionPlanner planner = new RevisionPlanner();

    private Stage studyPlanStage;
    private VBox planContent;
    private Label remainingCountLbl;
    private Label completedCountLbl;
    private int completedCount = 0;

    private Stage analysisStage;
    private VBox analysisContentBox;
    private Label lastUpdatedLabel;
    
    
     private static final String PRIMARY_COLOR = "#6366f1";
    private static final String SECONDARY_COLOR = "#8b5cf6";
    private static final String ACCENT_COLOR = "#ec4899";
    private static final String DARK_BG = "#0f172a";

    
    
    // Helper method to keep window ratios perfect
    private void switchScene(Region newRoot) {
        if (primaryStage.getScene() == null) {
            Scene scene = new Scene(newRoot, 1024, 700);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setMaximized(true);
        } else {
            primaryStage.getScene().setRoot(newRoot);
        }
    }

    @Override
       
    public void start(Stage primaryStage) {
        System.out.println("Starting application...");
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Smart Study Planner");
        showLoginScreen();
        System.out.println("UI should be visible now");

        primaryStage.show();
        primaryStage.toFront();
        primaryStage.requestFocus();
    }

    private void showLoginScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color:#1e293b;");
     

        Label title = new Label("Smart Study Tool");
        title.setFont(Font.font("System", FontWeight.BOLD, 36));
        title.setTextFill(Color.WHITE);
        
         
        VBox formBox = new VBox(20);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(40));
        formBox.setMaxWidth(400);
        formBox.setStyle("-fx-background-color: #e0d2c5; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10);");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setStyle("-fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: #ffffff; -fx-font-size: 14px;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: #ffffff; -fx-font-size: 14px;");

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #2b3f5e; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 40; -fx-background-radius: 6; -fx-cursor: hand;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-color: #537dc2; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 40; -fx-background-radius: 6; -fx-cursor: hand;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle("-fx-background-color: #385482; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 40; -fx-background-radius: 6; -fx-cursor: hand;"));

        Button registerBtn = new Button("Create Account");
        registerBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e293b; -fx-font-size: 14px; -fx-cursor: hand;");

        Label errorLbl = new Label();
        errorLbl.setTextFill(Color.web("#ef4444"));

        loginBtn.setOnAction(e -> {
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM students WHERE email=? AND password=?");
                ps.setString(1, emailField.getText());
                ps.setString(2, passField.getText());
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    currentStudent = new Student(rs.getInt("student_id"), rs.getString("name"), rs.getString("email"));
                    loadDataAndShowDashboard();
                } else {
                    errorLbl.setText("Invalid credentials!");
                }
            } catch (Exception ex) {
                errorLbl.setText("Database error: " + ex.getMessage());
            }
        });

        registerBtn.setOnAction(e -> showRegisterScreen());

        formBox.getChildren().addAll(
                new Label("Welcome Back") {{ setFont(Font.font("System", FontWeight.BOLD, 24)); setTextFill(Color.web("#0f172a")); }},
                new Label("Please login to continue") {{ setTextFill(Color.web("#64748b")); }},
                emailField, passField, loginBtn, registerBtn, errorLbl
        );

        root.getChildren().addAll(title, formBox);
        Platform.runLater(() -> root.requestFocus());
        switchScene(root);
    }

    private void showRegisterScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e1b4b, #312e81);");

        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(40));
        formBox.setMaxWidth(400);
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10);");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setStyle("-fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: #ffffff; -fx-font-size: 14px;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: #ffffff; -fx-font-size: 14px;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: #ffffff; -fx-font-size: 14px;");

        Button regBtn = new Button("Register");
        regBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 40; -fx-background-radius: 6; -fx-cursor: hand;");
        regBtn.setMaxWidth(Double.MAX_VALUE);

        Button backBtn = new Button("Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 14px; -fx-cursor: hand;");

        Label msgLbl = new Label();
        msgLbl.setTextFill(Color.web("#10b981"));

        regBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                msgLbl.setText("Please enter a valid email address!");
                msgLbl.setTextFill(Color.web("#ef4444"));
                return;
            }
            if (nameField.getText().trim().isEmpty() || passField.getText().trim().isEmpty()) {
                msgLbl.setText("All fields are required!");
                msgLbl.setTextFill(Color.web("#ef4444"));
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO students(name,email,password) VALUES(?,?,?)"
                );
                ps.setString(1, nameField.getText());
                ps.setString(2, emailField.getText());
                ps.setString(3, passField.getText());
                ps.executeUpdate();
                msgLbl.setText("Registration successful! Please login.");
            } catch (Exception ex) {
                msgLbl.setText("Error: " + ex.getMessage());
                msgLbl.setTextFill(Color.web("#ef4444"));
            }
        });

        backBtn.setOnAction(e -> showLoginScreen());

        formBox.getChildren().addAll(
                new Label("Create Account") {{ setFont(Font.font("System", FontWeight.BOLD, 24)); setTextFill(Color.web("#0f172a")); }},
                new Label("Please create here a new account") {{ setTextFill(Color.web("#64748b")); }},
                nameField, emailField, passField, regBtn, backBtn, msgLbl
        );

        root.getChildren().addAll(formBox);
        Platform.runLater(() -> root.requestFocus());
        switchScene(root);
    }

    private void loadDataAndShowDashboard() {
        try {
            graph.loadSubjects();
            tree.loadSyllabus();
            planner.loadPerformance(currentStudent.getId());
            showDashboard();
        } catch (Exception e) {
            showAlert("Error loading data: " + e.getMessage());
        }
    }

    private void showDashboard() {
        if(analysisStage != null && analysisStage.isShowing()) {
            analysisStage.close();
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");

        // --- Sidebar Configuration ---
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #1e293b; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 2, 0);");
        sidebar.getChildren().clear();

        Label userLbl = new Label("👤 " + currentStudent.getName());
        userLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        userLbl.setTextFill(Color.WHITE);
        userLbl.setWrapText(true);

        // Navigation Buttons with Tooltips
        Button quickAddBtn = createNavButton("➕ Add Score", "Record your topic test results");
        Button studyPathBtn = createNavButton("📚 Study Path", "Manage your subjects and prerequisites");
        Button syllabusBtn = createNavButton("🌳 Syllabus", "Organize chapters and topics");
        Button revisionBtn = createNavButton("🔥 Revision Plan", "Identify and focus on weak topics");
        Button studyPlanBtn = createNavButton("📋 Study Plan", "Generate your personalized schedule");
        Button logoutBtn = createNavButton("🚪 Logout", "Safely exit the application");

        quickAddBtn.setOnAction(e -> showAddPerformanceDialog());
        studyPathBtn.setOnAction(e -> showAddMySubjectDialog());
        syllabusBtn.setOnAction(e -> showMySyllabusSelector());
        studyPlanBtn.setOnAction(e -> generateStudyPlan());

        revisionBtn.setOnAction(e -> {
            try {
                planner.loadPerformance(currentStudent.getId());
                root.setCenter(planner.getRevisionView(currentStudent.getId(), this::showDashboard));
            } catch (Exception ex) {
                showAlert("Error: " + ex.getMessage());
            }
        });

        logoutBtn.setOnAction(e -> {
            currentStudent = null;
            showLoginScreen();
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(userLbl, new Region() {{ setPrefHeight(30); }},
                studyPathBtn, syllabusBtn, quickAddBtn,
                studyPlanBtn, revisionBtn, spacer, logoutBtn);

        // --- Center Content with Dynamic Greeting
        VBox welcome = new VBox(15);
        welcome.setAlignment(Pos.CENTER);
        welcome.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");
        BorderPane.setMargin(welcome, new Insets(40));

        // Logic for Dynamic Greeting
        int hour = java.time.LocalTime.now().getHour();
        String greeting = (hour < 12) ? "Good Morning" : (hour < 17) ? "Good Afternoon" : "Good Evening";
        String timeEmoji = (hour < 12) ? "🌅" : (hour < 17) ? "☀️" : "🌙";

        Label welcomeLbl = new Label(greeting + ", " + currentStudent.getName() + "! " + timeEmoji);
        welcomeLbl.setFont(Font.font("System", FontWeight.BOLD, 32));
        welcomeLbl.setTextFill(Color.web("#0f172a"));

        Label subLbl = new Label("Select an option from the sidebar to get started");
        subLbl.setFont(Font.font("System", 16));
        subLbl.setTextFill(Color.web("#64748b"));

        welcome.getChildren().addAll(welcomeLbl, subLbl);

        root.setLeft(sidebar);
        root.setCenter(welcome);
        switchScene(root);
        primaryStage.toFront();
    }

    private Button createNavButton(String text, String tooltipText) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-font-size: 15px; -fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-font-size: 15px; -fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand;"));

        // ADDED: Tooltip for UX marks
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(300));
        btn.setTooltip(tooltip);

        return btn;
    }

    private void showAddMySubjectDialog() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #e0d2c5;");

        Label title = new Label(" Add My Study Path");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#0f172a"));

        VBox formCard = new VBox(15);
        formCard.setPadding(new Insets(30));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        formCard.setMaxWidth(600);

        TextField subjectField = new TextField();
        subjectField.setPromptText("Enter Subject Name (e.g., Advanced Java)");
        subjectField.setStyle("-fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-font-size: 14px;");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description (optional)");
        descArea.setPrefRowCount(3);
        descArea.setStyle("-fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        ComboBox<String> prereqCombo = new ComboBox<>();
        prereqCombo.setPromptText("None - Leave empty if no prerequisite");
        prereqCombo.setMaxWidth(Double.MAX_VALUE);
        prereqCombo.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");
        loadMySubjects(prereqCombo);

        Label prereqLabel = new Label("Prerequisite (Optional):");
        prereqLabel.setTextFill(Color.web("#475569"));

        Label hint = new Label("Optional: Select if this subject requires another subject first");
        hint.setTextFill(Color.web("#64748b"));
        hint.setFont(Font.font("System", 11));

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("➕ Add Subject");
        addBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;");

        Button viewMyBtn = new Button("👁 View My Subjects");
        viewMyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b82f6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 10 20; -fx-cursor: hand;");

        Label status = new Label();
        status.setFont(Font.font("System", FontWeight.BOLD, 13));

        addBtn.setOnAction(ev -> {
            String name = subjectField.getText().trim();
            String desc = descArea.getText().trim();
            String prereq = prereqCombo.getValue();

            if(name.isEmpty()) {
                status.setText("Please enter subject name");
                status.setTextFill(Color.web("#ef4444"));
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO subjects(subject_name, description) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, name);
                ps.setString(2, desc.isEmpty() ? null : desc);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int newSubjectId = 0;
                if(rs.next()) newSubjectId = rs.getInt(1);

                if(prereq != null && !prereq.isEmpty() && newSubjectId > 0) {
                    ps = con.prepareStatement("SELECT subject_id FROM subjects WHERE subject_name=?");
                    ps.setString(1, prereq);
                    rs = ps.executeQuery();
                    if(rs.next()) {
                        int prereqId = rs.getInt("subject_id");
                        ps = con.prepareStatement("INSERT INTO prerequisites(subject_id, prerequisite_id) VALUES(?,?)");
                        ps.setInt(1, newSubjectId);
                        ps.setInt(2, prereqId);
                        ps.executeUpdate();
                    }
                }
                status.setText("✅ Added: " + name);
                status.setTextFill(Color.web("#10b981"));
                subjectField.clear(); descArea.clear(); prereqCombo.setValue(null);
                loadMySubjects(prereqCombo);
                graph.loadSubjects();
            } catch (SQLException ex) {
                if(ex.getMessage().contains("Duplicate")) {
                    status.setText(" Subject already exists!"); status.setTextFill(Color.web("#ef4444"));
                } else {
                    status.setText(" Error: " + ex.getMessage()); status.setTextFill(Color.web("#ef4444"));
                }
            } catch (Exception ex) {}
        });

        viewMyBtn.setOnAction(ev -> Platform.runLater(() -> showMySubjectsList()));

        btnBox.getChildren().addAll(addBtn, viewMyBtn, status);

        Label l1 = new Label("Subject Name:"); l1.setTextFill(Color.web("#475569"));
        Label l2 = new Label("Description:"); l2.setTextFill(Color.web("#475569"));
        formCard.getChildren().addAll(l1, subjectField, l2, descArea, prereqLabel, prereqCombo, hint, btnBox);

        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(ev -> showDashboard());

        container.getChildren().addAll(backBtn, title, formCard);
        ((BorderPane) primaryStage.getScene().getRoot()).setCenter(container);
    }

    private void showMySyllabusSelector() {
        final String[] currentSubjectHolder = new String[1];

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #e0d2c5;");

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-cursor: hand; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showDashboard());

        Label title = new Label("My Syllabus");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#0f172a"));

        HBox selectorBox = new HBox(15);
        selectorBox.setAlignment(Pos.CENTER_LEFT);
        selectorBox.setPadding(new Insets(20));
        selectorBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label selectLabel = new Label("Select Your Subject:");
        selectLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        selectLabel.setTextFill(Color.web("#1e293b"));

        ComboBox<String> subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Choose subject...");
        subjectCombo.setPrefWidth(300);
        subjectCombo.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 5; -fx-font-size: 14px;");

        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT subject_name FROM subjects");
            while(rs.next()) subjectCombo.getItems().add(rs.getString("subject_name"));
        } catch (Exception e) {}

        Button loadBtn = new Button("Load Syllabus");
        loadBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");

        VBox contentArea = new VBox(20);
        contentArea.setPadding(new Insets(25));
        contentArea.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        Label defaultMsg = new Label("Select a subject above to view its syllabus topics");
        defaultMsg.setFont(Font.font("System", 15));
        defaultMsg.setTextFill(Color.web("#64748b"));
        contentArea.getChildren().add(defaultMsg);

        loadBtn.setOnAction(e -> {
            String subject = subjectCombo.getValue();
            if(subject == null) return;
            currentSubjectHolder[0] = subject;
            contentArea.getChildren().clear();

            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT subject_id FROM subjects WHERE subject_name=?");
                ps.setString(1, subject);
                ResultSet rs = ps.executeQuery();

                if(rs.next()) {
                    int subjId = rs.getInt("subject_id");

                    Label subjTitle = new Label("📖 " + subject);
                    subjTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
                    subjTitle.setTextFill(Color.web("#0f172a"));

                    Button addTopicBtn = new Button("Add Topic to this Subject");
                    addTopicBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6;");
                    addTopicBtn.setOnAction(ev -> showAddTopicDialog(subjId, subject, contentArea));

                    HBox header = new HBox(20);
                    header.setAlignment(Pos.CENTER_LEFT);
                    header.getChildren().addAll(subjTitle, addTopicBtn);

                    contentArea.getChildren().add(header);

                    TreeView<String> treeView = new TreeView<>();
                    TreeItem<String> rootItem = new TreeItem<>("Topics");
                    rootItem.setExpanded(true);

                    ps = con.prepareStatement("SELECT * FROM syllabus WHERE subject_id=?");
                    ps.setInt(1, subjId);
                    rs = ps.executeQuery();

                    Map<Integer, TreeItem<String>> itemMap = new HashMap<>();
                    List<Integer[]> relations = new ArrayList<>();

                    while(rs.next()) {
                        int topicId = rs.getInt("topic_id");
                        String topicName = rs.getString("topic_name");
                        int parentId = rs.getInt("parent_topic_id");

                        TreeItem<String> item = new TreeItem<>("📂" + topicName);
                        item.setExpanded(true);
                        itemMap.put(topicId, item);

                        if(parentId == 0) rootItem.getChildren().add(item);
                        else relations.add(new Integer[]{topicId, parentId});
                    }

                    for(Integer[] rel : relations) {
                        TreeItem<String> child = itemMap.get(rel[0]);
                        TreeItem<String> parent = itemMap.get(rel[1]);
                        if(parent != null && child != null) parent.getChildren().add(child);
                    }

                    treeView.setRoot(rootItem);
                    treeView.setStyle("-fx-font-size: 14px; -fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

                    if(rootItem.getChildren().isEmpty()) {
                        Label lbl = new Label("No topics yet. Click 'Add Topic' to create your syllabus!");
                        lbl.setTextFill(Color.web("#64748b"));
                        contentArea.getChildren().add(lbl);
                    } else {
                        contentArea.getChildren().add(treeView);
                    }
                }
            } catch (Exception ex) {
                Label err = new Label("Error: " + ex.getMessage());
                err.setTextFill(Color.web("#ef4444"));
                contentArea.getChildren().add(err);
            }
        });

        selectorBox.getChildren().addAll(selectLabel, subjectCombo, loadBtn);
        container.getChildren().addAll(backBtn, title, selectorBox, contentArea);
        ((BorderPane) primaryStage.getScene().getRoot()).setCenter(container);
    }

    private void showAddTopicDialog(int subjectId, String subjectName, VBox parentContainer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Topic to " + subjectName);
        dialog.setHeaderText("Create new topic or sub-topic");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));

        TextField topicField = new TextField();
        topicField.setPromptText("Topic Name (e.g., Chapter 1: Basics)");
        topicField.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        ComboBox<String> parentCombo = new ComboBox<>();
        parentCombo.setPromptText("Select Parent (Optional)");
        parentCombo.setMaxWidth(Double.MAX_VALUE);
        parentCombo.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        Map<String, Integer> parentMap = new HashMap<>();

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT topic_id, topic_name FROM syllabus WHERE subject_id=? AND parent_topic_id=0 ORDER BY topic_name"
            );
            ps.setInt(1, subjectId);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int id = rs.getInt("topic_id");
                String name = rs.getString("topic_name");
                parentCombo.getItems().add(name);
                parentMap.put(name, id);
            }
            if(parentCombo.getItems().isEmpty()) {
                parentCombo.setPromptText("No existing topics - will create as main chapter");
                parentCombo.setDisable(true);
            }
        } catch (Exception e) { e.printStackTrace(); }

        Label hint = new Label(parentCombo.isDisable() ?
                "Creating main chapter (no parent available)" :
                "Leave empty to create main chapter, or select existing topic as parent");
        hint.setFont(Font.font("System", 11));
        hint.setTextFill(Color.web("#64748b"));

        Label l1 = new Label("Topic Name:"); l1.setTextFill(Color.web("#1e293b"));
        Label l2 = new Label("Parent Topic (Optional):"); l2.setTextFill(Color.web("#1e293b"));
        form.getChildren().addAll(l1, topicField, l2, parentCombo, hint);

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if(btn == addBtn) {
                String topicName = topicField.getText().trim();
                String selectedParent = parentCombo.getValue();

                if(topicName.isEmpty()) {
                    showAlert("Please enter a topic name!");
                    return null;
                }

                Connection con = null;
                try {
                    con = DBConnection.getConnection();
                    con.setAutoCommit(false);

                    Integer parentId = 0;
                    if(!parentCombo.isDisabled() && selectedParent != null && !selectedParent.isEmpty()) {
                        parentId = parentMap.get(selectedParent);
                        if(parentId == null) {
                            showAlert("Error: Selected parent topic not found!");
                            con.rollback(); return null;
                        }
                    }

                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO syllabus(subject_id, parent_topic_id, topic_name) VALUES(?,?,?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setInt(1, subjectId);
                    if(parentId == 0 || parentId == null) ps.setNull(2, Types.INTEGER);
                    else ps.setInt(2, parentId);
                    ps.setString(3, topicName);

                    int affectedRows = ps.executeUpdate();
                    if(affectedRows == 0) {
                        showAlert("Error: Failed to insert topic!");
                        con.rollback(); return null;
                    }

                    con.commit();
                    showAlert("Successfully added: " + topicName);
                    refreshSyllabusView(subjectId, subjectName, parentContainer);

                } catch (Exception ex) {
                    try { if(con != null) con.rollback(); } catch (SQLException e) {}
                    showAlert("Error adding topic: " + ex.getMessage());
                } finally {
                    try { if(con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void deleteTopicRecursive(int topicId, int subjectId, VBox contentArea) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            deleteTopicAndChildren(con, topicId);
            con.commit();
            showAlert("Topic deleted successfully!");

            String subjectName = "";
            PreparedStatement ps = con.prepareStatement("SELECT subject_name FROM subjects WHERE subject_id = ?");
            ps.setInt(1, subjectId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) subjectName = rs.getString("subject_name");

            refreshSyllabusView(subjectId, subjectName, contentArea);
            if(analysisStage != null && analysisStage.isShowing()) refreshAnalysisContent();

        } catch (Exception e) {
            try { if(con != null) con.rollback(); } catch (SQLException ex) {}
            showAlert("Error deleting topic: " + e.getMessage());
        } finally {
            try { if(con != null) con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    private void deleteTopicAndChildren(Connection con, int topicId) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT topic_id FROM syllabus WHERE parent_topic_id = ?");
        ps.setInt(1, topicId);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) deleteTopicAndChildren(con, rs.getInt("topic_id"));

        ps = con.prepareStatement("DELETE FROM performance WHERE topic_id = ?");
        ps.setInt(1, topicId); ps.executeUpdate();

        ps = con.prepareStatement("DELETE FROM syllabus WHERE topic_id = ?");
        ps.setInt(1, topicId); ps.executeUpdate();
    }

    private void loadMySubjects(ComboBox<String> combo) {
        combo.getItems().clear();
        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT subject_name FROM subjects ORDER BY subject_name");
            while(rs.next()) combo.getItems().add(rs.getString("subject_name"));
        } catch (Exception e) {}
    }

    private void showMySubjectsList() {
        Stage listStage = new Stage();
        listStage.setTitle("My Subjects");
        listStage.setWidth(650);
        listStage.setHeight(550);

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #f1f5f9;");

        Label header = new Label("📚 Your Study Path");
        header.setFont(Font.font("System", FontWeight.BOLD, 22));
        header.setTextFill(Color.web("#0f172a"));

        ScrollPane scrollPane = new ScrollPane();
        VBox subjectsContainer = new VBox(12);
        subjectsContainer.setPadding(new Insets(10));
        subjectsContainer.setStyle("-fx-background-color: #f1f5f9;");
        scrollPane.setContent(subjectsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f1f5f9; -fx-border-color: transparent;");

        loadSubjectsIntoContainer(subjectsContainer, listStage);

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> listStage.close());

        content.getChildren().addAll(header, scrollPane, closeBtn);
        listStage.setScene(new Scene(content));
        listStage.show();
    }

    private void loadSubjectsIntoContainer(VBox container, Stage parentStage) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT s.subject_id, s.subject_name, s.description, " +
                    "COUNT(DISTINCT sy.topic_id) as topic_count, " +
                    "COUNT(DISTINCT p.student_id) as score_count, " +
                    "GROUP_CONCAT(DISTINCT pr.prerequisite_id) as prereq_ids " +
                    "FROM subjects s " +
                    "LEFT JOIN syllabus sy ON s.subject_id = sy.subject_id " +
                    "LEFT JOIN performance p ON s.subject_id = p.subject_id " +
                    "LEFT JOIN prerequisites pr ON s.subject_id = pr.subject_id " +
                    "GROUP BY s.subject_id " +
                    "ORDER BY s.subject_name";

            ResultSet rs = con.createStatement().executeQuery(sql);

            boolean empty = true;
            while(rs.next()) {
                empty = false;
                int subjectId = rs.getInt("subject_id");
                String subjectName = rs.getString("subject_name");
                String desc = rs.getString("description");
                int topicCount = rs.getInt("topic_count");
                int scoreCount = rs.getInt("score_count");
                String prereqs = rs.getString("prereq_ids");

                HBox card = new HBox(15);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

                VBox info = new VBox(5);
                Label nameLbl = new Label("📘 " + subjectName);
                nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
                nameLbl.setTextFill(Color.web("#1e293b"));

                String details = "";
                if(desc != null && !desc.isEmpty()) details += desc + " • ";
                details += topicCount + " topics";
                if(scoreCount > 0) details += " • " + scoreCount + " scores";
                if(prereqs != null && !prereqs.isEmpty()) details += " • has prerequisites";

                Label detailLbl = new Label(details);
                detailLbl.setFont(Font.font("System", 13));
                detailLbl.setTextFill(Color.web("#64748b"));

                info.getChildren().addAll(nameLbl, detailLbl);
                HBox.setHgrow(info, Priority.ALWAYS);

                Button deleteBtn = new Button("🗑");
                deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-border-color: #fecaca; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-border-color: #fecaca; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;"));

                deleteBtn.setOnAction(e -> {
                    try {
                        PreparedStatement checkPs = con.prepareStatement("SELECT COUNT(*) as count FROM prerequisites WHERE prerequisite_id = ?");
                        checkPs.setInt(1, subjectId); ResultSet checkRs = checkPs.executeQuery();
                        if(checkRs.next() && checkRs.getInt("count") > 0) {
                            showAlert("⚠️ Cannot delete! Required as prerequisite."); return;
                        }
                    } catch (SQLException ex) { ex.printStackTrace(); }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Deletion");
                    confirm.setHeaderText("Delete '" + subjectName + "'?");
                    confirm.setContentText("This deletes topics and scores permanently.\nAre you sure?");

                    if(confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        deleteSubject(subjectId, container, parentStage);
                    }
                });

                card.getChildren().addAll(info, deleteBtn);
                container.getChildren().add(card);
            }
            if(empty) {
                Label lbl = new Label("No subjects added yet.");
                lbl.setTextFill(Color.web("#64748b"));
                container.getChildren().add(lbl);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteSubject(int subjectId, VBox container, Stage parentStage) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            PreparedStatement ps = con.prepareStatement("DELETE FROM performance WHERE subject_id = ?");
            ps.setInt(1, subjectId); ps.executeUpdate();

            ps = con.prepareStatement("DELETE FROM prerequisites WHERE subject_id = ? OR prerequisite_id = ?");
            ps.setInt(1, subjectId); ps.setInt(2, subjectId); ps.executeUpdate();

            ps = con.prepareStatement("DELETE FROM syllabus WHERE subject_id = ?");
            ps.setInt(1, subjectId); ps.executeUpdate();

            ps = con.prepareStatement("DELETE FROM subjects WHERE subject_id = ?");
            ps.setInt(1, subjectId); int affected = ps.executeUpdate();

            con.commit();

            if(affected > 0) {
                showAlert("✅ Subject deleted successfully!");
                if(container != null) { container.getChildren().clear(); loadSubjectsIntoContainer(container, parentStage); }
                if(analysisStage != null && analysisStage.isShowing()) refreshAnalysisContent();
                graph.loadSubjects();
            }
        } catch (Exception e) {
            try { if(con != null) con.rollback(); } catch (SQLException ex) {}
            showAlert("❌ Error deleting: " + e.getMessage());
        } finally {
            try { if(con != null) con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    private void showAddPerformanceDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Your Performance");
        dialog.setHeaderText("Track Marks");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f8fafc;");
        dialogPane.setPrefWidth(550);

        ButtonType addBtnType = new ButtonType("Add Score", ButtonBar.ButtonData.OK_DONE);
        ButtonType viewWeakBtnType = new ButtonType("View Analysis", ButtonBar.ButtonData.LEFT);
        dialogPane.getButtonTypes().addAll(viewWeakBtnType, addBtnType, ButtonType.CANCEL);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        ToggleGroup scoreTypeGroup = new ToggleGroup();
        HBox typeBox = new HBox(20);
        typeBox.setAlignment(Pos.CENTER);

        RadioButton subjectRadio = new RadioButton("Entire Subject");
        subjectRadio.setToggleGroup(scoreTypeGroup);
        subjectRadio.setSelected(true);
        subjectRadio.setTextFill(Color.web("#1e293b"));

        RadioButton topicRadio = new RadioButton("Specific Topic");
        topicRadio.setToggleGroup(scoreTypeGroup);
        topicRadio.setTextFill(Color.web("#1e293b"));

        typeBox.getChildren().addAll(subjectRadio, topicRadio);

        ComboBox<String> subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Select Subject");
        subjectCombo.setMaxWidth(Double.MAX_VALUE);
        subjectCombo.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 5;");

        ComboBox<String> topicCombo = new ComboBox<>();
        topicCombo.setPromptText("Select Topic");
        topicCombo.setMaxWidth(Double.MAX_VALUE);
        topicCombo.setDisable(true);
        topicCombo.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 5;");

        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT subject_name FROM subjects");
            while(rs.next()) subjectCombo.getItems().add(rs.getString("subject_name"));
        } catch (Exception e) {}

        subjectCombo.setOnAction(e -> {
            String selectedSubject = subjectCombo.getValue();
            if(selectedSubject == null) { topicCombo.getItems().clear(); return; }
            topicCombo.getItems().clear();
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT s.topic_name FROM syllabus s JOIN subjects sub ON s.subject_id = sub.subject_id WHERE sub.subject_name = ? AND s.topic_name NOT LIKE '📊 Overall Score%'"
                );
                ps.setString(1, selectedSubject);
                ResultSet rs = ps.executeQuery();
                while(rs.next()) topicCombo.getItems().add(rs.getString("topic_name"));
            } catch (Exception ex) {}
        });

        scoreTypeGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if(newVal == subjectRadio) {
                topicCombo.setDisable(true); topicCombo.setValue(null);
            } else {
                topicCombo.setDisable(false);
                if(subjectCombo.getValue() != null && topicCombo.getItems().isEmpty()) subjectCombo.fireEvent(new ActionEvent());
            }
        });

        VBox scoreBox = new VBox(10);
        Slider scoreSlider = new Slider(0, 100, 50);
        scoreSlider.setShowTickLabels(true); scoreSlider.setShowTickMarks(true); scoreSlider.setMajorTickUnit(10);

        HBox scoreDisplay = new HBox(10);
        scoreDisplay.setAlignment(Pos.CENTER);
        Label scoreLabel = new Label("50");
        scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
        Label percentLabel = new Label("%"); percentLabel.setTextFill(Color.web("#64748b"));

        scoreSlider.valueProperty().addListener((obs, old, val) -> {
            int score = val.intValue();
            scoreLabel.setText(String.valueOf(score));
            if(score < 50) scoreLabel.setTextFill(Color.web("#ef4444"));
            else if(score < 70) scoreLabel.setTextFill(Color.web("#f59e0b"));
            else scoreLabel.setTextFill(Color.web("#10b981"));
        });

        scoreDisplay.getChildren().addAll(scoreLabel, percentLabel);
        Label l1 = new Label("Your Score:"); l1.setTextFill(Color.web("#475569"));
        scoreBox.getChildren().addAll(l1, scoreSlider, scoreDisplay);

        Label t1 = new Label("Score For:"); t1.setTextFill(Color.web("#475569"));
        Label t2 = new Label("Subject:"); t2.setTextFill(Color.web("#475569"));
        Label t3 = new Label("Topic:"); t3.setTextFill(Color.web("#475569"));

        content.getChildren().addAll(t1, typeBox, t2, subjectCombo, t3, topicCombo, scoreBox);
        dialogPane.setContent(content);

        ((Button)dialogPane.lookupButton(viewWeakBtnType)).addEventFilter(ActionEvent.ACTION, e -> {
            e.consume(); showSubjectAnalysis();
        });

        dialog.setResultConverter(btn -> {
            if(btn == addBtnType) {
                try {
                    String subject = subjectCombo.getValue();
                    String topic = topicCombo.getValue();
                    int score = (int)scoreSlider.getValue();
                    if(subject == null) { showAlert("Please select a subject!"); return null; }

                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("SELECT subject_id FROM subjects WHERE subject_name=?");
                    ps.setString(1, subject);
                    ResultSet rs = ps.executeQuery();
                    int subjId = 0; if(rs.next()) subjId = rs.getInt(1);

                    if(subjectRadio.isSelected()) {
                        ps = con.prepareStatement("SELECT topic_id FROM syllabus WHERE subject_id=? AND topic_name=?");
                        ps.setInt(1, subjId); ps.setString(2, subject);
                        rs = ps.executeQuery();
                        int overallTopicId;
                        if(rs.next()) overallTopicId = rs.getInt("topic_id");
                        else {
                            ps = con.prepareStatement("INSERT INTO syllabus(subject_id, parent_topic_id, topic_name) VALUES(?, NULL, ?)", Statement.RETURN_GENERATED_KEYS);
                            ps.setInt(1, subjId); ps.setString(2, subject); ps.executeUpdate();
                            ResultSet genKeys = ps.getGeneratedKeys();
                            if(genKeys.next()) overallTopicId = genKeys.getInt(1);
                            else throw new SQLException("Failed to create topic");
                        }
                        ps = con.prepareStatement("INSERT INTO performance(student_id, subject_id, topic_id, score) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE score=?");
                        ps.setInt(1, currentStudent.getId()); ps.setInt(2, subjId); ps.setInt(3, overallTopicId); ps.setInt(4, score); ps.setInt(5, score);
                        ps.executeUpdate();
                        showAlert("✅ Saved Full Syllabus score: " + subject + " = " + score + "%");
                        Platform.runLater(() -> refreshAnalysisContent()); // Correctly placed!
                    } else {
                        if(topic == null || topic.isEmpty()) { showAlert("Please select a specific topic!"); return null; }
                        ps = con.prepareStatement("SELECT topic_id FROM syllabus WHERE topic_name=? AND subject_id=?");
                        ps.setString(1, topic); ps.setInt(2, subjId);
                        rs = ps.executeQuery();
                        if(rs.next()) {
                            int topicId = rs.getInt("topic_id");
                            ps = con.prepareStatement("INSERT INTO performance(student_id, subject_id, topic_id, score) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE score=?");
                            ps.setInt(1, currentStudent.getId()); ps.setInt(2, subjId); ps.setInt(3, topicId); ps.setInt(4, score); ps.setInt(5, score);
                            ps.executeUpdate();
                            showAlert("✅ Saved Topic score: " + topic + " = " + score + "%");
                            Platform.runLater(() -> refreshAnalysisContent()); // Correctly placed!
                            if(score < 60) showWeakPointAlert(topic, score);
                        }
                    }
                    planner.loadPerformance(currentStudent.getId());
                } catch (Exception e) { showAlert("Error: " + e.getMessage()); }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void refreshRevisionView() {
        try {
            planner.loadPerformance(currentStudent.getId());
            BorderPane root = (BorderPane) primaryStage.getScene().getRoot();
            root.setCenter(planner.getRevisionView(currentStudent.getId(), this::refreshRevisionView));
        } catch (Exception e) {
            showAlert("Refresh error: " + e.getMessage());
        }
    }

    private void showWeakPointAlert(String topic, int score) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Weak Point Detected");
        alert.setHeaderText("🔴 " + topic + " needs attention!");
        alert.setContentText(String.format(
                "Your score of %d%% is below 60%%.\n\n" +
                        "This topic has been added to your Priority Revision Plan.\n" +
                        "Go to '🔥 Revision Plan' to see study recommendations.", score));
        alert.showAndWait();
    }

    private void showSubjectAnalysis() {
        if (analysisStage != null && analysisStage.isShowing()) {
            refreshAnalysisContent(); analysisStage.toFront(); return;
        }

        analysisStage = new Stage();
        analysisStage.setTitle("📊 Real-time Performance Analysis");
        analysisStage.setWidth(650); analysisStage.setHeight(600);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f1f5f9;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Performance Analysis");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        lastUpdatedLabel = new Label("Updated: Just now");
        lastUpdatedLabel.setTextFill(Color.web("#64748b"));

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: white; -fx-text-fill: #3b82f6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 6 15; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshAnalysisContent());

        header.getChildren().addAll(title, spacer, lastUpdatedLabel, refreshBtn);

        analysisContentBox = new VBox(15);
        analysisContentBox.setPadding(new Insets(10));
        analysisContentBox.setStyle("-fx-background-color: #f1f5f9;");

        ScrollPane scrollPane = new ScrollPane(analysisContentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f1f5f9; -fx-border-color: transparent;");

        root.getChildren().addAll(header, scrollPane);
        analysisStage.setScene(new Scene(root));
        analysisStage.show();

        refreshAnalysisContent();
    }

    private void refreshSyllabusView(int subjectId, String subjectName, VBox contentArea) {
        contentArea.getChildren().clear();
        try {
            Connection con = DBConnection.getConnection();

            Label subjTitle = new Label("📖 " + subjectName);
            subjTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
            subjTitle.setTextFill(Color.web("#0f172a"));

            Button addTopicBtn = new Button("➕ Add Topic");
            addTopicBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;");
            addTopicBtn.setOnAction(ev -> showAddTopicDialog(subjectId, subjectName, contentArea));

            Button deleteSubjectBtn = new Button("🗑 Delete Subject");
            deleteSubjectBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-border-color: #fecaca; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 15; -fx-cursor: hand;");
            deleteSubjectBtn.setOnAction(ev -> {
                try {
                    PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as count FROM prerequisites WHERE prerequisite_id = ?");
                    ps.setInt(1, subjectId); ResultSet rs = ps.executeQuery();
                    if(rs.next() && rs.getInt("count") > 0) { showAlert("⚠️ Cannot delete! Required as prerequisite."); return; }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setHeaderText("Delete '" + subjectName + "'?");

                    if(confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        deleteSubject(subjectId, null, null); showDashboard();
                    }
                } catch (SQLException ex) { ex.printStackTrace(); }
            });

            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);
            header.getChildren().addAll(subjTitle, addTopicBtn, deleteSubjectBtn);
            contentArea.getChildren().add(header);

            TreeView<String> treeView = new TreeView<>();
            TreeItem<String> rootItem = new TreeItem<>("Topics");
            rootItem.setExpanded(true);

            PreparedStatement ps = con.prepareStatement("SELECT * FROM syllabus WHERE subject_id=? ORDER BY topic_id");
            ps.setInt(1, subjectId);
            ResultSet rs = ps.executeQuery();

            Map<Integer, TreeItem<String>> itemMap = new HashMap<>();
            Map<TreeItem<String>, Integer> treeItemToIdMap = new HashMap<>();
            List<Integer[]> relations = new ArrayList<>();

            while(rs.next()) {
                int topicId = rs.getInt("topic_id");
                String topicName = rs.getString("topic_name");
                int parentId = rs.getInt("parent_topic_id");

                TreeItem<String> item = new TreeItem<>("📂 " + topicName);
                item.setExpanded(true);
                itemMap.put(topicId, item);
                treeItemToIdMap.put(item, topicId);

                if(parentId == 0 || rs.wasNull()) rootItem.getChildren().add(item);
                else relations.add(new Integer[]{topicId, parentId});
            }

            for(Integer[] rel : relations) {
                TreeItem<String> child = itemMap.get(rel[0]);
                TreeItem<String> parent = itemMap.get(rel[1]);
                if(parent != null && child != null) parent.getChildren().add(child);
            }

            treeView.setRoot(rootItem);
            treeView.setStyle("-fx-font-size: 14px; -fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("🗑 Delete Topic");

            deleteItem.setOnAction(e -> {
                TreeItem<String> selected = treeView.getSelectionModel().getSelectedItem();
                if(selected != null && selected != rootItem) {
                    Integer topicId = treeItemToIdMap.get(selected);
                    if(topicId == null) return;

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setHeaderText("Delete Topic?");
                    if(confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        deleteTopicRecursive(topicId, subjectId, contentArea);
                    }
                }
            });
            contextMenu.getItems().add(deleteItem);
            treeView.setContextMenu(contextMenu);

            treeView.setOnMouseClicked(e -> {
                if(e.getButton() == MouseButton.SECONDARY) {
                    TreeItem<String> selected = treeView.getSelectionModel().getSelectedItem();
                    if(selected != null && selected != rootItem) contextMenu.show(treeView, e.getScreenX(), e.getScreenY());
                }
            });

            if(rootItem.getChildren().isEmpty()) {
                Label emptyLbl = new Label("No topics yet. Click 'Add Topic' to create your syllabus!");
                emptyLbl.setTextFill(Color.web("#64748b"));
                contentArea.getChildren().add(emptyLbl);
            } else {
                contentArea.getChildren().add(treeView);
            }
        } catch (Exception ex) { contentArea.getChildren().add(new Label("Error: " + ex.getMessage())); }
    }

    private void refreshAnalysisContent() {
        if (analysisContentBox == null) return;
        analysisContentBox.getChildren().clear();

        try {
            Connection con = DBConnection.getConnection();

            Label subjTitle = new Label("🎓 Subject Level Scores (Full Syllabus):");
            subjTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            subjTitle.setTextFill(Color.web("#1e293b"));
            analysisContentBox.getChildren().add(subjTitle);

            PreparedStatement ps = con.prepareStatement(
                    "SELECT sub.subject_name, p.score FROM performance p JOIN syllabus s ON p.topic_id = s.topic_id JOIN subjects sub ON p.subject_id = sub.subject_id WHERE p.student_id = ? AND s.topic_name = sub.subject_name ORDER BY p.score ASC"
            );
            ps.setInt(1, currentStudent.getId());
            ResultSet rs = ps.executeQuery();

            boolean hasSubjectScores = false;
            while(rs.next()) {
                hasSubjectScores = true;
                String name = rs.getString("subject_name");
                int score = rs.getInt("score");

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(15));
                row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

                String emoji = score < 60 ? "🔴" : (score < 75 ? "🟡" : "🟢");
                Label lbl = new Label(emoji + " " + name + ": " + score + "%");
                lbl.setFont(Font.font("System", FontWeight.BOLD, 14));
                lbl.setTextFill(Color.web("#1e293b"));

                Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

                ProgressBar progress = new ProgressBar(score / 100.0);
                progress.setPrefWidth(200);
                progress.setStyle("-fx-accent: " + (score < 60 ? "#ef4444" : (score < 75 ? "#f59e0b" : "#10b981")) + ";");

                row.getChildren().addAll(lbl, spacer, progress);
                analysisContentBox.getChildren().add(row);
            }
            if(!hasSubjectScores) {
                Label msg = new Label("No full-subject scores yet."); msg.setTextFill(Color.web("#64748b"));
                analysisContentBox.getChildren().add(msg);
            }

            analysisContentBox.getChildren().add(new Label(""));

            Label topicTitle = new Label("📝 Topic Level Weak Points (< 70%):");
            topicTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            topicTitle.setTextFill(Color.web("#1e293b"));
            analysisContentBox.getChildren().add(topicTitle);

            ps = con.prepareStatement(
                    "SELECT sub.subject_name, s.topic_name, p.score FROM performance p JOIN syllabus s ON p.topic_id = s.topic_id JOIN subjects sub ON s.subject_id = sub.subject_id WHERE p.student_id = ? AND p.score < 70 AND s.topic_name != sub.subject_name ORDER BY p.score ASC LIMIT 10"
            );
            ps.setInt(1, currentStudent.getId());
            rs = ps.executeQuery();

            boolean hasWeakTopics = false;
            while(rs.next()) {
                hasWeakTopics = true;
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12));
                row.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 8; -fx-border-color: #fecaca; -fx-border-radius: 8;");

                Label lbl = new Label("⚠️ " + rs.getString("subject_name") + " > " + rs.getString("topic_name") + ": " + rs.getInt("score") + "%");
                lbl.setTextFill(Color.web("#b91c1c"));
                lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
                row.getChildren().add(lbl);
                analysisContentBox.getChildren().add(row);
            }
            if(!hasWeakTopics) {
                Label msg = new Label("✅ No weak topics!"); msg.setTextFill(Color.web("#10b981"));
                analysisContentBox.getChildren().add(msg);
            }

            if(lastUpdatedLabel != null) lastUpdatedLabel.setText("Updated: " + java.time.LocalTime.now().withSecond(0).withNano(0).toString());

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void generateStudyPlan() {
        if(studyPlanStage != null && studyPlanStage.isShowing()) {
            refreshSimplePlan(); studyPlanStage.toFront(); return;
        }

        studyPlanStage = new Stage();
        studyPlanStage.setTitle("Study Plan - Priority List");
        studyPlanStage.setWidth(600); studyPlanStage.setHeight(700);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");

        HBox header = new HBox(30);
        header.setPadding(new Insets(25));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        int[] counts = getPlanCounts();
        VBox remainingBox = createSimpleCounter("Remaining", counts[0], "#ef4444"); // Red
        VBox completedBox = createSimpleCounter("Completed", counts[1], "#10b981"); // Green

        remainingCountLbl = (Label) ((VBox) remainingBox.getChildren().get(0)).getChildren().get(0);
        completedCountLbl = (Label) ((VBox) completedBox.getChildren().get(0)).getChildren().get(0);

        header.getChildren().addAll(remainingBox, completedBox);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f1f5f9; -fx-border-color: transparent;");

        planContent = new VBox(15);
        planContent.setPadding(new Insets(25));
        planContent.setStyle("-fx-background-color: #f1f5f9;");

        loadPrioritySubjects();

        scroll.setContent(planContent);
        root.setCenter(scroll); root.setTop(header);

        studyPlanStage.setScene(new Scene(root));
        studyPlanStage.show();
    }

    private VBox createSimpleCounter(String label, int count, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15, 30, 15, 30));
        box.setStyle("-fx-background-color: " + color + "15; -fx-background-radius: 8px; -fx-border-color: " + color + "40; -fx-border-radius: 8px;"); // Slight transparency for background

        VBox inner = new VBox();
        Label cnt = new Label(String.valueOf(count));
        cnt.setFont(Font.font("System", FontWeight.BOLD, 36));
        cnt.setTextFill(Color.web(color));
        inner.getChildren().add(cnt);

        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.web(color));

        box.getChildren().addAll(inner, lbl);
        return box;
    }

    private void loadPrioritySubjects() {
        planContent.getChildren().clear();
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT s.subject_id, s.subject_name, AVG(p.score) as avg_score, COUNT(p.topic_id) as topics_count " +
                    "FROM subjects s JOIN performance p ON s.subject_id = p.subject_id AND p.student_id = ? " +
                    "WHERE p.score < 60 GROUP BY s.subject_id ORDER BY AVG(p.score) ASC";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, currentStudent.getId());
            ResultSet rs = ps.executeQuery();

            boolean hasItems = false; int priority = 1;
            while(rs.next()) {
                hasItems = true;
                int subjectId = rs.getInt("subject_id");
                String subjectName = rs.getString("subject_name");
                double avgScore = rs.getDouble("avg_score");
                int topicCount = rs.getInt("topics_count");
                planContent.getChildren().add(createPriorityCard(subjectId, subjectName, avgScore, topicCount, priority++));
            }
            if(!hasItems) {
                Label done = new Label("✓ All subjects above 60% or no data!");
                done.setFont(Font.font("System", FontWeight.BOLD, 18));
                done.setTextFill(Color.web("#10b981"));
                done.setPadding(new Insets(50));
                planContent.getChildren().add(done);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox createPriorityCard(int subjectId, String subjectName, double avgScore, int topicCount, int priorityNum) {
        String color = avgScore < 40 ? "#ef4444" : "#f59e0b";
        boolean isCritical = avgScore < 30;

        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-border-color: " + color + "80; -fx-border-width: 2px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label priorityLbl = new Label("#" + priorityNum);
        priorityLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        priorityLbl.setTextFill(Color.web(color));

        Label nameLbl = new Label(subjectName);
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameLbl.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label scoreLbl = new Label(String.format("%.0f%%", avgScore));
        scoreLbl.setFont(Font.font("System", FontWeight.BOLD, 24));
        scoreLbl.setTextFill(Color.web(color));

        header.getChildren().addAll(priorityLbl, nameLbl, spacer, scoreLbl);

        Label infoLbl = new Label(topicCount + " topics recorded");
        infoLbl.setTextFill(Color.web("#64748b"));

        VBox scheduleBox = new VBox(8);
        scheduleBox.setPadding(new Insets(15));
        scheduleBox.setStyle("-fx-background-color: " + (isCritical ? "#fef2f2" : "#f8fafc") + "; -fx-background-radius: 8px; -fx-border-color: " + (isCritical ? "#fecaca" : "#e2e8f0") + "; -fx-border-radius: 8px;");

        if (isCritical) {
            Label alertLbl = new Label("⚠️ NEEDS IMMEDIATE ATTENTION");
            alertLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
            alertLbl.setTextFill(Color.web("#b91c1c"));
            scheduleBox.getChildren().add(alertLbl);
        }

        Label scheduleHeader = new Label("📅 Study Schedule:");
        scheduleHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        scheduleHeader.setTextFill(Color.web("#1e293b"));

        VBox timeTable = new VBox(5);
        String[] items = isCritical ?
                new String[]{"• Day 1-2: Concept review (3 hrs)", "• Target: Reach 50% in 5 days"} :
                new String[]{"• Day 1: Weak topic review (1.5 hrs)", "• Target: Reach 70% in 3 days"};

        for (String s : items) {
            Label l = new Label(s);
            l.setTextFill(Color.web("#475569"));
            timeTable.getChildren().add(l);
        }

        scheduleBox.getChildren().addAll(scheduleHeader, timeTable);

        Button completeBtn = new Button("✓ Complete");
        completeBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 6px; -fx-cursor: hand;");

        completeBtn.setOnAction(e -> {
            try {
                PreparedStatement ps = DBConnection.getConnection().prepareStatement("UPDATE performance SET score = 100 WHERE subject_id = ? AND student_id = ?");
                ps.setInt(1, subjectId); ps.setInt(2, currentStudent.getId()); ps.executeUpdate();
            } catch (Exception ex) {}

            FadeTransition ft = new FadeTransition(Duration.millis(300), card);
            ft.setToValue(0);
            ft.setOnFinished(ev -> {
                planContent.getChildren().remove(card);
                int[] newCounts = getPlanCounts();
                remainingCountLbl.setText(String.valueOf(newCounts[0]));
                completedCountLbl.setText(String.valueOf(newCounts[1]));
            });
            ft.play();
        });

        HBox buttonBox = new HBox(completeBtn); buttonBox.setAlignment(Pos.CENTER_RIGHT);
        card.getChildren().addAll(header, infoLbl, scheduleBox, buttonBox);
        return card;
    }

    private int[] getPlanCounts() {
        int[] counts = new int[2];
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(DISTINCT s.subject_id) as cnt FROM subjects s JOIN performance p ON s.subject_id = p.subject_id AND p.student_id = ? WHERE p.score < 60");
            ps.setInt(1, currentStudent.getId());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) counts[0] = rs.getInt("cnt");

            ps = con.prepareStatement("SELECT COUNT(DISTINCT s.subject_id) as cnt FROM subjects s JOIN performance p ON s.subject_id = p.subject_id AND p.student_id = ? WHERE p.score >= 60");
            ps.setInt(1, currentStudent.getId());
            rs = ps.executeQuery();
            if(rs.next()) counts[1] = rs.getInt("cnt");
        } catch (Exception e) {}
        return counts;
    }

    private void refreshSimplePlan() {
        int[] counts = getPlanCounts();
        remainingCountLbl.setText(String.valueOf(counts[0]));
        completedCountLbl.setText(String.valueOf(counts[1]));
        loadPrioritySubjects();
    }

    // Cleaned up showAlert method (removed the faulty runLater)
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        System.out.println("Launching...");
        launch(args);
    }
}