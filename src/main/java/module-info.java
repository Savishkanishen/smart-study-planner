module com.studyplanner.smartstudyplannerfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires com.google.gson;
    requires java.net.http;
   
    requires org.xerial.sqlitejdbc; 
   

    opens com.studyplanner.smartstudyplannerfx to javafx.fxml;
    exports com.studyplanner.smartstudyplannerfx;
}
