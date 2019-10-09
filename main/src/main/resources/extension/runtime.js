(function () {
    var API_PORT = '${apiPort}'
    var FRONT_PORT = '${frontPort}'
    var REST_PORT = '26339'
    var ajax = {
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
            this.proxySend2(async, method, url, null, data, onSuccess, onError)
        },
        proxySend2: function (async, method, url, heads, data, onSuccess, onError) {
            var xhr = this.buildXHR()
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
            //是浏览器环境，需要通过代理服务器来避免跨域安全问题
            if (window.navigator) {
                xhr.open('post', '/', async)
                var req = {method: method, url: url, heads: heads}
                if (data) {
                    if (typeof data == 'string') {
                        req.rawData = data
                    } else {
                        req.data = data
                    }
                }
                xhr.setRequestHeader('X-Proxy-Send', encodeURIComponent(JSON.stringify(req)))
                xhr.send()
            } else {
                xhr.open(method, url, async)
                xhr.setRequestHeader('Content-Type', 'application/json; charset=utf-8')
                xhr.send(data ? JSON.stringify(data) : null)
            }
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
        delete: function (url, data) {
            return this.send('delete', url, data)
        },
        deleteAsync: function (url, data, onSuccess, onError) {
            this.sendAsync('delete', url, data, onSuccess, onError)
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
    return {
        version: '${version}',
        settings: ${settings},
        fetchAsync: function (request, onSuccess, onError) {
            return ajax.proxySend2(true, request.method, request.url, request.heads, request.data, onSuccess, onError)
        },
        resolve: function (request) {
            return ajax.put('http://127.0.0.1:' + REST_PORT + '/util/resolve', request)
        },
        resolveAsync: function (request, onSuccess, onError) {
            ajax.putAsync('http://127.0.0.1:' + REST_PORT + '/util/resolve', request, onSuccess, onError)
        },
        createTask: function () {
            var request
            var response
            var config
            var data
            if (arguments.length == 1) {
                var taskForm = arguments[0]
                request = taskForm.request
                response = taskForm.response
                config = taskForm.config
                data = taskForm.data
            } else if (arguments.length == 2) {
                request = arguments[0]
                response = arguments[1]
            } else {
                return
            }
            var requestStr = encodeURIComponent(JSON.stringify(request))
            var responseStr = encodeURIComponent(JSON.stringify(response))
            var configStr = config ? encodeURIComponent(JSON.stringify(config)) : ''
            var dataStr = data ? encodeURIComponent(JSON.stringify(data)) : ''
            if ('${uiMode}' == '1') {
                ajax.get('http://127.0.0.1:' + API_PORT + '/api/createTask?request=' + requestStr + '&response=' + responseStr + '&config=' + configStr + '&data=' + dataStr)
            } else {
                window.open('http://127.0.0.1:' + FRONT_PORT + '/#/tasks?request=' + requestStr + '&response=' + responseStr + '&config=' + configStr + '&data=' + dataStr)
            }
        },
        createTaskAsync: function (taskForm, onSuccess, onError) {
            var requestStr = encodeURIComponent(encodeURIComponent(JSON.stringify(taskForm.request)))
            var responseStr = encodeURIComponent(encodeURIComponent(JSON.stringify(taskForm.response)))
            var configStr = taskForm.config ? encodeURIComponent(encodeURIComponent(JSON.stringify(taskForm.config))) : ''
            var dataStr = taskForm.data ? encodeURIComponent(encodeURIComponent(JSON.stringify(taskForm.data))) : ''
            if ('${uiMode}' == '1') {
                ajax.getAsync('http://127.0.0.1:' + API_PORT + '/api/createTask?request=' + requestStr + '&response=' + responseStr + '&config=' + configStr + '&data=' + dataStr, onSuccess, onError)
            } else {
                window.open('http://127.0.0.1:' + FRONT_PORT + '/#/tasks?request=' + requestStr + '&response=' + responseStr + '&config=' + configStr + '&data=' + dataStr)
            }
        },
        pushTask: function (taskForm, onSuccess, onError) {
            ajax.postAsync('http://127.0.0.1:' + REST_PORT + '/tasks?refresh=true', taskForm, onSuccess, onError)
        },
        refreshTask: function (id, request) {
            return ajax.put('http://127.0.0.1:' + REST_PORT + '/tasks/' + id, request)
        },
        refreshTaskAsync: function (id, request, onSuccess, onError) {
            ajax.putAsync('http://127.0.0.1:' + REST_PORT + '/tasks/' + id, request, onSuccess, onError)
        },
        pauseTask: function (id) {
            return ajax.put('http://127.0.0.1:' + REST_PORT + '/tasks/' + id + '/pause')
        },
        pauseTaskAsync: function (id, onSuccess, onError) {
            ajax.putAsync('http://127.0.0.1:' + REST_PORT + '/tasks/' + id + '/pause', null, onSuccess, onError)
        },
        resumeTask: function (id) {
            return ajax.put('http://127.0.0.1:' + REST_PORT + '/tasks/' + id + '/resume')
        },
        resumeTaskAsync: function (id, onSuccess, onError) {
            ajax.putAsync('http://127.0.0.1:' + REST_PORT + '/tasks/' + id + '/resume', null, onSuccess, onError)
        },
        deleteTask: function (id, delFile) {
            return ajax.delete('http://127.0.0.1:' + REST_PORT + '/tasks/' + id + '?delFile=' + !!delFile)
        },
        deleteTaskAsync: function (id, delFile, onSuccess, onError) {
            ajax.deleteAsync('http://127.0.0.1:' + REST_PORT + '/tasks/' + id + '?delFile=' + !!delFile, null, onSuccess, onError)
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
    }
})()