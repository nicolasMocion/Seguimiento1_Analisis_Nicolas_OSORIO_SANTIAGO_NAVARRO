package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CombSortAlgorithm implements SortingAlgorithm {

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        int n = data.size();
        int gap = n;
        boolean swapped = true;

        while (gap != 1 || swapped) {
            gap = getNextGap(gap);
            swapped = false;

            for (int i = 0; i < n - gap; i++) {
                if (compare(data.get(i), data.get(i + gap)) > 0) {
                    swap(data, i, i + gap);
                    swapped = true;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Comb Sort";
    }

    private int getNextGap(int gap) {
        // Factor de encogimiento estándar (1.3)
        gap = (gap * 10) / 13;
        if (gap < 1) {
            return 1;
        }
        return gap;
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
}