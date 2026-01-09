import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 禁用所有图片的右键菜单
app.directive('disable-right-click', {
  mounted(el: HTMLElement) {
    const images = el.querySelectorAll('img')
    images.forEach((img) => {
      img.addEventListener('contextmenu', (e) => {
        e.preventDefault()
        return false
      })
    })
  },
  updated(el: HTMLElement) {
    const images = el.querySelectorAll('img')
    images.forEach((img) => {
      img.addEventListener('contextmenu', (e) => {
        e.preventDefault()
        return false
      })
    })
  }
})

// 全局禁用图片右键菜单
document.addEventListener('contextmenu', (e) => {
  if (e.target instanceof HTMLImageElement) {
    e.preventDefault()
    return false
  }
  // 也处理Element Plus的el-image组件
  const target = e.target as HTMLElement
  if (target && (target.classList.contains('el-image__inner') || target.closest('.el-image'))) {
    e.preventDefault()
    return false
  }
})

app.mount('#app')
