<template>
  <div class="dashboard-container">
    <h2 class="tech-title">Dashboard Bursátil</h2>
    <p class="tech-description">Análisis avanzado: Correlación multivariada y Acción del Precio.</p>

    <!-- Botón de Exportación (Requerimiento 4.3) -->
    <div class="actions-panel">
      <button @click="exportToPDF" class="run-btn pdf-btn">
        <span>📄</span> EXPORTAR REPORTE A PDF
      </button>
    </div>

    <!-- Layout de dos columnas -->
    <div class="split-layout" ref="pdfContent">

      <!-- SECCIÓN IZQUIERDA: MATRIZ DE CORRELACIÓN (Heatmap) -->
      <div class="heatmap-section inner-panel">
        <h3 class="sub-title">Matriz de Correlación (Pearson)</h3>
        <p class="metric-hint">Identifica activos que se mueven en la misma dirección (cerca de 1.0) o en direcciones opuestas (cerca de -1.0).</p>

        <div v-if="loadingMatrix" class="loading pulse-text">Calculando matriz N x N...</div>
        <div v-else class="chart-wrapper heatmap-wrapper">
          <apexchart type="heatmap" height="400" :options="heatmapOptions" :series="heatmapSeries"></apexchart>
        </div>
      </div>

      <!-- SECCIÓN DERECHA: VELAS JAPONESAS + SMA -->
      <div class="candlestick-section inner-panel">
        <h3 class="sub-title">Acción del Precio y Media Móvil Simple</h3>

        <div class="controls-panel">
          <label class="tech-label">Analizar:</label>
          <select v-model="selectedSymbol" @change="fetchCandleData" class="tech-select full-width">
            <option v-for="sym in availableSymbols" :key="sym" :value="sym">{{ symbolNames.get(sym) || sym }}</option>
          </select>

          <label class="tech-label" style="margin-top:10px;">Ventana SMA:</label>
          <input type="number" v-model="smaWindow" @change="fetchCandleData" min="5" max="200" class="tech-select full-width" />
        </div>

        <div v-if="loadingCandles" class="loading pulse-text">Renderizando velas...</div>
        <div v-else class="chart-wrapper">
          <apexchart type="line" height="350" :options="candleOptions" :series="candleSeries"></apexchart>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
// Importamos html2pdf para la exportación a PDF
import html2pdf from 'html2pdf.js'

const availableSymbols = ref([])
const selectedSymbol = ref('')
const smaWindow = ref(20)

const loadingMatrix = ref(false)
const loadingCandles = ref(false)
const pdfContent = ref(null)

const symbolNames = reactive(new Map([
  ["AAPL", "APPLE"], ["IBM", "IBM"], ["GC=F", "GOLD"], ["NTDOY", "NINTENDO"],
  ["AMZN", "AMAZON"], ["TSLA", "TESLA"], ["MU", "MICRON"], ["BTC-USD", "BITCOIN"],
  ["MSFT", "MICROSOFT"], ["EC", "ECOPETROL"], ["^N225", "NIKKEI"],
  ["^DJI", "Dow Jones"], ["^NYA", "NYSE"], ["^GSPC", "S&P 500"],
  ["META", "META"], ["NVDA", "NVIDIA"], ["NU", "NU"]
]))

// --- Variables para el Heatmap ---
const heatmapSeries = ref([])
const heatmapOptions = ref({
  chart: { type: 'heatmap', toolbar: { show: false } },
  dataLabels: { enabled: true, style: { fontSize: '10px' } },
  colors: ['#008FFB'], // ApexCharts gestiona los rangos de color basado en este base
  title: { text: '' },
  xaxis: { type: 'category' }
})

// --- Variables para Candlestick + SMA ---
const candleSeries = ref([])
const candleOptions = ref({
  chart: {
    type: 'line',
    toolbar: { show: true, tools: { download: false } },
    animations: { enabled: false } // Desactivar para rendimiento con muchos datos
  },
  title: { text: '', align: 'left' },
  stroke: { width: [2, 1], curve: 'straight' }, // [Grosor SMA, Grosor Velas (borde)]
  xaxis: { type: 'datetime' },
  yaxis: {
    tooltip: { enabled: true },
    labels: { formatter: (value) => value.toFixed(2) }
  },
  colors: ['#EF3E42', '#005A9C'], // Rojo para SMA, Azul para las velas
  tooltip: { shared: true, custom: [function({seriesIndex, dataPointIndex, w}) { return w.globals.series[seriesIndex][dataPointIndex] }] }
})

onMounted(async () => {
  try {
    const response = await axios.get('https://projectoavanzada-production.up.railway.app/api/assets/symbols')
    availableSymbols.value = response.data

    if (availableSymbols.value.length > 0) {
      selectedSymbol.value = availableSymbols.value[0]
      // Para no saturar el heatmap, usamos solo los primeros 10 símbolos
      const subsetSymbols = availableSymbols.value.slice(0, 10)
      fetchMatrix(subsetSymbols)
      fetchCandleData()
    }
  } catch (error) { console.error("Error inicializando:", error) }
})

const fetchMatrix = async (symbolsArray) => {
  loadingMatrix.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/similarity/matrix', symbolsArray)
    const matrixData = res.data

    const series = []
    // Convertir el Map de Java al formato de ApexCharts
    for (const symA of symbolsArray) {
      const rowData = []
      for (const symB of symbolsArray) {
        // Redondear a 2 decimales para que el heatmap sea legible
        rowData.push({ x: symbolNames.get(symB) || symB, y: Number(matrixData[symA][symB].toFixed(2)) })
      }
      series.push({ name: symbolNames.get(symA) || symA, data: rowData })
    }

    heatmapSeries.value = series
  } catch (err) { console.error("Error en matriz:", err) }
  finally { loadingMatrix.value = false }
}

const fetchCandleData = async () => {
  if (!selectedSymbol.value) return
  loadingCandles.value = true
  try {
    // Traemos los datos históricos crudos y la SMA precalculada en Java
    const [rawRes, smaRes] = await Promise.all([
      axios.get(`https://projectoavanzada-production.up.railway.app/api/assets/${selectedSymbol.value}`),
      axios.get(`https://projectoavanzada-production.up.railway.app/api/analysis/sma/${selectedSymbol.value}?window=${smaWindow.value}`)
    ])

    const rawData = rawRes.data
    const smaData = smaRes.data

    const candleFormat = []
    const lineFormat = []

    // Restringimos a los últimos 100 días para que las velas se vean bien
    const limit = 100
    const startIdx = Math.max(0, rawData.length - limit)

    for (let i = startIdx; i < rawData.length; i++) {
      const point = rawData[i]
      const timestamp = new Date(point.date).getTime()

      // Formato para Candlestick: [O, H, L, C]
      candleFormat.push({
        x: timestamp,
        y: [point.open, point.high, point.low, point.close]
      })

      // Formato para Linea SMA
      lineFormat.push({
        x: timestamp,
        y: smaData[i]
      })
    }

    candleSeries.value = [
      { name: `SMA (${smaWindow.value})`, type: 'line', data: lineFormat },
      { name: 'Precio', type: 'candlestick', data: candleFormat }
    ]

  } catch (err) { console.error("Error en velas:", err) }
  finally { loadingCandles.value = false }
}

// Función para exportar a PDF (Requerimiento 4.3)
const exportToPDF = () => {
  const element = pdfContent.value
  const opt = {
    margin:       10,
    filename:     `Reporte_Bursatil_${new Date().toISOString().slice(0,10)}.pdf`,
    image:        { type: 'jpeg', quality: 0.98 },
    html2canvas:  { scale: 2 },
    jsPDF:        { unit: 'mm', format: 'a4', orientation: 'landscape' }
  }
  html2pdf().set(opt).from(element).save()
}
</script>

<style scoped>
.dashboard-container { width: 100%; }

.tech-title { color: var(--dodger-blue); text-transform: uppercase; letter-spacing: 2px; margin-top: 0; border-bottom: 2px solid rgba(0, 90, 156, 0.1); padding-bottom: 10px;}
.tech-description { color: var(--dodger-silver); margin-bottom: 20px; font-size: 0.95rem; }
.sub-title { color: var(--dodger-blue); margin-top: 0; text-transform: uppercase; font-size: 1rem; letter-spacing: 1px;}

.actions-panel { display: flex; justify-content: flex-end; margin-bottom: 20px; }
.run-btn { background-color: var(--dodger-blue); color: white; border: none; padding: 12px 24px; border-radius: 4px; cursor: pointer; font-weight: 700; letter-spacing: 1px; transition: all 0.3s ease; }
.pdf-btn { background-color: #27ae60; }
.pdf-btn:hover { background-color: #2ecc71; transform: translateY(-2px); box-shadow: 0 4px 10px rgba(46, 204, 113, 0.4); }

.split-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

.inner-panel { background-color: rgba(255, 255, 255, 0.4); border: 1px solid rgba(162, 170, 173, 0.3); border-radius: 8px; padding: 20px; }
.metric-hint { font-size: 0.8rem; color: #7f8c8d; margin: 0 0 15px 0; }

.controls-panel { display: flex; flex-direction: column; margin-bottom: 15px; }
.tech-label { font-weight: 600; font-size: 0.85rem; text-transform: uppercase; margin-bottom: 5px; color: var(--text-dark);}
.tech-select { padding: 10px; border-radius: 4px; border: 1px solid var(--dodger-blue); background-color: transparent; font-family: 'Exo 2', sans-serif; font-weight: 600; color: var(--dodger-blue); outline: none; }
.full-width { width: 100%; box-sizing: border-box; }
.tech-select:focus { box-shadow: 0 0 8px var(--blue-glow); }

.chart-wrapper { width: 100%; }
.heatmap-wrapper { overflow-x: auto; }

.pulse-text { animation: parpadeo 1.5s infinite; color: var(--dodger-blue); font-weight: bold; }
.loading { text-align: center; padding: 40px; }

@media (max-width: 1000px) {
  .split-layout { grid-template-columns: 1fr; }
}
</style>