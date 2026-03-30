package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class BitonicSortAlgorithm implements SortingAlgorithm {

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.isEmpty()) return;

        int originalSize = data.size();
        int nextPowerOfTwo = nextPowerOfTwo(originalSize);

        // Padding: Rellenar con valores máximos si no es potencia de 2
        for (int i = originalSize; i < nextPowerOfTwo; i++) {
            data.add(FinancialAsset.builder()
                    .date(LocalDate.MAX)
                    .close(Double.MAX_VALUE)
                    .build());
        }

        // Ejecutar el ordenamiento bitónico
        bitonicSort(data, 0, nextPowerOfTwo, 1);

        // Limpieza: Remover el padding del final de la lista
        data.subList(originalSize, nextPowerOfTwo).clear();
    }

    @Override
    public String getName() {
        return "Bitonic Sort";
    }

    private void bitonicSort(List<FinancialAsset> data, int low, int cnt, int dir) {
        if (cnt > 1) {
            int k = cnt / 2;
            bitonicSort(data, low, k, 1);       // Ordenar primera mitad en orden ascendente
            bitonicSort(data, low + k, k, 0);   // Ordenar segunda mitad en orden descendente
            bitonicMerge(data, low, cnt, dir);  // Fusionar toda la secuencia
        }
    }

    private void bitonicMerge(List<FinancialAsset> data, int low, int cnt, int dir) {
        if (cnt > 1) {
            int k = cnt / 2;
            for (int i = low; i < low + k; i++) {
                compAndSwap(data, i, i + k, dir);
            }
            bitonicMerge(data, low, k, dir);
            bitonicMerge(data, low + k, k, dir);
        }
    }

    private void compAndSwap(List<FinancialAsset> data, int i, int j, int dir) {
        int comp = compare(data.get(i), data.get(j));
        // dir == 1 significa ascendente, dir == 0 descendente
        if ((comp > 0 && dir == 1) || (comp < 0 && dir == 0)) {
            swap(data, i, j);
        }
    }

    private int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) p <<= 1; // Desplazamiento de bits (equivalente a multiplicar por 2)
        return p;
    }

    private int compare(FinancialAsset a, FinancialAsset b) {
        int dateComp = a.getDate().compareTo(b.getDate());
        if (dateComp != 0) return dateComp;
        return a.getClose().compareTo(b.getClose());
    }

    private void swap(List<FinancialAsset> data, int i, int j) {
        FinancialAsset temp = data.get(i);
        data.set(i, data.get(j));
        data.set(j, temp);
    }
}