import Vue from 'vue'
import App from './App.vue'
import { Table, TableColumn } from 'element-ui'

import webankPpcsUi from '../lib/webankwedprui.umd'
import '../lib/webankwedprui.css'
import 'element-ui/lib/theme-chalk/table.css'
import 'element-ui/lib/theme-chalk/table-column.css'
// Vue.use(Pagination)
Vue.use(Table)
Vue.use(TableColumn)
Vue.use(webankPpcsUi) // 注册组件库
Vue.config.productionTip = false

new Vue({
  render: (h) => h(App)
}).$mount('#app')
