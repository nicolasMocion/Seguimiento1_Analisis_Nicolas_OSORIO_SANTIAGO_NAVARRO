<template>
  <div class="chart-container">
    <h2>Histórico de Precio de Cierre - {{ symbol }}</h2>

    <div v-if="loading" class="loading">
      Cargando miles de registros desde Spring Boot...
    </div>

    <div v-else class="chart-wrapper">
      <Line :data="chartData" :options="chartOptions" />
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

// Registrar los componentes de Chart.js
ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
)

const symbol = ref('IBM')
const loading = ref(true)

// Estructura reactiva que Chart.js necesita
const chartData = ref({
  labels: [], // Aquí irán las fechas (Eje X)
  datasets: [
    {
      label: 'Precio de Cierre (USD)',
      backgroundColor: '#42b883',
      borderColor: '#35495e',
      borderWidth: 1,
      pointRadius: 0, // Ocultamos los puntos porque son 5 años de datos (muchos puntos)
      data: [] // Aquí irán los precios (Eje Y)
    }
  ]
})

// Opciones de configuración del gráfico
const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top',
    }
  },
  scales: {
    x: {
      ticks: {
        maxTicksLimit: 20 // Para no saturar el eje X con 1200 fechas
      }
    }
  }
})

// Al montar el componente, llamamos a nuestro backend en Java
onMounted(async () => {
  try {
    // Petición al endpoint que acabamos de crear en Spring Boot
    const response = await axios.get(`http://localhost:8080/api/assets/${symbol.value}`)
    const data = response.data

    // Transformamos el JSON de Java a los arrays que necesita Chart.js
    chartData.value.labels = data.map(asset => asset.date)
    chartData.value.datasets[0].data = data.map(asset => asset.close)

  } catch (error) {
    console.error("Error al obtener los datos:", error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.chart-container {
  width: 80vw;
  margin: 0 auto;
  text-align: center;
  font-family: Arial, sans-serif;
}
.chart-wrapper {
  height: 60vh;
  margin-top: 20px;
}
.loading {
  font-size: 1.2rem;
  color: #666;
  margin-top: 50px;
}
</style>