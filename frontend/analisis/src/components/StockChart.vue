<template>
  <div class="chart-container">
    <h2 class="tech-title">Comparativa Histórica de Precios de Cierre</h2>

    <div class="controls-panel inner-panel">
      <h3 class="sub-title">Selecciona los activos a comparar:</h3>

      <div v-if="loadingSymbols" class="loading-small pulse-text">
        Buscando acciones disponibles en la base de datos...
      </div>

      <div v-else class="checkbox-group">
        <label v-for="sym in availableSymbols" :key="sym" class="checkbox-label" :class="{ 'active': selectedSymbols.includes(sym) }">
          <input type="checkbox" :value="sym" v-model="selectedSymbols" @change="updateChart" class="hidden-cb"/>
          {{ symbolNames.get(sym) }}
        </label>
      </div>
    </div>

    <div v-if="loadingData" class="loading pulse-text">
      Cargando registros históricos...
    </div>

    <div v-else class="chart-wrapper inner-panel">
      <Line v-if="chartData.datasets.length > 0" :data="chartData" :options="chartOptions" />
      <div v-else class="empty-state">
        <span class="tech-prefix">Esperando selección de parámetros...</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, reactive} from 'vue'
import axios from 'axios'
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement,
  LineElement, Title, Tooltip, Legend
} from 'chart.js'
import { Line } from 'vue-chartjs'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

const availableSymbols = ref([])
const selectedSymbols = ref([])
const loadingSymbols = ref(true)
const loadingData = ref(false)
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
  plugins: { legend: { position: 'top', labels: { font: { family: "'Exo 2', sans-serif" } } } },
  scales: { x: { ticks: { maxTicksLimit: 20 } } },
  animation: { duration: 400 }
})

const stringToColor = (str) => {
  let hash = 0
  for (let i = 0; i < str.length; i++) { hash = str.charCodeAt(i) + ((hash << 5) - hash); }
  let color = '#'
  for (let i = 0; i < 3; i++) {
    const value = (hash >> (i * 8)) & 0xFF;
    color += ('00' + value.toString(16)).substr(-2);
  }
  return color
}

onMounted(async () => {
  try {
    const response = await axios.get('https://projectoavanzada-production.up.railway.app/api/assets/symbols')
    availableSymbols.value = response.data
    if (availableSymbols.value.length > 0) {
      selectedSymbols.value.push(availableSymbols.value[0])
      await updateChart()
    }
  } catch (error) { console.error("Error al obtener la lista de símbolos:", error) }
  finally { loadingSymbols.value = false }
})

const updateChart = async () => {
  if (selectedSymbols.value.length === 0) {
    chartData.value = { labels: [], datasets: [] }
    return
  }
  loadingData.value = true
  try {
    const requests = selectedSymbols.value.map(sym => axios.get(`https://projectoavanzada-production.up.railway.app/api/assets/${sym}`))
    const responses = await Promise.all(requests)

    const newLabels = responses[0].data.map(asset => asset.date)
    const newDatasets = []

    responses.forEach((response, index) => {
      const assetData = response.data
      const currentSymbol = selectedSymbols.value[index]
      const color = stringToColor(currentSymbol)

      newDatasets.push({
        label: symbolNames.get(currentSymbol),
        backgroundColor: color, borderColor: color, borderWidth: 2, pointRadius: 0,
        data: assetData.map(asset => asset.close)
      })
    })

    chartData.value = { labels: newLabels, datasets: newDatasets }
  } catch (error) { console.error("Error al obtener el histórico de precios:", error) }
  finally { loadingData.value = false }
}
</script>

<style scoped>
.chart-container { width: 100%; }

.tech-title { color: var(--dodger-blue); text-transform: uppercase; letter-spacing: 2px; margin-top: 0; border-bottom: 2px solid rgba(0, 90, 156, 0.1); padding-bottom: 10px; }
.sub-title { color: var(--dodger-blue); font-size: 1rem; margin-top: 0; margin-bottom: 15px; font-weight: 600; text-transform: uppercase;}

.inner-panel { background-color: rgba(255, 255, 255, 0.4); border: 1px solid rgba(162, 170, 173, 0.3); border-radius: 8px; padding: 20px; }
.controls-panel { margin-bottom: 20px; }

.checkbox-group { display: flex; flex-wrap: wrap; gap: 10px; }

/* Checkboxes estilo píldora tecnológica */
.hidden-cb { display: none; }
.checkbox-label {
  display: flex; align-items: center; justify-content: center;
  font-weight: 600; color: var(--dodger-blue); cursor: pointer; user-select: none;
  background-color: transparent; border: 1px solid var(--dodger-blue);
  padding: 6px 14px; border-radius: 20px; font-size: 0.9rem;
  transition: all 0.3s ease;
}

.checkbox-label:hover { background-color: rgba(0, 90, 156, 0.1); }

/* Estado Activo (Seleccionado) */
.checkbox-label.active {
  background-color: var(--dodger-blue); color: var(--dodger-white);
  box-shadow: 0 0 10px var(--blue-glow); border-color: transparent;
}

.chart-wrapper { height: 60vh; }
.pulse-text { animation: parpadeo 1.5s infinite; color: var(--dodger-blue); font-weight: bold; }
.loading, .loading-small { text-align: center; }
.loading { margin-top: 50px; font-size: 1.2rem; }
.empty-state { display: flex; justify-content: center; align-items: center; height: 100%; color: var(--dodger-silver); font-size: 1.1rem; }
</style>