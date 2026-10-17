module com.example.studentattendance {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires org.apache.commons.csv;
    requires java.desktop;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    exports com.example.studentattendance.controller to javafx.fxml;
    opens  com.example.studentattendance.controller;

    exports com.example.studentattendance to javafx.fxml;
    opens  com.example.studentattendance;

    exports com.example.studentattendance.models to javafx.fxml;
    opens  com.example.studentattendance.models;

    opens  com.example.studentattendance.security;
    opens  com.example.studentattendance.storage;

    // Allow ResourceBundle.getBundle to locate messages_*.properties at runtime
    opens  com.example.studentattendance.i18n;
    opens  com.example.studentattendance.images;
}
