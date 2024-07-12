package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.Main;
import app.wealthmetamorphosis.logic.file.FileReader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

public class HttpService {
    private final FileReader fileReader;
    private int counter;

    public HttpService(FileReader fileReader, int counter) {
        this.fileReader = fileReader;
        this.counter = counter;
    }

    public HttpResponse<String> getRealTimeStockPrice(String symbol) throws IOException, InterruptedException {
        counter++;
        List<String> apiKeys = getApiKeys();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://twelve-data1.p.rapidapi.com/price?format=json&outputsize=1&symbol=" + symbol))
                .header("X-RapidAPI-Key", apiKeys.get(counter % apiKeys.size()))
                .header("X-RapidAPI-Host", "twelve-data1.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> getStock(String symbol, String interval, String outputSize) throws IOException, InterruptedException {
        counter++;
        List<String> apiKeys = getApiKeys();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://twelve-data1.p.rapidapi.com/time_series?outputsize=" + outputSize + "&symbol=" + symbol + "&interval=" + interval + "&format=json"))
                .header("X-RapidAPI-Key", apiKeys.get(counter % apiKeys.size()))
                .header("X-RapidAPI-Host", "twelve-data1.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<String> getApiKeys() {
        URI path;
        try {
            path = Objects.requireNonNull(Main.class.getResource("/app/wealthMetamorphosis/files/ApiKeys.txt")).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return fileReader.readFromFile(path);
    }
}