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
            int pi = particion(data,low,high);

            //Ordena recursivamente los elementos antes y despues de la particion
            quickSort(data, low, pi -1);
            quickSort(data, pi + 1, high);
        }
    }

    private int particion(List<FinancialAsset> data, int low, int high){
        //Elegimos el ultimo elemento como pivote
        FinancialAsset pivot = data.get(high);
        int i = (low - 1);
        return 0;
    }
}
