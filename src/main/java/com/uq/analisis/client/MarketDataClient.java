package com.uq.analisis.client;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class MarketDataClient {

    // Nota: Regístrate en alphavantage.co para obtener una API Key gratuita.
    // Usamos "demo" solo como marcador, pero tiene límites muy estrictos.
    private static final String API_KEY = "DW4O4124MHWE4JWH";
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    private final HttpClient httpClient;

    public MarketDataClient() {
        // Configuramos un cliente HTTP estándar y eficiente
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Realiza la petición HTTP GET a la API financiera para obtener el histórico.
     * * @param symbol El símbolo del activo (ej. "SPY", "AAPL")
     * @return El JSON crudo con la respuesta de la API
     * @throws Exception Si hay problemas de red o la API falla
     */
    public String fetchHistoricalData(String symbol) throws Exception {

        // Construcción de la URL. "outputsize=full" nos garantiza traer los 5 años requeridos.
        String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&outputsize=full&apikey=%s",
                BASE_URL, symbol, API_KEY);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .build();

        // Petición explícita al recurso web (Cumple con el requerimiento de extracción manual)
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al conectar con la API financiera para " + symbol +
                    ". Código HTTP: " + response.statusCode());
        }

        return response.body();
    }
}