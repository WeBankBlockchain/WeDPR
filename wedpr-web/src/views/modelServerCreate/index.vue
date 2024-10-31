<template>
  <div class="create-data">
    <el-form :inline="false" @submit="checkService" :model="serverForm" :rules="serverRules" ref="serverForm" size="small">
      <formCard title="基础信息">
        <el-form-item label-width="96px" label="服务名称：" prop="serviceName">
          <el-input style="width: 480px" placeholder="请输入" v-model="serverForm.serviceName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label-width="96px" label="服务简介：" prop="serviceDesc">
          <el-input type="textarea" style="width: 480px" placeholder="请输入" v-model="serverForm.serviceDesc" autocomplete="off"></el-input>
        </el-form-item>
      </formCard>
      <formCard title="选择发布模型" v-if="type !== 'edit'">
        <el-form-item prop="serviceConfig" label-width="0">
          <modelSelect :queriedTypes="['XGB_MODEL_SETTING', 'LR_MODEL_SETTING']" v-model="serverForm.serviceConfig" />
        </el-form-item>
      </formCard>
    </el-form>
    <div>
      <el-button size="medium" type="primary" @click="checkService" v-if="type === 'edit'"> 编辑服务 </el-button>
      <el-button size="medium" type="primary" @click="checkService" v-else> 发布服务 </el-button>
    </div>
  </div>
</template>
<script>
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { serviceManageServer } from 'Api'
import formCard from '@/components/formCard.vue'
import { mapGetters } from 'vuex'
import modelSelect from '../leadMode/modelSelect/index.vue'
import { serviceTypeEnum } from 'Utils/constant.js'
export default {
  name: 'modelServerCreate',
  mixins: [tableHeightHandle],
  components: {
    formCard,
    modelSelect
  },
  data() {
    return {
      serverForm: {
        serviceName: '',
        serviceDesc: '',
        serviceConfig: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 8
      },
      total: -1,
      queryFlag: false,
      loadingFlag: false,
      showAddModal: false,
      type: '',
      dataList: [],
      selectedData: {},
      serviceId: ''
    }
  },

  created() {
    const { type, serviceId } = this.$route.query
    this.type = type
    if (this.type === 'edit') {
      this.serviceId = serviceId
      this.queryService()
    }
  },
  computed: {
    ...mapGetters(['userinfo', 'userId']),
    serverRules() {
      return {
        serviceName: [
          {
            required: true,
            message: '请输入服务名称',
            trigger: 'blur'
          }
        ],
        serviceDesc: [
          {
            required: true,
            message: '请输入服务描述',
            trigger: 'blur'
          }
        ],
        serviceConfig: [
          {
            required: true,
            message: '请选择模型',
            trigger: 'blur'
          }
        ]
      }
    }
  },
  methods: {
    // 获取服务详情回显
    async queryService() {
      this.loadingFlag = true
      const { serviceId } = this
      const params = { condition: { serviceId: '' }, serviceIdList: [serviceId], pageNum: 1, pageSize: 1 }
      const res = await serviceManageServer.getPublishList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { wedprPublishedServiceList = [] } = res.data
        const { serviceName, serviceDesc, serviceConfig } = wedprPublishedServiceList[0] || {}
        this.serverForm = { ...this.serverForm, serviceName, serviceDesc, serviceConfig }
      }
    },

    async createService(params) {
      const res = await serviceManageServer.publishService(params)
      if (res.code === 0 && res.data) {
        console.log(res)
        this.$message.success('服务发布成功')
        this.$router.push({ path: 'serverManage' })
      }
    },
    async updateService(params) {
      const res = await serviceManageServer.updateService(params)
      if (res.code === 0) {
        console.log(res)
        this.$message.success('服务编辑成功')
        history.go(-1)
      }
    },

    checkService() {
      this.$refs.serverForm.validate((valid) => {
        if (valid) {
          const { serviceName, serviceDesc, serviceConfig } = this.serverForm
          let setting = ''
          if (this.type === 'edit') {
            setting = serviceConfig
          } else {
            setting = serviceConfig.setting
          }
          const { model_type } = JSON.parse(setting)
          const serviceType = model_type === 'xgb_model' ? serviceTypeEnum.XGB : serviceTypeEnum.LR
          if (this.type === 'edit') {
            this.updateService({ serviceName, serviceDesc, serviceId: this.serviceId, serviceConfig: setting, serviceType })
          } else {
            this.createService({ serviceName, serviceDesc, serviceType, serviceConfig: setting })
          }
        } else {
          return false
        }
      })
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getAccountList()
    }
  }
}
</script>
<style lang="less" scoped>
div.create-data {
  div.card-container {
    overflow: hidden;
    margin-left: -16px;
    margin-right: -16px;
  }
}
</style>
