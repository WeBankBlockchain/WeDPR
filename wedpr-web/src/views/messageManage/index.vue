<template>
  <div class="group-manage">
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
      <el-tab-pane label="参与任务" name="first">
        <div class="form-search">
          <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
            <el-form-item prop="username" label="任务模板：">
              <el-select style="width: 160px" v-model="searchForm.jobType" placeholder="请选择模板" clearable>
                <el-option :label="item.label" :value="item.value" v-for="item in algList" :key="item.value"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item prop="name" label="任务名称：">
              <el-input style="width: 160px" v-model="searchForm.name" placeholder="请输入" />
            </el-form-item>
            <el-form-item prop="status" label="任务状态：">
              <el-select style="width: 160px" v-model="searchForm.status" placeholder="请选择" clearable>
                <el-option :label="item.label" :value="item.value" v-for="item in jobStatusList" :key="item.value"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item prop="createTime" label="创建时间：">
              <el-date-picker style="width: 160px" v-model="searchForm.createTime" type="date" placeholder="请选择日期"> </el-date-picker>
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
            <el-table-column label="任务模板" prop="jobType">
              <template v-slot="scope"> <img class="type-img" :src="handleData(scope.row.jobType).src" /> {{ handleData(scope.row.jobType).label }} </template>
            </el-table-column>
            <el-table-column label="任务ID" prop="id" />
            <el-table-column label="创建时间" prop="createTime" />
            <el-table-column label="发起机构" prop="ownerAgency" />
            <el-table-column label="发起人" prop="owner" />
            <el-table-column label="任务状态" prop="status">
              <template v-slot="scope">
                <el-tag size="small" v-if="scope.row.status === 'RunSuccess'" effect="dark" color="#52B81F">成功</el-tag>
                <el-tag size="small" v-else-if="scope.row.status == 'RunFailed'" effect="dark" color="#FF4D4F">失败</el-tag>
                <el-tag size="small" v-else-if="scope.row.status === 'Running'" effect="dark" color="#3071F2">运行中</el-tag>
                <el-tag size="small" v-else effect="dark" color="#3071F2">{{ jobStatusMap[scope.row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template v-slot="scope">
                <el-button @click="goDetail(scope.row.id)" size="small" type="text">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
        </div>
      </el-tab-pane>
      <el-tab-pane v-if="false" label="参与项目" name="second">
        <div class="tableContent autoTableWrap" v-if="total">
          <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="tableData" :border="true" class="table-wrap">
            <el-table-column label="项目ID" prop="projectId" />
            <el-table-column label="项目名称" prop="projectName" />
            <el-table-column label="发起机构|用户" prop="initAgency">
              <template v-slot="scope"> {{ scope.row.initAgency }} | {{ scope.row.creater }} </template>
            </el-table-column>
            <el-table-column label="参与机构数" prop="participateCount" />
            <el-table-column label="参与机构" prop="participateAgencyNames" />
            <el-table-column label="操作">
              <template v-slot="scope">
                <el-button @click="handleAgree(scope.row, '驳回')" size="small" type="text">查看日志</el-button>
                <el-button @click="handleAgree(scope.row, '同意')" size="small" type="text">操作日志</el-button>
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
import { jobManageServer } from 'Api'
import wePagination from '@/components/wePagination.vue'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { jobStatusMap } from 'Utils/constant.js'
import { mapGetters } from 'vuex'
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
        jobType: '',
        name: '',
        status: '',
        createTime: ''
      },
      searchQuery: {
        jobType: '',
        name: '',
        status: '',
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
      typeList: [],
      jobStatusMap
    }
  },
  computed: {
    ...mapGetters(['algList'])
  },
  created() {
    this.queryFollowerJobByCondition()
  },
  methods: {
    handleData(key) {
      const data = this.algList.filter((v) => v.value === key)
      return data[0] || {}
    },
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.queryFollowerJobByCondition()
        } else {
          return false
        }
      })
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.queryFollowerJobByCondition()
    },
    // 获取消息列表
    async queryFollowerJobByCondition() {
      const { page_offset, page_size } = this.pageData
      const { jobType, name, status, createTime } = this.searchForm
      const params = handleParamsValid({ jobType, name, status })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      this.loadingFlag = true
      const res = await jobManageServer.queryFollowerJobByCondition({
        job: {
          id: '',
          ...params
        },
        pageNum: page_offset,
        pageSize: page_size
      })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { jobs = [], total } = res.data
        this.tableData = jobs
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    goDetail(id) {
      this.$router.push({ path: '/jobDetail', query: { id } })
    },
    reset() {
      this.$refs.searchForm.resetFields()
    }
  }
}
</script>
<style lang="less" scoped>
.type-img {
  width: 42px;
  height: auto;
  vertical-align: middle;
  margin-right: 10px;
}
.tableContent {
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
  }
}
</style>
