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
import { dataManageServer, accountManageServer, authManageServer, accessKeyManageServer } from 'Api'
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
      approveFormRules: {
        applyTitle: [{ required: true, message: '申请标题不能为空', trigger: 'blur' }],
        followers: [{ required: false, message: '关注人不能为空', trigger: 'blur' }],
        applyDesc: [{ required: true, message: '申请背景不能为空', trigger: 'blur' }]
      },
      columns: [],
      authID: '',
      accessKeyList: [],
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() <= Date.now()
        }
      }
    }
  },
  created() {
    const { authID, applyType } = this.$route.query
    this.authID = authID
    this.queryAuthTemplateDetails([applyType])
    authID && this.getDetail()
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
    // 获取审批单详情
    async getDetail() {
      this.loadingFlag = true
      const { authID } = this
      const res = await authManageServer.queryAuthDetail({ authID })
      this.loadingFlag = false
      if (res.code === 0 && res.data) {
        const { applyTitle, followers, applyDesc, applyContent, authChain } = res.data
        this.applyInfo = { applyTitle, followers, applyDesc }
        this.applyDataList = JSON.parse(applyContent)
        const { chain } = authChain
        const dataOwners = this.applyDataList.map((v) => v.ownerUserName)
        // 自定义的能删
        this.approveChainList = chain.map((v) => {
          return {
            ...v,
            deleteAble: !dataOwners.includes(v.name) && v.name !== this.userId
          }
        })
      }
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
    // 获取数据集详情
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
        // 插入自己 FIXME:
        data.unshift({ agency: this.agencyId, ownerUserName: this.userId })
        const approveChainListStr = data.map((v) => {
          return JSON.stringify({
            agency: v.ownerAgencyName,
            name: v.ownerUserName,
            deleteAble: false,
            visible: false
          })
        })
        this.approveChainList = Array.from(new Set(approveChainListStr)).map((v) => {
          return JSON.parse(v)
        })
      } else {
        this.applyDataList = []
        this.approveChainList = []
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
    },
    submit() {
      this.$refs.applyInfo.validate((valid) => {
        if (valid) {
          if (this.applyDataList.some((v) => !v.authTime)) {
            this.$message.error('请选择授权期限')
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
      const { applyDataList } = this
      const { applyTitle, applyDesc, followers } = this.applyInfo
      console.log(applyDataList, 'applyDataList', this.approveChainList)
      const applyChain = this.approveChainList.map((v) => {
        return {
          name: v.name,
          agency: v.agency
        }
      })
      const params = {
        applyType: 'wedpr_data_auth',
        applyContent: JSON.stringify(applyDataList),
        applyTitle,
        applyDesc,
        applyTemplateName: 'wedpr_data_auth',
        applyChain: JSON.stringify({ chain: applyChain }),
        followers,
        id: this.authID
      }
      const res = await authManageServer.updateAuth({ authList: [params] })
      if (res.code === 0) {
        this.$message.success('申请单编辑成功')
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
            this.queryAccessKeyList()
            break
          case 'wedpr_data_auth':
            break
          default:
            break
        }
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
