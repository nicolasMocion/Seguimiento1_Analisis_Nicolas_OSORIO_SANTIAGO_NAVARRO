package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class HeapSortAlgorithm implements SortingAlgorithm {

    public void sort(List<FinancialAsset> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        int n = data.size();

        // Construir el Max-Heap
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(data, n, i);
        }

        // Extraer elementos del heap uno por uno
        for (int i = n - 1; i > 0; i--) {
            swap(data, 0, i); // Mover la raíz actual al final
            heapify(data, i, 0); // Llamar heapify en el heap reducido
        }
    }

    private void heapify(List<FinancialAsset> data, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && compare(data.get(left), data.get(largest)) > 0) {
            largest = left;
        }
        if (right < n && compare(data.get(right), data.get(largest)) > 0) {
            largest = right;
        }
        if (largest != i) {
            swap(data, i, largest);
            heapify(data, n, largest);
        }
    }

    private int compare(FinancialAsset a, FinancialAsset b) {
        int dateComparison = a.getDate().compareTo(b.getDate());
        if (dateComparison != 0) {
            return dateComparison;
        }
        return a.getClose().compareTo(b.getClose());
    }

    private void swap(List<FinancialAsset> data, int i, int j) {
        FinancialAsset temp = data.get(i);
        data.set(i, data.get(j));
        data.set(j, temp);
    }

    @Override
    public String getName() {
        return "Heap Sort";
    }

}
