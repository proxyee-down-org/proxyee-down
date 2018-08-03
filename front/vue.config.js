module.exports = {
  productionSourceMap: true, // 生产环境不生成source map
  css: {
    sourceMap: false // css不生成source map
  },
  devServer: {
    proxy: {
      '/native': {
        target: 'http://127.0.0.1:7478',
        changeOrigin: true
      }
    }
  },
  chainWebpack: config => {
    config.module
      .rule('vue')
      .test(/\.vue$/)
      .use('iview-loader')
      .loader('iview-loader')
      .options({
        prefix: true
      })
  }
}
