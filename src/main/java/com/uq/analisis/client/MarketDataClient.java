package com.uq.analisis.client;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class MarketDataClient {

    // URL pública de Yahoo Finance v8 (No requiere API Key)
    private static final String BASE_URL = "https://query2.finance.yahoo.com/v8/finance/chart/";
    private final HttpClient httpClient;

    public MarketDataClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String fetchHistoricalData(String symbol) throws Exception {
        // range=5y trae los 5 años exactos, interval=1d trae datos diarios
        String url = String.format("%s%s?range=5y&interval=1d", BASE_URL, symbol);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                // IMPORTANTE: Yahoo Finance requiere un User-Agent para no rechazar la conexión (Error 403)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al conectar con Yahoo Finance para " + symbol + ". Código: " + response.statusCode());
        }

        return response.body();
    }
}