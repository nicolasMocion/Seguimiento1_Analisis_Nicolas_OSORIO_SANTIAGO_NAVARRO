package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import java.util.List;

public interface SortingAlgorithm {
    void sort(List<FinancialAsset> data);
    String getName();
}