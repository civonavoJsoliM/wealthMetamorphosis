module app.wealthmetamorphosis {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.json;
    requires java.sql;


    opens app.wealthmetamorphosis to javafx.fxml;
    opens app.wealthmetamorphosis.logic to javafx.fxml;
    exports app.wealthmetamorphosis;
    exports app.wealthmetamorphosis.data to com.fasterxml.jackson.databind;
    exports app.wealthmetamorphosis.logic to javafx.fxml;
}