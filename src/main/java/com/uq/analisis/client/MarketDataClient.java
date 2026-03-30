package com.uq.analisis.client;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class MarketDataClient {

    private static final String BASE_URL = "https://query2.finance.yahoo.com/v8/finance/chart/";
    private final HttpClient httpClient;

    public MarketDataClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String fetchHistoricalData(String symbol) throws Exception {
        // IMPORTANTE: Codificamos el símbolo para que ^N225 se convierta en %5EN225
        String encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8);

        String url = String.format("%s%s?range=5y&interval=1d", BASE_URL, encodedSymbol);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
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