package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SelectionSortAlgorithm implements SortingAlgorithm {

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int n = data.size();

        // Mueve el límite del arreglo no ordenado uno por uno
        for (int i = 0; i < n - 1; i++) {
            // Encuentra el elemento mínimo en el arreglo no ordenado
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (compare(data.get(j), data.get(minIdx)) < 0) {
                    minIdx = j;
                }
            }

            // Intercambia el elemento mínimo encontrado con el primer elemento de la sección
            if (minIdx != i) {
                swap(data, i, minIdx);
            }
        }
    }

    @Override
    public String getName() {
        return "Selection Sort";
    }

    private int compare(FinancialAsset a, FinancialAsset b) {
        int dateComparison = a.getDate().compareTo(b.getDate());
        if (dateComparison != 0) {
            return dateComparison;
        }
        // Desempate por precio de cierre
        return a.getClose().compareTo(b.getClose());
    }

    private void swap(List<FinancialAsset> data, int i, int j) {
        FinancialAsset temp = data.get(i);
        data.set(i, data.get(j));
        data.set(j, temp);
    }
}