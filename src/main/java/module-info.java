module io.github.mfirash {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;   
    requires json.simple;

    opens io.github.mfirash to javafx.fxml;
    exports io.github.mfirash;
}
