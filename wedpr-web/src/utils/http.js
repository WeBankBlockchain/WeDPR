'use strict'

import axios from 'axios'
import store from 'Store'
import fileDownload from 'js-file-download'
import { Message } from 'element-ui'
import router from '@/router'
import { jwtDecode } from 'jwt-decode'
import { permissionMap } from './config.js'
const http = axios.create({
  baseURL: process.env.VUE_APP_BASE_URL,
  timeout: 30000
})
http.interceptors.request.use(
  (config) => {
    const authorization = store.state.authorization
    authorization && (config.headers.Authorization = authorization)
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 请求网络失败 回调先走拦截器 => 再走 post.catch => 再走页面的 try-catch
// 请求返回code不对 进拦截器response后=> 直接走 post.catch => 再走页面的 try-catch
// 错误提示 统一处理 不需要 页面进行额外处理 包括token失效,断网

http.interceptors.response.use(
  (response) => {
    console.log(response, 'response')
    const { headers } = response
    if (headers && headers.authorization) {
      console.log(headers.authorization)
      const jwt = headers.authorization
      const { user: userData } = jwtDecode(jwt)
      console.log(JSON.parse(userData), 'JSON.parse(userData)')
      const { username, roleName, groupInfos = [] } = JSON.parse(userData)
      store.commit('SET_AUTHORIZATION', jwt)
      store.commit('SET_USERID', username)
      store.commit('SET_USERINFO', { ...store.state.userinfo, ...JSON.parse(userData) })
      store.commit('SET_GROUPLIST', groupInfos)
      const isGroupAdmin = groupInfos.map((v) => v.groupAdminName).includes(username)
      if (roleName === 'admin_user') {
        store.commit('SET_PERMISSION', permissionMap.admin_user)
      } else if (isGroupAdmin) {
        store.commit('SET_PERMISSION', permissionMap.group_admin)
      } else {
        store.commit('SET_PERMISSION', permissionMap[roleName])
      }
    }
    // 如果返回的状态码为200，说明接口请求成功，可以正常拿到数据
    const data = response.data
    const { code, msg } = data
    if (code && code !== 0) {
      msg &&
        Message.error({
          message: msg,
          duration: 5000
        })
    }
    return Promise.resolve(data)
  },
  // 网络请求失败会走这里
  (error) => {
    console.log('error:', error)
    // 兼容后台处理
    const data = error.response.data
    const status = error.response.status
    const { msg } = data
    switch (status) {
      case 401:
        Message.error({
          message: '您的登录态已超时，请重新登录'
        })
        router.push({ path: '/login', query: { redirectUrl: encodeURIComponent(location.href) } })

        break
      // case 400:
      //   Message.error({
      //     message: '您的登录态已超时，请重新登录'
      //   })
      //   router.push({ path: '/login' })
      //   break
      case 403:
        Message.error({
          message: '您的登录态已超时，请重新登录'
        })
        router.push({ path: '/login', query: { redirectUrl: encodeURIComponent(location.href) } })
        break
      case 404:
        Message.error({
          message: '请求URL错误：' + error.message,
          center: true,
          duration: 5 * 1000
        })
        break
      case 500:
        Message.error({
          message: '服务器异常'
        })
        break
      default:
        Message.error(msg || '网络请求失败')
    }
    return Promise.reject(error)
  }
)
export default {
  post(url, params) {
    return new Promise((resolve) => {
      http
        .post(url, params)
        .then((res) => {
          resolve(res)
        })
        .catch((err) => {
          resolve(err)
        })
    })
  },
  get(url, params) {
    return new Promise((resolve, reject) => {
      http
        .get(url, {
          params: params
        })
        .then((res) => {
          resolve(res)
        })
        .catch((err) => {
          resolve(err)
        })
    })
  },
  delete(url, params) {
    return new Promise((resolve, reject) => {
      http
        .delete(url, {
          params: params
        })
        .then((res) => {
          resolve(res)
        })
        .catch((err) => {
          resolve(err)
        })
    })
  },
  put(url, params) {
    return new Promise((resolve, reject) => {
      http
        .put(url, params)
        .then((res) => {
          resolve(res)
        })
        .catch((err) => {
          resolve(err)
        })
    })
  },
  patch(url, params) {
    return new Promise((resolve, reject) => {
      http
        .patch(url, params)
        .then((res) => {
          resolve(res)
        })
        .catch((err) => {
          resolve(err)
        })
    })
  },
  getStream(url, params) {
    return new Promise((resolve, reject) => {
      http
        .get(
          url,
          {
            params: params
          },
          {
            responseType: 'blob'
          }
        )
        .then((res) => {
          resolve(res)
        })
        .catch((err) => {
          resolve(err)
        })
    })
  },
  postStream(url, params) {
    return new Promise((resolve, reject) => {
      http
        .post(url, params, {
          responseType: 'blob'
        })
        .then((res) => {
          resolve(res)
        })
        .catch((err) => {
          resolve(err)
        })
    })
  },
  download(url, params, fileName = 'temp.xlsx') {
    console.log(params, 'params')
    return new Promise((resolve, reject) => {
      http
        .get(
          url,
          {
            params: params
          },
          {
            responseType: 'blob'
          }
        )
        .then((res) => {
          fileDownload(res, fileName)
          resolve({ code: '0' })
        })
        .catch((err) => {
          resolve(err)
        })
    })
  },
  uploadAjax(url, params) {
    return new Promise((resolve, reject) => {
      http
        .post(url, params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
          }
        })
        .then((res) => {
          resolve(res)
        })
        .catch((err) => {
          resolve(err)
        })
    })
  }
}
