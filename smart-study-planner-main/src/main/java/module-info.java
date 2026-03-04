module com.studyplanner.smartstudyplannerfx {
    requires javafx.controls;
    requires java.sql;
    // add sql lite
    requires org.xerial.sqlitejdbc;

    exports com.studyplanner.smartstudyplannerfx;
}