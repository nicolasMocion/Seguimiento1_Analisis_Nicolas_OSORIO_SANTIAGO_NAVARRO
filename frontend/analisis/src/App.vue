<script setup>
import StockChart from './components/StockChart.vue'
import AlgorithmBenchmark from './components/AlgorithmBenchmark.vue'
import TopVolume from './components/TopVolume.vue' // <-- Nuevo
</script>

<template>
  <div id="app">
    <header>
      <h1>Dashboard de Análisis Financiero</h1>
    </header>
    <main>
      <StockChart />
      <AlgorithmBenchmark />
      <TopVolume /> </main>
  </div>
</template>