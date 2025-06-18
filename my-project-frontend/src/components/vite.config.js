import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

Components({
    resolvers: [
      ElementPlusResolver({
        importStyle: 'css',
        directives: true,
        version: '2.3.9' // 保持与实际版本一致
      })
    ]
  })
  
export default defineConfig({
  plugins: [
    vue(),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
})
