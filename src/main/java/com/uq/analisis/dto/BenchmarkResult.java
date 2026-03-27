package com.uq.analisis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenchmarkResult {
    private String algorithmName;
    private int datasetSize;
    private double timeInMilliseconds;
    private String expectedComplexity;
}