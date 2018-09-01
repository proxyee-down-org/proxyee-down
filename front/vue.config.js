module.exports = {
  productionSourceMap: true, // Production environment does not generate source-map
  css: {
    sourceMap: false // CSS does not generate source-map
  },
  outputDir: '../main/src/main/resources/http',
  devServer: {
    proxy: {
      '/native': {
        target: 'http://127.0.0.1:7478',
        changeOrigin: true
      },
      '/pac': {
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
