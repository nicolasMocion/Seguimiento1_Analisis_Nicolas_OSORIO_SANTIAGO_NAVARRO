<template>
  <div class="top-volume-container">
    <h2>Top 15 Días de Mayor Volumen Transaccional</h2>

    <div class="controls-panel">
      <label for="symbolSelectVolume">Selecciona la acción: </label>
      <select id="symbolSelectVolume" v-model="selectedSymbol" @change="fetchTopVolume" :disabled="loading">
        <option v-for="sym in availableSymbols" :key="sym" :value="sym">
          {{ sym }}
        </option>
      </select>
    </div>

    <div v-if="loading" class="loading">
      Analizando histórico y aislando el Top 15...
    </div>

    <div v-else-if="topAssets.length > 0" class="table-container">
      <table>
        <thead>
        <tr>
          <th>Ranking</th>
          <th>Fecha</th>
          <th>Volumen (Acciones)</th>
          <th>Precio Cierre (USD)</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(asset, index) in topAssets" :key="asset.date" :class="{'top-3': index < 3}">
          <td class="rank-cell">#{{ index + 1 }}</td>
          <td><strong>{{ asset.date }}</strong></td>
          <td class="volume-cell">{{ formatNumber(asset.volume) }}</td>
          <td>${{ asset.close.toFixed(2) }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const availableSymbols = ref([])
const selectedSymbol = ref('')
const topAssets = ref([])
const loading = ref(false)

// Formateador para poner comas en los millones (ej. 1,000,000)
const formatNumber = (num) => {
  return new Intl.NumberFormat('en-US').format(num)
}

onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/assets/symbols')
    availableSymbols.value = response.data
    if (availableSymbols.value.length > 0) {
      selectedSymbol.value = availableSymbols.value[0]
      await fetchTopVolume()
    }
  } catch (error) {
    console.error("Error al cargar símbolos:", error)
  }
})

const fetchTopVolume = async () => {
  if (!selectedSymbol.value) return

  loading.value = true
  try {
    const response = await axios.get(`http://localhost:8080/api/assets/${selectedSymbol.value}/top-volume`)
    topAssets.value = response.data
  } catch (error) {
    console.error("Error al obtener el Top 15:", error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.top-volume-container {
  width: 90vw;
  max-width: 1000px;
  margin: 40px auto;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.controls-panel {
  background-color: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  border: 1px solid #e1e4e8;
  margin-bottom: 20px;
}

select {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ccc;
  font-size: 1rem;
  margin-left: 10px;
}

.table-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  border: 1px solid #e1e4e8;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 15px;
  text-align: center;
  border-bottom: 1px solid #eee;
}

th {
  background-color: #2c3e50;
  color: white;
  font-weight: 600;
}

.rank-cell {
  font-weight: bold;
  color: #7f8c8d;
}

.volume-cell {
  font-family: monospace;
  font-size: 1.1rem;
  color: #27ae60;
  font-weight: bold;
}

/* Resaltar visualmente el podio (Top 3) */
.top-3 {
  background-color: #fcf8e3;
}
.top-3:nth-child(1) .rank-cell { color: #f1c40f; font-size: 1.2rem; } /* Oro */
.top-3:nth-child(2) .rank-cell { color: #bdc3c7; font-size: 1.1rem; } /* Plata */
.top-3:nth-child(3) .rank-cell { color: #cd7f32; font-size: 1.1rem; } /* Bronce */

.loading {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
  font-size: 1.2rem;
}
</style>