package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BinaryInsertionSortingAlgorithm implements SortingAlgorithm {

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        int n = data.size();

        for (int i = 1; i < n; i++) {
            FinancialAsset key = data.get(i);
            int insertedPosition = binarySearch(data, key, 0, i - 1);

            // Desplazar elementos a la derecha para hacer espacio
            for (int j = i - 1; j >= insertedPosition; j--) {
                data.set(j + 1, data.get(j));
            }
            data.set(insertedPosition, key);
        }
    }

    @Override
    public String getName() {
        return "Binary Insertion Sort";
    }

    private int binarySearch(List<FinancialAsset> data, FinancialAsset item, int low, int high) {
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int comp = compare(item, data.get(mid));

            if (comp == 0) {
                return mid + 1;
            } else if (comp > 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    private int compare(FinancialAsset a, FinancialAsset b) {
        int dateComparison = a.getDate().compareTo(b.getDate());
        if (dateComparison != 0) {
            return dateComparison;
        }
        return a.getClose().compareTo(b.getClose());
    }
}