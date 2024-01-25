module com.example.transferablefool {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.transferablefool to javafx.fxml;
    exports com.example.transferablefool;
}