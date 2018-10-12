(function (ajax) {
  var API_PORT = '${apiPort}'
  var FRONT_PORT = '${frontPort}'
  var REST_PORT = '26339'
  ;(function (pdown) {
    ${content}
  })({
    resolve: function (request) {
      return ajax.put('http://127.0.0.1:' + REST_PORT + '/util/resolve', request)
    },
    resolveAsync: function (request, onSuccess, onError) {
      ajax.putAsync('http://127.0.0.1:' + REST_PORT + '/util/resolve', request, onSuccess, onError)
    },
    createTask: function () {
      var request
      var response
      if (arguments.length == 1) {
        var taskForm = arguments[0]
        request = taskForm.request
        response = taskForm.response
      } else if (arguments.length == 2) {
        request = arguments[0]
        response = arguments[1]
      } else {
        return
      }
      var requestStr = encodeURIComponent(JSON.stringify(request))
      var responseStr = encodeURIComponent(JSON.stringify(response))
      if ('${uiMode}' == '1') {
        ajax.get('http://127.0.0.1:' + API_PORT + '/api/createTask?request=' + requestStr + '&response=' + responseStr)
      } else {
        window.open('http://127.0.0.1:' + FRONT_PORT + '/#/tasks?request=' + requestStr + '&response=' + responseStr)
      }
    },
    createTaskAsync: function (taskForm, onSuccess, onError) {
      var requestStr = encodeURIComponent(JSON.stringify(taskForm.request))
      var responseStr = encodeURIComponent(JSON.stringify(taskForm.response))
      if ('${uiMode}' == '1') {
        ajax.getAsync('http://127.0.0.1:' + API_PORT + '/api/createTask?request=' + requestStr + '&response=' + responseStr, onSuccess, onError)
      } else {
        window.open('http://127.0.0.1:' + FRONT_PORT + '/#/tasks?request=' + requestStr + '&response=' + responseStr)
      }
    },
    pushTask: function (taskForm, onSuccess, onError) {
      ajax.postAsync('http://127.0.0.1:' + REST_PORT + '/tasks?refresh=true', taskForm, onSuccess, onError)
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
    getDownConfigAsync: function (onSuccess, onError) {
      ajax.getAsync('http://127.0.0.1:' + REST_PORT + '/config', function (config) {
        delete config['port']
        delete config['proxyConfig']
        delete config['speedLimit']
        delete config['taskLimit']
        delete config['totalSpeedLimit']
        if (onSuccess) {
          onSuccess(config)
        }
      }, onError)
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
    },
    getCookieAsync: function (url, onSuccess, onError) {
      var xhr = ajax.buildXHR()
      xhr.withCredentials = true
      xhr.open('get', url, true)
      xhr.setRequestHeader('Accept', 'application/x-sniff-cookie,*/*;q=0.8')
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            var cookie = xhr.getResponseHeader('X-Sniff-Cookie')
            if (onSuccess) {
              onSuccess(cookie)
            }
          } else if (onError) {
            onError(xhr)
          }
        }
      }
      xhr.send()
    }
  })
})((function () {
  return {
    version: '${version}',
    buildXHR: function () {
      var xhr = null
      if (window.XMLHttpRequest) {
        xhr = new XMLHttpRequest()
      } else {
        xhr = new ActiveXObject('Microsoft.XMLHTTP')
      }
      return xhr
    },
    proxySend: function (async, method, url, data, onSuccess, onError) {
      var xhr = this.buildXHR()
      xhr.open('post', '/', async)
      var data = {method: method, url: url, data: data}
      xhr.setRequestHeader('X-Proxy-Send', encodeURIComponent(JSON.stringify(data)))
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            if (onSuccess) {
              onSuccess(xhr.responseText ? JSON.parse(xhr.responseText) : {})
            }
          } else if (onError) {
            onError(xhr)
          }
        }
      }
      xhr.send()
    },
    get: function (url) {
      return this.send('get', url)
    },
    getAsync: function (url, onSuccess, onError) {
      return this.sendAsync('get', url, null, onSuccess, onError)
    },
    post: function (url, data) {
      return this.send('post', url, data)
    },
    postAsync: function (url, data, onSuccess, onError) {
      this.sendAsync('post', url, data, onSuccess, onError)
    },
    put: function (url, data) {
      return this.send('put', url, data)
    },
    putAsync: function (url, data, onSuccess, onError) {
      this.sendAsync('put', url, data, onSuccess, onError)
    },
    send: function (method, url, data) {
      var result = null
      var error = null
      this.proxySend(false, method, url, data, function (data) {
        result = data
      }, function (xhr) {
        error = xhr
      })
      if (error) {
        throw error
      }
      return result
    },
    sendAsync: function (method, url, data, onSuccess, onError) {
      this.proxySend(true, method, url, data, onSuccess, onError)
    }
  }
})())