<template>
  <div class="benchmark-container">
    <h2>Análisis de Complejidad y Tiempos de Ejecución</h2>

    <div class="controls-panel">
      <label for="symbolSelect">Ejecutar pruebas sobre: </label>
      <select id="symbolSelect" v-model="selectedSymbol" :disabled="loading">
        <option v-for="sym in availableSymbols" :key="sym" :value="sym">
          {{ sym }}
        </option>
      </select>
      <button @click="runBenchmark" :disabled="loading || !selectedSymbol" class="run-btn">
        {{ loading ? 'Ejecutando algoritmos...' : 'Iniciar Competición' }}
      </button>
    </div>

    <div v-if="loading" class="loading">
      Cronometrando algoritmos en el backend (Java)...
    </div>

    <div v-if="results.length > 0 && !loading" class="results-wrapper">

      <div class="table-container">
        <h3>Tabla 1. Comparativa de Algoritmos</h3>
        <table>
          <thead>
          <tr>
            <th>Método</th>
            <th>Tamaño (n)</th>
            <th>Complejidad Teórica</th>
            <th>Tiempo (ms)</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="res in results" :key="res.algorithmName">
            <td><strong>{{ res.algorithmName }}</strong></td>
            <td>{{ res.datasetSize }}</td>
            <td>{{ res.expectedComplexity }}</td>
            <td class="time-cell">{{ res.timeInMilliseconds.toFixed(3) }} ms</td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="chart-container">
        <h3>Tiempos de Ejecución (Ascendente)</h3>
        <div class="chart-wrapper">
          <Bar :data="chartData" :options="chartOptions" />
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement, // Importante: Necesitamos BarElement para gráficas de barras
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { Bar } from 'vue-chartjs'

// Registramos los componentes para el diagrama de barras
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const availableSymbols = ref([])
const selectedSymbol = ref('')
const loading = ref(false)
const results = ref([])

const chartData = ref({ labels: [], datasets: [] })
const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } }, // Ocultamos la leyenda porque solo es una métrica
  scales: {
    y: {
      beginAtZero: true,
      title: { display: true, text: 'Milisegundos (ms)' }
    }
  }
})

// Al montar, traemos la lista de acciones disponibles
onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/assets/symbols')
    availableSymbols.value = response.data
    if (availableSymbols.value.length > 0) {
      selectedSymbol.value = availableSymbols.value[0]
    }
  } catch (error) {
    console.error("Error al obtener símbolos:", error)
  }
})

// Función que dispara la carrera en Spring Boot
const runBenchmark = async () => {
  loading.value = true
  try {
    const response = await axios.get(`http://localhost:8080/api/assets/${selectedSymbol.value}/benchmark`)

    // El documento exige que el ordenamiento sea ascendente por tiempo
    // Ordenamos el arreglo JSON usando JavaScript antes de mostrarlo
    const sortedData = response.data.sort((a, b) => a.timeInMilliseconds - b.timeInMilliseconds)
    results.value = sortedData

    // Armamos los datos para la gráfica de barras
    chartData.value = {
      labels: sortedData.map(r => r.algorithmName),
      datasets: [{
        label: 'Tiempo (ms)',
        backgroundColor: sortedData.map((_, i) => i === 0 ? '#27ae60' : '#3498db'), // El más rápido en verde, el resto en azul
        data: sortedData.map(r => r.timeInMilliseconds)
      }]
    }

  } catch (error) {
    console.error("Error ejecutando el benchmark:", error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.benchmark-container {
  width: 90vw;
  max-width: 1200px;
  margin: 40px auto;
  font-family: 'Segoe UI', sans-serif;
}

.controls-panel {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  border: 1px solid #e1e4e8;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

select {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ccc;
  font-size: 1rem;
}

.run-btn {
  background-color: #2c3e50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background 0.2s;
}

.run-btn:hover:not(:disabled) { background-color: #1a252f; }
.run-btn:disabled { background-color: #95a5a6; cursor: not-allowed; }

.results-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.table-container, .chart-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  border: 1px solid #e1e4e8;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th { background-color: #f8f9fa; color: #2c3e50; }

.time-cell {
  font-family: monospace;
  font-size: 1.1rem;
  color: #e74c3c;
  text-align: right;
}

.chart-wrapper {
  height: 300px;
  margin-top: 15px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
  font-size: 1.2rem;
}

@media (max-width: 768px) {
  .results-wrapper { grid-template-columns: 1fr; }
}
</style>