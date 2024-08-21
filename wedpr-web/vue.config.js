const { defineConfig } = require('@vue/cli-service')
const NodePolyfillPlugin = require('node-polyfill-webpack-plugin')
const path = require('path') // 引入path模块
function resolve(dir) {
  return path.join(__dirname, dir) // path.join(__dirname)设置绝对路径
}

module.exports = defineConfig({
  transpileDependencies: true,
  parallel: true,
  publicPath: './',
  configureWebpack: (config) => {
    // 调试JS
    process.env.NODE_ENV !== 'production' && (config.devtool = 'source-map')
    // config.plugins = [new NodePolyfillPlugin()]
  },
  css: {
    loaderOptions: {
      sass: {
        // 注意： sass-loader v8 以上版本，这个选项名是 prependData
        //       sass-loader v8 以下版本，这个选项名是 additionalData
        additionalData: "$--font-path: '~element-ui/lib/theme-chalk/fonts'; @import './src/assets/style/variables.scss';"
      }
    }
  },
  devServer: {
    proxy: {
      '/api': {
        target: '',
        secure: false,
        changeOrigin: true
      }
    },
    host: '127.0.0.1',
    allowedHosts: 'all',
    port: 3000
  },
  chainWebpack: (config) => {
    config.resolve.alias
      .set('@', resolve('src'))
      .set('Components', resolve('.src/components'))
      .set('Assets', resolve('src/assets'))
      .set('Api', resolve('./src/apis'))
      .set('Utils', resolve('./src/utils'))
      .set('Mixin', resolve('./src/mixin'))
      .set('Store', resolve('./src/store'))
  }
})
