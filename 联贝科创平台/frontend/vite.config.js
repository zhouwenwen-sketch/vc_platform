import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

export default defineConfig({
  plugins: [uni()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 注入 uView 变量与 mixin，解决组件内 $u-primary、@include flex 未定义
        additionalData: `
          @import "uview-plus/theme.scss";
          @import "uview-plus/libs/css/mixin.scss";
          @import "@/styles/uview-overrides.scss";
        `
      }
    }
  },
  optimizeDeps: {
    // 避免 vite 预构建 uview-plus 导致缓存路径异常
    exclude: ['uview-plus']
  }
})
