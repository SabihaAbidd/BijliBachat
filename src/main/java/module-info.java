module com.example.bijlibachat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;
    requires java.mail;
    requires org.apache.pdfbox;
    requires jbcrypt;

    opens com.example.bijlibachat to javafx.fxml;
    exports com.example.bijlibachat;

    opens com.example.bijlibachat.model to javafx.base;
    exports com.example.bijlibachat.model;
    exports com.example.bijlibachat.dao;
    exports com.example.bijlibachat.service;
}
