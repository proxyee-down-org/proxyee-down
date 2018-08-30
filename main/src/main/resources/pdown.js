(function (ajax) {
  var API_PORT = '${apiPort}'
  var FRONT_PORT = '${frontPort}'
  var REST_PORT = '26339'
  window.pdown = {
    resolveTask: function (request, onSuccess, onError) {
      ajax.send('put', 'http://127.0.0.1:' + REST_PORT + '/util/resolve', request, onSuccess, onError)
    },
    createTask: function (request, response, onSuccess, onError) {
      var requestStr = encodeURIComponent(JSON.stringify(request))
      var responseStr = encodeURIComponent(JSON.stringify(response))
      if ('${uiMode}' == '1') {
        ajax.get('http://127.0.0.1:' + API_PORT + '/api/createTask?request=' + requestStr + '&response=' + responseStr, onSuccess, onError)
      } else {
        window.open('http://127.0.0.1:' + FRONT_PORT + '/#/tasks?request=' + requestStr + '&response=' + responseStr)
      }
    },
    pushTask: function (request, response, onSuccess, onError) {
      ajax.send('post', 'http://127.0.0.1:' + REST_PORT + '/tasks', {request: request, response: response, config: {autoRename: true}}, onSuccess, onError)
    }
  }
})((function () {
  function buildXHR() {
    var xhr = null
    if (window.XMLHttpRequest) {
      xhr = new XMLHttpRequest()
    } else {
      xhr = new ActiveXObject("Microsoft.XMLHTTP")
    }
    return xhr;
  }

  return {
    get: function (url, onSuccess, onError) {
      var xhr = buildXHR();
      xhr.open('get', url)
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            if (onSuccess) {
              onSuccess(JSON.parse(xhr.responseText))
            }
          } else {
            if (onError) {
              onError(xhr)
            }
          }
        }
      }
      xhr.send()
    },
    send: function (method, url, data, onSuccess, onError) {
      var xhr = buildXHR()
      xhr.open(method, url)
      xhr.setRequestHeader('Content-Type', 'application/json; charset=utf-8')
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            onSuccess(JSON.parse(xhr.responseText))
          } else {
            onError(xhr)
          }
        }
      }
      xhr.send(JSON.stringify(data))
    }
  }
})())