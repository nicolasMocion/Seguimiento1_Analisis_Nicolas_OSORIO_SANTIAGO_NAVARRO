<template>
  <div class="chart-container">
    <h2>Comparativa Histórica de Precios de Cierre</h2>

    <div class="controls-panel">
      <h3>Selecciona los activos a comparar:</h3>

      <div v-if="loadingSymbols" class="loading-small">
        Buscando acciones disponibles en la base de datos...
      </div>

      <div v-else class="checkbox-group">
        <label v-for="sym in availableSymbols" :key="sym" class="checkbox-label">
          <input
              type="checkbox"
              :value="sym"
              v-model="selectedSymbols"
              @change="updateChart"
          />
          {{ sym }}
        </label>
      </div>
    </div>

    <div v-if="loadingData" class="loading">
      Cargando registros históricos...
    </div>

    <div v-else class="chart-wrapper">
      <Line v-if="chartData.datasets.length > 0" :data="chartData" :options="chartOptions" />
      <div v-else class="empty-state">
        Selecciona al menos una acción arriba para visualizar la gráfica.
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
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { Line } from 'vue-chartjs'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

// Estados reactivos
const availableSymbols = ref([])  // Todos los símbolos en la base de datos
const selectedSymbols = ref([])   // Los símbolos que el usuario tiene marcados
const loadingSymbols = ref(true)
const loadingData = ref(false)

const chartData = ref({ labels: [], datasets: [] })

const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'top' } },
  scales: { x: { ticks: { maxTicksLimit: 20 } } },
  animation: { duration: 400 } // Animación rápida y suave
})

// Función para generar un color único y constante basado en el nombre del símbolo
const stringToColor = (str) => {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  let color = '#'
  for (let i = 0; i < 3; i++) {
    const value = (hash >> (i * 8)) & 0xFF
    color += ('00' + value.toString(16)).substr(-2)
  }
  return color
}

// 1. Al cargar la página, solo consultamos QUÉ acciones existen
onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/assets/symbols')
    availableSymbols.value = response.data

    // Si hay datos, seleccionamos automáticamente el primero para que el gráfico no inicie en blanco
    if (availableSymbols.value.length > 0) {
      selectedSymbols.value.push(availableSymbols.value[0])
      await updateChart()
    }
  } catch (error) {
    console.error("Error al obtener la lista de símbolos:", error)
  } finally {
    loadingSymbols.value = false
  }
})

// 2. Esta función se ejecuta cada vez que el usuario marca o desmarca una casilla
const updateChart = async () => {
  // Si no hay nada seleccionado, vaciamos el gráfico
  if (selectedSymbols.value.length === 0) {
    chartData.value = { labels: [], datasets: [] }
    return
  }

  loadingData.value = true

  try {
    // Preparamos peticiones solo para las acciones seleccionadas
    const requests = selectedSymbols.value.map(sym =>
        axios.get(`http://localhost:8080/api/assets/${sym}`)
    )
    const responses = await Promise.all(requests)

    // Eje X: Usamos las fechas de la primera acción consultada
    const newLabels = responses[0].data.map(asset => asset.date)
    const newDatasets = []

    // Eje Y: Iteramos las respuestas y armamos las líneas
    responses.forEach((response, index) => {
      const assetData = response.data
      const currentSymbol = selectedSymbols.value[index]
      const color = stringToColor(currentSymbol) // El color siempre será el mismo para ese símbolo

      newDatasets.push({
        label: currentSymbol,
        backgroundColor: color,
        borderColor: color,
        borderWidth: 2,
        pointRadius: 0, // 0 para no saturar con 1200 puntos
        data: assetData.map(asset => asset.close)
      })
    })

    // Actualizamos el gráfico de golpe para que Vue sea reactivo
    chartData.value = {
      labels: newLabels,
      datasets: newDatasets
    }

  } catch (error) {
    console.error("Error al obtener el histórico de precios:", error)
  } finally {
    loadingData.value = false
  }
}
</script>

<style scoped>
.chart-container {
  width: 90vw;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Estilos para el panel de control */
.controls-panel {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 15px 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  border: 1px solid #e1e4e8;
}

.controls-panel h3 {
  margin-top: 0;
  font-size: 1rem;
  color: #2c3e50;
  margin-bottom: 15px;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 500;
  color: #34495e;
  cursor: pointer;
  user-select: none;
  background-color: #f8f9fa;
  padding: 5px 10px;
  border-radius: 4px;
  border: 1px solid #ddd;
  transition: background-color 0.2s;
}

.checkbox-label:hover {
  background-color: #e9ecef;
}

.chart-wrapper {
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  border: 1px solid #e1e4e8;
  height: 60vh;
}

.loading, .loading-small {
  text-align: center;
  color: #7f8c8d;
  font-weight: 500;
}

.loading {
  margin-top: 50px;
  font-size: 1.2rem;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #95a5a6;
  font-size: 1.1rem;
}
</style>