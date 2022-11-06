const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer:{
    allowedHosts:'all',
    proxy:{
      '/api':{
        target: "http://localhost:8095",
        changeOrigin: true,
        pathRewrite:{
          '/api': ''
        }
      }
    }
  }
})
