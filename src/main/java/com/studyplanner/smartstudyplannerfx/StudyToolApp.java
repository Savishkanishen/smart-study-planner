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
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);");
        
        Label title = new Label(" Smart Study Tool");
        title.setFont(Font.font("System", FontWeight.BOLD, 36));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.3)));
        
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(30));
        formBox.setMaxWidth(400);
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setStyle("-fx-padding: 12; -fx-background-radius: 5; -fx-font-size: 14px;");
        
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-padding: 12; -fx-background-radius: 5; -fx-font-size: 14px;");
        
        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12 40; -fx-background-radius: 25; -fx-cursor: hand;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        
        Button registerBtn = new Button("Create Account");
        registerBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea; -fx-font-size: 14px; -fx-border-color: #667eea; -fx-border-radius: 25; -fx-padding: 10 30; -fx-cursor: hand;");
        
        Label errorLbl = new Label();
        errorLbl.setTextFill(Color.web("#e74c3c"));
        
        loginBtn.setOnAction(e -> {
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM students WHERE email=? AND password=?"
                );
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
            new Label("Welcome Back!") {{ setFont(Font.font("System", FontWeight.BOLD, 24)); setTextFill(Color.web("#2c3e50")); }},
            new Label("Please login to continue") {{ setTextFill(Color.web("#7f8c8d")); }},
            emailField, passField, loginBtn, registerBtn, errorLbl
        );
        
        root.getChildren().addAll(title, formBox);
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }
    
    private void showRegisterScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f093fb, #f5576c);");
        
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(30));
        formBox.setMaxWidth(400);
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setStyle("-fx-padding: 12; -fx-background-radius: 5; -fx-font-size: 14px;");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-padding: 12; -fx-background-radius: 5; -fx-font-size: 14px;");
        
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-padding: 12; -fx-background-radius: 5; -fx-font-size: 14px;");
        
        Button regBtn = new Button("Register");
        regBtn.setStyle("-fx-background-color: #f5576c; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12 40; -fx-background-radius: 25;");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        
        Button backBtn = new Button("Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f5576c; -fx-font-size: 14px;");
        
        Label msgLbl = new Label();
        msgLbl.setTextFill(Color.web("#27ae60"));
        
        regBtn.setOnAction(e -> {
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
                msgLbl.setTextFill(Color.web("#e74c3c"));
            }
        });
        
        backBtn.setOnAction(e -> showLoginScreen());
        
        formBox.getChildren().addAll(
            new Label("Create Account") {{ setFont(Font.font("System", FontWeight.BOLD, 24)); }},
            nameField, emailField, passField, regBtn, backBtn, msgLbl
        );
        
        root.getChildren().addAll(formBox);
        primaryStage.setScene(new Scene(root, 1000, 700));
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
    // Close any existing analysis windows to prevent node conflicts
    if(analysisStage != null && analysisStage.isShowing()) {
        analysisStage.close();
    }
    
    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: #f5f6fa;");
    
    // Sidebar - fresh instance
    VBox sidebar = new VBox(10);
    sidebar.setPadding(new Insets(20));
    sidebar.setPrefWidth(250);
    sidebar.setStyle("-fx-background-color: #2c3e50;");
    
    // CRITICAL FIX: Ensure sidebar is empty before adding
    sidebar.getChildren().clear();
    
    Label userLbl = new Label("👤 " + currentStudent.getName());
    userLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
    userLbl.setTextFill(Color.WHITE);
    userLbl.setWrapText(true);
    
    // Create buttons fresh (local variables, not class fields)
    Button quickAddBtn = createNavButton("➕ Add Score", "#e67e22");
    Button studyPathBtn = createNavButton("📚 Study Path", "#3498db");
    Button syllabusBtn = createNavButton("🌳 Syllabus", "#27ae60");
    Button perfBtn = createNavButton("➕ Add Performance", "#f39c12");
    Button revisionBtn = createNavButton("🔥 Revision Plan", "#e74c3c");
    Button studyPlanBtn = createNavButton("📋 Study Plan", "#9b59b6");
    Button logoutBtn = createNavButton("🚪 Logout", "#95a5a6");
    
    // Set actions
    quickAddBtn.setOnAction(e -> showAddPerformanceDialog());
    studyPathBtn.setOnAction(e -> showAddMySubjectDialog());
    syllabusBtn.setOnAction(e -> showMySyllabusSelector());
    studyPlanBtn.setOnAction(e -> generateStudyPlan());
    
    perfBtn.setOnAction(e -> showAddPerformanceDialog());
    
    revisionBtn.setOnAction(e -> {
        try {
            planner.loadPerformance(currentStudent.getId());
            Runnable refreshView = () -> {
                try {
                    planner.loadPerformance(currentStudent.getId());
                    root.setCenter(planner.getRevisionView(currentStudent.getId(), this::refreshRevisionView));
                } catch (Exception ex) {
                    showAlert("Error refreshing: " + ex.getMessage());
                }
            };
            root.setCenter(planner.getRevisionView(currentStudent.getId(), refreshView));
        } catch (Exception ex) {
            showAlert("Error: " + ex.getMessage());
        }
    });
    
    logoutBtn.setOnAction(e -> {
        currentStudent = null;
        showLoginScreen();
    });
    
    // Spacer
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);
    
    // Add ALL children at once using a single addAll call
    sidebar.getChildren().addAll(
        userLbl, 
        new Region() {{ setPrefHeight(30); }}, 
        studyPathBtn, 
        syllabusBtn, 
        quickAddBtn, 
        studyPlanBtn,
        revisionBtn, 
        spacer,
        logoutBtn
    );
    
    // Welcome content
    VBox welcome = new VBox(20);
    welcome.setAlignment(Pos.CENTER);
    welcome.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f7fa, #c3cfe2);");
    
    Label welcomeLbl = new Label("Welcome, " + currentStudent.getName() + "! 👋");
    welcomeLbl.setFont(Font.font("System", FontWeight.BOLD, 32));
    welcomeLbl.setTextFill(Color.web("#2c3e50"));
    
    Label subLbl = new Label("Select an option from the sidebar to get started");
    subLbl.setFont(Font.font("System", 16));
    subLbl.setTextFill(Color.web("#7f8c8d"));
    
    welcome.getChildren().addAll(welcomeLbl, subLbl);
    
    root.setLeft(sidebar);
    root.setCenter(welcome);
    
    Scene scene = new Scene(root, 1200, 800);
    primaryStage.setScene(scene);
    primaryStage.setMaximized(true);
    
    // Bring to front to ensure it's visible
    primaryStage.toFront();
}
    
    private Button createNavButton(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5; -fx-cursor: hand;"));
        return btn;
    }
    
    
    
    //own data
    
    
   private void showAddMySubjectDialog() {
       
   
       
       
       
    VBox container = new VBox(15);
    container.setPadding(new Insets(20));
    container.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);");
    
    Label title = new Label(" Add My Study Path");
    title.setFont(Font.font("System", FontWeight.BOLD, 24));
    title.setTextFill(Color.WHITE);
    
    // Form card
    VBox formCard = new VBox(15);
    formCard.setPadding(new Insets(25));
    formCard.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 10;");
    formCard.setMaxWidth(500);
    
    // Subject Name
    TextField subjectField = new TextField();
    subjectField.setPromptText("Enter Subject Name (e.g., Advanced Java)");
    subjectField.setStyle("-fx-padding: 12; -fx-background-radius: 5; -fx-font-size: 14px;");
    
    // Description
    TextArea descArea = new TextArea();
    descArea.setPromptText("Description (optional)");
    descArea.setPrefRowCount(3);
    descArea.setStyle("-fx-background-radius: 5;");
    
    // Prerequisite Selection (Your existing subjects)
    ComboBox<String> prereqCombo = new ComboBox<>();
    prereqCombo.setPromptText("None - Leave empty if no prerequisite");
    prereqCombo.setMaxWidth(Double.MAX_VALUE);
    loadMySubjects(prereqCombo); // Load user's existing subjects
    
    // OPTIONAL LABELS
    Label prereqLabel = new Label("Prerequisite (Optional):");
    prereqLabel.setTextFill(Color.web("#7f8c8d"));
    
    Label hint = new Label("Optional: Select if this subject requires another subject first\nOr leave empty if no prerequisites");
    hint.setTextFill(Color.web("#7f8c8d"));
    hint.setFont(Font.font("System", 11));
    
    // Buttons
    HBox btnBox = new HBox(10);
    btnBox.setAlignment(Pos.CENTER);
    
    Button addBtn = new Button("➕ Add Subject");
    addBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30; -fx-background-radius: 20;");
    
    Button viewMyBtn = new Button("👁 View My Subjects");
    viewMyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea; -fx-border-color: #667eea; -fx-padding: 10 20; -fx-background-radius: 20;");
    
    Label status = new Label();
    status.setTextFill(Color.web("#27ae60"));
    status.setFont(Font.font("System", FontWeight.BOLD, 12));
    
    // Button Actions (defined after variables exist)
    addBtn.setOnAction(ev -> {
        String name = subjectField.getText().trim();
        String desc = descArea.getText().trim();
        String prereq = prereqCombo.getValue();
        
        if(name.isEmpty()) {
            status.setText("⚠️ Please enter subject name");
            status.setTextFill(Color.web("#e74c3c"));
            return;
        }
        
        try {
            Connection con = DBConnection.getConnection();
            
            // Insert subject
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO subjects(subject_name, description) VALUES(?,?)", 
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setString(2, desc.isEmpty() ? null : desc);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            int newSubjectId = 0;
            if(rs.next()) newSubjectId = rs.getInt(1);
            
            // Add prerequisite if selected (optional logic)
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
            
            status.setText(" Added: " + name);
            status.setTextFill(Color.web("#27ae60"));
            subjectField.clear();
            descArea.clear();
            prereqCombo.setValue(null);
            loadMySubjects(prereqCombo); // Refresh list
            
            graph.loadSubjects();
            
        } catch (SQLException ex) {
            if(ex.getMessage().contains("Duplicate")) {
                status.setText(" Subject already exists!");
                status.setTextFill(Color.web("#e74c3c"));
            } else {
                status.setText(" Error: " + ex.getMessage());
                status.setTextFill(Color.web("#e74c3c"));
            }
        } catch (Exception ex) {
            Logger.getLogger(StudyToolApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    });
    
       viewMyBtn.setOnAction(ev -> {
    Platform.runLater(() -> showMySubjectsList());
});
    
    // Add to formCard AFTER all variables are initialized
    btnBox.getChildren().addAll(addBtn, viewMyBtn);
    formCard.getChildren().addAll(
        new Label("Subject Name:"), subjectField,
        new Label("Description:"), descArea,
        prereqLabel, prereqCombo, hint,  // Using the optional label here
        btnBox, status
    );
    
    container.getChildren().addAll(title, formCard);
    
    // Back button
    Button backBtn = new Button("← Back to Dashboard");
    backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-padding: 8 20;");
    backBtn.setOnAction(ev -> showDashboard());
    container.getChildren().add(backBtn);
    
    // Set to center
    ((BorderPane) primaryStage.getScene().getRoot()).setCenter(container);
}
    
    //show syllabus sector 
    
    private void showMySyllabusSelector() {
        
        final String[] currentSubjectHolder = new String[1];
        
       
        
        
    VBox container = new VBox(15);
    container.setPadding(new Insets(20));
    container.setStyle("-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);");
    
    
    
    
    
    Label title = new Label("My Syllabus");
    title.setFont(Font.font("System", FontWeight.BOLD, 24));
    title.setTextFill(Color.web("#2c3e50"));
    
    // Subject selector
    HBox selectorBox = new HBox(15);
    selectorBox.setAlignment(Pos.CENTER_LEFT);
    selectorBox.setPadding(new Insets(10));
    selectorBox.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 10;");
    
    Label selectLabel = new Label("Select Your Subject:");
    selectLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
    
    ComboBox<String> subjectCombo = new ComboBox<>();
    subjectCombo.setPromptText("Choose subject...");
    subjectCombo.setPrefWidth(300);
    subjectCombo.setStyle("-fx-padding: 8; -fx-font-size: 14px;");
    
    // Load user's subjects
    try {
        Connection con = DBConnection.getConnection();
        ResultSet rs = con.createStatement().executeQuery("SELECT subject_name FROM subjects");
        while(rs.next()) {
            subjectCombo.getItems().add(rs.getString("subject_name"));
        }
    } catch (Exception e) {}
    
    Button loadBtn = new Button("Load Syllabus");
    loadBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 5;");
    
    // Content area (for selected subject)
    VBox contentArea = new VBox(15);
    contentArea.setPadding(new Insets(15));
    contentArea.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10;");
    
    Label defaultMsg = new Label("👆 Select a subject above to view its syllabus topics");
    defaultMsg.setFont(Font.font("System", 14));
    defaultMsg.setTextFill(Color.web("#7f8c8d"));
    contentArea.getChildren().add(defaultMsg);
    
    loadBtn.setOnAction(e -> {
    String subject = subjectCombo.getValue();
    if(subject == null) return;
    currentSubjectHolder[0] = subject;
        
        contentArea.getChildren().clear();
        
        try {
            Connection con = DBConnection.getConnection();
            
            // Get subject ID
            PreparedStatement ps = con.prepareStatement("SELECT subject_id FROM subjects WHERE subject_name=?");
            ps.setString(1, subject);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next()) {
                int subjId = rs.getInt("subject_id");
                
                Label subjTitle = new Label("📖 " + subject);
                subjTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
                subjTitle.setTextFill(Color.web("#2c3e50"));
                
                // Add new topic button
                Button addTopicBtn = new Button("➕ Add Topic to this Subject");
                addTopicBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15;");
                addTopicBtn.setOnAction(ev -> showAddTopicDialog(subjId, subject, contentArea));
                
                HBox header = new HBox(20);
                header.setAlignment(Pos.CENTER_LEFT);
                header.getChildren().addAll(subjTitle, addTopicBtn);
                
                contentArea.getChildren().add(header);
                
                // Load topics for this subject only
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
                    
                    TreeItem<String> item = new TreeItem<>("📂 " + topicName);
                    item.setExpanded(true);
                    itemMap.put(topicId, item);
                    
                    if(parentId == 0) {
                        rootItem.getChildren().add(item);
                    } else {
                        relations.add(new Integer[]{topicId, parentId});
                    }
                }
                
                // Build hierarchy
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
            }
            
        } catch (Exception ex) {
            contentArea.getChildren().add(new Label("Error: " + ex.getMessage()));
        }
    });
    
    selectorBox.getChildren().addAll(selectLabel, subjectCombo, loadBtn);
    
    // Back button
    Button backBtn = new Button("← Back to Dashboard");
    backBtn.setStyle("-fx-background-color: rgba(44,62,80,0.8); -fx-text-fill: white; -fx-padding: 8 20;");
    backBtn.setOnAction(e -> showDashboard());
    
    container.getChildren().addAll(title, selectorBox, contentArea, backBtn);
    ((BorderPane) primaryStage.getScene().getRoot()).setCenter(container);
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
    parentCombo.setPromptText("Select Parent (Optional)");
    parentCombo.setMaxWidth(Double.MAX_VALUE);
    
    // Store mapping of name -> ID for validation
    Map<String, Integer> parentMap = new HashMap<>();
    
    // Load ONLY root topics from THIS subject
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
            parentMap.put(name, id); // Store ID mapping
        }
        
        // If no parents exist, disable the combo
        if(parentCombo.getItems().isEmpty()) {
            parentCombo.setPromptText("No existing topics - will create as main chapter");
            parentCombo.setDisable(true);
        }
        
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    Label hint = new Label(parentCombo.isDisable() ? 
        "Creating main chapter (no parent available)" : 
        "Leave empty to create main chapter, or select existing topic as parent");
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
            String selectedParent = parentCombo.getValue();
            
            if(topicName.isEmpty()) {
                showAlert("Please enter a topic name!");
                return null;
            }
            
            Connection con = null;
            try {
                con = DBConnection.getConnection();
                con.setAutoCommit(false); // Start transaction
                
                Integer parentId = 0; // Default: no parent (root level)
                
                // If parent selected and combo is not disabled
                if(!parentCombo.isDisabled() && selectedParent != null && !selectedParent.isEmpty()) {
                    // Get ID from our map (safer than querying again)
                    parentId = parentMap.get(selectedParent);
                    
                    if(parentId == null) {
                        showAlert("Error: Selected parent topic not found!");
                        con.rollback();
                        return null;
                    }
                    
                    // Verify parent exists in database
                    PreparedStatement verifyPs = con.prepareStatement(
                        "SELECT topic_id FROM syllabus WHERE topic_id=? AND subject_id=?"
                    );
                    verifyPs.setInt(1, parentId);
                    verifyPs.setInt(2, subjectId);
                    ResultSet verifyRs = verifyPs.executeQuery();
                    
                    if(!verifyRs.next()) {
                        showAlert("Error: Parent topic does not exist in this subject!");
                        con.rollback();
                        return null;
                    }
                }
                
                // Insert new topic
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO syllabus(subject_id, parent_topic_id, topic_name) VALUES(?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setInt(1, subjectId);
                
                // CRITICAL: Handle NULL vs 0 properly
                if(parentId == 0 || parentId == null) {
                    ps.setNull(2, Types.INTEGER); // Use NULL for root level
                } else {
                    ps.setInt(2, parentId);
                }
                
                ps.setString(3, topicName);
                
                int affectedRows = ps.executeUpdate();
                
                if(affectedRows == 0) {
                    showAlert("Error: Failed to insert topic!");
                    con.rollback();
                    return null;
                }
                
                con.commit(); // Commit transaction
                showAlert("✅ Successfully added: " + topicName);
                refreshSyllabusView(subjectId, subjectName, parentContainer);
                
            } catch (Exception ex) {
                try {
                    if(con != null) con.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                showAlert("Error adding topic: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    if(con != null) {
                        con.setAutoCommit(true);
                        con.close();
                    }
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
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
        
        // Recursive deletion function to handle tree structure
        deleteTopicAndChildren(con, topicId);
        
        con.commit();
        showAlert("✅ Topic deleted successfully!");
        
        // Refresh the view - need to get subjectName again
        String subjectName = "";
        PreparedStatement ps = con.prepareStatement("SELECT subject_name FROM subjects WHERE subject_id = ?");
        ps.setInt(1, subjectId);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) subjectName = rs.getString("subject_name");
        
        refreshSyllabusView(subjectId, subjectName, contentArea);
        
        // Refresh analysis window if open
        if(analysisStage != null && analysisStage.isShowing()) {
            refreshAnalysisContent();
        }
        
    } catch (Exception e) {
        try {
            if(con != null) con.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        showAlert("❌ Error deleting topic: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try {
            if(con != null) con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// Helper to recursively delete topic and all its children
private void deleteTopicAndChildren(Connection con, int topicId) throws SQLException {
    // First delete children recursively
    PreparedStatement ps = con.prepareStatement(
        "SELECT topic_id FROM syllabus WHERE parent_topic_id = ?"
    );
    ps.setInt(1, topicId);
    ResultSet rs = ps.executeQuery();
    
    while(rs.next()) {
        int childId = rs.getInt("topic_id");
        deleteTopicAndChildren(con, childId); // Recursive call
    }
    
    // Delete performance scores for this topic
    ps = con.prepareStatement("DELETE FROM performance WHERE topic_id = ?");
    ps.setInt(1, topicId);
    ps.executeUpdate();
    
    // Now delete this topic
    ps = con.prepareStatement("DELETE FROM syllabus WHERE topic_id = ?");
    ps.setInt(1, topicId);
    ps.executeUpdate();
}
    
    
    
    
    

private void loadMySubjects(ComboBox<String> combo) {
    combo.getItems().clear();
    try {
        Connection con = DBConnection.getConnection();
        ResultSet rs = con.createStatement().executeQuery("SELECT subject_name FROM subjects ORDER BY subject_name");
        while(rs.next()) {
            combo.getItems().add(rs.getString("subject_name"));
        }
    } catch (Exception e) {}
}

private void showMySubjectsList() {
    Stage listStage = new Stage();
    listStage.setTitle("My Subjects");
    listStage.setWidth(600);
    listStage.setHeight(500);
    
    VBox content = new VBox(10);
    content.setPadding(new Insets(15));
    content.setStyle("-fx-background-color: #f5f5f5;");
    
    Label header = new Label("📚 Your Study Path (Graph Structure)");
    header.setFont(Font.font("System", FontWeight.BOLD, 18));
    header.setPadding(new Insets(0, 0, 10, 0));
    
    ScrollPane scrollPane = new ScrollPane();
    VBox subjectsContainer = new VBox(8);
    subjectsContainer.setPadding(new Insets(10));
    scrollPane.setContent(subjectsContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background: #f5f5f5;");
    
    // Refresh function
    Runnable refreshList = () -> {
        subjectsContainer.getChildren().clear();
        loadSubjectsIntoContainer(subjectsContainer, listStage);
    };
    
    loadSubjectsIntoContainer(subjectsContainer, listStage);
    
    Button closeBtn = new Button("Close");
    closeBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-padding: 8 20;");
    closeBtn.setOnAction(e -> listStage.close());
    
    content.getChildren().addAll(header, scrollPane, closeBtn);
    
    Scene scene = new Scene(content);
    listStage.setScene(scene);
    listStage.show();
}

private void loadSubjectsIntoContainer(VBox container, Stage parentStage) {
    try {
        Connection con = DBConnection.getConnection();
        
        // FIXED: Removed p.performance_id reference, using COUNT(*) instead
        String sql = "SELECT s.subject_id, s.subject_name, s.description, " +
                     "COUNT(DISTINCT sy.topic_id) as topic_count, " +
                     "COUNT(DISTINCT p.student_id) as score_count, " +  // Changed from performance_id
                     "GROUP_CONCAT(DISTINCT pr.prerequisite_id) as prereq_ids " +
                     "FROM subjects s " +
                     "LEFT JOIN syllabus sy ON s.subject_id = sy.subject_id " +
                     "LEFT JOIN performance p ON s.subject_id = p.subject_id " +
                     "LEFT JOIN prerequisites pr ON s.subject_id = pr.subject_id " +
                     "GROUP BY s.subject_id " +
                     "ORDER BY s.subject_name";
        
        ResultSet rs = con.createStatement().executeQuery(sql);
        
        while(rs.next()) {
            int subjectId = rs.getInt("subject_id");
            String subjectName = rs.getString("subject_name");
            String desc = rs.getString("description");
            int topicCount = rs.getInt("topic_count");
            int scoreCount = rs.getInt("score_count");
            String prereqs = rs.getString("prereq_ids");
            
            // Subject card
            HBox card = new HBox(15);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(12));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            VBox info = new VBox(3);
            Label nameLbl = new Label("📘 " + subjectName);
            nameLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            String details = "";
            if(desc != null && !desc.isEmpty()) details += desc + " • ";
            details += topicCount + " topics";
            if(scoreCount > 0) details += " • " + scoreCount + " scores";
            if(prereqs != null && !prereqs.isEmpty()) {
                details += " • has prerequisites";
            }
            
            Label detailLbl = new Label(details);
            detailLbl.setFont(Font.font("System", 11));
            detailLbl.setTextFill(Color.web("#7f8c8d"));
            
            info.getChildren().addAll(nameLbl, detailLbl);
            HBox.setHgrow(info, Priority.ALWAYS);
            
            // Delete button
            Button deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                              "-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 12px;");
            deleteBtn.setTooltip(new Tooltip("Delete this subject"));
            
            deleteBtn.setOnAction(e -> {
                // Check if this subject is a prerequisite for others
                try {
                    PreparedStatement checkPs = con.prepareStatement(
                        "SELECT COUNT(*) as count FROM prerequisites WHERE prerequisite_id = ?"
                    );
                    checkPs.setInt(1, subjectId);
                    ResultSet checkRs = checkPs.executeQuery();
                    if(checkRs.next() && checkRs.getInt("count") > 0) {
                        showAlert("⚠️ Cannot delete! This subject is required as prerequisite for other subjects.");
                        return;
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                
                // Confirmation dialog
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Deletion");
                confirm.setHeaderText("Delete '" + subjectName + "'?");
                confirm.setContentText("This will permanently delete:\n" +
                                     "• " + topicCount + " topic(s)\n" +
                                     "• " + scoreCount + " performance record(s)\n\n" +
                                     "Are you sure?");
                
                Optional<ButtonType> result = confirm.showAndWait();
                if(result.isPresent() && result.get() == ButtonType.OK) {
                    deleteSubject(subjectId, container, parentStage);
                }
            });
            
            card.getChildren().addAll(info, deleteBtn);
            container.getChildren().add(card);
        }
        
        if(container.getChildren().isEmpty()) {
            container.getChildren().add(new Label("No subjects added yet."));
        }
        
    } catch (Exception e) {
        container.getChildren().add(new Label("Error loading: " + e.getMessage()));
        e.printStackTrace();
    }
}

private void deleteSubject(int subjectId, VBox container, Stage parentStage) {
    Connection con = null;
    try {
        con = DBConnection.getConnection();
        con.setAutoCommit(false);
        
        // 1. Delete performance records
        PreparedStatement ps = con.prepareStatement("DELETE FROM performance WHERE subject_id = ?");
        ps.setInt(1, subjectId);
        ps.executeUpdate();
        
        // 2. Delete prerequisites links
        ps = con.prepareStatement("DELETE FROM prerequisites WHERE subject_id = ? OR prerequisite_id = ?");
        ps.setInt(1, subjectId);
        ps.setInt(2, subjectId);
        ps.executeUpdate();
        
        // 3. Delete topics (cascade will handle children if using FK constraints, otherwise delete in order)
        ps = con.prepareStatement("DELETE FROM syllabus WHERE subject_id = ?");
        ps.setInt(1, subjectId);
        ps.executeUpdate();
        
        // 4. Delete subject
        ps = con.prepareStatement("DELETE FROM subjects WHERE subject_id = ?");
        ps.setInt(1, subjectId);
        int affected = ps.executeUpdate();
        
        con.commit();
        
        if(affected > 0) {
            showAlert("✅ Subject deleted successfully!");
            
            // Refresh list if container exists (from subject list view)
            if(container != null) {
                container.getChildren().clear();
                loadSubjectsIntoContainer(container, parentStage);
            }
            
            // Refresh analysis window if open
            if(analysisStage != null && analysisStage.isShowing()) {
                refreshAnalysisContent();
            }
            
            // Reload graph/tree data
            graph.loadSubjects();
        }
        
    } catch (Exception e) {
        try {
            if(con != null) con.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        showAlert("❌ Error deleting: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try {
            if(con != null) con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
    
    
    
    
private void showAddPerformanceDialog() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Add Your Performance");
    dialog.setHeaderText("Track Marks - Full Syllabus or Specific Topic");
    dialog.getDialogPane().setPrefWidth(550);
    
    ButtonType addBtnType = new ButtonType("Add Score", ButtonBar.ButtonData.OK_DONE);
    ButtonType viewWeakBtnType = new ButtonType("View Analysis", ButtonBar.ButtonData.LEFT);
    dialog.getDialogPane().getButtonTypes().addAll(viewWeakBtnType, addBtnType, ButtonType.CANCEL);
    
    VBox content = new VBox(20);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: #f8f9fa;");
    
    // Score Type Selection
    ToggleGroup scoreTypeGroup = new ToggleGroup();
    HBox typeBox = new HBox(20);
    typeBox.setAlignment(Pos.CENTER);
    
    RadioButton subjectRadio = new RadioButton("Entire Subject (Full Syllabus)");
    subjectRadio.setToggleGroup(scoreTypeGroup);
    subjectRadio.setSelected(true);
    
    RadioButton topicRadio = new RadioButton("Specific Topic Only");
    topicRadio.setToggleGroup(scoreTypeGroup);
    
    typeBox.getChildren().addAll(subjectRadio, topicRadio);
    
    // Subject Selection
    ComboBox<String> subjectCombo = new ComboBox<>();
    subjectCombo.setPromptText("Select Subject");
    subjectCombo.setMaxWidth(Double.MAX_VALUE);
    subjectCombo.setStyle("-fx-padding: 10; -fx-font-size: 14px;");
    
    // Topic Selection
    ComboBox<String> topicCombo = new ComboBox<>();
    topicCombo.setPromptText("Select Topic");
    topicCombo.setMaxWidth(Double.MAX_VALUE);
    topicCombo.setDisable(true); // Disabled by default for Subject mode
    topicCombo.setStyle("-fx-padding: 10; -fx-font-size: 14px;");
    
    // Load subjects
    try {
        Connection con = DBConnection.getConnection();
        ResultSet rs = con.createStatement().executeQuery("SELECT subject_name FROM subjects");
        while(rs.next()) {
            subjectCombo.getItems().add(rs.getString("subject_name"));
        }
    } catch (Exception e) {}
    
    // Load topics when subject changes
    subjectCombo.setOnAction(e -> {
        String selectedSubject = subjectCombo.getValue();
        if(selectedSubject == null) {
            topicCombo.getItems().clear();
            return;
        }
        
        topicCombo.getItems().clear();
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT s.topic_name FROM syllabus s " +
                "JOIN subjects sub ON s.subject_id = sub.subject_id " +
                "WHERE sub.subject_name = ? AND s.topic_name NOT LIKE '📊 Overall Score%'" // Exclude dummy topics from list
            );
            ps.setString(1, selectedSubject);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                topicCombo.getItems().add(rs.getString("topic_name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });
    
    // Toggle between Subject and Topic mode
    scoreTypeGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
        if(newVal == subjectRadio) {
            // Subject mode: Disable topic dropdown
            topicCombo.setDisable(true);
            topicCombo.setValue(null);
            topicCombo.setPromptText("Select Topic");
        } else {
            // Topic mode: Enable dropdown and load topics
            topicCombo.setDisable(false);
            if(subjectCombo.getValue() != null && topicCombo.getItems().isEmpty()) {
                subjectCombo.fireEvent(new ActionEvent()); // Force reload
            }
        }
    });
    
    // Score Input
    VBox scoreBox = new VBox(10);
    Slider scoreSlider = new Slider(0, 100, 50);
    scoreSlider.setShowTickLabels(true);
    scoreSlider.setShowTickMarks(true);
    scoreSlider.setMajorTickUnit(10);
    
    HBox scoreDisplay = new HBox(10);
    scoreDisplay.setAlignment(Pos.CENTER);
    Label scoreLabel = new Label("50");
    scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
    Label percentLabel = new Label("%");
    scoreLabel.setTextFill(Color.web("#667eea"));
    
    scoreSlider.valueProperty().addListener((obs, old, val) -> {
        int score = val.intValue();
        scoreLabel.setText(String.valueOf(score));
        if(score < 50) scoreLabel.setTextFill(Color.web("#e74c3c"));
        else if(score < 70) scoreLabel.setTextFill(Color.web("#f39c12"));
        else scoreLabel.setTextFill(Color.web("#27ae60"));
    });
    
    scoreDisplay.getChildren().addAll(scoreLabel, percentLabel);
    scoreBox.getChildren().addAll(new Label("Your Score:"), scoreSlider, scoreDisplay);
    
    content.getChildren().addAll(
        new Label("Score For:"), typeBox,
        new Label("Subject:"), subjectCombo,
        new Label("Topic:"), topicCombo,
        scoreBox
    );
    
    dialog.getDialogPane().setContent(content);
    
    // View Analysis button
    ((Button)dialog.getDialogPane().lookupButton(viewWeakBtnType)).addEventFilter(ActionEvent.ACTION, e -> {
        e.consume();
        showSubjectAnalysis();
    });
    
    // Add Score button
    dialog.setResultConverter(btn -> {
        if(btn == addBtnType) {
            try {
                String subject = subjectCombo.getValue();
                String topic = topicCombo.getValue();
                int score = (int)scoreSlider.getValue();
                
                if(subject == null) {
                    showAlert("Please select a subject!");
                    return null;
                }
                
                Connection con = DBConnection.getConnection();
                
                // Get subject_id
                PreparedStatement ps = con.prepareStatement("SELECT subject_id FROM subjects WHERE subject_name=?");
                ps.setString(1, subject);
                ResultSet rs = ps.executeQuery();
                int subjId = 0;
                if(rs.next()) subjId = rs.getInt(1);
                
                
                //full score for subjects 
                
               if(subjectRadio.isSelected()) {
    // ========== FULL SYLLABUS (SUBJECT LEVEL) ==========
    // Check if subject-level topic exists (using subject name instead of "Overall Score")
    ps = con.prepareStatement(
        "SELECT topic_id FROM syllabus WHERE subject_id=? AND topic_name=?"
    );
    ps.setInt(1, subjId);
    ps.setString(2, subject); // Use actual subject name, not "Overall Score"
    rs = ps.executeQuery();
    
    int overallTopicId;
    if(rs.next()) {
        overallTopicId = rs.getInt("topic_id");
    } else {
        // Create topic with SUBJECT NAME (not "Overall Score")
        ps = con.prepareStatement(
            "INSERT INTO syllabus(subject_id, parent_topic_id, topic_name) VALUES(?, NULL, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        ps.setInt(1, subjId);
        ps.setString(2, subject); // Use subject name here
        ps.executeUpdate();
        
        ResultSet genKeys = ps.getGeneratedKeys();
        if(genKeys.next()) {
            overallTopicId = genKeys.getInt(1);
        } else {
            throw new SQLException("Failed to create subject score topic");
        }
    }
    
    // Save using this topic_id
    ps = con.prepareStatement(
        "INSERT INTO performance(student_id, subject_id, topic_id, score) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE score=?"
    );
    ps.setInt(1, currentStudent.getId());
    ps.setInt(2, subjId);
    ps.setInt(3, overallTopicId);
    ps.setInt(4, score);
    ps.setInt(5, score);
    ps.executeUpdate();
    
    showAlert("✅ Saved Full Syllabus score: " + subject + " = " + score + "%");


} else {
                    // ========== SPECIFIC TOPIC ==========
                    if(topic == null || topic.isEmpty()) {
                        showAlert("Please select a specific topic, or switch to 'Full Syllabus' mode!");
                        return null;
                    }
                    
                    // Get topic_id
                    ps = con.prepareStatement("SELECT topic_id FROM syllabus WHERE topic_name=? AND subject_id=?");
                    ps.setString(1, topic);
                    ps.setInt(2, subjId);
                    rs = ps.executeQuery();
                    
                    if(rs.next()) {
                        int topicId = rs.getInt("topic_id");
                        
                        ps = con.prepareStatement(
                            "INSERT INTO performance(student_id, subject_id, topic_id, score) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE score=?"
                        );
                        ps.setInt(1, currentStudent.getId());
                        ps.setInt(2, subjId);
                        ps.setInt(3, topicId);
                        ps.setInt(4, score);
                        ps.setInt(5, score);
                        ps.executeUpdate();
                        
                        showAlert("✅ Saved Topic score: " + topic + " = " + score + "%");
                        
                        if(score < 60) showWeakPointAlert(topic, score);
                    }
                }
                
                planner.loadPerformance(currentStudent.getId());
                
            } catch (Exception e) {
                showAlert("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    });
    
    dialog.showAndWait();
}



// Add this helper method in StudyToolApp
private void showDeleteConfirmation(String itemType, String itemName, Runnable deleteAction) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Delete " + itemType);
    confirm.setHeaderText("Delete " + itemName + "?");
    confirm.setContentText("This will also delete all associated scores. Are you sure?");
    
    confirm.showAndWait().ifPresent(response -> {
        if(response.getText().equals("OK")) {
            deleteAction.run();
        }
    });
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


    
    
    
    

private void showSuccessNotification(String topic, int score) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Success");
    alert.setHeaderText("Score Recorded!");
    
    String emoji = score < 60 ? "⚠️" : (score < 80 ? "📊" : "🌟");
    String message = String.format("%s Added %s with score: %d%%\n\n%s", 
        emoji, topic, score,
        score < 60 ? "This is a weak point! Check the Revision Plan." : "Good job! Keep tracking your progress.");
    
    alert.setContentText(message);
    alert.showAndWait();
}

private void showWeakPointAlert(String topic, int score) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("Weak Point Detected");
    alert.setHeaderText("🔴 " + topic + " needs attention!");
    alert.setContentText(String.format(
        "Your score of %d%% is below 60%%.\n\n" +
        "This topic has been added to your Priority Revision Plan.\n" +
        "Go to '🔥 Revision Plan' to see study recommendations.", 
        score));
    alert.showAndWait();
}

private void showWeakPointsSummary() {
    try {
        planner.loadPerformance(currentStudent.getId());
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Your Weak Points Analysis");
        alert.setHeaderText("📉 Topics Requiring Immediate Attention");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        PriorityQueue<Topic> weakQueue = planner.getWeakTopics();
        
        if(weakQueue.isEmpty()) {
            content.getChildren().add(new Label("🎉 No weak points! All scores are above 60%"));
        } else {
            Label summary = new Label(String.format("You have %d weak topics (< 60%%):", weakQueue.size()));
            summary.setFont(Font.font("System", FontWeight.BOLD, 14));
            content.getChildren().add(summary);
            
            int count = 1;
            while(!weakQueue.isEmpty() && count <= 5) {
                Topic t = weakQueue.poll();
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                
                Label bullet = new Label("⚠️");
                Label name = new Label(t.name);
                Label score = new Label(t.score + "%");
                score.setTextFill(Color.web("#e74c3c"));
                score.setFont(Font.font("System", FontWeight.BOLD, 14));
                
                row.getChildren().addAll(bullet, name, score);
                content.getChildren().add(row);
                count++;
            }
            
            if(weakQueue.size() > 5) {
                content.getChildren().add(new Label("... and " + weakQueue.size() + " more"));
            }
        }
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
        
    } catch (Exception e) {
        showAlert("Error loading weak points: " + e.getMessage());
    }
}

private void showSubjectAnalysis() {
    // If window already open, just refresh and bring to front
    if (analysisStage != null && analysisStage.isShowing()) {
        refreshAnalysisContent();
        analysisStage.toFront();
        return;
    }
    
    // Create new non-modal window
    analysisStage = new Stage();
    analysisStage.setTitle("📊 Real-time Performance Analysis");
    analysisStage.setWidth(650);
    analysisStage.setHeight(600);
    
    VBox root = new VBox(15);
    root.setPadding(new Insets(20));
    root.setStyle("-fx-background-color: #f8f9fa;");
    
    // Header with refresh button and timestamp
    HBox header = new HBox(15);
    header.setAlignment(Pos.CENTER_LEFT);
    
    Label title = new Label("Performance Analysis");
    title.setFont(Font.font("System", FontWeight.BOLD, 20));
    title.setTextFill(Color.web("#2c3e50"));
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    lastUpdatedLabel = new Label("Updated: Just now");
    lastUpdatedLabel.setTextFill(Color.web("#7f8c8d"));
    lastUpdatedLabel.setFont(Font.font("System", 11));
    
    Button refreshBtn = new Button("🔄 Refresh");
    refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 15;");
    refreshBtn.setOnAction(e -> refreshAnalysisContent());
    
    header.getChildren().addAll(title, spacer, lastUpdatedLabel, refreshBtn);
    
    // Content box that will be refreshed
    analysisContentBox = new VBox(15);
    analysisContentBox.setPadding(new Insets(10));
    
    ScrollPane scrollPane = new ScrollPane(analysisContentBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background: #f8f9fa;");
    
    root.getChildren().addAll(header, scrollPane);
    
    Scene scene = new Scene(root);
    analysisStage.setScene(scene);
    analysisStage.show();
    
    // Initial load
    refreshAnalysisContent();
}



//refresh sylabus

private void refreshSyllabusView(int subjectId, String subjectName, VBox contentArea) {
    contentArea.getChildren().clear();
    
    try {
        Connection con = DBConnection.getConnection();
        
        Label subjTitle = new Label("📖 " + subjectName);
        subjTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        subjTitle.setTextFill(Color.web("#2c3e50"));
        
        Button addTopicBtn = new Button("➕ Add Topic");
        addTopicBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15;");
        addTopicBtn.setOnAction(ev -> showAddTopicDialog(subjectId, subjectName, contentArea));
        
        // Add Delete Subject button here too for convenience
        Button deleteSubjectBtn = new Button("🗑 Delete Subject");
        deleteSubjectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 15;");
        deleteSubjectBtn.setOnAction(ev -> {
            // Check if prerequisite for others
            try {
                PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) as count FROM prerequisites WHERE prerequisite_id = ?"
                );
                ps.setInt(1, subjectId);
                ResultSet rs = ps.executeQuery();
                if(rs.next() && rs.getInt("count") > 0) {
                    showAlert("⚠️ Cannot delete! This subject is required as prerequisite for other subjects.");
                    return;
                }
                
                // Confirmation
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Subject");
                confirm.setHeaderText("Delete '" + subjectName + "'?");
                confirm.setContentText("This will delete all topics and scores for this subject!");
                
                if(confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    deleteSubject(subjectId, null, null);
                    showDashboard(); // Go back to dashboard
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(subjTitle, addTopicBtn, deleteSubjectBtn);
        
        contentArea.getChildren().add(header);
        
        // Rebuild tree with delete functionality
        TreeView<String> treeView = new TreeView<>();
        TreeItem<String> rootItem = new TreeItem<>("Topics");
        rootItem.setExpanded(true);
        
        PreparedStatement ps = con.prepareStatement("SELECT * FROM syllabus WHERE subject_id=? ORDER BY topic_id");
        ps.setInt(1, subjectId);
        ResultSet rs = ps.executeQuery();
        
        Map<Integer, TreeItem<String>> itemMap = new HashMap<>();
        // Use TreeItem directly as key, NOT hashCode!
        Map<TreeItem<String>, Integer> treeItemToIdMap = new HashMap<>();
        List<Integer[]> relations = new ArrayList<>();
        
        while(rs.next()) {
            int topicId = rs.getInt("topic_id");
            String topicName = rs.getString("topic_name");
            int parentId = rs.getInt("parent_topic_id");
            
            TreeItem<String> item = new TreeItem<>("📂 " + topicName);
            item.setExpanded(true);
            itemMap.put(topicId, item);
            treeItemToIdMap.put(item, topicId); // Direct mapping!
            
            if(parentId == 0 || rs.wasNull()) {
                rootItem.getChildren().add(item);
            } else {
                relations.add(new Integer[]{topicId, parentId});
            }
        }
        
        // Build hierarchy
        for(Integer[] rel : relations) {
            TreeItem<String> child = itemMap.get(rel[0]);
            TreeItem<String> parent = itemMap.get(rel[1]);
            if(parent != null && child != null) {
                parent.getChildren().add(child);
            }
        }
        
        treeView.setRoot(rootItem);
        treeView.setStyle("-fx-font-size: 14px;");
        
        // Right-click context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("🗑 Delete Topic");
        deleteItem.setStyle("-fx-text-fill: #e74c3c;");
        
        deleteItem.setOnAction(e -> {
            TreeItem<String> selected = treeView.getSelectionModel().getSelectedItem();
            if(selected != null && selected != rootItem) {
                // Get ID using the TreeItem directly as key
                Integer topicId = treeItemToIdMap.get(selected);
                
                if(topicId == null) {
                    showAlert("Error: Could not find topic ID");
                    return;
                }
                
                String topicName = selected.getValue().replace("📂 ", "");
                boolean hasChildren = !selected.getChildren().isEmpty();
                
                try {
                    // Check for scores
                    PreparedStatement checkPs = con.prepareStatement(
                        "SELECT COUNT(*) as count FROM performance WHERE topic_id = ?"
                    );
                    checkPs.setInt(1, topicId);
                    ResultSet checkRs = checkPs.executeQuery();
                    int scoreCount = 0;
                    if(checkRs.next()) scoreCount = checkRs.getInt("count");
                    
                    String warning = "Delete '" + topicName + "'?\n\n";
                    if(hasChildren) warning += "⚠️ This topic has sub-topics!\n";
                    if(scoreCount > 0) warning += "⚠️ Will delete " + scoreCount + " score(s)!\n";
                    
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Deletion");
                    confirm.setHeaderText("Delete Topic");
                    confirm.setContentText(warning);
                    
                    if(confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        deleteTopicRecursive(topicId, subjectId, contentArea);
                    }
                    
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Error checking dependencies: " + ex.getMessage());
                }
            }
        });
        
        contextMenu.getItems().add(deleteItem);
        treeView.setContextMenu(contextMenu); // Set context menu on tree
        
        // Also handle right-click to select the item first
        treeView.setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.SECONDARY) {
                TreeItem<String> selected = treeView.getSelectionModel().getSelectedItem();
                if(selected != null && selected != rootItem) {
                    contextMenu.show(treeView, e.getScreenX(), e.getScreenY());
                }
            }
        });
        
        if(rootItem.getChildren().isEmpty()) {
            Label emptyLbl = new Label("No topics yet. Click 'Add Topic' to create your syllabus!");
            emptyLbl.setTextFill(Color.web("#7f8c8d"));
            contentArea.getChildren().add(emptyLbl);
        } else {
            contentArea.getChildren().add(treeView);
            Label hint = new Label("💡 Right-click any topic to delete");
            hint.setFont(Font.font("System", 11));
            hint.setTextFill(Color.web("#7f8c8d"));
            contentArea.getChildren().add(hint);
        }
        
    } catch (Exception ex) {
        ex.printStackTrace();
        contentArea.getChildren().add(new Label("Error: " + ex.getMessage()));
    }
}





private void refreshAnalysisContent() {
    if (analysisContentBox == null) return;
    
    analysisContentBox.getChildren().clear();
    
    try {
        Connection con = DBConnection.getConnection();
        
        // Subject-Level Scores
        Label subjTitle = new Label("🎓 Subject Level Scores (Full Syllabus):");
        subjTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        subjTitle.setTextFill(Color.web("#2c3e50"));
        analysisContentBox.getChildren().add(subjTitle);
        
        PreparedStatement ps = con.prepareStatement(
            "SELECT sub.subject_name, p.score FROM performance p " +
            "JOIN syllabus s ON p.topic_id = s.topic_id " +
            "JOIN subjects sub ON p.subject_id = sub.subject_id " +
            "WHERE p.student_id = ? AND s.topic_name = sub.subject_name " +
            "ORDER BY p.score ASC"
        );
        ps.setInt(1, currentStudent.getId());
        ResultSet rs = ps.executeQuery();
        
        boolean hasSubjectScores = false;
        while(rs.next()) {
            hasSubjectScores = true;
            String name = rs.getString("subject_name");
            int score = rs.getInt("score");
            
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            String emoji = score < 60 ? "🔴" : (score < 75 ? "🟡" : "🟢");
            Label lbl = new Label(emoji + " " + name + ": " + score + "%");
            lbl.setFont(Font.font("System", 14));
            lbl.setTextFill(score < 60 ? Color.web("#e74c3c") : Color.web("#2c3e50"));
            
            // Progress bar
            ProgressBar progress = new ProgressBar(score / 100.0);
            progress.setPrefWidth(150);
            progress.setStyle("-fx-accent: " + (score < 60 ? "#e74c3c" : (score < 75 ? "#f39c12" : "#27ae60")) + ";");
            
            row.getChildren().addAll(lbl, progress);
            analysisContentBox.getChildren().add(row);
        }
        
        if(!hasSubjectScores) {
            analysisContentBox.getChildren().add(new Label("No full-subject scores yet. Use 'Entire Subject' option to add overall marks."));
        }
        
        analysisContentBox.getChildren().add(new Label("")); // Spacer
        
        // Topic-Level Weak Points
        Label topicTitle = new Label("📝 Topic Level Weak Points (< 70%):");
        topicTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        topicTitle.setTextFill(Color.web("#2c3e50"));
        analysisContentBox.getChildren().add(topicTitle);
        
        ps = con.prepareStatement(
            "SELECT sub.subject_name, s.topic_name, p.score FROM performance p " +
            "JOIN syllabus s ON p.topic_id = s.topic_id " +
            "JOIN subjects sub ON s.subject_id = sub.subject_id " +
            "WHERE p.student_id = ? AND p.score < 70 AND s.topic_name != sub.subject_name " +
            "ORDER BY p.score ASC LIMIT 10"
        );
        ps.setInt(1, currentStudent.getId());
        rs = ps.executeQuery();
        
        boolean hasWeakTopics = false;
        while(rs.next()) {
            hasWeakTopics = true;
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
            
            Label lbl = new Label("⚠️ " + rs.getString("subject_name") + " > " + 
                                rs.getString("topic_name") + ": " + rs.getInt("score") + "%");
            lbl.setTextFill(Color.web("#e74c3c"));
            row.getChildren().add(lbl);
            analysisContentBox.getChildren().add(row);
        }
        
        if(!hasWeakTopics) {
            analysisContentBox.getChildren().add(new Label("✅ No weak topics! All specific topics are above 70%"));
        }
        
        analysisContentBox.getChildren().add(new Label("")); // Spacer
        
        // Calculated Averages
        Label avgTitle = new Label("📈 Calculated Averages (from Topic Scores):");
        avgTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        topicTitle.setTextFill(Color.web("#2c3e50"));
        analysisContentBox.getChildren().add(avgTitle);
        
        ps = con.prepareStatement(
            "SELECT sub.subject_name, AVG(p.score) as avg_score, COUNT(*) as count " +
            "FROM performance p JOIN syllabus s ON p.topic_id = s.topic_id " +
            "JOIN subjects sub ON s.subject_id = sub.subject_id " +
            "WHERE p.student_id = ? AND s.topic_name != sub.subject_name " +
            "GROUP BY sub.subject_id HAVING count > 0"
        );
        ps.setInt(1, currentStudent.getId());
        rs = ps.executeQuery();
        
        boolean hasAverages = false;
        while(rs.next()) {
            hasAverages = true;
            int avg = (int)rs.getDouble("avg_score");
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            
            Label lbl = new Label((avg < 60 ? "🔴" : "📊") + " " + rs.getString("subject_name") + 
                                ": " + avg + "% (" + rs.getInt("count") + " topics)");
            lbl.setTextFill(avg < 60 ? Color.web("#e74c3c") : Color.web("#2c3e50"));
            row.getChildren().add(lbl);
            analysisContentBox.getChildren().add(row);
        }
        
        if(!hasAverages) {
            analysisContentBox.getChildren().add(new Label("No topic scores available for average calculation."));
        }
        
        // Update timestamp
        if(lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Updated: " + java.time.LocalTime.now().withSecond(0).withNano(0).toString());
        }
        
    } catch (Exception e) {
        analysisContentBox.getChildren().add(new Label("Error loading data: " + e.getMessage()));
        e.printStackTrace();
    }
}



//study plan


// Add this method to create the completion tracking table (run once)
// ==================== STUDY PLAN METHODS ====================

private void initializeWeekCompletionTable() {
    try {
        Connection con = DBConnection.getConnection();
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS week_completion (" +
                "student_id INT, " +
                "subject_id INT, " +
                "completed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "original_avg_score DOUBLE, " +
                "PRIMARY KEY (student_id, subject_id))");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private int[] calculateWeeklyStats() {
    int[] stats = new int[2]; // [serious(≤40), weekly(41-70)]
    
    try {
        Connection con = DBConnection.getConnection();
        
        String seriousSql = "SELECT COUNT(*) as cnt FROM (" +
                "SELECT s.subject_id FROM subjects s " +
                "JOIN performance p ON s.subject_id = p.subject_id AND p.student_id = ? " +
                "LEFT JOIN week_completion wc ON s.subject_id = wc.subject_id AND wc.student_id = ? " +
                "WHERE wc.subject_id IS NULL " +
                "GROUP BY s.subject_id HAVING AVG(p.score) <= 40) t";
        
        PreparedStatement ps = con.prepareStatement(seriousSql);
        ps.setInt(1, currentStudent.getId());
        ps.setInt(2, currentStudent.getId());
        ResultSet rs = ps.executeQuery();
        if(rs.next()) stats[0] = rs.getInt("cnt");
        
        String weeklySql = "SELECT COUNT(*) as cnt FROM (" +
                "SELECT s.subject_id FROM subjects s " +
                "JOIN performance p ON s.subject_id = p.subject_id AND p.student_id = ? " +
                "LEFT JOIN week_completion wc ON s.subject_id = wc.subject_id AND wc.student_id = ? " +
                "WHERE wc.subject_id IS NULL " +
                "GROUP BY s.subject_id HAVING AVG(p.score) > 40 AND AVG(p.score) <= 70) t";
        
        ps = con.prepareStatement(weeklySql);
        ps.setInt(1, currentStudent.getId());
        ps.setInt(2, currentStudent.getId());
        rs = ps.executeQuery();
        if(rs.next()) stats[1] = rs.getInt("cnt");
        
    } catch (Exception e) {
        e.printStackTrace();
    }
    return stats;
}

private void completeWeek(int subjectId, double originalScore) {
    try {
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO week_completion (student_id, subject_id, original_avg_score) " +
            "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE completed_date = CURRENT_TIMESTAMP"
        );
        ps.setInt(1, currentStudent.getId());
        ps.setInt(2, subjectId);
        ps.setDouble(3, originalScore);
        ps.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Error: " + e.getMessage());
    }
}

private void boostSubjectScore(int subjectId, int boost) {
    try {
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "UPDATE performance SET score = LEAST(100, score + ?) " +
            "WHERE subject_id = ? AND student_id = ?"
        );
        ps.setInt(1, boost);
        ps.setInt(2, subjectId);
        ps.setInt(3, currentStudent.getId());
        ps.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void showCompletedWeeks() {
    Stage stage = new Stage();
    stage.setTitle("Completed Study History");
    stage.setWidth(600);
    stage.setHeight(500);
    
    VBox content = new VBox(15);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: white;");
    
    Label header = new Label("✓ Completed Study Plans");
    header.setFont(Font.font("System", FontWeight.BOLD, 20));
    header.setTextFill(Color.BLACK);
    content.getChildren().add(header);
    
    try {
        Connection con = DBConnection.getConnection();
        String sql = "SELECT s.subject_name, wc.original_avg_score, wc.completed_date " +
                     "FROM week_completion wc JOIN subjects s ON wc.subject_id = s.subject_id " +
                     "WHERE wc.student_id = ? ORDER BY wc.completed_date DESC";
        
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, currentStudent.getId());
        ResultSet rs = ps.executeQuery();
        
        boolean hasData = false;
        while(rs.next()) {
            hasData = true;
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(15));
            row.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");
            
            Label name = new Label(rs.getString("subject_name"));
            name.setFont(Font.font("System", FontWeight.BOLD, 14));
            name.setTextFill(Color.BLACK);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label score = new Label(String.format("%.0f%%", rs.getDouble("original_avg_score")));
            score.setTextFill(Color.web("#27ae60"));
            
            Label date = new Label(rs.getTimestamp("completed_date").toLocalDateTime().toLocalDate().toString());
            date.setTextFill(Color.GRAY);
            
            row.getChildren().addAll(name, spacer, score, date);
            content.getChildren().add(row);
        }
        
        if(!hasData) {
            Label empty = new Label("No completed weeks yet.");
            empty.setTextFill(Color.GRAY);
            content.getChildren().add(empty);
        }
    } catch (Exception e) {
        content.getChildren().add(new Label("Error: " + e.getMessage()));
    }
    
    Button close = new Button("Close");
    close.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 8 20;");
    close.setOnAction(e -> stage.close());
    content.getChildren().add(close);
    
    stage.setScene(new Scene(content));
    stage.show();
}

private void generateStudyPlan() {
    if(studyPlanStage != null && studyPlanStage.isShowing()) {
        refreshSimplePlan();
        studyPlanStage.toFront();
        return;
    }
    
    studyPlanStage = new Stage();
    studyPlanStage.setTitle("Study Plan - Priority List");
    studyPlanStage.setWidth(600);
    studyPlanStage.setHeight(700);
    
    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: #ffffff;");
    
    // Simple header with counters
    HBox header = new HBox(30);
    header.setPadding(new Insets(20));
    header.setAlignment(Pos.CENTER);
    header.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd; -fx-border-width: 0 0 2 0;");
    
    // Initialize counters
    int[] counts = getPlanCounts();
    
    VBox remainingBox = createSimpleCounter("Remaining", counts[0], "#e74c3c");
    VBox completedBox = createSimpleCounter("Completed", counts[1], "#27ae60");
    
    remainingCountLbl = (Label) ((VBox) remainingBox.getChildren().get(0)).getChildren().get(0);
    completedCountLbl = (Label) ((VBox) completedBox.getChildren().get(0)).getChildren().get(0);
    
    header.getChildren().addAll(remainingBox, completedBox);
    
    ScrollPane scroll = new ScrollPane();
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background: white;");
    
    planContent = new VBox(10);
    planContent.setPadding(new Insets(15));
    planContent.setStyle("-fx-background-color: white;");
    
    loadPrioritySubjects();
    
    scroll.setContent(planContent);
    root.setCenter(scroll);
    root.setTop(header);
    
    Scene scene = new Scene(root);
    studyPlanStage.setScene(scene);
    studyPlanStage.show();
}

private VBox createSimpleCounter(String label, int count, String color) {
    VBox box = new VBox(5);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(15, 30, 15, 30));
    box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8px;");
    
    VBox inner = new VBox(); // Wrapper to hold reference
    Label cnt = new Label(String.valueOf(count));
    cnt.setFont(Font.font("System", FontWeight.BOLD, 32));
    cnt.setTextFill(Color.WHITE);
    inner.getChildren().add(cnt);
    
    Label lbl = new Label(label);
    lbl.setFont(Font.font("System", FontWeight.BOLD, 14));
    lbl.setTextFill(Color.WHITE);
    
    box.getChildren().addAll(inner, lbl);
    return box;
}

private void loadPrioritySubjects() {
    planContent.getChildren().clear();
    
    try {
        Connection con = DBConnection.getConnection();
        
        // Get subjects with avg score < 60, sorted low to high
        String sql = "SELECT s.subject_id, s.subject_name, AVG(p.score) as avg_score, COUNT(p.topic_id) as topics_count " +
                     "FROM subjects s " +
                     "JOIN performance p ON s.subject_id = p.subject_id AND p.student_id = ? " +
                     "WHERE p.score < 60 " +
                     "GROUP BY s.subject_id " +
                     "ORDER BY AVG(p.score) ASC";
        
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, currentStudent.getId());
        ResultSet rs = ps.executeQuery();
        
        boolean hasItems = false;
        int priority = 1;
        
        while(rs.next()) {
            hasItems = true;
            int subjectId = rs.getInt("subject_id");
            String subjectName = rs.getString("subject_name");
            double avgScore = rs.getDouble("avg_score");
            int topicCount = rs.getInt("topics_count");
            
            VBox card = createPriorityCard(subjectId, subjectName, avgScore, topicCount, priority++);
            planContent.getChildren().add(card);
        }
        
        if(!hasItems) {
            Label done = new Label("✓ All subjects above 60% or no data!");
            done.setFont(Font.font("System", FontWeight.BOLD, 18));
            done.setTextFill(Color.web("#27ae60"));
            done.setPadding(new Insets(50));
            planContent.getChildren().add(done);
        }
        
    } catch (Exception e) {
        Label err = new Label("Error: " + e.getMessage());
        err.setTextFill(Color.BLACK);
        planContent.getChildren().add(err);
    }
}

private VBox createPriorityCard(int subjectId, String subjectName, double avgScore, int topicCount, int priorityNum) {
    String color = avgScore < 40 ? "#e74c3c" : "#f39c12";
    boolean isCritical = avgScore < 30;
    
    VBox card = new VBox(10);
    card.setPadding(new Insets(15));
    card.setStyle("-fx-background-color: white; " +
                  "-fx-border-color: " + color + "; " +
                  "-fx-border-width: 2px; " +
                  "-fx-border-radius: 8px; " +
                  "-fx-background-radius: 8px;");
    
    // Header row
    HBox header = new HBox(10);
    header.setAlignment(Pos.CENTER_LEFT);
    
    Label priorityLbl = new Label("#" + priorityNum);
    priorityLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
    priorityLbl.setTextFill(Color.web(color));
    
    Label nameLbl = new Label(subjectName);
    nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
    nameLbl.setTextFill(Color.BLACK);
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    Label scoreLbl = new Label(String.format("%.0f%%", avgScore));
    scoreLbl.setFont(Font.font("System", FontWeight.BOLD, 20));
    scoreLbl.setTextFill(Color.web(color));
    
    header.getChildren().addAll(priorityLbl, nameLbl, spacer, scoreLbl);
    
    // Info
    Label infoLbl = new Label(topicCount + " topics recorded");
    infoLbl.setTextFill(Color.BLACK);
    infoLbl.setFont(Font.font(12));
    
    // MESSAGE & TIME TABLE BASED ON SCORE
    VBox scheduleBox = new VBox(8);
    scheduleBox.setPadding(new Insets(10));
    scheduleBox.setStyle("-fx-background-color: " + (isCritical ? "#ffebee" : "#fff3e0") + "; " +
                        "-fx-background-radius: 5px;");
    
    if (isCritical) {
        // Below 30% - Critical Alert
        Label alertLbl = new Label("⚠️ NEEDS IMMEDIATE ATTENTION!");
        alertLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        alertLbl.setTextFill(Color.web("#c62828"));
        
        Label subtext = new Label("Score critically low. Intensive study required.");
        subtext.setTextFill(Color.BLACK);
        subtext.setFont(Font.font(11));
        
        scheduleBox.getChildren().addAll(alertLbl, subtext);
    }
    
    // Time Table (for both ranges, different intensity)
    Label scheduleHeader = new Label(isCritical ? "📅 Intensive Schedule:" : "📅 Study Schedule:");
    scheduleHeader.setFont(Font.font("System", FontWeight.BOLD, 12));
    scheduleHeader.setTextFill(Color.BLACK);
    
    VBox timeTable = new VBox(5);
    timeTable.setPadding(new Insets(0, 0, 0, 10));
    
    if (isCritical) {
        // Below 30%: Intensive 3 hours daily
        Label[] items = {
            new Label("• Day 1-2: Concept review & basics (3 hrs)"),
            new Label("• Day 3-4: Practice problems (3 hrs)"),
            new Label("• Day 5: Mock test & revision (2 hrs)"),
            new Label("• Target: Reach 50% within 5 days")
        };
        for (Label l : items) {
            l.setTextFill(Color.BLACK);
            l.setFont(Font.font(11));
            timeTable.getChildren().add(l);
        }
    } else {
        // 30-60%: Moderate 1.5 hours daily
        Label[] items = {
            new Label("• Day 1: Weak topic review (1.5 hrs)"),
            new Label("• Day 2: Practice questions (1.5 hrs)"),
            new Label("• Day 3: Past paper practice (1.5 hrs)"),
            new Label("• Target: Reach 70% within 3 days")
        };
        for (Label l : items) {
            l.setTextFill(Color.BLACK);
            l.setFont(Font.font(11));
            timeTable.getChildren().add(l);
        }
    }
    
    scheduleBox.getChildren().addAll(scheduleHeader, timeTable);
    
    // Complete button
    Button completeBtn = new Button("✓ Complete");
    completeBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 8 20; " +
                        "-fx-background-radius: 15px;");
    
    completeBtn.setOnAction(e -> {
        markSubjectAsComplete(subjectId);
        
        FadeTransition ft = new FadeTransition(Duration.millis(200), card);
        ft.setToValue(0);
        ft.setOnFinished(ev -> {
            planContent.getChildren().remove(card);
            
            int[] newCounts = getPlanCounts();
            remainingCountLbl.setText(String.valueOf(newCounts[0]));
            completedCountLbl.setText(String.valueOf(newCounts[1]));
            
            if(planContent.getChildren().isEmpty()) {
                Label allDone = new Label("✓ All caught up!");
                allDone.setFont(Font.font("System", FontWeight.BOLD, 18));
                allDone.setTextFill(Color.web("#27ae60"));
                allDone.setPadding(new Insets(50));
                planContent.getChildren().add(allDone);
            }
        });
        ft.play();
    });
    
    HBox buttonBox = new HBox();
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.getChildren().add(completeBtn);
    
    // Add all to card: header, info, schedule (message + timetable), button
    card.getChildren().addAll(header, infoLbl, scheduleBox, buttonBox);
    return card;
}

private void markSubjectAsComplete(int subjectId) {
    try {
        Connection con = DBConnection.getConnection();
        // Option 1: Boost all scores to 100
        PreparedStatement ps = con.prepareStatement(
            "UPDATE performance SET score = 100 WHERE subject_id = ? AND student_id = ?"
        );
        ps.setInt(1, subjectId);
        ps.setInt(2, currentStudent.getId());
        ps.executeUpdate();
        
        // Option 2: Or insert into completion tracking table if you prefer
        // Keep old marks but track completion separately
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private int[] getPlanCounts() {
    int[] counts = new int[2]; // [remaining, completed]
    
    try {
        Connection con = DBConnection.getConnection();
        
        // Count remaining (<60%)
        String remainingSql = "SELECT COUNT(DISTINCT s.subject_id) as cnt " +
                             "FROM subjects s " +
                             "JOIN performance p ON s.subject_id = p.subject_id AND p.student_id = ? " +
                             "WHERE p.score < 60";
        
        PreparedStatement ps = con.prepareStatement(remainingSql);
        ps.setInt(1, currentStudent.getId());
        ResultSet rs = ps.executeQuery();
        if(rs.next()) counts[0] = rs.getInt("cnt");
        
        // Count completed (>=60% or marked complete) - assuming completed means >=60
        String completedSql = "SELECT COUNT(DISTINCT s.subject_id) as cnt " +
                             "FROM subjects s " +
                             "JOIN performance p ON s.subject_id = p.subject_id AND p.student_id = ? " +
                             "WHERE p.score >= 60";
        
        ps = con.prepareStatement(completedSql);
        ps.setInt(1, currentStudent.getId());
        rs = ps.executeQuery();
        if(rs.next()) counts[1] = rs.getInt("cnt");
        
    } catch (Exception e) {
        e.printStackTrace();
    }
    return counts;
}

private void refreshSimplePlan() {
    int[] counts = getPlanCounts();
    remainingCountLbl.setText(String.valueOf(counts[0]));
    completedCountLbl.setText(String.valueOf(counts[1]));
    loadPrioritySubjects();
}



    
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
        // After: showAlert("✅ Saved Full Syllabus score: " + subject + " = " + score + "%");
// ADD THIS LINE:
Platform.runLater(() -> refreshAnalysisContent());

// And after the topic-specific save (around line 900), add:
Platform.runLater(() -> refreshAnalysisContent());
    }
    
    public static void main(String[] args) {
        System.out.println("Launching..."); 
        launch(args);
    }
}