import Vue from 'vue'
import VueRouter from 'vue-router'
console.log(process.env.VUE_APP_MODE, 'process.env.VUE_APP_MODE')
console.log(process.env.VUE_APP_MODE === '"manage"')
Vue.use(VueRouter)
const loginCom =
  process.env.VUE_APP_MODE === 'manage'
    ? import(/* webpackChunkName: "login" */ '../views/adminLogin/index.vue')
    : import(/* webpackChunkName: "login" */ '../views/login/index.vue')
const projectManageCom =
  process.env.VUE_APP_MODE === 'manage'
    ? import(/* webpackChunkName: "projectManage" */ '../views/adminProjectManage/index.vue')
    : import(/* webpackChunkName: "projectManage" */ '../views/projectManage/index.vue')
const dataManageCom =
  process.env.VUE_APP_MODE === 'manage'
    ? import(/* webpackChunkName: "dataManage" */ '../views/adminDataManage/index.vue')
    : import(/* webpackChunkName: "dataManage" */ '../views/dataManage/index.vue')
const dataDetailCom =
  process.env.VUE_APP_MODE === 'manage'
    ? import(/* webpackChunkName: "dataManage" */ '../views/adminDataDetail/index.vue')
    : import(/* webpackChunkName: "dataManage" */ '../views/dataDetail/index.vue')
const projectDetailCom =
  process.env.VUE_APP_MODE === 'manage'
    ? import(/* webpackChunkName: "projectDetail" */ '../views/adminProjectDetail/index.vue')
    : import(/* webpackChunkName: "projectDetail" */ '../views/projectDetail/index.vue')
const logCom =
  process.env.VUE_APP_MODE === 'manage'
    ? import(/* webpackChunkName: "logManage" */ '../views/adminLogManage/index.vue')
    : import(/* webpackChunkName: "logManage" */ '../views/logManage/index.vue')
const routes = [
  {
    path: '/feed',
    name: 'feed',
    component: () => import(/* webpackChunkName: "feed" */ '../views/feed/index.vue'),
    meta: {
      title: '欢迎反馈',
      requireAuth: false,
      permissionCheck: false,
      isParent: true
    }
  },
  {
    path: '/login',
    name: 'login',
    component: () => loginCom,
    meta: {
      title: '用户登录',
      requireAuth: false,
      permissionCheck: false,
      isParent: true
    }
  },
  {
    path: '/register',
    name: '/register',
    component: () => import(/* webpackChunkName: "about" */ '../views/register/index.vue'),
    meta: {
      title: '用户注册',
      requireAuth: false,
      permissionCheck: false,
      isParent: true
    }
  },
  {
    path: '/noPermission',
    component: () => import(/* webpackChunkName: "about" */ '../views/noPermission/index.vue'),
    meta: {
      title: '暂无权限',
      requireAuth: false,
      permissionCheck: false,
      isParent: true
    }
  },
  {
    path: '/',
    redirect: '/login',
    meta: {
      title: '',
      requireAuth: false,
      permissionCheck: false,
      isParent: true
    }
  },
  {
    name: 'screen',
    path: '/screen',
    component: () => import(/* webpackChunkName: "layout" */ '@/views/screen/index.vue'),
    meta: {
      title: '新增证书',
      isParent: true,
      requireAuth: true,
      permissionCheck: true,
      permissionNeed: ['screen']
    }
  },
  {
    path: '/layout',
    name: 'layout',
    redirect: '/tenantManage',
    component: () => import(/* webpackChunkName: "about" */ '../views/layout/index.vue'),
    children: [
      {
        name: 'home',
        path: '/home',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/home/index.vue'),
        meta: {
          title: '平台首页',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['home']
        }
      },
      {
        name: 'tenantManage',
        path: '/tenantManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/tenantManage/index.vue'),
        meta: {
          title: '用户管理',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['tenantManage']
        }
      },
      {
        name: 'accountManage',
        path: '/accountManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/accountManage/index.vue'),
        meta: {
          title: '管理用户组',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['accountManage']
        }
      },
      {
        name: 'dataManage',
        path: '/dataManage',
        component: () => dataManageCom,
        meta: {
          title: '数据资源',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['dataManage']
        }
      },
      {
        name: 'dataCreate',
        path: '/dataCreate',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/dataCreate/index.vue'),
        meta: {
          title: '新增数据资源',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['dataManage']
        }
      },
      {
        name: 'dataApply',
        path: '/dataApply',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/dataApply/index.vue'),
        meta: {
          title: '申请使用',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['dataManage']
        }
      },
      {
        name: 'dataApplyModify',
        path: '/dataApplyModify',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/dataApplyModify/index.vue'),
        meta: {
          title: '申请使用',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['dataManage']
        }
      },
      {
        name: 'dataDetail',
        path: '/dataDetail',
        component: () => dataDetailCom,
        meta: {
          title: '数据详情',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['dataManage']
        }
      },
      {
        name: 'projectManage',
        path: '/projectManage',
        component: () => projectManageCom,
        meta: {
          title: '项目空间',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['projectManage']
        }
      },
      {
        name: 'projectCreate',
        path: '/projectCreate',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/projectCreate/index.vue'),
        meta: {
          title: '新增项目',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['projectManage']
        }
      },
      {
        name: 'projectEdit',
        path: '/projectEdit',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/projectEdit/index.vue'),
        meta: {
          title: '编辑项目',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['projectManage']
        }
      },
      {
        name: 'projectDetail',
        path: '/projectDetail',
        component: () => projectDetailCom,
        meta: {
          title: '项目详情',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['projectManage']
        }
      },
      {
        name: 'jobDetail',
        path: '/jobDetail',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/jobDetail/index.vue'),
        meta: {
          title: '任务详情',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['projectManage']
        }
      },
      {
        name: 'leadMode',
        path: '/leadMode',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/leadMode/index.vue'),
        meta: {
          title: '新增项目',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['projectManage']
        }
      },
      {
        name: 'resetParams',
        path: '/resetParams',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/resetParams/index.vue'),
        meta: {
          title: '调参重跑',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['projectManage']
        }
      },
      {
        name: 'messageManage',
        path: '/messageManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/messageManage/index.vue'),
        meta: {
          title: '消息通知',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['messageManage']
        }
      },
      {
        name: 'logManage',
        path: '/logManage',
        component: () => logCom,
        meta: {
          title: '日志审计',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['logManage']
        }
      },
      {
        name: 'accessKeyManage',
        path: '/accessKeyManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/accessKeyManage/index.vue'),
        meta: {
          title: '凭证管理',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['accessKeyManage']
        }
      },
      {
        name: 'approveManage',
        path: '/approveManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/approveManage/index.vue'),
        meta: {
          title: '审批中心',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['approveManage']
        }
      },
      {
        name: 'approveDetail',
        path: '/approveDetail',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/approveDetail/index.vue'),
        meta: {
          title: '审批详情',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['approveManage']
        }
      },
      {
        name: 'serverManage',
        path: '/serverManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/serverManage/index.vue'),
        meta: {
          title: '服务发布',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['serverManage']
        }
      },
      {
        name: 'serverCreate',
        path: '/serverCreate',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/serverCreate/index.vue'),
        meta: {
          title: '服务发布',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['serverManage']
        }
      },
      {
        name: 'serverDetail',
        path: '/serverDetail',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/serverDetail/index.vue'),
        meta: {
          title: '服务详情',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['serverManage']
        }
      },
      {
        name: 'certificateManage',
        path: '/certificateManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/certificateManage/index.vue'),
        meta: {
          title: '证书管理',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['certificateManage']
        }
      },
      {
        name: 'addCertificate',
        path: '/addCertificate',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/addCertificate/index.vue'),
        meta: {
          title: '新增证书',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['addCertificate']
        }
      },
      {
        name: 'agencyManage',
        path: '/agencyManage',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/agencyManage/index.vue'),
        meta: {
          title: '机构管理',
          isParent: true,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['agencyManage']
        }
      },
      {
        name: 'agencyCreate',
        path: '/agencyCreate',
        component: () => import(/* webpackChunkName: "layout" */ '@/views/agencyCreate/index.vue'),
        meta: {
          title: '新增机构',
          isParent: false,
          requireAuth: true,
          permissionCheck: true,
          permissionNeed: ['agencyCreate']
        }
      }
    ]
  }
]

const router = new VueRouter({
  routes
})

export default router
