package app.wealthmetamorphosis.logic.calculator;

import app.wealthmetamorphosis.Main;
import app.wealthmetamorphosis.logic.file.FileReader;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DaysCalculator {
    private final FileReader fileReader;

    public DaysCalculator(FileReader fileReader) {
        this.fileReader = fileReader;
    }

    public int getNumberOfTradingDaysFromBeginOfYearTillNow() {
        LocalDate beginOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        List<LocalDate> daysFromBeginOfYearTillNow = beginOfYear.datesUntil(LocalDate.now().plusDays(1)).toList();
        return (int) daysFromBeginOfYearTillNow.stream().filter(date -> !isDayHoliday(date) && !isDayWeekend(date)).count();
    }

    private boolean isDayHoliday(LocalDate date) {
        URI path;
        try {
            path = Objects.requireNonNull(Main.class.getResource("/app/wealthMetamorphosis/files/StockMarketHolidays.txt")).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        List<String> stockMarketHolidays = fileReader.readFromFile(path);
        List<LocalDate> holidays = new ArrayList<>();
        for (String stockMarketHoliday : stockMarketHolidays) {
            LocalDate holiday = LocalDate.parse(stockMarketHoliday);
            holidays.add(holiday);
        }
        return holidays.stream().anyMatch(day -> day.isEqual(date));
    }

    private boolean isDayWeekend(LocalDate date) {
        return date.getDayOfWeek().name().equalsIgnoreCase("Saturday") ||
                date.getDayOfWeek().name().equalsIgnoreCase("Sunday");
    }
}
