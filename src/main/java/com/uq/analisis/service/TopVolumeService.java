package com.uq.analisis.service;

import com.uq.analisis.model.FinancialAsset;
import com.uq.analisis.Repository.FinancialAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.uq.analisis.algorithms.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopVolumeService {

    private final FinancialAssetRepository repository;
    private final QuickSortAlgorithm quick = new QuickSortAlgorithm();

    public List<FinancialAsset> getTop15Volume(String symbol) {
        // 1. Traemos los datos
        List<FinancialAsset> data = repository.findAllBySymbolOrderByDateAsc(symbol);
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Ordenamos usando nuestra versión optimizada de QuickSort (Volumen Descendente)
        quickSortByVolumeDesc(data, 0, data.size() - 1);


        // 3. Recortamos y devolvemos solo los primeros 15
        return data.size() > 15 ? data.subList(0, 15) : data;
    }




    /**
     * QuickSort optimizado: Usa el elemento central como pivote para evitar el peor caso O(n^2)
     * y ordena de MAYOR a MENOR basándose exclusivamente en el campo 'volume'.
     */
    private void quickSortByVolumeDesc(List<FinancialAsset> data, int low, int high) {
        if (low >= high) return;

        // Elegir el pivote en el medio
        int mid = low + (high - low) / 2;
        long pivotValue = data.get(mid).getVolume();

        int i = low;
        int j = high;

        while (i <= j) {
            // Mientras el elemento izquierdo sea MAYOR que el pivote (porque es descendente)
            while (data.get(i).getVolume() > pivotValue) {
                i++;
            }
            // Mientras el elemento derecho sea MENOR que el pivote
            while (data.get(j).getVolume() < pivotValue) {
                j--;
            }

            // Intercambiar si se cruzaron
            if (i <= j) {
                FinancialAsset temp = data.get(i);
                data.set(i, data.get(j));
                data.set(j, temp);
                i++;
                j--;
            }
        }

        // Llamadas recursivas
        if (low < j) {
            quickSortByVolumeDesc(data, low, j);
        }
        if (i < high) {
            quickSortByVolumeDesc(data, i, high);
        }
    }
}