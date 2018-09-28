import http from './http'
import axios from 'axios'

const client = http.build()
const clientNoSpin = axios.create()

/**
 * 弹出原生文件选择框
 */
export const showFileChooser = () => {
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
export const showDirChooser = () => {
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
export const getInitConfig = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/getInitConfig')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 弹出系统资源管理器并选中指定文件
 * @param {string} path 文件路径
 */
export const showFile = path => {
  return new Promise((resolve, reject) => {
    client
      .post('/native/showFile', {
        path: path
      })
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 检查证书是否安装
 */
export const checkCert = () => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .get('/native/checkCert')
      .then(response => resolve(response.data.status))
      .catch(error => reject(error))
  })
}

/**
 * 安装证书
 */
export const installCert = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/installCert')
      .then(response => resolve(response.data.status))
      .catch(error => reject(error))
  })
}

/**
 * 取设置的代理模式
 */
export const getProxyMode = () => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .get('/native/getProxyMode')
      .then(response => resolve(response.data.mode))
      .catch(error => reject(error))
  })
}

/**
 * 修改代理模式
 * @param {number} mode 0.不接管系统代理 1.接管系统代理
 */
export const changeProxyMode = mode => {
  return new Promise((resolve, reject) => {
    client
      .post('/native/changeProxyMode', {
        mode: mode
      })
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 取本地已安装的扩展列表
 */
export const getExtensions = () => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .get('/native/getExtensions')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 安装指定扩展
 * @param {object} data 扩展相关信息
 */
export const installExtension = data => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .post('/native/installExtension', data)
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 更新指定扩展
 * @param {object} data 扩展相关信息
 */
export const updateExtension = data => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .post('/native/updateExtension', data)
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 安装本地扩展
 * @param {string} path 扩展所在目录
 */
export const installLocalExtension = path => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .post('/native/installLocalExtension', {
        path: path
      })
      .then(response => resolve(response.data.data))
      .catch(error => reject(error))
  })
}

/**
 * 卸载扩展
 * @param {string} path 扩展所在目录
 * @param {boolean} isLocal 是否本地加载的扩展
 */
export const uninstallExtension = (path, local) => {
  return new Promise((resolve, reject) => {
    client
      .post('/native/uninstallExtension', {
        path,
        local
      })
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 启用或禁用扩展
 * @param {object} data 
 */
export const toggleExtension = data => {
  return new Promise((resolve, reject) => {
    client
      .post('/native/toggleExtension', data)
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 打开浏览器并访问指定url
 * @param {object} data 
 */
export const openUrl = url => {
  if (window.navigator.userAgent.indexOf('JavaFX') !== -1) {
    clientNoSpin.post('/native/openUrl', {
      url: encodeURIComponent(url)
    })
  } else {
    window.open(url)
  }
}

/**
 * 更新软件
 * @param {string} path 更新包下载地址
 */
export const doUpdate = path => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .post('/native/doUpdate', {
        path: path
      })
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 更新软件进度获取
 */
export const getUpdateProgress = () => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .get('/native/getUpdateProgress')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 重启软件
 */
export const doRestart = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/doRestart')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 取软件设置信息
 */
export const getConfig = () => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .get('/native/getConfig')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

/**
 * 保存软件设置
 * @param {object} config 
 */
export const setConfig = config => {
  return new Promise((resolve, reject) => {
    client
      .put('/native/setConfig', config)
      .then(response => resolve(response))
      .catch(error => reject(error))
  })
}

/**
 * 复制数据到系统剪贴板
 * @param {object} data 
 */
export const copy = data => {
  return new Promise((resolve, reject) => {
    clientNoSpin
      .put('/native/copy', data)
      .then(response => resolve(response))
      .catch(error => reject(error))
  })
}