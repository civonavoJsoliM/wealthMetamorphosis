package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OwnedStockServiceTest {
    private static User user;
    private static User user1;
    private static User user2;
    private static User user3;
    private static User user4;
    private static User user5;
    private static User user6;
    private static User user7;
    private static User user8;
    private static User user9;
    private static User user10;

    @BeforeAll
    static void initialize() {
        user = new User();
        user.setOrders(List.of(
                new Order("", "AAPL", 100, 10, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AAPL", 200, 10, OrderType.SELL, LocalDateTime.now(), "")
        ));

        user1 = new User();
        user1.setOrders(List.of(
                new Order("", "GME", 1000, 6, OrderType.BUY, LocalDateTime.now(), "")
        ));

        user2 = new User();
        user2.setOrders(List.of());

        user3 = new User();
        user3.setOrders(List.of(
                new Order("", "TSLA", 200, 10, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 100, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "TSLA", 100, 5, OrderType.SELL, LocalDateTime.now(), "")
        ));

        user4 = new User();
        user4.setOrders(List.of(
                new Order("", "NVDA", 120, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "NVDA", 150, 5, OrderType.BUY, LocalDateTime.now(), "")
        ));

        user5 = new User();
        user5.setOrders(List.of(
                new Order("", "SBUX", 50, 10, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "SBUX", 20, 4, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "SBUX", 10, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "SBUX", 15, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "NKE", 10, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "NKE", 2, 4, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "NKE", 3, 1, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "MSFT", 500, 7, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "MSFT", 100, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "MSFT", 200, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "AMD", 100, 15, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AMD", 50, 5, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "AMD", 40, 5, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "AMD", 70, 5, OrderType.SELL, LocalDateTime.now(), "")
        ));

        user6 = new User();
        user6.setOrders(List.of(
                new Order("", "AAPL", 100, 2, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AAPL", 200, 2, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AAPL", 300, 2, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AAPL", 400, 2, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AAPL", 300, 4, OrderType.SELL, LocalDateTime.now(), "")
        ));

        user7 = new User();
        user7.setOrders(List.of(
                new Order("", "MSFT", 100, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "MSFT", 200, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "MSFT", 300, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "MSFT", 400, 5, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "MSFT", 500, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "MSFT", 600, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "MSFT", 700, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "MSFT", 800, 5, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "MSFT", 900, 5, OrderType.BUY, LocalDateTime.now(), "")
        ));

        user8 = new User();
        user8.setOrders(List.of(
                new Order("", "NKE", 100, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "NKE", 110, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "NKE", 150, 2, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "NKE", 170, 2, OrderType.SELL, LocalDateTime.now(), "")
        ));

        user9 = new User();
        user9.setOrders(List.of(
                new Order("", "TSLA", 100, 2, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 100, 2, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 400, 4, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "TSLA", 200, 3, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 200, 3, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 300, 6, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "TSLA", 500, 3, OrderType.BUY, LocalDateTime.now(), "")
        ));

        user10 = new User();
        user10.setOrders(List.of(
                new Order("", "AAPL", 100, 2, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 50, 10, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "NKE", 20, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "GOOG", 220, 3, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 30, 10, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "GME", 150, 1, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AMD", 30, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AAPL", 150, 3, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AAPL", 120, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "MSFT", 200, 2, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 40, 10, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 20, 10, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "AMZN", 170, 3, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AAPL", 180, 1, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "GOOG", 250, 3, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "NKE", 50, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "GME", 100, 1, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "AAPL", 120, 3, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 50, 10, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "GOOG", 270, 2, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "GOOG", 275, 2, OrderType.SELL, LocalDateTime.now(), "")
        ));
    }

    @ParameterizedTest
    @MethodSource
    void getAllOwnedStockShares(User user, Map<String, Double> expected) {
        OwnedStockService ownedStockService = new OwnedStockService();

        Map<String, Double> ownedStocks = ownedStockService.getAllOwnedStockShares(user);

        assertEquals(ownedStocks, (expected));
    }

    static Stream<Arguments> getAllOwnedStockShares() {
        return Stream.of(
                Arguments.of(user, Map.of()),
                Arguments.of(user1, Map.of("GME", 6.0)),
                Arguments.of(user2, Map.of()),
                Arguments.of(user3, Map.of("TSLA", 2.0)),
                Arguments.of(user4, Map.of("NVDA", 10.0)),
                Arguments.of(user5, Map.of("MSFT", 1.0))
        );
    }

    @ParameterizedTest
    @MethodSource
    void getSharesFromCertainStock(User user, String stock, double expected) {
        OwnedStockService ownedStockService = new OwnedStockService();

        double shares = ownedStockService.getSharesFromCertainStock(user, stock);

        assertEquals(expected, shares);
    }

    static Stream<Arguments> getSharesFromCertainStock() {
        return Stream.of(
                Arguments.of(user, "AAPL", 0.0),
                Arguments.of(user, "MSFT", 0.0),
                Arguments.of(user, "", 0.0),
                Arguments.of(user2, "GME", 0.0),
                Arguments.of(user2, "", 0.0),
                Arguments.of(user3, "TSLA", 2.0),
                Arguments.of(user3, "TSLa", 0.0),
                Arguments.of(user4, "NVDA", 10.0),
                Arguments.of(user5, "SBUX", 0.0),
                Arguments.of(user5, "NKE", 0.0),
                Arguments.of(user5, "MSFT", 1.0),
                Arguments.of(user5, "AMD", 0.0)
        );
    }

    @ParameterizedTest
    @MethodSource
    void getInvestedInStock(User user, String stockSymbol, double expected) {
        OwnedStockService ownedStockService = new OwnedStockService();

        double investedInStock = ownedStockService.getInvestedInStock(user, stockSymbol);

        assertEquals(expected, investedInStock);
    }
    static Stream<Arguments> getInvestedInStock() {
        return Stream.of(
                Arguments.of(user, "GOOG", 0.0),
                Arguments.of(user, "AAPL", 0.0),
                Arguments.of(user1, "GME", 6000.0),
                Arguments.of(user2, "GME", 0.0),
                Arguments.of(user3, "TSLA", 1489.655172413793),
                Arguments.of(user3, "TSLa", 0.0),
                Arguments.of(user5, "", 0.0),
                Arguments.of(user5, "SBUX", 0.0),
                Arguments.of(user5, "MSFT", 2992.4242424242425),
                Arguments.of(user6, "AAPL", 1000.0),
                Arguments.of(user7, "MSFT", 13300.0),
                Arguments.of(user8, "NKE", 630.0),
                Arguments.of(user9, "TSLA", 1500.0),
                Arguments.of(user9, "NKE", 0.0),
                Arguments.of(user10, "AAPL", 1485.0),
                Arguments.of(user10, "GOOG", 470.0),
                Arguments.of(user10, "TSLA", 500.0)
        );
    }

    @ParameterizedTest
    @MethodSource
    void getPercentage(double investedInStock, double stockPrice, double ownedShares, double expected) {
        OwnedStockService ownedStockService = new OwnedStockService();

        double percentage = ownedStockService.getPercentage(investedInStock, stockPrice, ownedShares);

        assertEquals(expected, percentage);
    }

    static Stream<Arguments> getPercentage() {
        return Stream.of(
                Arguments.of(1000, 200, 10, 2),
                Arguments.of(3519, 17, 30, 1.855072463768116),
                Arguments.of(1234.78, 77.63, 17, 1.068781483341162)
        );
    }
}