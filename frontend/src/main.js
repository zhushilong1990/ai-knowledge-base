import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router/index.js'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.config.errorHandler = (err, vm, info) => {
  console.error('Global error:', err, info)
}

app.mount('#app')
