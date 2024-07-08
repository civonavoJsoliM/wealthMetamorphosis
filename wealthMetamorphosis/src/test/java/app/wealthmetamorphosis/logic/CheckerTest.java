package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.logic.service.OwnedStockService;
import app.wealthmetamorphosis.logic.verifier.Checker;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CheckerTest {
    static private User user;
    static private User user1;
    static private User user2;
    static private User user3;
    static private User user4;
    static private User user5;
    static private List<User> users;
    static private List<User> users1;
    static private OwnedStockService ownedStockService;

    @BeforeAll
    static void initialize() {
        ownedStockService = new OwnedStockService();

        String hashedPassword = DigestUtils.sha256Hex("12345");
        user = new User("", "Max", hashedPassword, LocalDateTime.now(), 1000);
        user.setOrders(List.of(
                new Order("", "AAPL", 1, 10, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AAPL", 1, 10, OrderType.SELL, LocalDateTime.now(), "")
        ));

        String hashedPassword1 = DigestUtils.sha256Hex("54321");
        user1 = new User("", "Florian", hashedPassword1, LocalDateTime.now(), 100);
        user1.setOrders(List.of(
                new Order("", "GME", 1, 6, OrderType.BUY, LocalDateTime.now(), "")
        ));

        String hashedPassword2 = DigestUtils.sha256Hex("password");
        user2 = new User("", "Andrew", hashedPassword2, LocalDateTime.now(), 10000);
        user2.setOrders(List.of());

        String hashedPassword3 = DigestUtils.sha256Hex("catsAndDogs");
        user3 = new User("", "Fritz", hashedPassword3, LocalDateTime.now(), 500);
        user3.setOrders(List.of(
                new Order("", "TSLA", 1, 10, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "TSLA", 1, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "TSLA", 1, 5, OrderType.SELL, LocalDateTime.now(), "")
        ));

        String hashedPassword4 = DigestUtils.sha256Hex("table123");
        user4 = new User("", "Sheldon", hashedPassword4, LocalDateTime.now(), 700);
        user4.setOrders(List.of(
                new Order("", "NVDA", 1, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "NVDA", 1, 5, OrderType.BUY, LocalDateTime.now(), "")
        ));

        String hashedPassword5 = DigestUtils.sha256Hex("St0ckM@rk€t!");
        user5 = new User("", "John", hashedPassword5, LocalDateTime.now(), 200);
        user5.setOrders(List.of(
                new Order("", "SBUX", 1, 10, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "SBUX", 1, 4, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "SBUX", 1, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "SBUX", 1, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "NKE", 1, 5, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "NKE", 1, 4, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "NKE", 1, 1, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "MSFT", 1, 7, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "MSFT", 1, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "MSFT", 1, 3, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "AMD", 1, 15, OrderType.BUY, LocalDateTime.now(), ""),
                new Order("", "AMD", 1, 5, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "AMD", 1, 5, OrderType.SELL, LocalDateTime.now(), ""),
                new Order("", "AMD", 1, 5, OrderType.SELL, LocalDateTime.now(), "")
        ));

        users = new ArrayList<>(List.of(user, user1, user2, user3, user4, user5));
        users1 = new ArrayList<>();
    }

    @ParameterizedTest
    @MethodSource
    void areEnoughStockSharesToBeSold(User user, String stockSymbol, double input, boolean expected) {
        Checker checker = new Checker(ownedStockService);

        boolean isEnough = checker.areEnoughStockSharesToBeSold(user, stockSymbol, input);

        assertEquals(expected, isEnough);
    }

    static Stream<Arguments> areEnoughStockSharesToBeSold() {
        return Stream.of(
                Arguments.of(user, "AAPL", 1.0, false),
                Arguments.of(user, "", 0.1, false),
                Arguments.of(user1, "AAPL", 1.0, false),
                Arguments.of(user1, "GME", 5.9, true),
                Arguments.of(user1, "GME", 6.1, false),
                Arguments.of(user2, "", 1.0, false),
                Arguments.of(user2, "MSFT", 10.0, false),
                Arguments.of(user3, "TSLA", 3.0, false),
                Arguments.of(user3, "TSLa", 2.0, false),
                Arguments.of(user4, "NVDA", 10.1, false),
                Arguments.of(user5, "SBUX", 1.0, false),
                Arguments.of(user5, "NKE", 1.0, false),
                Arguments.of(user5, "MSFT", 1.0, true),
                Arguments.of(user5, "MFST", 1.1, false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void doesUserExistInDB(List<User> users, String username, boolean expected) {
        Checker checker = new Checker(ownedStockService);

        boolean doesExist = checker.doesUserExistInDB(users, username);

        assertEquals(expected, doesExist);
    }

    static Stream<Arguments> doesUserExistInDB() {
        return Stream.of(
                Arguments.of(users, "", false),
                Arguments.of(users, "max", false),
                Arguments.of(users, "Max", true),
                Arguments.of(users, "Johnn", false),
                Arguments.of(users, "Martin", false),
                Arguments.of(users1, "Max", false),
                Arguments.of(users1, "", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void isPasswordCorrect(List<User> users, String username, String hashedPassword, boolean expected) {
        Checker checker = new Checker(ownedStockService);

        boolean isCorrect = checker.isPasswordCorrect(users, username, hashedPassword);

        assertEquals(expected, isCorrect);
    }

    static Stream<Arguments> isPasswordCorrect() {
        return Stream.of(
                Arguments.of(users, "", "", false),
                Arguments.of(users, "Fritz", DigestUtils.sha256Hex("catsAndDogs"), true),
                Arguments.of(users, "max", DigestUtils.sha256Hex("12345"), false),
                Arguments.of(users, "Max", DigestUtils.sha256Hex("123456"), false),
                Arguments.of(users, "Andrew", "password", false),
                Arguments.of(users, "Andrew", DigestUtils.sha256Hex("Password"), false),
                Arguments.of(users1, "John", DigestUtils.sha256Hex("St0ckM@rk€t!"), false),
                Arguments.of(users1, "", "", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void doPasswordsMatch(String password, String confirmPassword, boolean expected) {
        Checker checker = new Checker(ownedStockService);

        boolean doMatch = checker.doPasswordsMatch(password, confirmPassword);

        assertEquals(expected, doMatch);
    }

    static Stream<Arguments> doPasswordsMatch() {
        return Stream.of(
                Arguments.of("123", "124", false),
                Arguments.of("", "123", false),
                Arguments.of("123", "", false),
                Arguments.of("123", "123 ", false),
                Arguments.of(" 123", "  123", false)
        );
    }
}