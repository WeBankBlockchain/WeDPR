<template>
  <div class="group-manage" style="position: relative; height: 100%">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item prop="resourceType" label="日志类型：">
          <el-select style="width: 160px" @change="handleTypeChange" v-model="searchForm.resourceType" placeholder="请选择" clearable>
            <el-option :label="item.label" :value="item.value" v-for="item in opTypeList" :key="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="resourceAction" label="操作：">
          <el-select style="width: 160px" v-model="searchForm.resourceAction" placeholder="请选择" clearable>
            <el-option :label="item.label" :value="item.value" v-for="item in actionList" :key="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="status" label="操作状态：">
          <el-select style="width: 160px" v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option :label="item.label" :value="item.value" v-for="item in actionStatusList" :key="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="createTime" label="生成时间：">
          <el-date-picker value-format="yyyy-MM-dd" v-model="searchForm.createTime" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="queryFlag" @click="queryHandle">
            {{ queryFlag ? '查询中...' : '查询' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="default" :loading="queryFlag" @click="reset"> 重置 </el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="tableContent autoTableWrap" v-if="total">
      <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="tableData" :border="true" class="table-wrap">
        <el-table-column show-overflow-tooltip label="日志ID" prop="resourceID" />
        <el-table-column label="日志类型" prop="resourceType">
          <template v-slot="scope">
            {{ opType[scope.row.resourceType] || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" prop="resourceAction">
          <template v-slot="scope">
            {{ actionMap[scope.row.resourceAction] || '--' }}
          </template>
        </el-table-column>
        <el-table-column show-overflow-tooltip label="操作时间" prop="createTime" />
        <el-table-column label="操作状态" prop="status">
          <template v-slot="scope">
            {{ actionStatus[scope.row.status] || '--' }}
          </template>
        </el-table-column>
        <el-table-column show-overflow-tooltip width="320px" label="链上存证" prop="transactionHash" />
      </el-table>
      <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
    </div>
    <el-empty v-if="!total" :image-size="120" description="暂无数据">
      <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
    </el-empty>
  </div>
</template>
<script>
import { projectManageServer } from 'Api'
import wePagination from '@/components/wePagination.vue'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { opType, opTypeList, actionMap, actionStatus, actionStatusList, actionMapList, dataActionMapList, approveActionMapList, jobActionMapList } from 'Utils/constant.js'
import { handleParamsValid } from 'Utils/index.js'
export default {
  name: 'groupManage',
  mixins: [tableHeightHandle],
  components: {
    wePagination
  },
  data() {
    return {
      searchForm: {
        createTime: '',
        resourceType: '',
        resourceAction: '',
        status: ''
      },
      searchQuery: {
        createTime: '',
        resourceType: '',
        resourceAction: '',
        status: ''
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
      opType,
      actionMap,
      actionStatus,
      opTypeList,
      actionStatusList,
      actionMapList,
      actionList: []
    }
  },
  created() {
    this.queryRecordSyncStatus()
    this.actionList = actionMapList
  },
  methods: {
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.queryRecordSyncStatus()
        } else {
          return false
        }
      })
    },
    reset() {
      this.$refs.searchForm.resetFields()
    },
    handleTypeChange(type) {
      console.log(type, 'type')
      this.searchForm.resourceAction = ''
      switch (type) {
        case 'Authorization':
          this.actionList = approveActionMapList
          break
        case 'Job':
          this.actionList = jobActionMapList
          break
        case 'Dataset':
          this.actionList = dataActionMapList
          break
        default:
          this.actionList = actionMapList
      }
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.queryRecordSyncStatus()
    },
    // 获取账户列表
    async queryRecordSyncStatus() {
      const { page_offset, page_size } = this.pageData
      const { createTime, resourceType, resourceAction, status } = this.searchQuery
      let params = handleParamsValid({ resourceType, resourceAction, status })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      params = { resourceActionDO: { ...params, resourceID: '' }, pageNum: page_offset, pageSize: page_size }
      this.loadingFlag = true
      const res = await projectManageServer.queryRecordSyncStatus(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { dataList = [], total } = res.data
        this.tableData = dataList
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    }
  }
}
</script>
<style lang="less" scoped></style>
