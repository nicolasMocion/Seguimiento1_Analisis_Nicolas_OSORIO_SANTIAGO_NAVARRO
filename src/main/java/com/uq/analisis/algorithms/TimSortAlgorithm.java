package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TimSortAlgorithm implements SortingAlgorithm {

    private static final int RUN = 32;

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.isEmpty()) return;
        int n = data.size();

        // 1. Ordenar subarreglos individuales (RUNs) con Insertion Sort
        for (int i = 0; i < n; i += RUN) {
            insertionSort(data, i, Math.min((i + RUN - 1), (n - 1)));
        }

        // 2. Fusionar los RUNs con la lógica de Merge Sort
        for (int size = RUN; size < n; size = 2 * size) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid = left + size - 1;
                int right = Math.min((left + 2 * size - 1), (n - 1));

                if (mid < right) {
                    merge(data, left, mid, right);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "TimSort";
    }

    private void insertionSort(List<FinancialAsset> data, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            FinancialAsset temp = data.get(i);
            int j = i - 1;
            while (j >= left && compare(data.get(j), temp) > 0) {
                data.set(j + 1, data.get(j));
                j--;
            }
            data.set(j + 1, temp);
        }
    }

    private void merge(List<FinancialAsset> data, int left, int mid, int right) {
        int len1 = mid - left + 1, len2 = right - mid;
        List<FinancialAsset> leftArr = new ArrayList<>(len1);
        List<FinancialAsset> rightArr = new ArrayList<>(len2);

        for (int i = 0; i < len1; i++) leftArr.add(data.get(left + i));
        for (int i = 0; i < len2; i++) rightArr.add(data.get(mid + 1 + i));

        int i = 0, j = 0, k = left;

        while (i < len1 && j < len2) {
            if (compare(leftArr.get(i), rightArr.get(j)) <= 0) {
                data.set(k, leftArr.get(i));
                i++;
            } else {
                data.set(k, rightArr.get(j));
                j++;
            }
            k++;
        }

        while (i < len1) {
            data.set(k, leftArr.get(i));
            k++;
            i++;
        }

        while (j < len2) {
            data.set(k, rightArr.get(j));
            k++;
            j++;
        }
    }

    private int compare(FinancialAsset a, FinancialAsset b) {
        int dateComp = a.getDate().compareTo(b.getDate());
        if (dateComp != 0) return dateComp;
        return a.getClose().compareTo(b.getClose());
    }
}