<template>
  <div class="risk-pattern-container">
    <h2 class="tech-title">Análisis de Volatilidad y Patrones (Sliding Window)</h2>
    <p class="tech-description">Clasificación algorítmica de riesgo y detección de eventos en series de tiempo.</p>

    <div class="split-layout">

      <!-- SECCIÓN IZQUIERDA: MATRIZ DE RIESGO -->
      <div class="risk-section inner-panel">
        <h3 class="sub-title">Perfil de Riesgo del Portafolio</h3>
        <p class="metric-hint">Basado en Volatilidad Histórica Anualizada (σ * √252)</p>

        <div v-if="loadingRisk" class="loading pulse-text">Calculando dispersión poblacional...</div>

        <table v-else class="tech-table">
          <thead>
          <tr>
            <th>ACTIVO</th>
            <th>DESVIACIÓN (Diaria)</th>
            <th>VOLATILIDAD (Anual)</th>
            <th>PERFIL ASIGNADO</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="risk in riskData" :key="risk.symbol">
            <td class="bold-text" style="color: var(--dodger-blue)">{{ symbolNames.get(risk.symbol) || risk.symbol }}</td>
            <td>{{ (risk.dailyStdDev * 100).toFixed(2) }}%</td>
            <td class="bold-text">{{ (risk.annualizedVolatility * 100).toFixed(2) }}%</td>
            <td>
                <span class="risk-badge" :class="getRiskClass(risk.riskProfile)">
                  {{ risk.riskProfile }}
                </span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- SECCIÓN DERECHA: VENTANA DESLIZANTE -->
      <div class="pattern-section inner-panel">
        <h3 class="sub-title">Buscador de Patrones (O(N))</h3>

        <div class="controls-panel">
          <label class="tech-label">Buscar en Activo:</label>
          <select v-model="selectedSymbol" @change="fetchPatterns" class="tech-select full-width">
            <option v-for="sym in availableSymbols" :key="sym" :value="sym">{{ symbolNames.get(sym) || sym }}</option>
          </select>

          <label class="tech-label" style="margin-top:15px;">Días para Racha Alcista:</label>
          <input type="number" v-model="windowSize" @change="fetchPatterns" min="2" max="10" class="tech-select full-width" />
        </div>

        <div v-if="loadingPattern" class="loading pulse-text">Deslizando ventana...</div>

        <div v-else class="patterns-grid">
          <div v-for="pattern in patternData" :key="pattern.patternName" class="pattern-card">
            <h4>{{ pattern.patternName }}</h4>
            <div class="pattern-count">{{ pattern.occurrences }} <span>veces detectado</span></div>
            <p class="pattern-desc">{{ pattern.description }}</p>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'

const availableSymbols = ref([])
const selectedSymbol = ref('')
const windowSize = ref(3)

const riskData = ref([])
const patternData = ref([])

const loadingRisk = ref(false)
const loadingPattern = ref(false)

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

    if (availableSymbols.value.length > 0) {
      selectedSymbol.value = availableSymbols.value[0]
      // Enviar todos los símbolos para el análisis masivo de riesgo
      fetchRiskProfile(availableSymbols.value)
      fetchPatterns()
    }
  } catch (error) { console.error("Error inicializando:", error) }
})

const fetchRiskProfile = async (symbolsArray) => {
  loadingRisk.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/analysis/risk-profile', symbolsArray)
    riskData.value = res.data
  } catch (err) { console.error("Error en riesgo:", err) }
  finally { loadingRisk.value = false }
}

const fetchPatterns = async () => {
  if (!selectedSymbol.value) return
  loadingPattern.value = true
  try {
    const res = await axios.get(`https://projectoavanzada-production.up.railway.app/api/analysis/patterns/${selectedSymbol.value}?days=${windowSize.value}`)
    patternData.value = res.data
  } catch (err) { console.error("Error en patrones:", err) }
  finally { loadingPattern.value = false }
}

const getRiskClass = (profile) => {
  if (profile === 'Conservador') return 'badge-green'
  if (profile === 'Moderado') return 'badge-yellow'
  return 'badge-red'
}
</script>

<style scoped>
.risk-pattern-container { width: 100%; }

.tech-title { color: var(--dodger-blue); text-transform: uppercase; letter-spacing: 2px; margin-top: 0; border-bottom: 2px solid rgba(0, 90, 156, 0.1); padding-bottom: 10px;}
.tech-description { color: var(--dodger-silver); margin-bottom: 20px; font-size: 0.95rem; }
.sub-title { color: var(--dodger-blue); margin-top: 0; text-transform: uppercase; font-size: 1rem; letter-spacing: 1px;}

.split-layout { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }

.inner-panel { background-color: rgba(255, 255, 255, 0.4); border: 1px solid rgba(162, 170, 173, 0.3); border-radius: 8px; padding: 20px; }

.tech-table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 0.9rem;}
.tech-table th, .tech-table td { padding: 10px; text-align: left; border-bottom: 1px solid rgba(162, 170, 173, 0.2); }
.tech-table th { background-color: var(--dodger-blue); color: var(--dodger-white); font-weight: 600; text-transform: uppercase; }
.tech-table tr:hover { background-color: rgba(0, 90, 156, 0.05); }

.bold-text { font-weight: bold; }
.metric-hint { font-size: 0.8rem; color: #7f8c8d; margin: 0; }

.risk-badge { padding: 4px 10px; border-radius: 12px; font-weight: bold; font-size: 0.8rem; text-transform: uppercase; color: white;}
.badge-green { background-color: #27ae60; }
.badge-yellow { background-color: #f39c12; }
.badge-red { background-color: var(--dodger-red); }

.controls-panel { display: flex; flex-direction: column; margin-bottom: 20px; }
.tech-label { font-weight: 600; font-size: 0.85rem; text-transform: uppercase; margin-bottom: 5px; color: var(--text-dark);}
.tech-select { padding: 10px; border-radius: 4px; border: 1px solid var(--dodger-blue); background-color: transparent; font-family: 'Exo 2', sans-serif; font-weight: 600; color: var(--dodger-blue); outline: none; }
.full-width { width: 100%; box-sizing: border-box; }
.tech-select:focus { box-shadow: 0 0 8px var(--blue-glow); }

.patterns-grid { display: flex; flex-direction: column; gap: 15px; }
.pattern-card { background-color: rgba(0, 90, 156, 0.03); border: 1px solid var(--dodger-blue); border-radius: 8px; padding: 15px; text-align: center; }
.pattern-card h4 { margin: 0 0 10px 0; color: var(--dodger-blue); font-size: 1.1rem; }
.pattern-count { font-size: 2.5rem; font-weight: bold; color: var(--dodger-red); font-family: monospace; }
.pattern-count span { font-size: 0.9rem; color: var(--text-dark); font-family: 'Exo 2', sans-serif; display: block;}
.pattern-desc { font-size: 0.8rem; color: var(--dodger-silver); margin: 10px 0 0 0; }

.pulse-text { animation: parpadeo 1.5s infinite; color: var(--dodger-blue); font-weight: bold; }
.loading { text-align: center; padding: 20px; }

@media (max-width: 900px) {
  .split-layout { grid-template-columns: 1fr; }
}
</style>