package app.wealthmetamorphosis.logic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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
                .uri(URI.create("https://twelve-data1.p.rapidapi.com/price?symbol=" + symbol + "&format=json&outputsize=1"))
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
                .uri(URI.create("https://twelve-data1.p.rapidapi.com/time_series?symbol=" + symbol + "&interval=" + interval + "&outputsize=" + outputSize + "&format=json"))
                .header("X-RapidAPI-Key", apiKeys.get(counter % apiKeys.size()))
                .header("X-RapidAPI-Host", "twelve-data1.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<String> getApiKeys() {
        return fileReader.readFromFile("/Users/ipoce/Desktop/wealthMetamorphosis/ApiKeys.txt");
    }
}