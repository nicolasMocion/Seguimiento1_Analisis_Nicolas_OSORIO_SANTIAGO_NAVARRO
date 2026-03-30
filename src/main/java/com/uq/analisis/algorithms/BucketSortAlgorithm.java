package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BucketSortAlgorithm implements SortingAlgorithm {

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.size() <= 1) return;

        int n = data.size();

        // Encontramos el valor mínimo y máximo (usando el tiempo UNIX / Epoch Day)
        long minDay = data.get(0).getDate().toEpochDay();
        long maxDay = minDay;

        for (FinancialAsset asset : data) {
            long currentDay = asset.getDate().toEpochDay();
            if (currentDay < minDay) minDay = currentDay;
            if (currentDay > maxDay) maxDay = currentDay;
        }

        // Crear las cubetas (buckets). Usamos la raíz cuadrada de N como número de cubetas
        int numBuckets = (int) Math.sqrt(n);
        List<List<FinancialAsset>> buckets = new ArrayList<>(numBuckets);
        for (int i = 0; i < numBuckets; i++) {
            buckets.add(new ArrayList<>());
        }

        // Distribuir los elementos en las cubetas
        double range = (double) (maxDay - minDay + 1) / numBuckets;
        for (FinancialAsset asset : data) {
            int bucketIndex = (int) ((asset.getDate().toEpochDay() - minDay) / range);
            if (bucketIndex == numBuckets) {
                bucketIndex--; // Caso borde para el valor máximo
            }
            buckets.get(bucketIndex).add(asset);
        }

        // Ordenar cada cubeta individualmente y fusionar (Usamos Insertion Sort para las cubetas)
        int currentIndex = 0;
        for (List<FinancialAsset> bucket : buckets) {
            insertionSort(bucket);
            for (FinancialAsset asset : bucket) {
                data.set(currentIndex++, asset);
            }
        }
    }

    @Override
    public String getName() {
        return "Bucket Sort";
    }

    // Algoritmo auxiliar para ordenar dentro de cada cubeta
    private void insertionSort(List<FinancialAsset> bucket) {
        for (int i = 1; i < bucket.size(); i++) {
            FinancialAsset key = bucket.get(i);
            int j = i - 1;
            while (j >= 0 && compare(bucket.get(j), key) > 0) {
                bucket.set(j + 1, bucket.get(j));
                j--;
            }
            bucket.set(j + 1, key);
        }
    }

    private int compare(FinancialAsset a, FinancialAsset b) {
        int dateComparison = a.getDate().compareTo(b.getDate());
        if (dateComparison != 0) return dateComparison;
        return a.getClose().compareTo(b.getClose());
    }
}