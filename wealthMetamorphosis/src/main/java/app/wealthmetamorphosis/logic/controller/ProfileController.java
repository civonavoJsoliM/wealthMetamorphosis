package app.wealthmetamorphosis.logic.controller;

import app.wealthmetamorphosis.data.parameters.OrderAvailablePaneChangerParameters;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.file.FileReader;
import app.wealthmetamorphosis.logic.paneChanger.NoOrdersPaneChanger;
import app.wealthmetamorphosis.logic.paneChanger.NoOwnedStocksPaneChanger;
import app.wealthmetamorphosis.logic.paneChanger.OrdersAndStocksAvailablePaneChanger;
import app.wealthmetamorphosis.logic.paneChanger.PaneChanger;
import app.wealthmetamorphosis.logic.service.HttpService;
import app.wealthmetamorphosis.logic.service.OwnedStockService;
import app.wealthmetamorphosis.logic.service.ProfileControllerService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

public class ProfileController {

    @FXML
    private VBox stocksVBox;
    @FXML
    private Label registeredLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private VBox ordersVBox;
    @FXML
    private PieChart pieChart;
    @FXML
    private Circle profilePicture;
    @FXML
    private ScrollPane stocksScrollPane;
    @FXML
    private ScrollPane ordersScrollPane;
    @FXML
    private Label availableFundsLabel;
    @FXML
    private Label portfolioWorthLabel;
    @FXML
    private VBox progressVBox;
    @FXML
    private HBox myStocksHBox;
    @FXML
    private VBox noStocksToDisplayVBox;
    @FXML
    private VBox noOrdersToDisplayVBox;

    private Stage profileStage;
    private List<String> colors;
    private List<ScheduledExecutorService> scheduledExecutorServices;
    private ProfileControllerService profileControllerService;

    @FXML
    void initialize() throws IOException {
        OwnedStockService ownedStockService = new OwnedStockService();
        FileReader fileReader = new FileReader();
        int counter = 0;
        HttpService service = new HttpService(fileReader, counter);
        scheduledExecutorServices = new ArrayList<>();
        colors = Files.readAllLines(Path.of("/Users/ipoce/Desktop/wealthMetamorphosis/Colors.txt"));
        profileControllerService = new ProfileControllerService(ownedStockService, service, portfolioWorthLabel,
                myStocksHBox, scheduledExecutorServices);

        setUpUserInfo();

        changePane();

        setProfileStage();
    }

    private void changePane() {
        PaneChanger noOrdersPaneChanger = new NoOrdersPaneChanger(noStocksToDisplayVBox, noOrdersToDisplayVBox, portfolioWorthLabel);
        PaneChanger noOwnedStocksPaneChanger = new NoOwnedStocksPaneChanger(noStocksToDisplayVBox, portfolioWorthLabel,
                ordersScrollPane, ordersVBox, profileControllerService);
        OrderAvailablePaneChangerParameters orderAvailablePaneChangerParameters = new OrderAvailablePaneChangerParameters(progressVBox,
                ordersScrollPane, profileControllerService, pieChart, colors, stocksVBox, ordersVBox);
        PaneChanger ordersAndStocksAvailablePaneChanger =
                new OrdersAndStocksAvailablePaneChanger(orderAvailablePaneChangerParameters);
        List<PaneChanger> paneChangers = new ArrayList<>(List.of(noOrdersPaneChanger, noOwnedStocksPaneChanger, ordersAndStocksAvailablePaneChanger));

        for (PaneChanger paneChanger : paneChangers) {
            paneChanger.change();
        }
    }

    private void setUpUserInfo() {
        setProfilePicture();
        usernameLabel.setText(UserSingleton.getInstance().getUsername());
        registeredLabel.setText(UserSingleton.getInstance().getRegistered().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        double availableFunds = Math.round(UserSingleton.getInstance().getBalance() * 100.0) / 100.0;
        availableFundsLabel.setText(availableFunds + "$");
    }

    private void setProfileStage() {
        profileStage = new Stage();
        profileStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                scheduledExecutorServices.forEach(ScheduledExecutorService::shutdownNow);
            }
        });
        profileStage.setResizable(false);
    }

    private void setProfilePicture() {
        Image image = new Image(String.valueOf(Objects.requireNonNull(
                getClass().getResource("/app/wealthmetamorphosis/image/profile-picture.jpg"))));
        ImagePattern pattern = new ImagePattern(image);
        profilePicture.setFill(pattern);
    }

    @FXML
    void onMouseEnteredStocksScrollPane() {
        stocksScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        stocksScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    @FXML
    void onMouseExitedStocksScrollPane() {
        stocksScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        stocksScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    @FXML
    void onMouseEnteredOrdersScrollPane() {
        ordersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        ordersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    @FXML
    void onMouseExitedOrdersScrollPane() {
        ordersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ordersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    @FXML
    void onLogOutClicked() {
        UserSingleton.setUser(null);
        profileStage.close();
    }

    public Stage getProfileStage() {
        return profileStage;
    }
}