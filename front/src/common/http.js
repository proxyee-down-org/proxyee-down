import Vue from 'vue'
import axios from 'axios'

export default {
  build() {
    const client = axios.create()
    client.interceptors.request.use(
      config => {
        Vue.prototype.$Spin.show()
        return config
      },
      error => Promise.reject(error)
    )
    client.interceptors.response.use(
      response => {
        Vue.prototype.$Spin.hide()
        return response
      },
      error => {
        Vue.prototype.$Spin.hide()
        return Promise.reject(error)
      }
    )
    return client
  }
}
