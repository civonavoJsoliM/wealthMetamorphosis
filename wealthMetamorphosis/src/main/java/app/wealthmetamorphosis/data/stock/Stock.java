package app.wealthmetamorphosis.data.stock;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.scene.control.Button;

import java.util.List;

@JsonIgnoreProperties({"button"})
public class Stock {
    private String symbol;
    private Meta meta;
    private List<Value> values;
    private String status;
    private Button button;

    public Stock() {
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Button getButton() {
        return button;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setButton(Button button) {
        this.button = button;
    }
}
