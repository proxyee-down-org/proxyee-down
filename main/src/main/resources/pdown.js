(function (ajax) {
  window.pdown = {
    createTask: function (request, response) {
      var requestStr = encodeURIComponent(JSON.stringify(request))
      var responseStr = encodeURIComponent(JSON.stringify(response))
      if ('${uiMode}' == '1') {
        ajax('/createTask?request=' + requestStr + '&response=' + responseStr)
      } else {
        window.open('http://127.0.0.1:${frontPort}/#/tasks?request=' + requestStr + '&response=' + responseStr)
      }
    }
  }
})(function (uri) {
  var xhr = null
  if (window.XMLHttpRequest) {
    xhr = new XMLHttpRequest()
  } else {
    xhr = new ActiveXObject("Microsoft.XMLHTTP")
  }
  xhr.open('get', 'http://127.0.0.1:${apiPort}/api' + uri)
  xhr.send()
})