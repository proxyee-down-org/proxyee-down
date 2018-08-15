import axios from 'axios'

const client = axios.create()

const showFileChooser = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/fileChooser')
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

const showDirChooser = () => {
  return new Promise((resolve, reject) => {
    client
      .get('/native/dirChooser')
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
      .post('/native/showFile', { path: path })
      .then(response => resolve(response.data))
      .catch(error => reject(error))
  })
}

export { showFileChooser }
export { showDirChooser }
export { getLocale }
export { setLocale }
export { showFile }
