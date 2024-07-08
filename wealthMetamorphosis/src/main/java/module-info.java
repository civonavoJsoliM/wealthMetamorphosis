module app.wealthmetamorphosis {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.json;
    requires java.sql;
    requires org.apache.commons.codec;


    opens app.wealthmetamorphosis to javafx.fxml;
    exports app.wealthmetamorphosis;
    exports app.wealthmetamorphosis.data to com.fasterxml.jackson.databind;
    exports app.wealthmetamorphosis.data.singleton to com.fasterxml.jackson.databind;
    exports app.wealthmetamorphosis.data.stock to com.fasterxml.jackson.databind;
    exports app.wealthmetamorphosis.logic.refresher to com.fasterxml.jackson.databind;
    exports app.wealthmetamorphosis.logic.service to javafx.fxml;
    opens app.wealthmetamorphosis.logic.service to javafx.fxml;
    exports app.wealthmetamorphosis.logic.controller;
    opens app.wealthmetamorphosis.logic.controller to javafx.fxml;
    exports app.wealthmetamorphosis.logic.message to javafx.fxml;
    opens app.wealthmetamorphosis.logic.message to javafx.fxml;
    exports app.wealthmetamorphosis.logic.message.creatingAccount to javafx.fxml;
    opens app.wealthmetamorphosis.logic.message.creatingAccount to javafx.fxml;
    exports app.wealthmetamorphosis.logic.orderToPlace to javafx.fxml;
    opens app.wealthmetamorphosis.logic.orderToPlace to javafx.fxml;
    exports app.wealthmetamorphosis.logic.verifier to javafx.fxml;
    opens app.wealthmetamorphosis.logic.verifier to javafx.fxml;
    exports app.wealthmetamorphosis.logic.file to javafx.fxml;
    opens app.wealthmetamorphosis.logic.file to javafx.fxml;
    exports app.wealthmetamorphosis.logic.calculator to javafx.fxml;
    opens app.wealthmetamorphosis.logic.calculator to javafx.fxml;
    exports app.wealthmetamorphosis.data.parameters to com.fasterxml.jackson.databind;
}