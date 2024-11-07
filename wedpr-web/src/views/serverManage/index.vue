<template>
  <div class="group-manage">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item prop="agency" label="发布机构：">
          <el-select clearable size="small" style="width: 150px" v-model="searchForm.agency" placeholder="请选择">
            <el-option :key="item" v-for="item in agencyList" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="owner" label="发布用户：">
          <el-input clearable style="width: 150px" v-model="searchForm.owner" placeholder="请输入"> </el-input>
        </el-form-item>
        <el-form-item prop="serviceName" label="服务名称：">
          <el-input clearable style="width: 150px" v-model="searchForm.serviceName" placeholder="请输入"> </el-input>
        </el-form-item>
        <el-form-item prop="status" label="发布状态：">
          <el-select clearable size="small" style="width: 150px" v-model="searchForm.status" placeholder="请选择">
            <el-option :key="item" v-for="item in servicePulishStatusList" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="createTime" label="发布时间：">
          <el-date-picker
            style="width: 280px"
            value-format="yyyy-MM-dd"
            v-model="searchForm.createTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="queryFlag" @click="queryHandle">
            {{ queryFlag ? '查询中...' : '查询' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="default" @click="reset"> 重置 </el-button>
        </el-form-item>
        <div class="op-container">
          <div class="btn" @click="creatPsi"><img class="icon-btn" src="~Assets/images/lead icon_service1.png" alt="" /> 发布匿踪查询服务</div>
          <div class="btn model" @click="creatModel"><img class="icon-btn" src="~Assets/images/lead icon_service2.png" alt="" /> 发布模型预测服务</div>
        </div>
      </el-form>
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
      <el-tab-pane label="我的服务" name="IsOwner"></el-tab-pane>
      <el-tab-pane label="可申请的" name="NoPermission"></el-tab-pane>
      <el-tab-pane label="已授权的" name="Authorized"></el-tab-pane>
    </el-tabs>
    <div>
      <div v-if="activeName === 'NoPermission'" class="apply-container">
        <el-button size="small" icon="el-icon-plus" type="primary" @click="startApplySelect"> 批量申请授权 </el-button>
        <div class="handle" v-if="showApplySelect">
          <span>已选{{ selectdDataList.length }}项</span><el-button size="small" :disabled="!selectdDataList.length" type="primary" @click="applyAuth"> 确认申请 </el-button
          ><el-button size="small" type="info" @click="cancelApply"> 取消 </el-button>
        </div>
      </div>
      <div class="card-container" v-loading="loadingFlag">
        <serviceCard
          @selected="(checked) => selected(checked, item)"
          :selected="selectdDataList.map((v) => v.serviceId).includes(item.serviceId)"
          @deleteService="showDeleteModal(item)"
          @modifyData="modifyData(item)"
          v-for="item in tableData"
          :serviceInfo="item"
          :key="item.serviceId"
        />
      </div>
      <we-pagination
        :pageSizesOption="[8, 12, 16, 24, 32]"
        :total="total"
        :page_offset="pageData.page_offset"
        :page_size="pageData.page_size"
        @paginationChange="paginationHandle"
      ></we-pagination>
    </div>
    <el-empty v-if="!total" :image-size="120" desccription="暂无数据">
      <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
    </el-empty>
  </div>
</template>
<script>
import wePagination from '@/components/wePagination.vue'
import { serviceManageServer } from 'Api'
import { handleParamsValid } from 'Utils/index.js'
import { serviceTypeEnum, servicePulishStatusList } from 'Utils/constant.js'
import { mapGetters } from 'vuex'
import serviceCard from '@/components/serviceCard.vue'
export default {
  name: 'serverManage',
  components: {
    wePagination,
    serviceCard
  },
  data() {
    return {
      searchForm: {
        agency: '',
        owner: '',
        serviceName: '',
        createTime: '',
        status: ''
      },
      searchQuery: {
        agency: '',
        owner: '',
        serviceName: '',
        createTime: '',
        status: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 8
      },
      total: 10,
      queryFlag: false,
      tableData: [],
      loadingFlag: false,
      showAddModal: false,
      serviceTypeEnum,
      serverStatusList: [
        {
          label: '发布中',
          value: 'Publishing'
        },
        {
          label: '发布中',
          value: 'PublishSuccess'
        },
        {
          label: '发布失败',
          value: 'PublishFailed'
        }
      ],
      selectdDataList: [],
      showApplySelect: false,
      servicePulishStatusList,
      activeName: 'IsOwner'
    }
  },
  computed: {
    ...mapGetters(['agencyList', 'userId', 'agencyId'])
  },
  created() {
    this.getPublishList()
  },
  methods: {
    handleClick() {
      this.getPublishList()
    },
    getParams() {
      const { activeName } = this
      let params = {}
      switch (activeName) {
        case 'IsOwner':
          params = { agency: this.agencyId, owner: this.userId }
          break
        default:
          params = { authStatus: activeName }
          break
      }
      return params
    },
    cancelApply() {
      this.showApplySelect = false
      this.selectdDataList = []
      this.tableData = this.tableData.map((v) => {
        return {
          ...v,
          showSelect: false
        }
      })
    },
    selected(checked, row) {
      const { serviceId } = row
      if (checked) {
        this.selectdDataList.push({ ...row })
      } else {
        this.selectdDataList = this.selectdDataList.filter((v) => v.serviceId !== serviceId)
      }
    },
    applyAuth() {
      const serviceId = encodeURIComponent(this.selectdDataList.map((v) => v.serviceId).join(','))
      this.$router.push({ path: '/dataApply', query: { serviceId, applyType: 'wedpr_service_auth' } })
    },
    reset() {
      this.$refs.searchForm.resetFields()
    },
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.getPublishList()
        } else {
          return false
        }
      })
    },
    // 删除服务
    showDeleteModal(serverData) {
      const { serviceId } = serverData
      this.$confirm('确认删除服务吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.deleteServer({ serviceId })
        })
        .catch(() => {})
    },
    async deleteServer(params) {
      const res = await serviceManageServer.revokeService(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('服务删除成功')
        this.getPublishList()
      }
    },
    startApplySelect() {
      this.showApplySelect = true
      this.selectdDataList = []
      this.filterApplyAbleSelectData()
    },
    filterApplyAbleSelectData() {
      this.tableData = this.tableData.map((v) => {
        return {
          ...v,
          showSelect: !v.isOnwer
        }
      })
    },
    // 获取服务列表
    async getPublishList() {
      const { page_offset, page_size } = this.pageData
      const { agency = '', owner = '', serviceName = '', createTime = '', status } = this.searchQuery
      let params = handleParamsValid({ agency, owner, serviceName, status })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      params = { condition: { ...params, serviceId: '', ...this.getParams() }, serviceIdList: [], pageNum: page_offset, pageSize: page_size }
      this.loadingFlag = true
      const res = await serviceManageServer.getPublishList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { wedprPublishedServiceList = [], total } = res.data
        this.tableData = wedprPublishedServiceList.map((v) => {
          return {
            ...v,
            isOnwer: v.owner === this.userId && v.agency === this.agencyId,
            showSelect: false
          }
        })
        if (this.showApplySelect) {
          this.filterApplyAbleSelectData()
        }
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getPublishList()
    },
    creatPsi() {
      this.$router.push({ path: '/pirServerCreate' })
    },
    creatModel() {
      this.$router.push({ path: '/modelServerCreate' })
    },

    modifyData(item) {
      const { serviceId, serviceType } = item
      switch (serviceType) {
        case serviceTypeEnum.PIR:
          this.$router.push({ path: '/pirServerCreate', query: { type: 'edit', serviceId } })
          break
        case serviceTypeEnum.XGB:
          this.$router.push({ path: '/modelServerCreate', query: { type: 'edit', serviceId } })
          break
        case serviceTypeEnum.LR:
          this.$router.push({ path: '/modelServerCreate', query: { type: 'edit', serviceId } })
          break
        default:
          break
      }
    }
  }
}
</script>
<style lang="less" scoped>
div.op-container {
  margin: 16px 0;
  div.btn {
    padding: 5px 16px;
    border-radius: 4px;
    cursor: pointer;
    background: #51a14e;
    color: white;
    display: inline-block;
    font-size: 14px;
    line-height: 22px;
    margin-right: 12px;
  }
  div.btn:hover {
    opacity: 0.8;
  }
  div.model {
    background: #0ca8ff;
  }
  img.icon-btn {
    width: 16px;
    height: 16px;
    transform: translateY(2px);
    margin-right: 9px;
  }
}
div.card-container {
  margin-left: -16px;
  margin-right: -16px;
  overflow: hidden;
}
div.apply-container {
  display: flex;
  div.handle{
    padding-left: 20px;
  }
}
div.handle {
  span {
    width: 75px;
    color: #787b84;
    float: left;
    line-height: 32px;
  }
  ::v-deep .el-button--small {
    margin-left: 10px;
  }
}
</style>
