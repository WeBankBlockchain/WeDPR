import WePagination from './src/index.vue'

/* istanbul ignore next */
WePagination.install = function (Vue) {
  Vue.component(WePagination.name, WePagination)
}

export default WePagination
