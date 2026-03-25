package com.uq.analisis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uq.analisis.Repository.FinancialAssetRepository;
import com.uq.analisis.client.MarketDataClient;
import com.uq.analisis.model.FinancialAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j // Lombok: Para usar el logger automáticamente (log.info, log.error)
@Service
@RequiredArgsConstructor // Lombok: Inyecta automáticamente los 'final'
public class MarketDataETLService {

    private final MarketDataClient marketDataClient;
    private final FinancialAssetRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Método principal que orquesta la extracción y carga.
     * @Transactional asegura que si algo falla, no se guarden datos a medias.
     */
    @Transactional
    public void syncMarketData(List<String> symbols) {
        for (String symbol : symbols) {
            if (repository.existsBySymbol(symbol)) {
                log.info("Los datos para {} ya existen en la BD. Saltando extracción.", symbol);
                continue;
            }

            try {
                log.info("Extrayendo datos históricos para: {}", symbol);
                String jsonResponse = marketDataClient.fetchHistoricalData(symbol);
                log.info("Respuesta cruda de la API para {}: {}", symbol, jsonResponse);
                List<FinancialAsset> assets = parseJsonResponse(jsonResponse, symbol);

                // Carga masiva a la base de datos
                repository.saveAll(assets);
                log.info("Se guardaron {} registros para {}", assets.size(), symbol);

                // IMPORTANTE: Pausa para no saturar APIs públicas (Rate Limiting)
                Thread.sleep(2000);

            } catch (Exception e) {
                log.error("Error procesando el símbolo {}: {}", symbol, e.getMessage());
            }
        }
    }

    /**
     * Parseo manual del JSON.
     * Nota: Esta estructura asume el formato típico de Alpha Vantage (Time Series (Daily)).
     * Deberás ajustar las claves ("Time Series (Daily)", "1. open", etc.) según la API exacta que elijas.
     */

    private List<FinancialAsset> parseJsonResponse(String jsonResponse, String symbol) throws Exception {
        // Usamos un Map (Fecha -> Activo) para evitar fechas duplicadas en la respuesta de Yahoo
        Map<LocalDate, FinancialAsset> uniqueAssetsMap = new java.util.HashMap<>();

        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        JsonNode resultNode = rootNode.path("chart").path("result").get(0);
        if (resultNode == null || resultNode.isMissingNode()) {
            throw new RuntimeException("Estructura JSON inválida desde Yahoo Finance para " + symbol);
        }

        JsonNode timestamps = resultNode.path("timestamp");
        JsonNode quote = resultNode.path("indicators").path("quote").get(0);

        JsonNode opens = quote.path("open");
        JsonNode closes = quote.path("close");
        JsonNode highs = quote.path("high");
        JsonNode lows = quote.path("low");
        JsonNode volumes = quote.path("volume");

        if (timestamps == null || timestamps.isMissingNode()) {
            return new ArrayList<>(); // Retorna lista vacía si no hay datos
        }

        for (int i = 0; i < timestamps.size(); i++) {
            if (opens.get(i).isNull() || closes.get(i).isNull()) {
                continue;
            }

            long unixTime = timestamps.get(i).asLong();
            LocalDate date = java.time.Instant.ofEpochSecond(unixTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

            FinancialAsset asset = FinancialAsset.builder()
                    .symbol(symbol)
                    .date(date)
                    .open(opens.get(i).asDouble())
                    .high(highs.get(i).asDouble())
                    .low(lows.get(i).asDouble())
                    .close(closes.get(i).asDouble())
                    .volume(volumes.get(i).asLong())
                    .build();

            // Si Yahoo envía dos registros para el mismo día, esto sobrescribe el viejo con el nuevo,
            // garantizando que a la base de datos solo llegue una copia por fecha.
            uniqueAssetsMap.put(date, asset);
        }

        // Convertimos el Map de vuelta a una Lista
        return new ArrayList<>(uniqueAssetsMap.values());
    }
}