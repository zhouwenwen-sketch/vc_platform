import App from './App'
import uviewPlus from 'uview-plus'
import { THEME_PRIMARY } from '@/utils/theme.js'

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)
  app.use(uviewPlus)
  uni.$u.setConfig({
    color: {
      primary: THEME_PRIMARY
    },
    config: {
      color: {
        'u-primary': THEME_PRIMARY
      }
    }
  })
  return {
    app
  }
}
// #endif
