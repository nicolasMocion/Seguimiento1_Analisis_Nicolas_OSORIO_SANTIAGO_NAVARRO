<template>
  <div class="similarity-container">
    <h2 class="tech-title">Análisis Comparativo de Series de Tiempo</h2>
    <p class="tech-description">Evalúa la correlación matemática y la distancia geométrica entre dos activos.</p>

    <!-- Panel de Control -->
    <div class="controls-panel inner-panel">
      <div class="selector-group">
        <label class="tech-label" style="color: var(--dodger-blue)">ACTIVO A (Azul):</label>
        <select v-model="symbolA" :disabled="loading" class="tech-select">
          <option v-for="sym in availableSymbols" :key="sym" :value="sym">{{ symbolNames.get(sym) || sym }}</option>
        </select>
      </div>

      <div class="vs-badge">VS</div>

      <div class="selector-group">
        <label class="tech-label" style="color: var(--dodger-red)">ACTIVO B (Rojo):</label>
        <select v-model="symbolB" :disabled="loading" class="tech-select border-red">
          <option v-for="sym in availableSymbols" :key="sym" :value="sym">{{ symbolNames.get(sym) || sym }}</option>
        </select>
      </div>

      <button @click="runComparison" :disabled="loading || !symbolA || !symbolB || symbolA === symbolB" class="run-btn">
        {{ loading ? 'CALCULANDO...' : 'EJECUTAR ANÁLISIS' }}
      </button>
    </div>

    <div v-if="symbolA === symbolB && symbolA !== ''" class="error-msg pulse-text">
      Por favor selecciona dos activos diferentes para la comparación.
    </div>

    <div v-if="loading" class="loading pulse-text">
      Ejecutando O(n) y O(n*m) + renderizando gráficas...
    </div>

    <div v-if="Object.keys(results).length > 0 && !loading" class="analysis-results">

      <!-- NUEVO: Gráfica de Series de Tiempo -->
      <div class="chart-container inner-panel">
        <h3 class="sub-title">Comportamiento Histórico (Precios de Cierre)</h3>
        <div class="chart-wrapper">
          <Line :data="chartData" :options="chartOptions" />
        </div>
      </div>

      <!-- Resultados Numéricos -->
      <div class="metrics-grid">
        <div class="metric-card inner-panel">
          <h4>Distancia Euclidiana</h4>
          <div class="metric-value">{{ results['Distancia Euclidiana'].toFixed(2) }}</div>
          <p class="metric-hint">Separación geométrica pura. <br/><strong>(Más cerca a 0 = Más similares)</strong></p>
        </div>

        <div class="metric-card inner-panel">
          <h4>Dynamic Time Warping</h4>
          <div class="metric-value">{{ results['Dynamic Time Warping (DTW)'].toFixed(2) }}</div>
          <p class="metric-hint">Alineación elástica temporal. <br/><strong>(Más cerca a 0 = Misma forma)</strong></p>
        </div>

        <div class="metric-card inner-panel">
          <h4>Correlación de Pearson</h4>
          <div class="metric-value" :class="getCorrelationColor(results['Correlación de Pearson'])">
            {{ results['Correlación de Pearson'].toFixed(4) }}
          </div>
          <p class="metric-hint">Relación lineal [-1 a 1]. <br/><strong>(1 = Movimiento idéntico)</strong></p>
        </div>

        <div class="metric-card inner-panel">
          <h4>Similitud por Coseno</h4>
          <div class="metric-value" :class="getCorrelationColor(results['Similitud por Coseno'])">
            {{ results['Similitud por Coseno'].toFixed(4) }}
          </div>
          <p class="metric-hint">Ángulo vectorial de retornos. <br/><strong>(1 = Misma dirección)</strong></p>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement,
  LineElement, Title, Tooltip, Legend
} from 'chart.js'
import { Line } from 'vue-chartjs'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

const availableSymbols = ref([])
const symbolA = ref('')
const symbolB = ref('')
const loading = ref(false)
const results = ref({})

const chartData = ref({ labels: [], datasets: [] })
const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'top', labels: { font: { family: "'Exo 2', sans-serif", weight: 'bold' } } } },
  scales: { x: { ticks: { maxTicksLimit: 15 } } },
  animation: { duration: 500 }
})

const symbolNames = reactive(new Map([
  ["AAPL", "APPLE"], ["IBM", "IBM"], ["GC=F", "GOLD"], ["NTDOY", "NINTENDO"],
  ["AMZN", "AMAZON"], ["TSLA", "TESLA"], ["MU", "MICRON"], ["BTC-USD", "BITCOIN"],
  ["MSFT", "MICROSOFT"], ["EC", "ECOPETROL"], ["^N225", "NIKKEI 225"],
  ["^DJI", "Dow Jones"], ["^NYA", "NYSE Composite Index"], ["^GSPC", "S&P 500"],
  ["META", "META"], ["NVDA", "NVIDIA"], ["NU", "NU"]
]))

onMounted(async () => {
  try {
    const response = await axios.get('https://projectoavanzada-production.up.railway.app/api/assets/symbols')
    availableSymbols.value = response.data
    if (availableSymbols.value.length >= 2) {
      symbolA.value = availableSymbols.value[0]
      symbolB.value = availableSymbols.value[1]
    }
  } catch (error) { console.error("Error al obtener símbolos:", error) }
})

const runComparison = async () => {
  loading.value = true
  try {
    // Hacemos 3 peticiones en paralelo: Las métricas, los datos históricos de A, y los de B
    const [simRes, dataARes, dataBRes] = await Promise.all([
      axios.get(`https://projectoavanzada-production.up.railway.app/api/similarity/compare?symbolA=${symbolA.value}&symbolB=${symbolB.value}`),
      axios.get(`https://projectoavanzada-production.up.railway.app/api/assets/${symbolA.value}`),
      axios.get(`https://projectoavanzada-production.up.railway.app/api/assets/${symbolB.value}`)
    ])

    results.value = simRes.data

    // Armamos la gráfica
    const assetA = dataARes.data
    const assetB = dataBRes.data

    // Usamos las fechas del Activo A como base para el eje X
    const labels = assetA.map(a => a.date)

    chartData.value = {
      labels: labels,
      datasets: [
        {
          label: symbolNames.get(symbolA.value) || symbolA.value,
          backgroundColor: '#005A9C', // Azul Dodgers
          borderColor: '#005A9C',
          borderWidth: 2,
          pointRadius: 0, // 0 para que no se vea una mancha de puntos
          data: assetA.map(a => a.close)
        },
        {
          label: symbolNames.get(symbolB.value) || symbolB.value,
          backgroundColor: '#EF3E42', // Rojo Dodgers
          borderColor: '#EF3E42',
          borderWidth: 2,
          pointRadius: 0,
          data: assetB.map(b => b.close)
        }
      ]
    }

  } catch (error) {
    console.error("Error en la comparación:", error)
  } finally {
    loading.value = false
  }
}

const getCorrelationColor = (value) => {
  if (value > 0.5) return 'positive-corr'
  if (value < -0.5) return 'negative-corr'
  return 'neutral-corr'
}
</script>

<style scoped>
.similarity-container { width: 100%; }

.tech-title { color: var(--dodger-blue); text-transform: uppercase; letter-spacing: 2px; margin-top: 0; border-bottom: 2px solid rgba(0, 90, 156, 0.1); padding-bottom: 10px;}
.tech-description { color: var(--dodger-silver); margin-bottom: 20px; font-size: 0.95rem; }
.sub-title { color: var(--dodger-blue); margin-top: 0; text-transform: uppercase; font-size: 1rem; letter-spacing: 1px;}

.inner-panel { background-color: rgba(255, 255, 255, 0.4); border: 1px solid rgba(162, 170, 173, 0.3); border-radius: 8px; padding: 20px; }
.controls-panel { display: flex; align-items: center; justify-content: center; gap: 20px; flex-wrap: wrap; margin-bottom: 20px; }

.selector-group { display: flex; flex-direction: column; gap: 5px; }
.tech-label { font-weight: 600; font-size: 0.85rem; text-transform: uppercase; }
.tech-select { padding: 10px; border-radius: 4px; border: 1px solid var(--dodger-blue); background-color: transparent; font-family: 'Exo 2', sans-serif; font-weight: 600; color: var(--dodger-blue); outline: none; min-width: 150px; }
.border-red { border-color: var(--dodger-red); color: var(--dodger-red); }
.tech-select:focus { box-shadow: 0 0 8px var(--blue-glow); }

.vs-badge { background-color: var(--dodger-red); color: white; font-weight: bold; padding: 10px; border-radius: 50%; font-size: 1rem; box-shadow: 0 0 10px rgba(239, 62, 66, 0.4); margin-top: 15px; }

.run-btn { background-color: var(--dodger-blue); color: white; border: none; padding: 12px 24px; border-radius: 4px; cursor: pointer; font-weight: 700; letter-spacing: 1px; transition: all 0.3s ease; margin-top: 15px; }
.run-btn:hover:not(:disabled) { background-color: var(--dodger-red); box-shadow: 0 0 15px rgba(239, 62, 66, 0.5); transform: translateY(-2px); }
.run-btn:disabled { background-color: var(--dodger-silver); cursor: not-allowed; }

.analysis-results { display: flex; flex-direction: column; gap: 20px; margin-top: 20px;}

.chart-wrapper { height: 350px; margin-top: 15px; }

.metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; }
.metric-card { text-align: center; display: flex; flex-direction: column; justify-content: space-between; }
.metric-card h4 { color: var(--text-dark); margin-top: 0; font-size: 0.95rem; border-bottom: 1px solid rgba(162, 170, 173, 0.3); padding-bottom: 10px; }

.metric-value { font-family: monospace; font-size: 2rem; font-weight: bold; color: var(--dodger-blue); margin: 15px 0; }
.positive-corr { color: #27ae60; }
.negative-corr { color: var(--dodger-red); }
.neutral-corr { color: var(--dodger-silver); }

.metric-hint { font-size: 0.8rem; color: #7f8c8d; margin: 0; }
.error-msg { text-align: center; color: var(--dodger-red); margin-bottom: 15px; font-weight: bold; }
.pulse-text { animation: parpadeo 1.5s infinite; color: var(--dodger-blue); font-weight: bold; }
.loading { text-align: center; padding: 20px; font-size: 1.2rem; }
</style>