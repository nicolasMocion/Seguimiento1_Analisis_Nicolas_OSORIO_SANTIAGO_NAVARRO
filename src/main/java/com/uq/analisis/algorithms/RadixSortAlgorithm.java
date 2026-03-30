package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class RadixSortAlgorithm implements SortingAlgorithm {

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.size() <= 1) return;

        // 1. Primero ordenamos por PRECIO DE CIERRE usando Radix (LSD)
        // Multiplicamos por 100 para convertir los decimales (ej. 150.25 -> 15025) a enteros
        radixSortByCriteria(data, true);

        // 2. Luego ordenamos por FECHA (Epoch Day). Como Counting Sort es estable,
        // mantendrá el orden de los precios cuando las fechas sean iguales.
        radixSortByCriteria(data, false);
    }

    @Override
    public String getName() {
        return "Radix Sort";
    }

    private void radixSortByCriteria(List<FinancialAsset> data, boolean isPrice) {
        int n = data.size();

        // Encontrar el valor máximo para saber cuántos dígitos tiene
        long max = getMaxValue(data, isPrice);

        // Hacer Counting Sort por cada dígito. exp es 10^i (1, 10, 100...)
        for (long exp = 1; max / exp > 0; exp *= 10) {
            countingSort(data, n, exp, isPrice);
        }
    }

    private void countingSort(List<FinancialAsset> data, int n, long exp, boolean isPrice) {
        List<FinancialAsset> output = new ArrayList<>(Arrays.asList(new FinancialAsset[n]));
        int[] count = new int[10];

        // Almacenar conteo de ocurrencias de cada dígito
        for (int i = 0; i < n; i++) {
            long val = getValue(data.get(i), isPrice);
            int digit = (int) ((val / exp) % 10);
            count[digit]++;
        }

        // Cambiar count[i] para que contenga la posición real del dígito en output
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        // Construir el arreglo de salida respetando la estabilidad
        for (int i = n - 1; i >= 0; i--) {
            long val = getValue(data.get(i), isPrice);
            int digit = (int) ((val / exp) % 10);
            output.set(count[digit] - 1, data.get(i));
            count[digit]--;
        }

        // Copiar el arreglo de salida al arreglo original
        for (int i = 0; i < n; i++) {
            data.set(i, output.get(i));
        }
    }

    private long getValue(FinancialAsset asset, boolean isPrice) {
        if (isPrice) {
            // Convierte 240.93 a 24093 para poder procesarlo dígito por dígito
            return (long) (asset.getClose() * 100);
        } else {
            // Convierte la fecha a un entero positivo (Días desde 1970)
            return asset.getDate().toEpochDay();
        }
    }

    private long getMaxValue(List<FinancialAsset> data, boolean isPrice) {
        long max = getValue(data.get(0), isPrice);
        for (int i = 1; i < data.size(); i++) {
            long val = getValue(data.get(i), isPrice);
            if (val > max) {
                max = val;
            }
        }
        return max;
    }
}