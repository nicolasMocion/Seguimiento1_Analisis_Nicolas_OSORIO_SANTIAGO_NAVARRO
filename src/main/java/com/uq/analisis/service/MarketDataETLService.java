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
        List<FinancialAsset> assetList = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Navegamos al nodo que contiene las series de tiempo
        JsonNode timeSeriesNode = rootNode.get("Time Series (Daily)");

        if (timeSeriesNode == null) {
            throw new RuntimeException("El JSON no contiene el nodo de series de tiempo. Revisa el límite de la API.");
        }

        Iterator<Map.Entry<String, JsonNode>> fields = timeSeriesNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String dateString = field.getKey();
            JsonNode dailyData = field.getValue();

            // Usamos el patrón Builder de Lombok que definimos en la entidad
            FinancialAsset asset = FinancialAsset.builder()
                    .symbol(symbol)
                    .date(LocalDate.parse(dateString))
                    .open(dailyData.get("1. open").asDouble())
                    .high(dailyData.get("2. high").asDouble())
                    .low(dailyData.get("3. low").asDouble())
                    .close(dailyData.get("4. close").asDouble())
                    .volume(dailyData.get("5. volume").asLong())
                    .build();

            assetList.add(asset);
        }

        return assetList;
    }
}