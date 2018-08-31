(function (ajax) {
  var API_PORT = '${apiPort}'
  var FRONT_PORT = '${frontPort}'
  var REST_PORT = '26339'
  window.pdown = {
    resolve: function (request) {
      return ajax.put('http://127.0.0.1:' + REST_PORT + '/util/resolve', request)
    },
    resolveAsync: function (request, onSucc, onErr) {
      ajax.putAsync('http://127.0.0.1:' + REST_PORT + '/util/resolve', request, onSucc, onErr)
    },
    createTask: function (request, response) {
      var requestStr = encodeURIComponent(JSON.stringify(request))
      var responseStr = encodeURIComponent(JSON.stringify(response))
      if ('${uiMode}' == '1') {
        ajax.get('http://127.0.0.1:' + API_PORT + '/api/createTask?request=' + requestStr + '&response=' + responseStr)
      } else {
        window.open('http://127.0.0.1:' + FRONT_PORT + '/#/tasks?request=' + requestStr + '&response=' + responseStr)
      }
    },
    pushTask: function (taskForm, onSucc, onErr) {
      ajax.postAsync('http://127.0.0.1:' + REST_PORT + '/tasks', taskForm, onSucc, onErr)
    },
    getDownConfig: function () {
      var config = ajax.get('http://127.0.0.1:' + REST_PORT + '/config')
      delete config['port']
      delete config['proxyConfig']
      delete config['speedLimit']
      delete config['taskLimit']
      delete config['totalSpeedLimit']
      return config
    },
    getCookie: function (url) {
      var cookie = ''
      var xhr = ajax.buildXHR()
      xhr.withCredentials = true
      xhr.open('get', url, false)
      //https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Access_control_CORS#%E7%AE%80%E5%8D%95%E8%AF%B7%E6%B1%82
      xhr.setRequestHeader('Accept', 'application/x-sniff-cookie,*/*;q=0.8')
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            cookie = xhr.getResponseHeader('X-Sniff-Cookie')
          }
        }
      }
      xhr.send()
      return cookie
    }
  }
})((function () {
  return {
    buildXHR: function () {
      var xhr = null
      if (window.XMLHttpRequest) {
        xhr = new XMLHttpRequest()
      } else {
        xhr = new ActiveXObject('Microsoft.XMLHTTP')
      }
      return xhr
    },
    get: function (url) {
      return this.send('get', url)
    },
    jsonSend: function (method, url, data) {
      return this.send(method, url, 'application/json; charset=utf-8', data)
    },
    jsonSendAsync: function (method, url, data, onSucc, onErr) {
      this.sendAsync(method, url, 'application/json; charset=utf-8', data, onSucc, onErr)
    },
    post: function (url, data) {
      return this.jsonSend('post', url, data)
    },
    postAsync: function (url, data, onSucc, onErr) {
      this.jsonSendAsync('post', url, data, onSucc, onErr)
    },
    put: function (url, data) {
      return this.jsonSend('put', url, data)
    },
    putAsync: function (url, data, onSucc, onErr) {
      this.jsonSendAsync('put', url, data, onSucc, onErr)
    },
    send: function (method, url, contentType, data) {
      var result = null
      var error = true
      var xhr = this.buildXHR()
      xhr.open(method, url, false)
      if (contentType) {
        xhr.setRequestHeader('Content-Type', contentType)
      }
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            error = false
            result = xhr.responseText ? JSON.parse(xhr.responseText) : {}
          }
        }
      }
      xhr.send(data ? JSON.stringify(data) : null)
      if (error) {
        throw xhr
      } else {
        return result
      }
    },
    sendAsync: function (method, url, contentType, data, onSucc, onErr) {
      var xhr = this.buildXHR()
      xhr.open(method, url)
      if (contentType) {
        xhr.setRequestHeader('Content-Type', contentType)
      }
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            if (onSucc) {
              onSucc(xhr.responseText ? JSON.parse(xhr.responseText) : {})
            }
          } else {
            if (onErr) {
              onErr(xhr)
            }
          }
        }
      }
      xhr.send(data ? JSON.stringify(data) : null)
    }
  }
})())