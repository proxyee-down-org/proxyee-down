//1.0
if (self.fetch) {
  var _proxyee_down_oriFetch = window.fetch;
  Object.defineProperty(window, 'fetch', {
    get: function () {
      return function () {
        console.log('hook fetch:' + arguments[0]);
        return _proxyee_down_oriFetch.apply(this, arguments);
      };
    }, configurable: true
  });
  var _proxyee_down_oriThen = Promise.prototype.then;
  Promise.prototype.then = function (callback) {
    return _proxyee_down_oriThen.apply(this, callback && [function () {
      if (arguments[0] && arguments[0].constructor === Response) {
        console.log(arguments[0]);
      }
      return callback.apply(this, arguments);
    }]);
  };
}
if (XMLHttpRequest in Window) {
  var _proxyee_down_oriOpen = XMLHttpRequest.prototype.open;
  var _proxyee_down_oriSend = XMLHttpRequest.prototype.send;
  XMLHttpRequest.prototype.open = function () {
    this._uri = arguments[1];
    return _proxyee_down_oriOpen.apply(this, arguments);
  };
  XMLHttpRequest.prototype.send = function () {
    return _proxyee_down_oriSend.apply(this, arguments);
  };
}
