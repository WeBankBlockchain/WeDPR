import WePagination from './wePagination/index.js'
import FormCard from './formCard/index.js'
import ColFilter from './colFilter/index.js'

const components = [WePagination, FormCard, ColFilter]

const MyModule = {}
MyModule.install = (Vue) => {
  components.forEach((component) => {
    Vue.component(component.name, component)
  })
}

if (typeof window !== 'undefined' && window.Vue) {
  MyModule.install(window.Vue)
}

export default MyModule
