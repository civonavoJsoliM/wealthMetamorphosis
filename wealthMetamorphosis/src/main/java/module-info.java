module app.wealthmetamorphosis {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;


    opens app.wealthmetamorphosis to javafx.fxml;
    exports app.wealthmetamorphosis;
    exports app.wealthmetamorphosis.data to com.fasterxml.jackson.databind;
}