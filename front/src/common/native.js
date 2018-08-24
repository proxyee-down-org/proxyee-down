import http from './http'
import axios from 'axios'

const client = http.build()
const clientNoSpin = axios.create()

/**
 * 弹出原生文件选择框
 */
const showFileChooser = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/fileChooser')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 弹出原生文件夹选择框
 */
const showDirChooser = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/dirChooser')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 取应用初始化配置信息
 */
const getInitConfig = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/getInitConfig')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

const getLocale = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/getLocale')
      .then(response => resolve(response.data.locale))
      .catch(error => reject(error))
  })
}

const setLocale = locale => {
  return new Promise((resolve, reject) => {
    client
      .post('/native/setLocale', { locale: locale })
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

const showFile = path => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/showFile', { path: path })
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

const checkCert = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/checkCert')
      .then(response => resolve(response.data.status))
      .catch(error => reject(error))
  })
}

const installCert = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/installCert')
      .then(response => resolve(response.data.status))
      .catch(error => reject(error))
  })
}

const getProxyMode = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/getProxyMode')
      .then(response => resolve(response.data.mode))
      .catch(error => reject(error))
  })
}

const changeProxyMode = mode => {
  return new Promise((resolve, reject) => {
    client
      .post('/native/changeProxyMode', { mode: mode })
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

const getExtensions = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/getExtensions')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

const installExtension = data => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .post('/native/installExtension', data)
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

const toggleExtension = data => {
  return new Promise((resolve, reject) => {
    client
      .post('/native/toggleExtension', data)
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

export { showFileChooser }
export { showDirChooser }
export { getInitConfig }
export { getLocale }
export { setLocale }
export { showFile }
export { checkCert }
export { installCert }
export { getProxyMode }
export { changeProxyMode }
export { getExtensions }
export { installExtension }
export { toggleExtension }
