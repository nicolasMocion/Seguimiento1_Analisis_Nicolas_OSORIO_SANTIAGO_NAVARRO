package com.uq.analisis.Repository;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialAssetRepository extends JpaRepository<FinancialAsset, Long> {

    // Método útil para el motor ETL: verificar si ya existen datos para no duplicar peticiones
    boolean existsBySymbol(String symbol);

    // Método para recuperar los datos de un activo específico ordenados por fecha
    List<FinancialAsset> findAllBySymbolOrderByDateAsc(String symbol);

    // Método para recuperar los datos en un rango de fechas (útil para alinear calendarios)
    List<FinancialAsset> findBySymbolAndDateBetweenOrderByDateAsc(String symbol, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT f.symbol FROM FinancialAsset f")
    List<String> findAllDistinctSymbols();
}