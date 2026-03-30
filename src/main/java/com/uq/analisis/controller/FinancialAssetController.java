package com.uq.analisis.controller;

import com.uq.analisis.model.FinancialAsset;
import com.uq.analisis.Repository.FinancialAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.uq.analisis.dto.BenchmarkResult;
import com.uq.analisis.service.AlgorithmBenchmarkService;
import com.uq.analisis.service.TopVolumeService;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permite que Vue.js (que corre en otro puerto) consuma esta API sin errores de CORS
public class FinancialAssetController {

    private final FinancialAssetRepository repository;
    private final AlgorithmBenchmarkService benchmarkService;
    private final TopVolumeService topVolumeService;

    @GetMapping("/symbols")
    public List<String> getAllSymbols() {
        return repository.findAllDistinctSymbols();
    }

    @GetMapping("/{symbol}")
    public List<FinancialAsset> getAssetsBySymbol(@PathVariable String symbol) {
        // Usamos el método que creaste en el repositorio para traer los datos ordenados por fecha
        return repository.findAllBySymbolOrderByDateAsc(symbol);
    }


    @GetMapping("/{symbol}/benchmark")
    public List<BenchmarkResult> getBenchmarkResults(@PathVariable String symbol) {
        return benchmarkService.runBenchmark(symbol);
    }

    @GetMapping("/{symbol}/top-volume")
    public List<FinancialAsset> getTop15Volume(@PathVariable String symbol) {
        return topVolumeService.getTop15Volume(symbol);
    }



}


