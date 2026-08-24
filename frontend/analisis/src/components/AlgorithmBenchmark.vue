<template>
  <div class="benchmark-container">
    <h2 class="tech-title">Análisis de Complejidad y Tiempos de Ejecución</h2>

    <div class="controls-panel inner-panel">
      <label for="symbolSelect" class="tech-label">Ejecutar pruebas sobre: </label>
      <select id="symbolSelect" v-model="selectedSymbol" :disabled="loading" class="tech-select">
        <option v-for="sym in availableSymbols" :key="sym" :value="sym">
          {{ symbolNames.get(sym) }}
        </option>
      </select>
      <button @click="runBenchmark" :disabled="loading || !selectedSymbol" class="run-btn">
        {{ loading ? 'EJECUTANDO...' : 'INICIAR COMPETICIÓN' }}
      </button>
    </div>

    <div v-if="loading" class="loading pulse-text">
      Cronometrando algoritmos en el backend (Java)...
    </div>

    <div v-if="results.length > 0 && !loading" class="results-wrapper">

      <div class="table-container inner-panel">
        <h3 class="sub-title">Tabla 1. Comparativa de Algoritmos</h3>
        <table class="tech-table">
          <thead>
          <tr>
            <th>MÉTODO</th>
            <th>TAMAÑO (n)</th>
            <th>COMPLEJIDAD</th>
            <th>TIEMPO (ms)</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="res in results" :key="res.algorithmName">
            <td class="highlight-text"><strong>{{ res.algorithmName }}</strong></td>
            <td>{{ res.datasetSize }}</td>
            <td class="complexity-text">{{ res.expectedComplexity }}</td>
            <td class="time-cell">{{ res.timeInMilliseconds.toFixed(3) }} ms</td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="chart-container inner-panel">
        <h3 class="sub-title">Tiempos de Ejecución (Ascendente)</h3>
        <div class="chart-wrapper">
          <Bar :data="chartData" :options="chartOptions" />
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, reactive} from 'vue'
import axios from 'axios'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { Bar } from 'vue-chartjs'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const availableSymbols = ref([])
const selectedSymbol = ref('')
const loading = ref(false)
const results = ref([])
const symbolNames = reactive(new Map([
  ["AAPL", "APPLE"], ["IBM", "IBM"], ["GC=F", "GOLD"], ["NTDOY", "NINTENDO"],
  ["AMZN", "AMAZON"], ["TSLA", "TESLA"], ["MU", "MICRON"], ["BTC-USD", "BITCOIN"],
  ["MSFT", "MICROSOFT"], ["EC", "ECOPETROL"], ["^N225", "NIKKEI 225"],
  ["^DJI", "Dow Jones"], ["^NYA", "NYSE Composite Index"], ["^GSPC", "S&P 500"],
  ["META", "META"], ["NVDA", "NVIDIA"], ["NU", "NU"],
]))

const chartData = ref({ labels: [], datasets: [] })
const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    y: { beginAtZero: true, title: { display: true, text: 'Milisegundos (ms)' } }
  }
})

onMounted(async () => {
  try {
    const response = await axios.get('https://seguimiento1analisisnicolasosoriosantiagona-production-d160.up.railway.app/api/assets/symbols')
    availableSymbols.value = response.data
    if (availableSymbols.value.length > 0) {
      selectedSymbol.value = availableSymbols.value[0]
    }
  } catch (error) {
    console.error("Error al obtener símbolos:", error)
  }
})

const runBenchmark = async () => {
  loading.value = true
  try {
    const response = await axios.get(`https://seguimiento1analisisnicolasosoriosantiagona-production-d160.up.railway.app/api/assets/${selectedSymbol.value}/benchmark`)
    const sortedData = response.data.sort((a, b) => a.timeInMilliseconds - b.timeInMilliseconds)
    results.value = sortedData

    chartData.value = {
      labels: sortedData.map(r => r.algorithmName),
      datasets: [{
        label: 'Tiempo (ms)',
        // Rojo Dodgers para el ganador, Azul Dodgers para el resto
        backgroundColor: sortedData.map((_, i) => i === 0 ? '#EF3E42' : '#005A9C'),
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
.benchmark-container { width: 100%; }

.tech-title {
  color: var(--dodger-blue);
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-top: 0;
  border-bottom: 2px solid rgba(0, 90, 156, 0.1);
  padding-bottom: 10px;
}

.sub-title {
  color: var(--dodger-blue);
  font-weight: 600;
  text-transform: uppercase;
  margin-top: 0;
  letter-spacing: 1px;
}

.inner-panel {
  background-color: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(162, 170, 173, 0.3);
  border-radius: 8px;
  padding: 20px;
}

.controls-panel {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.tech-label { font-weight: 600; color: var(--text-dark); }

.tech-select {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid var(--dodger-blue);
  background-color: transparent;
  font-family: 'Exo 2', sans-serif;
  font-weight: 600;
  color: var(--dodger-blue);
  outline: none;
}

.tech-select:focus { box-shadow: 0 0 8px var(--blue-glow); }

.run-btn {
  background-color: var(--dodger-blue);
  color: var(--dodger-white);
  border: 1px solid transparent;
  padding: 10px 24px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.run-btn:hover:not(:disabled) {
  background-color: var(--dodger-red);
  box-shadow: 0 0 15px rgba(239, 62, 66, 0.5);
  transform: translateY(-2px);
}
.run-btn:disabled { background-color: var(--dodger-silver); cursor: not-allowed; }

.results-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.tech-table { width: 100%; border-collapse: collapse; margin-top: 15px; }
.tech-table th, .tech-table td { padding: 12px; text-align: left; border-bottom: 1px solid rgba(162, 170, 173, 0.2); }
.tech-table th { background-color: var(--dodger-blue); color: var(--dodger-white); font-weight: 600; }
.tech-table tr:hover { background-color: rgba(0, 90, 156, 0.05); }

.highlight-text { color: var(--dodger-blue); }
.complexity-text { font-family: monospace; color: var(--dodger-silver); font-weight: bold; }
.time-cell { font-family: monospace; font-size: 1.1rem; color: var(--dodger-red); text-align: right; font-weight: bold; }

.chart-wrapper { height: 300px; margin-top: 15px; }

.pulse-text { animation: parpadeo 1.5s infinite; color: var(--dodger-blue); font-weight: bold; }
.loading { text-align: center; padding: 40px; font-size: 1.2rem; }

@media (max-width: 768px) { .results-wrapper { grid-template-columns: 1fr; } }
</style>