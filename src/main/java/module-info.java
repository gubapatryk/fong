module com.example.ph {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ph to javafx.fxml;
    exports com.example.ph;
}