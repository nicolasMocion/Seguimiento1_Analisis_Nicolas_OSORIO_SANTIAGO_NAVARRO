package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GnomeSortAlgorithm implements SortingAlgorithm {

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        int index = 0;
        int n = data.size();

        while (index < n) {
            if (index == 0) {
                index++;
            }
            if (compare(data.get(index), data.get(index - 1)) >= 0) {
                index++;
            } else {
                swap(data, index, index - 1);
                index--;
            }
        }
    }

    @Override
    public String getName() {
        return "Gnome Sort";
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