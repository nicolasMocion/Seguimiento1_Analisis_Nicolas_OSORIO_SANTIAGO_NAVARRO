<template>
  <div class="top-volume-container">
    <h2 class="tech-title">Top 15 Días de Mayor Volumen Transaccional</h2>

    <div class="controls-panel inner-panel">
      <label for="symbolSelectVolume" class="tech-label">Selecciona la acción: </label>
      <select id="symbolSelectVolume" v-model="selectedSymbol" @change="fetchTopVolume" :disabled="loading" class="tech-select">
        <option v-for="sym in availableSymbols" :key="sym" :value="sym">
          {{ symbolNames.get(sym) }}
        </option>
      </select>
    </div>

    <div v-if="loading" class="loading pulse-text">
      Analizando histórico y aislando el Top 15...
    </div>

    <div v-else-if="topAssets.length > 0" class="table-container inner-panel">
      <table class="tech-table">
        <thead>
        <tr>
          <th>RANKING</th>
          <th>FECHA</th>
          <th>VOLUMEN (ACCIONES)</th>
          <th>PRECIO CIERRE (USD)</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(asset, index) in topAssets" :key="asset.date" :class="{'top-podium': index <= 3}">
          <td class="rank-cell">
            <span v-if="index === 0" class="medal gold"> #1</span>
            <span v-else-if="index === 1" class="medal silver"> #2</span>
            <span v-else-if="index === 2" class="medal bronce"> #3</span>
            <span v-else>#{{ index + 1 }}</span>
          </td>
          <td class="date-cell">{{ asset.date }}</td>
          <td class="volume-cell">{{ formatNumber(asset.volume) }}</td>
          <td class="price-cell">${{ asset.close.toFixed(2) }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, reactive} from 'vue'
import axios from 'axios'

const availableSymbols = ref([])
const selectedSymbol = ref('')
const topAssets = ref([])
const loading = ref(false)
const symbolNames = reactive(new Map([
  ["AAPL", "APPLE"], ["IBM", "IBM"], ["GC=F", "GOLD"], ["NTDOY", "NINTENDO"],
  ["AMZN", "AMAZON"], ["TSLA", "TESLA"], ["MU", "MICRON"], ["BTC-USD", "BITCOIN"],
  ["MSFT", "MICROSOFT"], ["EC", "ECOPETROL"], ["^N225", "NIKKEI 225"],
  ["^DJI", "Dow Jones"], ["^NYA", "NYSE Composite Index"], ["^GSPC", "S&P 500"],
  ["META", "META"], ["NVDA", "NVIDIA"], ["NU", "NU"],
]))

const formatNumber = (num) => new Intl.NumberFormat('en-US').format(num)

onMounted(async () => {
  try {
    const response = await axios.get('seguimiento1analisisnicolasosoriosantiagona-production-d160.up.railway.app/api/assets/symbols')
    availableSymbols.value = response.data
    if (availableSymbols.value.length > 0) {
      selectedSymbol.value = availableSymbols.value[0]
      await fetchTopVolume()
    }
  } catch (error) { console.error("Error al cargar símbolos:", error) }
})

const fetchTopVolume = async () => {
  if (!selectedSymbol.value) return
  loading.value = true
  try {
    const response = await axios.get(`seguimiento1analisisnicolasosoriosantiagona-production-d160.up.railway.app/api/assets/${selectedSymbol.value}/top-volume`)
    topAssets.value = response.data
  } catch (error) { console.error("Error al obtener el Top 15:", error) }
  finally { loading.value = false }
}
</script>

<style scoped>
.top-volume-container { width: 100%; }

.tech-title { color: var(--dodger-blue); text-transform: uppercase; letter-spacing: 2px; margin-top: 0; border-bottom: 2px solid rgba(0, 90, 156, 0.1); padding-bottom: 10px; }

.inner-panel { background-color: rgba(255, 255, 255, 0.4); border: 1px solid rgba(162, 170, 173, 0.3); border-radius: 8px; padding: 20px; }
.controls-panel { margin-bottom: 20px; display: flex; align-items: center; }

.tech-label { font-weight: 600; color: var(--text-dark); }
.tech-select {
  padding: 8px 12px; border-radius: 4px; border: 1px solid var(--dodger-blue); background-color: transparent;
  font-family: 'Exo 2', sans-serif; font-weight: 600; color: var(--dodger-blue); outline: none; margin-left: 10px;
}
.tech-select:focus { box-shadow: 0 0 8px var(--blue-glow); }

.table-container { padding: 0; overflow: hidden; border: 1px solid var(--dodger-blue); }

.tech-table { width: 100%; border-collapse: collapse; }
.tech-table th, .tech-table td { padding: 15px; text-align: center; border-bottom: 1px solid rgba(162, 170, 173, 0.2); }
.tech-table th { background-color: var(--dodger-blue); color: var(--dodger-white); font-weight: 700; letter-spacing: 1px; }

.tech-table tr:hover { background-color: rgba(0, 90, 156, 0.05); }

.rank-cell { font-weight: bold; color: var(--dodger-silver); }
.date-cell { font-weight: 600; color: var(--dodger-blue); }

/* Acento rojo para los volúmenes para que resalten numéricamente */
.volume-cell { font-family: monospace; font-size: 1.15rem; color: var(--dodger-red); font-weight: 700; }
.price-cell { font-weight: 600; color: var(--text-dark); }

/* Estilos de podio */
.top-podium { background-color: rgba(0, 90, 156, 0.03); }
.medal { display: inline-block; padding: 2px 8px; border-radius: 4px; color: white; text-shadow: 1px 1px 2px rgba(0,0,0,0.3); }
.gold { background: linear-gradient(135deg, #f1c40f, #f39c12); font-size: 1.1rem; }
.silver { background: linear-gradient(135deg, #bdc3c7, #95a5a6); font-size: 1rem; }
.bronce { background: linear-gradient(135deg, #cd7f32, #a0522d); font-size: 1rem; }

.pulse-text { animation: parpadeo 1.5s infinite; color: var(--dodger-blue); font-weight: bold; }
.loading { text-align: center; padding: 40px; font-size: 1.2rem; }
</style>