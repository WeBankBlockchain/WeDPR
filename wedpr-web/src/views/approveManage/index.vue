<template>
  <div class="approve-manage" style="position: relative; height: 100%">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <div>
          <el-form-item prop="status" label="审批状态：">
            <el-select style="width: 160px" v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option v-for="item in approveStatusList" :label="item.label" :value="item.value" :key="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="applyTitle" label="审批单名称：">
            <el-input style="width: 160px" v-model="searchForm.applyTitle" placeholder="请输入"> </el-input>
          </el-form-item>
          <el-form-item prop="createTime" label="申请时间：">
            <el-date-picker value-format="yyyy-MM-dd" v-model="searchForm.createTime" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="queryFlag" @click="queryHandle">
              {{ queryFlag ? '查询中...' : '查询' }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="default" :loading="queryFlag" @click="reset"> 重置 </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
      <el-tab-pane label="我的审批" name="first">
        <div class="tableContent autoTableWrap" v-if="total">
          <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="tableData" :border="true" class="table-wrap">
            <el-table-column label="申请表单ID" prop="id" show-overflow-tooltip />
            <el-table-column label="表单名称" prop="applyTitle" show-overflow-tooltip />
            <el-table-column label="申请机构" prop="applicantAgency" show-overflow-tooltip />
            <el-table-column label="申请用户" prop="applicant" show-overflow-tooltip />
            <el-table-column label="申请时间" prop="createTime" />
            <el-table-column label="状态" prop="status">
              <template v-slot="scope">
                <el-tag size="small" v-if="SuccessStatus.includes(scope.row.status)" effect="dark" color="#52B81F">{{ approveStatusMap[scope.row.status] }}</el-tag>
                <el-tag size="small" v-else-if="FailStatus.includes(scope.row.status)" effect="dark" color="#FF4D4F">{{ approveStatusMap[scope.row.status] }}</el-tag>
                <el-tag size="small" v-else-if="PendingStatus.includes(scope.row.status)" effect="dark" color="#3071F2">{{ approveStatusMap[scope.row.status] }}</el-tag>
                <el-tag size="small" v-else effect="dark" color="#3071F2">{{ approveStatusMap[scope.row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180px">
              <template v-slot="scope">
                <el-button @click="openDetail(scope.row)" size="small" type="text">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的申请" name="second">
        <div class="tableContent autoTableWrap" v-if="total">
          <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="tableData" :border="true" class="table-wrap">
            <el-table-column label="申请表单ID" prop="id" show-overflow-tooltip />
            <el-table-column label="表单名称" prop="applyTitle" show-overflow-tooltip />
            <el-table-column label="申请机构" prop="applicantAgency" show-overflow-tooltip />
            <el-table-column label="申请用户" prop="applicant" show-overflow-tooltip />
            <el-table-column label="申请时间" prop="createTime" show-overflow-tooltip />
            <el-table-column label="状态" prop="status">
              <template v-slot="scope">
                <el-tag size="small" v-if="SuccessStatus.includes(scope.row.status)" effect="dark" color="#52B81F">{{ approveStatusMap[scope.row.status] }}</el-tag>
                <el-tag size="small" v-else-if="FailStatus.includes(scope.row.status)" effect="dark" color="#FF4D4F">{{ approveStatusMap[scope.row.status] }}</el-tag>
                <el-tag size="small" v-else-if="PendingStatus.includes(scope.row.status)" effect="dark" color="#3071F2">{{ approveStatusMap[scope.row.status] }}</el-tag>
                <el-tag size="small" v-else effect="dark" color="#3071F2">{{ approveStatusMap[scope.row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180px">
              <template v-slot="scope">
                <el-button @click="openDetail(scope.row)" size="small" type="text">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的关注" name="third">
        <div class="tableContent autoTableWrap" v-if="total">
          <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="tableData" :border="true" class="table-wrap">
            <el-table-column label="申请表单ID" prop="id" show-overflow-tooltip />
            <el-table-column label="表单名称" prop="applyTitle" show-overflow-tooltip />
            <el-table-column label="申请机构" prop="applicantAgency" show-overflow-tooltip />
            <el-table-column label="申请用户" prop="applicant" show-overflow-tooltip />
            <el-table-column label="申请时间" prop="createTime" show-overflow-tooltip />
            <el-table-column label="状态" prop="status">
              <template v-slot="scope">
                <el-tag size="small" v-if="SuccessStatus.includes(scope.row.status)" effect="dark" color="#52B81F">{{ approveStatusMap[scope.row.status] }}</el-tag>
                <el-tag size="small" v-else-if="FailStatus.includes(scope.row.status)" effect="dark" color="#FF4D4F">{{ approveStatusMap[scope.row.status] }}</el-tag>
                <el-tag size="small" v-else-if="PendingStatus.includes(scope.row.status)" effect="dark" color="#3071F2">{{ approveStatusMap[scope.row.status] }}</el-tag>
                <el-tag size="small" v-else effect="dark" color="#3071F2">{{ approveStatusMap[scope.row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180px">
              <template v-slot="scope">
                <el-button @click="openDetail(scope.row)" size="small" type="text">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
        </div>
      </el-tab-pane>
    </el-tabs>
    <el-empty v-if="!total" :image-size="120" description="暂无数据">
      <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
    </el-empty>
  </div>
</template>
<script>
import { authManageServer } from 'Api'
import wePagination from '@/components/wePagination.vue'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { approveStatusList, approveStatusMap } from 'Utils/constant.js'
import { handleParamsValid } from 'Utils/index.js'
export default {
  name: 'approveManage',
  mixins: [tableHeightHandle],
  components: {
    wePagination
  },
  data() {
    return {
      PendingStatus: ['ToConfirm', 'Progressing', 'Approving'],
      FailStatus: ['ApproveFailed', 'ProgressFailed', 'ApproveRejected', 'ApproveCanceled'],
      SuccessStatus: ['ApproveSuccess', 'ProgressSuccess'],
      searchForm: {
        status: '',
        applyTitle: '',
        createTime: ''
      },
      searchQuery: {
        status: '',
        applyTitle: '',
        createTime: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 20
      },
      total: -1,
      queryFlag: false,
      tableData: [],
      loadingFlag: false,
      showAddModal: false,
      activeName: 'first',
      approveStatusList,
      approveStatusMap
    }
  },
  created() {
    this.queryFollowerAuthList()
  },
  watch: {
    activeName(nv) {
      this.$refs.searchForm.resetFields()
      this.pageData = {
        page_offset: 1,
        page_size: 20
      }
      switch (nv) {
        case 'first':
          this.queryFollowerAuthList()
          break
        case 'second':
          this.queryMyApplyList()
          break
        case 'third':
          this.queryMyFollowList()
          break
      }
    }
  },
  methods: {
    openDetail(row) {
      this.$router.push({ path: '/approveDetail', query: { authID: row.id } })
    },
    // 查询
    queryHandle() {
      this.searchQuery = { ...this.searchForm }
      this.pageData.page_offset = 1
      const { activeName } = this
      switch (activeName) {
        case 'first':
          this.queryFollowerAuthList()
          break
        case 'second':
          this.queryMyApplyList()
          break
        case 'third':
          this.queryMyFollowList()
          break
      }
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.queryFollowerAuthList()
    },

    // 获取我的审批列表
    async queryFollowerAuthList() {
      const { page_offset, page_size } = this.pageData
      const { status = '', applyTitle = '', createTime = '' } = this.searchQuery
      this.tableData = []
      let params = handleParamsValid({ applyTitle })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      if (status) {
        if (status === 'ApproveFailed') {
          params.authStatusList = ['ApproveFailed', 'ProgressFailed']
        } else {
          params.authStatusList = [status]
        }
      }
      params = {
        authFollowerDO: { id: '', followerType: 'auth_auditor' },
        condition: {
          id: '',
          ...params
        },
        pageOffset: page_offset,
        pageSize: page_size
      }
      this.loadingFlag = true
      const res = await authManageServer.queryFollowerAuthList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { dataList = [], total } = res.data
        this.tableData = dataList
        console.log(this.tableData)
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    // 获取我的申请列表
    async queryMyApplyList() {
      const { page_offset, page_size } = this.pageData
      const { status = '', applyTitle = '', createTime = '' } = this.searchQuery
      this.tableData = []
      let params = handleParamsValid({ applyTitle })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      if (status) {
        if (status === 'ApproveFailed') {
          params.authStatusList = ['ApproveFailed', 'ProgressFailed']
        } else {
          params.authStatusList = [status]
        }
      }
      params = {
        authorizationDO: { id: '', ...params, followerType: 'auth_auditor' },
        pageOffset: page_offset,
        pageSize: page_size
      }
      this.loadingFlag = true
      const res = await authManageServer.queryAuthList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { dataList = [], total } = res.data
        this.tableData = dataList
        console.log(this.tableData)
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    // 获取我的关注列表
    async queryMyFollowList() {
      const { page_offset, page_size } = this.pageData
      const { status = '', applyTitle = '', createTime = '' } = this.searchQuery
      this.tableData = []
      let params = handleParamsValid({ applyTitle })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      if (status) {
        if (status === 'ApproveFailed') {
          params.authStatusList = ['ApproveFailed', 'ProgressFailed']
        } else {
          params.authStatusList = [status]
        }
      }
      params = { authFollowerDO: { ...params, id: '', followerType: 'auth_follower' }, pageOffset: page_offset, pageSize: page_size }
      this.loadingFlag = true
      const res = await authManageServer.queryFollowerAuthList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { dataList = [], total } = res.data
        this.tableData = dataList
        console.log(this.tableData)
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    reset() {
      this.$refs.searchForm.resetFields()
    },
    createAccount() {
      this.showAddModal = true
    },
    closeModal() {
      this.showAddModal = false
    },
    handlOK() {
      this.showAddModal = false
      this.getAccountList()
    }
  }
}
</script>
<style lang="less" scoped>
.approve-manage {
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
  }
}
</style>
