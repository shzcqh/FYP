import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import '@/assets/css/global.css'
import { use } from 'echarts/core'
import VChart from 'vue-echarts'
// 渲染器
import { CanvasRenderer } from 'echarts/renderers'
// 常用图表类型
import { BarChart, LineChart, PieChart } from 'echarts/charts'
// 常用组件
import {
    TitleComponent,
    TooltipComponent,
    LegendComponent,
    GridComponent,
} from 'echarts/components'

// 把渲染器、图表、组件都注入给 echarts
use([
    CanvasRenderer,
    BarChart,
    LineChart,
    PieChart,
    TitleComponent,
    TooltipComponent,
    LegendComponent,
    GridComponent,
])
const app = createApp(App)

app.use(router)
app.use(ElementPlus, {
    locale: zhCn,
})


for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
// 全局注册 vue-echarts 组件，之后任何地方都可以直接用 <v-chart>
app.component('VChart', VChart)

// 挂载
app.mount('#app')