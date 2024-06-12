package app.wealthmetamorphosis.data.stock;

public class Meta {
    private String symbol;
    private String interval;
    private String currency;
    private String exchange_timezone;
    private String exchange;
    private String mic_code;
    private String type;

    public Meta() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchange_timezone() {
        return exchange_timezone;
    }

    public void setExchange_timezone(String exchange_timezone) {
        this.exchange_timezone = exchange_timezone;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getMic_code() {
        return mic_code;
    }

    public void setMic_code(String mic_code) {
        this.mic_code = mic_code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
