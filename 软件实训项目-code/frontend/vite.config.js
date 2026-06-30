import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
// Vite 配置 - API 代理到后端 8080 端口
export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    },
    server: {
        port: 5173,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
});
