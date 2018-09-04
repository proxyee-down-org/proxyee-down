import Vue from 'vue'
import axios from 'axios'

export default {
  build() {
    const client = axios.create()
    client.interceptors.request.use(
      config => {
        return config
      },
      error => Promise.reject(error)
    )
    client.interceptors.response.use(
      response => {
        return response
      },
      error => {
        return Promise.reject(error)
      }
    )
    return client
  }
}
