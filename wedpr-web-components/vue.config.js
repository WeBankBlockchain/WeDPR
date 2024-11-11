const { defineConfig } = require("@vue/cli-service");
const path = require("path");
module.exports = defineConfig({
  transpileDependencies: false, // 默认情况下 babel-loader 会忽略所有 node_modules 中的文件。你可以启用本选项，以避免构建后的代码中出现未转译的第三方依赖。
  publicPath: "./", // 部署应用包时的基本URL
  outputDir: "dist", // 当运行 vue-cli-service build 时生成的生产环境构建文件的目录。
  assetsDir: "static", // 放置生成的静态资源 (js、css、img、fonts) 的 (相对于 outputDir 的) 目录
  indexPath: "index.html", // 设置index.html
  filenameHashing: true, // 设置打包生成的静态资源的文件名中是否加入hash以便控制浏览器缓存问题
  productionSourceMap: false, // 设置生产环境的 source map 开启与关闭
  devServer: {
    open: true, // npm run dev启动项目后是否自动打开浏览器
    host: "localhost", // 匹配本机IP地址(默认是0.0.0.0或localhost)
    port: 8080, // 端口号    ,默认:8080
    hot: true, // 是否开启热更新(局部刷新,不刷新整个页面)
    https: false, // 是否启用https
    compress: true, // 是否启动gzip压缩,用于减少服务器向前端传输的数据量,提高浏览的速度
  },
  // 扩展 webpack 配置，使 packages 加入编译
  chainWebpack: (config) => {
    config.module
      .rule("js")
      .include.add(__dirname + "packages")
      .end()
      .use("babel")
      .loader("babel-loader")
      .tap((options) => {
        // 修改它的选项...
        return options;
      });
  },
});
