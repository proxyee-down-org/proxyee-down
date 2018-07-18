console.log(1);

/**
 * 获取当前系统语言
 * Get the current system language
 * @return {String}
 */
const getLanguage = () => {
  let language = navigator.browserLanguage || navigator.language;
  return language.indexOf("zh") !== -1 ? "zh" : "en";
};

module.exports = {
  getLanguage
};
