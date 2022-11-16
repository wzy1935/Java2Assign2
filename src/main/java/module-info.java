module com.example.java2_a2 {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.example.java2_a2;
    exports com.example.java2_a2.network;
    exports com.example.java2_a2.client;
    opens com.example.java2_a2 to javafx.fxml;
    opens com.example.java2_a2.network to javafx.fxml;
    opens com.example.java2_a2.client to javafx.fxml;
}