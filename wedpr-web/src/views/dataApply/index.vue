<template>
  <div class="apply-data">
    <el-form :inline="false" @submit="queryHandle" label-width="96px" :rules="approveFormRules" :model="applyInfo" ref="applyInfo" size="small">
      <el-form-item label="申请标题：" prop="applyTitle">
        <el-input style="width: 480px" v-model="applyInfo.applyTitle" placeholder="请输入"> </el-input>
      </el-form-item>
      <el-form-item label="关注人：" prop="followers">
        <el-select
          multiple
          loading-text="搜索中"
          filterable
          style="width: 480px"
          v-model="applyInfo.followers"
          remote
          :remote-method="getUserNameSelect"
          placeholder="请选择"
          clearable
        >
          <el-option v-for="item in userNameSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="申请背景：" prop="applyDesc">
        <el-input style="width: 480px" v-model="applyInfo.applyDesc" placeholder="请输入"> </el-input>
      </el-form-item>
      <el-form-item label="申请内容：" prop="applyContent">
        <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="applyDataList" :border="true" class="table-wrap">
          <el-table-column :label="item.title" :prop="item.key" :key="item.key" v-for="item in columns">
            <template v-slot="scope">
              <el-date-picker
                size="small"
                v-if="item.type === 'date'"
                value-format="yyyy-MM-dd"
                style="width: 160px"
                v-model="scope.row[item.key]"
                type="date"
                placeholder="请选择日期"
                :picker-options="pickerOptions"
              >
              </el-date-picker>
              <el-select style="width: 160px" v-model="scope.row[item.key]" v-else-if="item.type === 'select'" placeholder="请选择" clearable>
                <el-option :label="item.label" :value="item.value" :key="item.label" v-for="item in accessKeyList"></el-option>
              </el-select>
              <span v-else> {{ scope.row[item.key] }} </span>
            </template>
          </el-table-column>
        </el-table>
      </el-form-item>
      <el-form-item label="审批流程：" prop="applyChain">
        <approveChain showAdd @addUserToChain="addUserToChain" @deleteUser="deleteUser" :approveChainList="approveChainList" />
      </el-form-item>
    </el-form>
    <div class="sub-con">
      <el-button size="medium" type="primary" @click="submit"> 提交申请 </el-button>
    </div>
  </div>
</template>
<script>
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { dataManageServer, accountManageServer, authManageServer, serviceManageServer, accessKeyManageServer } from 'Api'
import approveChain from '@/components/approveChain.vue'
import { mapGetters } from 'vuex'
export default {
  name: 'dataApply',
  mixins: [tableHeightHandle],
  components: {
    approveChain
  },
  data() {
    return {
      applyDataList: [],
      applyInfo: {
        applyTitle: '',
        followers: [],
        applyDesc: ''
      },
      userNameSelectList: [],
      approveChainList: [],

      columns: [],
      applyType: '',
      accessKeyList: [],
      approveFormRules: {
        applyTitle: [{ required: true, message: '申请标题不能为空', trigger: 'blur' }],
        followers: [{ required: false, message: '关注人不能为空', trigger: 'blur' }],
        applyDesc: [{ required: true, message: '申请背景不能为空', trigger: 'blur' }]
      },
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() <= Date.now()
        }
      }
    }
  },
  created() {
    const { applyType } = this.$route.query
    this.applyType = applyType
    this.queryAuthTemplateDetails([applyType])
  },
  computed: {
    ...mapGetters(['userId', 'agencyId'])
  },
  methods: {
    addUserToChain(list) {
      this.approveChainList = [...list]
    },
    deleteUser(list) {
      this.approveChainList = [...list]
    },
    async getUserNameSelect(username) {
      if (!username) {
        this.userNameSelectList = []
        return
      }
      const res = await accountManageServer.getUser({ pageNum: 1, pageSize: 9999, username })
      if (res.code === 0 && res.data) {
        const { userList = [] } = res.data
        this.userNameSelectList = userList.map((v) => {
          return {
            label: v.username,
            value: v.username
          }
        })
      } else {
        this.userNameSelectList = []
      }
    },
    // 获取数据集列表详情
    async getListDetail(params) {
      this.loadingFlag = true
      const res = await dataManageServer.queryDatasetList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { data = [] } = res
        this.applyDataList = data.map((dataValue) => {
          const dataObj = {}
          this.columns.forEach((column) => {
            const key = column.key
            dataObj[key] = dataValue[key] || column.defaultValue
          })
          return dataObj
        })
        console.log(data, 'data')
        // 合并审批链 FIXME:
        // 插入自己
        let combineApproveChainList = [{ agency: this.agencyId, name: this.userId, deleteAble: false }]
        // 循环插入每个数据集自己设置的审批链,且申请方不能更改，只能在前方插入自己机构的人
        data.forEach((v) => {
          const { approvalChain = '' } = v
          const approvalChainList = Array.isArray(JSON.parse(approvalChain)) ? JSON.parse(approvalChain) : []
          approvalChainList.forEach((k) => {
            // 已有的不放入链内
            combineApproveChainList = combineApproveChainList.filter((dataPushed) => {
              return !(dataPushed.agency === v.ownerAgencyName && dataPushed.name === k)
            })
            combineApproveChainList.push({
              agency: v.ownerAgencyName,
              name: k,
              deleteAble: false,
              addNextUserDisbaled: true,
              visible: false
            })
          })
        })
        console.log(combineApproveChainList, 'combineApproveChainList')
        this.approveChainList = combineApproveChainList
        console.log(this.approveChainList, 'this.approveChainList ')
      } else {
        this.applyDataList = []
        this.approveChainList = []
      }
    },
    submit() {
      this.$refs.applyInfo.validate((valid) => {
        if (valid) {
          if (this.applyDataList.some((v) => !v.authTime)) {
            this.$message.error('请选择授权期限')
            return false
          }
          if (this.applyType === 'wedpr_service_auth' && this.applyDataList.some((v) => !v.accessKeyID)) {
            this.$message.error('请选择访问凭证ID')
            return false
          }
          this.subApply()
        } else {
          return false
        }
      })
    },
    async subApply() {
      this.loadingFlag = true
      const { applyDataList, applyType } = this
      const { applyTitle, applyDesc, followers } = this.applyInfo
      console.log(applyDataList, 'applyDataList', this.approveChainList)
      const applyChain = this.approveChainList.map((v) => {
        return {
          name: v.name,
          agency: v.agency
        }
      })
      const params = {
        applyType,
        applyTemplateName: applyType,
        applyContent: JSON.stringify(applyDataList),
        applyTitle,
        applyDesc,
        applyChain: JSON.stringify({ chain: applyChain }),
        followers
      }
      const res = await authManageServer.createAuth({ authList: [params] })
      if (res.code === 0) {
        this.$message.success('申请已成功提交')
        history.back()
      }
    },
    async queryAuthTemplateDetails(params) {
      const res = await authManageServer.queryAuthTemplateDetails(params)
      console.log(res)
      if (res.code === 0) {
        const { templateSetting } = res.data[0]
        const { columns = [] } = JSON.parse(templateSetting)
        this.columns = columns
        console.log(this.columns, 'this.columns')
        switch (this.applyType) {
          case 'wedpr_service_auth':
            this.initServiceTable()
            break
          case 'wedpr_data_auth':
            this.initDataTable()
            break
          default:
            break
        }
      }
    },
    initDataTable() {
      const { selectdDataStr } = this.$route.query
      const selectdDataList = decodeURIComponent(selectdDataStr).split(',')
      this.getListDetail({ datasetIdList: selectdDataList })
    },
    initServiceTable() {
      const { serviceId } = this.$route.query
      const serviceIdList = decodeURIComponent(serviceId).split(',')
      const params = { condition: {}, serviceIdList, pageNum: -1, pageSize: 1 }
      this.queryService(params)
    },
    // 获取服务详情
    async queryService(params) {
      const res = await serviceManageServer.getPublishList(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { wedprPublishedServiceList = [] } = res.data
        const serviceData = wedprPublishedServiceList
        this.applyDataList = serviceData.map((dataValue) => {
          const dataObj = {}
          this.columns.forEach((column) => {
            const key = column.key
            dataObj[key] = dataValue[key] || column.defaultValue
          })
          return dataObj
        })
        // 合并审批链 FIXME:
        // 插入自己
        const combineApproveChainList = [{ agency: this.agencyId, name: this.userId, deleteAble: false }]
        // 循环插入每个数据集自己设置的审批链,且申请方不能更改，只能在前方插入自己机构的人
        serviceData.forEach((v) => {
          const inList = combineApproveChainList.some((k) => {
            return k.agency === v.agency && k.name === v.owner
          })
          if (!inList) {
            combineApproveChainList.push({
              agency: v.agency,
              name: v.owner,
              deleteAble: false,
              addNextUserDisbaled: true,
              visible: false
            })
          }
        })
        this.approveChainList = combineApproveChainList
        this.queryAccessKeyList()
      }
    },
    // 获取ak列表
    async queryAccessKeyList() {
      const params = { condition: { status: 'Enable', id: '' }, pageNum: -1, pageSize: 1 }
      const res = await accessKeyManageServer.queryAccessKeyList(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { credentials = [] } = res.data
        this.accessKeyList = credentials.map((v) => {
          return {
            label: v.accessKeyID,
            value: v.accessKeyID
          }
        })
      }
    }
  }
}
</script>
<style lang="less" scoped>
::v-deep .el-textarea__inner {
  height: 100%;
}
::v-deep .el-form-item--mini.el-form-item,
.el-form-item--small.el-form-item {
  margin-bottom: 20px;
}
.sub-con {
  padding-left: 96px;
  margin-top: 32px;
}
span.info {
  color: #262a32;
}
</style>
