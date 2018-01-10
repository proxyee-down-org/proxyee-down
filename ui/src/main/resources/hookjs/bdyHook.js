var pfHook = function () {
  return 'GYun';
};
var uaHook = function () {
  return 'Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36';
};
if (Object.defineProperty) {
  Object.defineProperty(navigator, 'platform', {get: pfHook,configurable:true});
  Object.defineProperty(navigator, 'userAgent', {get: uaHook,configurable:true});
} else if (Object.prototype.__defineGetter__) {
  navigator.__defineGetter__('platform', pfHook);
  navigator.__defineGetter__('userAgent', uaHook);
}
