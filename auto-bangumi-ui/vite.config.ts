import { fileURLToPath, URL } from 'node:url';

import { ConfigEnv, defineConfig, loadEnv, UserConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import Components from 'unplugin-vue-components/vite';
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers';
import vueDevTools from 'vite-plugin-vue-devtools';
import viteCompression from 'vite-plugin-compression';

// https://vite.dev/config/
export default defineConfig(({ mode }: ConfigEnv): UserConfig => {
  const root = process.cwd();
  const env = loadEnv(mode, root);
  return {
    plugins: [
      vue(),
      vueDevTools(),
      Components({
        resolvers: [
          AntDesignVueResolver({
            importStyle: false // css in js
          })
        ]
      }),
      // 配置 gzip 压缩插件
      viteCompression({
        algorithm: 'gzip', // 使用 gzip 压缩
        ext: '.gz', // 压缩文件扩展名
        threshold: 10240, // 只有大于 10 KB 的文件才会被压缩
        deleteOriginFile: false // 不删除源文件
      })
    ],
    build: {
      sourcemap: true,
      chunkSizeWarningLimit: 1024,
      rollupOptions: {
        output: {
          manualChunks(path) {
            if (path.includes('node_modules/ant-design-vue/es')) {
              return 'ant-es';
            }
            if (path.includes('node_modules/ant-design-vue')) {
              return 'ant';
            }
            if (path.includes('node_modules/vue') || path.includes('node_modules/@vue')) {
              return 'vue';
            }
            if (path.includes('node_modules/@xterm')) {
              return 'xterm';
            }
            if (path.includes('node_modules/@codemirror')) {
              return 'codemirror';
            }
          }
        }
      }
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        'package.json': new URL('package.json', import.meta.url).pathname
      }
    },
    server: {
      host: '0.0.0.0',
      port: Number(env.VITE_PORT),
      open: env.VITE_OPEN === 'true',
      proxy: {
        '/api': {
          target: env.VITE_API_URL,
          changeOrigin: true,
          rewrite: path => path.replace(new RegExp(`^/api`), ''),
          ...(/^https:\/\//.test(env.VITE_API_URL) ? { secure: false } : {})
        }
      }
    }
  };
});
