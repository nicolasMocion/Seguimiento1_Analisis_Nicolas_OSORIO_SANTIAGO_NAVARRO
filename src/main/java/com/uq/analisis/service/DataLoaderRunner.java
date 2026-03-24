package com.uq.analisis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoaderRunner implements CommandLineRunner {

    private final MarketDataETLService etlService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando proceso automático de ETL (Extracción, Transformación y Carga)...");

        // Mezcla de ETFs globales y posibles tickers locales (ajusta los tickers de la BVC según tu API)
        List<String> targetSymbols = Arrays.asList(
                "IBM"   // S&P 500 ETF

        );

        etlService.syncMarketData(targetSymbols);

        log.info("Proceso ETL finalizado. La base de datos H2 en memoria está lista para las pruebas de algoritmos.");
    }
}