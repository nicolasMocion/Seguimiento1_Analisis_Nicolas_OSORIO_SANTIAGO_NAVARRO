package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.List;

public class QuickSortAlgorithm implements SortingAlgorithm{


    @Override
    public void sort(List<FinancialAsset> data) {
        if(data == null || data.isEmpty()){
            return;
        }
        quickSort(data, 0, data.size() -1);
    }

    @Override
    public String getName() {
        return "QuickSort";
    }

    private void quickSort(List<FinancialAsset> data, int low, int high ){

        if(low < high){
            // Encuentra el indice de la particion
            int pi = partition(data,low,high);

            //Ordena recursivamente los elementos antes y despues de la particion
            quickSort(data, low, pi -1);
            quickSort(data, pi + 1, high);
        }
    }

    private int partition(List<FinancialAsset> data, int low, int high) {
        // Elegimos el último elemento como pivote
        FinancialAsset pivot = data.get(high);
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            // Si el elemento actual es menor o igual al pivote
            if (compare(data.get(j), pivot) <= 0) {
                i++;
                swap(data, i, j);
            }
        }
        // Intercambiamos el elemento siguiente al índice más pequeño con el pivote
        swap(data, i + 1, high);
        return i + 1;
    }

    private int compare(FinancialAsset a, FinancialAsset b) {
        int dateComparison = a.getDate().compareTo(b.getDate());
        if (dateComparison != 0) {
            return dateComparison;
        }
        // Si las fechas son exactamente iguales, desempatamos por precio de cierre
        return a.getClose().compareTo(b.getClose());
    }

    private void swap(List<FinancialAsset> data, int i, int j) {
        FinancialAsset temp = data.get(i);
        data.set(i, data.get(j));
        data.set(j, temp);
    }
}
