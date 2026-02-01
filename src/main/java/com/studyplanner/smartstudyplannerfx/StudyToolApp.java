package com.studyplanner.smartstudyplannerfx;

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
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;

public class StudyToolApp extends Application {
    private Stage primaryStage;
    private Student currentStudent;
    private StudyGraph graph = new StudyGraph();
    private SyllabusTree tree = new SyllabusTree();
    private RevisionPlanner planner = new RevisionPlanner();
    
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
        
        Label title = new Label("🎓 Smart Study Tool");
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
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f6fa;");
        
        // Sidebar
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #2c3e50;");
       
        
        Label userLbl = new Label("👤 " + currentStudent.getName());
        userLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        userLbl.setTextFill(Color.WHITE);
        userLbl.setWrapText(true);
        
        Button quickAddBtn = createNavButton("➕ Quick Add Score", "#e67e22");
quickAddBtn.setOnAction(e -> showAddPerformanceDialog());
        Button studyPathBtn = createNavButton("📚 Study Path", "#3498db");
        Button syllabusBtn = createNavButton("🌳 Syllabus", "#27ae60");
        Button perfBtn = createNavButton("➕ Add Performance", "#f39c12");
        Button revisionBtn = createNavButton("🔥 Revision Plan", "#e74c3c");
        Button logoutBtn = createNavButton("🚪 Logout", "#95a5a6");
        
        
        // STUDY PATH - Now for Adding Your Subjects (Not showing default graph)
studyPathBtn.setOnAction(e -> showAddMySubjectDialog());

// SYLLABUS - Now for Viewing/Selecting Your Data (Not showing default tree)
syllabusBtn.setOnAction(e -> showMySyllabusSelector());



        
       // studyPathBtn.setOnAction(e -> root.setCenter(graph.getStudyPathView()));
       // syllabusBtn.setOnAction(e -> root.setCenter(tree.getTreeView()));
       
       
        revisionBtn.setOnAction(e -> {
            try {
                planner.loadPerformance(currentStudent.getId());
                root.setCenter(planner.getRevisionView(currentStudent.getId()));
            } catch (Exception ex) {
                showAlert("Error: " + ex.getMessage());
            }
        });
        perfBtn.setOnAction(e -> showAddPerformanceDialog());
        logoutBtn.setOnAction(e -> showLoginScreen());
        
        Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);
        
        
         sidebar.getChildren().addAll(userLbl, new Region() {{ setPrefHeight(30); }}, 
    studyPathBtn, syllabusBtn, quickAddBtn, revisionBtn, logoutBtn);
        
        // Default center
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
    
    Label title = new Label("📚 Add My Study Path");
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
    
    Label hint = new Label("💡 Optional: Select if this subject requires another subject first\nOr leave empty if no prerequisites");
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
            
            status.setText("✅ Added: " + name);
            status.setTextFill(Color.web("#27ae60"));
            subjectField.clear();
            descArea.clear();
            prereqCombo.setValue(null);
            loadMySubjects(prereqCombo); // Refresh list
            
            graph.loadSubjects();
            
        } catch (SQLException ex) {
            if(ex.getMessage().contains("Duplicate")) {
                status.setText("⚠️ Subject already exists!");
                status.setTextFill(Color.web("#e74c3c"));
            } else {
                status.setText("✗ Error: " + ex.getMessage());
                status.setTextFill(Color.web("#e74c3c"));
            }
        } catch (Exception ex) {
            Logger.getLogger(StudyToolApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    });
    
    viewMyBtn.setOnAction(ev -> showMySubjectsList());
    
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
    VBox container = new VBox(15);
    container.setPadding(new Insets(20));
    container.setStyle("-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);");
    
    Label title = new Label("🌳 My Syllabus");
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
        "💡 Creating main chapter (no parent available)" : 
        "💡 Leave empty to create main chapter, or select existing topic as parent");
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

// Helper method to refresh the tree view after adding
private void refreshSyllabusView(int subjectId, String subjectName, VBox contentArea) {
    contentArea.getChildren().clear();
    
    try {
        Connection con = DBConnection.getConnection();
        
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
        
        // Rebuild tree
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
        
    } catch (Exception ex) {
        contentArea.getChildren().add(new Label("Error refreshing: " + ex.getMessage()));
    }
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
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("My Subjects");
    alert.setHeaderText("📚 Your Study Path (Graph Structure)");
    
    VBox content = new VBox(10);
    content.setPadding(new Insets(10));
    
    try {
        Connection con = DBConnection.getConnection();
        
        // Get subjects with prerequisites
        String sql = "SELECT s.subject_name, GROUP_CONCAT(p.subject_name) as prereqs " +
                     "FROM subjects s LEFT JOIN prerequisites pr ON s.subject_id = pr.subject_id " +
                     "LEFT JOIN subjects p ON pr.prerequisite_id = p.subject_id " +
                     "GROUP BY s.subject_id";
        
        ResultSet rs = con.createStatement().executeQuery(sql);
        
        while(rs.next()) {
            String subject = rs.getString("subject_name");
            String prereqs = rs.getString("prereqs");
            
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            
            Label subjLabel = new Label("📘 " + subject);
            subjLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            String prereqText = (prereqs == null || prereqs.isEmpty()) ? " (No prerequisite)" : " → Requires: " + prereqs;
            Label prereqLabel = new Label(prereqText);
            prereqLabel.setTextFill(Color.web("#7f8c8d"));
            
            row.getChildren().addAll(subjLabel, prereqLabel);
            content.getChildren().add(row);
        }
        
        if(content.getChildren().isEmpty()) {
            content.getChildren().add(new Label("No subjects added yet. Add your first subject above!"));
        }
        
    } catch (Exception e) {
        content.getChildren().add(new Label("Error loading: " + e.getMessage()));
    }
    
    alert.getDialogPane().setContent(content);
    alert.showAndWait();
}
    
    
    
    
private void showAddPerformanceDialog() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Add Your Performance");
    dialog.setHeaderText("Track Marks - Subject or Topic Level");
    dialog.getDialogPane().setPrefWidth(550);
    
    ButtonType addBtnType = new ButtonType("Add Score", ButtonBar.ButtonData.OK_DONE);
    ButtonType viewWeakBtnType = new ButtonType("View Analysis", ButtonBar.ButtonData.LEFT);
    dialog.getDialogPane().getButtonTypes().addAll(viewWeakBtnType, addBtnType, ButtonType.CANCEL);
    
    VBox content = new VBox(20);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: #f8f9fa;");
    
    // Score Type Selection (NEW)
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
    
    // Topic Selection (NEW - only visible when topicRadio selected)
    ComboBox<String> topicCombo = new ComboBox<>();
    topicCombo.setPromptText("Select Topic");
    topicCombo.setMaxWidth(Double.MAX_VALUE);
    topicCombo.setDisable(true); // Disabled by default
    
    // Load subjects
    try {
        Connection con = DBConnection.getConnection();
        ResultSet rs = con.createStatement().executeQuery("SELECT subject_name FROM subjects");
        while(rs.next()) {
            subjectCombo.getItems().add(rs.getString("subject_name"));
        }
    } catch (Exception e) {}
    
    // Update topics when subject changes (only for topic mode)
    subjectCombo.setOnAction(e -> {
        if(!topicRadio.isSelected()) return;
        
        String selectedSubject = subjectCombo.getValue();
        if(selectedSubject == null) return;
        
        topicCombo.getItems().clear();
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT s.topic_name FROM syllabus s JOIN subjects sub ON s.subject_id = sub.subject_id WHERE sub.subject_name = ?"
            );
            ps.setString(1, selectedSubject);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                topicCombo.getItems().add(rs.getString("topic_name"));
            }
            
            if(topicCombo.getItems().isEmpty()) {
                topicCombo.setPromptText("No topics - use Subject mode instead");
            } else {
                topicCombo.setPromptText("Select Topic");
            }
        } catch (Exception ex) {}
    });
    
    // Toggle visibility based on selection
    scoreTypeGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
        if(newVal == subjectRadio) {
            topicCombo.setDisable(true);
            topicCombo.setValue(null);
        } else {
            topicCombo.setDisable(false);
            // Trigger subject combo to load topics
            if(subjectCombo.getValue() != null) {
                subjectCombo.fireEvent(new ActionEvent());
            }
        }
    });
    
    // Score Slider
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
    
    // Subject Average Preview (NEW)
    TitledPane previewPane = new TitledPane("Current Subject Performance", new Label("Select a subject to see current average"));
    previewPane.setCollapsible(false);
    
    subjectCombo.setOnAction(e -> {
        String subject = subjectCombo.getValue();
        if(subject == null) return;
        
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT AVG(score) as avg_score, COUNT(*) as count FROM performance p " +
                "JOIN syllabus s ON p.topic_id = s.topic_id " +
                "JOIN subjects sub ON s.subject_id = sub.subject_id " +
                "WHERE p.student_id = ? AND sub.subject_name = ?"
            );
            ps.setInt(1, currentStudent.getId());
            ps.setString(2, subject);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next() && rs.getInt("count") > 0) {
                int avg = rs.getInt("avg_score");
                Label avgLabel = new Label(String.format("Current Average: %d%% (%d topics scored)", avg, rs.getInt("count")));
                avgLabel.setTextFill(avg < 60 ? Color.web("#e74c3c") : (avg < 70 ? Color.web("#f39c12") : Color.web("#27ae60")));
                avgLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                previewPane.setContent(avgLabel);
            } else {
                previewPane.setContent(new Label("No scores yet for this subject"));
            }
        } catch (Exception ex) {}
    });
    
    content.getChildren().addAll(
        new Label("Score For:"), typeBox,
        new Label("Subject:"), subjectCombo,
        new Label("Topic:"), topicCombo,
        scoreBox,
        previewPane
    );
    
    dialog.getDialogPane().setContent(content);
    
    // Handle buttons
    ((Button)dialog.getDialogPane().lookupButton(viewWeakBtnType)).addEventFilter(ActionEvent.ACTION, e -> {
        e.consume();
        showSubjectAnalysis(); // New method for subject-level analysis
    });
    
    dialog.setResultConverter(dialogButton -> {
        if(dialogButton == addBtnType) {
            String subject = subjectCombo.getValue();
            String topic = topicCombo.getValue();
            int score = (int)scoreSlider.getValue();
            
            if(subject == null) {
                showAlert("Please select a subject!");
                return null;
            }
            
            try {
                Connection con = DBConnection.getConnection();
                
                if(subjectRadio.isSelected()) {
                    // SUBJECT-LEVEL SCORE (Full Syllabus)
                    // First get subject_id
                    PreparedStatement ps = con.prepareStatement("SELECT subject_id FROM subjects WHERE subject_name=?");
                    ps.setString(1, subject);
                    ResultSet rs = ps.executeQuery();
                    
                    if(rs.next()) {
                        int subjId = rs.getInt("subject_id");
                        
                        // Insert subject-level score (topic_id is NULL)
                        ps = con.prepareStatement(
                            "INSERT INTO performance(student_id, subject_id, topic_id, score, last_updated) " +
                            "VALUES(?,?,NULL,?,NOW()) ON DUPLICATE KEY UPDATE score=?, last_updated=NOW()"
                        );
                        ps.setInt(1, currentStudent.getId());
                        ps.setInt(2, subjId);
                        ps.setInt(3, score);
                        ps.setInt(4, score);
                        ps.executeUpdate();
                        
                        showAlert("✅ Saved subject score: " + subject + " = " + score + "%");
                    }
                } else {
                    // TOPIC-LEVEL SCORE
                    if(topic == null || topic.isEmpty()) {
                        showAlert("Please select a topic, or switch to 'Entire Subject' mode!");
                        return null;
                    }
                    
                    PreparedStatement ps = con.prepareStatement("SELECT topic_id FROM syllabus WHERE topic_name=?");
                    ps.setString(1, topic);
                    ResultSet rs = ps.executeQuery();
                    
                    if(rs.next()) {
                        int topicId = rs.getInt("topic_id");
                        
                        // Also get subject_id for consistency
                        ps = con.prepareStatement("SELECT subject_id FROM subjects WHERE subject_name=?");
                        ps.setString(1, subject);
                        rs = ps.executeQuery();
                        int subjId = rs.next() ? rs.getInt("subject_id") : 0;
                        
                        ps = con.prepareStatement(
                            "INSERT INTO performance(student_id, subject_id, topic_id, score, last_updated) " +
                            "VALUES(?,?,?,?,NOW()) ON DUPLICATE KEY UPDATE score=?, last_updated=NOW()"
                        );
                        ps.setInt(1, currentStudent.getId());
                        ps.setInt(2, subjId);
                        ps.setInt(3, topicId);
                        ps.setInt(4, score);
                        ps.setInt(5, score);
                        ps.executeUpdate();
                        
                        showAlert("✅ Saved topic score: " + topic + " = " + score + "%");
                        
                        if(score < 60) showWeakPointAlert(topic, score);
                    }
                }
                
                planner.loadPerformance(currentStudent.getId());
                
            } catch (Exception e) {
                showAlert("Error: " + e.getMessage());
            }
        }
        return null;
    });
    
    dialog.showAndWait();
}


private void showSubjectAnalysis() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Performance Analysis");
    alert.setHeaderText("📊 Subject & Topic Weak Points");
    
    VBox content = new VBox(15);
    content.setPadding(new Insets(10));
    
    try {
        Connection con = DBConnection.getConnection();
        
        // Get Subject-Level Scores (Full Syllabus marks)
        Label subjTitle = new Label("🎓 Subject Level Scores (Full Syllabus):");
        subjTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        content.getChildren().add(subjTitle);
        
        PreparedStatement ps = con.prepareStatement(
            "SELECT sub.subject_name, p.score FROM performance p " +
            "JOIN subjects sub ON p.subject_id = sub.subject_id " +
            "WHERE p.student_id = ? AND p.topic_id IS NULL " +
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
            
            String emoji = score < 60 ? "🔴" : (score < 75 ? "🟡" : "🟢");
            Label lbl = new Label(emoji + " " + name + ": " + score + "%");
            lbl.setFont(Font.font("System", 14));
            lbl.setTextFill(score < 60 ? Color.web("#e74c3c") : Color.web("#2c3e50"));
            
            row.getChildren().add(lbl);
            content.getChildren().add(row);
        }
        
        if(!hasSubjectScores) {
            content.getChildren().add(new Label("No full-subject scores yet. Use 'Entire Subject' option to add overall marks."));
        }
        
        // Separator
        content.getChildren().add(new Label(""));
        
        // Get Topic-Level Weak Points (detailed)
        Label topicTitle = new Label("📝 Topic Level Weak Points:");
        topicTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        content.getChildren().add(topicTitle);
        
        ps = con.prepareStatement(
            "SELECT sub.subject_name, s.topic_name, p.score FROM performance p " +
            "JOIN syllabus s ON p.topic_id = s.topic_id " +
            "JOIN subjects sub ON s.subject_id = sub.subject_id " +
            "WHERE p.student_id = ? AND p.score < 70 " +
            "ORDER BY p.score ASC LIMIT 10"
        );
        ps.setInt(1, currentStudent.getId());
        rs = ps.executeQuery();
        
        while(rs.next()) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            
            Label lbl = new Label("⚠️ " + rs.getString("subject_name") + " > " + 
                                rs.getString("topic_name") + ": " + rs.getInt("score") + "%");
            lbl.setTextFill(Color.web("#e74c3c"));
            row.getChildren().add(lbl);
            content.getChildren().add(row);
        }
        
        // Calculate Subject Averages from Topics
        content.getChildren().add(new Label(""));
        Label avgTitle = new Label("📈 Calculated Averages (from Topic Scores):");
        avgTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        content.getChildren().add(avgTitle);
        
        ps = con.prepareStatement(
            "SELECT sub.subject_name, AVG(p.score) as avg_score, COUNT(*) as count " +
            "FROM performance p JOIN syllabus s ON p.topic_id = s.topic_id " +
            "JOIN subjects sub ON s.subject_id = sub.subject_id " +
            "WHERE p.student_id = ? GROUP BY sub.subject_id HAVING count > 0"
        );
        ps.setInt(1, currentStudent.getId());
        rs = ps.executeQuery();
        
        while(rs.next()) {
            int avg = (int)rs.getDouble("avg_score");
            HBox row = new HBox(10);
            
            Label lbl = new Label((avg < 60 ? "🔴" : "📊") + " " + rs.getString("subject_name") + 
                                ": " + avg + "% (" + rs.getInt("count") + " topics)");
            lbl.setTextFill(avg < 60 ? Color.web("#e74c3c") : Color.web("#2c3e50"));
            row.getChildren().add(lbl);
            content.getChildren().add(row);
        }
        
    } catch (Exception e) {
        content.getChildren().add(new Label("Error: " + e.getMessage()));
    }
    
    alert.getDialogPane().setContent(content);
    alert.showAndWait();
}
    


    /*private void loadMySubjects(ComboBox<String> combo) {
    combo.getItems().clear();
    try {
    Connection con = DBConnection.getConnection();
    ResultSet rs = con.createStatement().executeQuery("SELECT subject_name FROM subjects ORDER BY subject_name");
    while(rs.next()) {
    combo.getItems().add(rs.getString("subject_name"));
    }
    } catch (Exception e) {}
    }*/

/*private void showMySubjectsList() {
Alert alert = new Alert(Alert.AlertType.INFORMATION);
alert.setTitle("My Subjects");
alert.setHeaderText("📚 Your Study Path (Graph Structure)");

VBox content = new VBox(10);
content.setPadding(new Insets(10));

try {
Connection con = DBConnection.getConnection();

// Get subjects with prerequisites
String sql = "SELECT s.subject_name, GROUP_CONCAT(p.subject_name) as prereqs " +
"FROM subjects s LEFT JOIN prerequisites pr ON s.subject_id = pr.subject_id " +
"LEFT JOIN subjects p ON pr.prerequisite_id = p.subject_id " +
"GROUP BY s.subject_id";

ResultSet rs = con.createStatement().executeQuery(sql);

while(rs.next()) {
String subject = rs.getString("subject_name");
String prereqs = rs.getString("prereqs");

HBox row = new HBox(10);
row.setAlignment(Pos.CENTER_LEFT);

Label subjLabel = new Label("📘 " + subject);
subjLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

String prereqText = (prereqs == null || prereqs.isEmpty()) ? " (No prerequisite)" : " → Requires: " + prereqs;
Label prereqLabel = new Label(prereqText);
prereqLabel.setTextFill(Color.web("#7f8c8d"));

row.getChildren().addAll(subjLabel, prereqLabel);
content.getChildren().add(row);
}

if(content.getChildren().isEmpty()) {
content.getChildren().add(new Label("No subjects added yet. Add your first subject above!"));
}

} catch (Exception e) {
content.getChildren().add(new Label("Error loading: " + e.getMessage()));
}

alert.getDialogPane().setContent(content);
alert.showAndWait();
}*/
    
    
    
    
    

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