package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PigeonholeSortAlgorithm implements SortingAlgorithm {

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.isEmpty()) return;

        // 1. Encontrar el mínimo y máximo Epoch Day (Días desde 1970)
        long min = data.get(0).getDate().toEpochDay();
        long max = min;

        for (FinancialAsset asset : data) {
            long current = asset.getDate().toEpochDay();
            if (current < min) min = current;
            if (current > max) max = current;
        }

        // 2. Crear los "palomares" (holes)
        int range = (int) (max - min + 1);
        List<List<FinancialAsset>> holes = new ArrayList<>(range);
        for (int i = 0; i < range; i++) {
            holes.add(new ArrayList<>());
        }

        // 3. Poner cada activo en su palomar correspondiente según la fecha
        for (FinancialAsset asset : data) {
            holes.get((int) (asset.getDate().toEpochDay() - min)).add(asset);
        }

        // 4. Extraer los elementos. Si hay colisiones (mismo día), ordenamos por precio
        int index = 0;
        for (List<FinancialAsset> hole : holes) {
            if (hole.size() > 1) {
                insertionSortByPrice(hole); // Desempate por precio de cierre
            }
            for (FinancialAsset asset : hole) {
                data.set(index++, asset);
            }
        }
    }

    @Override
    public String getName() {
        return "Pigeonhole Sort";
    }

    private void insertionSortByPrice(List<FinancialAsset> hole) {
        for (int i = 1; i < hole.size(); i++) {
            FinancialAsset key = hole.get(i);
            int j = i - 1;
            while (j >= 0 && hole.get(j).getClose() > key.getClose()) {
                hole.set(j + 1, hole.get(j));
                j--;
            }
            hole.set(j + 1, key);
        }
    }
}