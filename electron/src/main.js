import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'

// 引入ECharts组件（如果使用）
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
use([LineChart, GridComponent, TooltipComponent, LegendComponent])

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')