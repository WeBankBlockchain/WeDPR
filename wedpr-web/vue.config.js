const { defineConfig } = require('@vue/cli-service')
// const NodePolyfillPlugin = require('node-polyfill-webpack-plugin')
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin')

const path = require('path') // 引入path模块
function resolve(dir) {
  return path.join(__dirname, dir) // path.join(__dirname)设置绝对路径
}

module.exports = defineConfig({
  outputDir: process.env.VUE_APP_MODE === 'agency' ? 'dist' : 'manage',
  transpileDependencies: true,
  parallel: true,
  publicPath: './',
  configureWebpack: (config) => {
    // 调试JS
    process.env.NODE_ENV !== 'production' && (config.devtool = 'source-map')
    //  = [new MonacoWebpackPlugin()]
    config.plugins.push(
      new MonacoWebpackPlugin({
        languages: ['python', 'sql']
      })
    )
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
        // target: 'http://apidesign.weoa.com/apidesign-core/mock/18220',
        // target: 'http://139.159.202.235:13000',
        target: 'http://139.159.202.235:8005',
        // target: 'http://139.159.202.235:8016',
        // target: 'http://139.159.202.235:6855',
        // target: 'http://175.178.109.119:5810',
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
