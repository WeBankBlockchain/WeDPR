import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import WebankWedprUi from 'webank-wedpr-ui'
import 'webank-wedpr-ui/lib/webankwedprui.css'
import 'element-ui/lib/theme-chalk/index.css'
import 'Assets/style/index.less'
import 'element-ui/packages/theme-chalk/src/index.scss'
Vue.use(ElementUI)
Vue.use(WebankWedprUi)
Vue.config.productionTip = false

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? to.meta.title : ''
  const authorization = store.state.authorization
  const permission = store.state.permission
  // token 失效
  if (!authorization && to.meta.requireAuth) {
    next('/login')
  } else if (to.meta.permissionNeed && to.meta.permissionNeed.some((v) => !permission.includes(v)) && to.meta.permissionCheck) {
    // 没有权限
    next('/noPermission')
  } else {
    next()
  }
})

new Vue({
  router,
  store,
  render: (h) => h(App)
}).$mount('#app')
