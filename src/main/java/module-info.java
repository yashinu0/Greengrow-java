module smart_farming {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.web;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires jakarta.mail;
    requires jdk.jsobject;

    opens Controlles to javafx.fxml;
    exports Controlles;
} 