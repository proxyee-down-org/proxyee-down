module.exports = {
  productionSourceMap: true, // 生产环境不生成source map
  css: {
    sourceMap: false // css不生成source map
  },
  chainWebpack: config => {
    config.module
      .rule("vue")
      .test(/\.vue$/)
      .use("iview-loader")
      .loader("iview-loader")
      .options({
        prefix: true
      });
  }
};
