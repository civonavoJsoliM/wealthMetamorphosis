package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.logic.file.FileReader;

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

    /*
     * API keys are no longer stored in this repository.
     *
     * Previously the keys were read from the bundled resource
     * /app/wealthMetamorphosis/files/ApiKeys.txt. That file has been removed
     * for security reasons, so the original code is commented out below:
     *
     * private List<String> getApiKeys() {
     *     URI path;
     *     try {
     *         path = Objects.requireNonNull(Main.class.getResource("/app/wealthMetamorphosis/files/ApiKeys.txt")).toURI();
     *     } catch (URISyntaxException e) {
     *         throw new RuntimeException(e);
     *     }
     *     return fileReader.readFromFile(path);
     * }
     *
     * To run the app, get your own Twelve Data key on RapidAPI and set it as an
     * environment variable (several keys may be comma-separated, they are rotated):
     *
     *     export RAPIDAPI_KEYS="your-key-1,your-key-2"
     */
    private List<String> getApiKeys() {
        String keys = System.getenv("RAPIDAPI_KEYS");
        if (keys == null || keys.isBlank()) {
            throw new IllegalStateException("No API key configured. Set the environment variable RAPIDAPI_KEYS (see comment above).");
        }
        return List.of(keys.split("\\s*,\\s*"));
    }
}
