package com.uq.analisis.service;

import com.uq.analisis.algorithms.SortingAlgorithm;
import com.uq.analisis.dto.BenchmarkResult;
import com.uq.analisis.model.FinancialAsset;
import com.uq.analisis.Repository.FinancialAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmBenchmarkService {

    private final FinancialAssetRepository repository;
    // ¡Spring Boot inyectará automáticamente QuickSort y cualquier otro algoritmo que crees!
    private final List<SortingAlgorithm> algorithms;

    public List<BenchmarkResult> runBenchmark(String symbol) {
        List<BenchmarkResult> results = new ArrayList<>();

        // 1. Obtenemos los datos originales de la base de datos
        List<FinancialAsset> originalData = repository.findAllBySymbolOrderByDateAsc(symbol);
        int datasetSize = originalData.size();

        if (datasetSize == 0) {
            log.warn("No hay datos para hacer el benchmark del símbolo {}", symbol);
            return results;
        }

        // 2. Hacemos competir a cada algoritmo
        for (SortingAlgorithm algorithm : algorithms) {
            // MUY IMPORTANTE: Crear una copia del dataset para cada algoritmo.
            // Si no lo hacemos, el primer algoritmo ordenará la lista y los demás
            // recibirán una lista ya ordenada, arruinando la medición de tiempos.
            List<FinancialAsset> dataCopy = new ArrayList<>(originalData);

            log.info("Iniciando prueba con el algoritmo: {}", algorithm.getName());

            // 3. Medición precisa de tiempo (Cronómetro en nanosegundos)
            long startTime = System.nanoTime();

            algorithm.sort(dataCopy); // Ejecución del algoritmo

            long endTime = System.nanoTime();

            // 4. Cálculo de resultados
            long durationInNano = (endTime - startTime);
            double durationInMillis = durationInNano / 1_000_000.0;

            log.info("{} terminó en {} ms", algorithm.getName(), durationInMillis);

            // Asignación estática de la complejidad para la Tabla 1 del documento
            String complexity = determineComplexity(algorithm.getName());

            results.add(BenchmarkResult.builder()
                    .algorithmName(algorithm.getName())
                    .datasetSize(datasetSize)
                    .timeInMilliseconds(durationInMillis)
                    .expectedComplexity(complexity)
                    .build());
        }

        return results;
    }

    private String determineComplexity(String algorithmName) {
        return switch (algorithmName) {
            case "QuickSort", "TimSort", "HeapSort" -> "O(n log n)";
            case "Selection Sort", "Gnome Sort", "Bubble Sort" -> "O(n^2)";
            case "RadixSort" -> "O(nk)";
            default -> "Desconocida";
        };
    }
}