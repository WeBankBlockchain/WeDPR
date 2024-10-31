<template>
  <div class="project-manage" style="position: relative; height: 100%">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item prop="name" label="项目名称：">
          <el-select filterable clearable style="width: 160px" v-model="searchForm.name" remote :remote-method="getProjectNameSelect" placeholder="请输入">
            <el-option v-for="item in projectNameSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="createTime" label="创建时间：">
          <el-date-picker value-format="yyyy-MM-dd" v-model="searchForm.createTime" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button :disabled="showDeleteSelect" type="primary" :loading="queryFlag" @click="queryHandle">
            {{ queryFlag ? '查询中...' : '查询' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="default" :disabled="showDeleteSelect" :loading="queryFlag" @click="reset"> 重置 </el-button>
        </el-form-item>
        <el-form-item>
          <el-button icon="el-icon-plus" type="primary" @click="createProject"> 新建项目 </el-button>
        </el-form-item>
        <el-form-item>
          <el-button icon="el-icon-folder-opened" type="primary" @click="getJupterLink"> 打开juypter </el-button>
        </el-form-item>
        <el-form-item v-if="!showDeleteSelect">
          <el-button icon="el-icon-delete" style="color: red" @click="startDelete"> 批量删除 </el-button>
        </el-form-item>
      </el-form>
      <div class="handle" v-if="showDeleteSelect">
        <span>已选{{ selectdDataList.length }}项</span><el-button style="color: red" size="small" :disabled="!selectdDataList.length" @click="showDeleteMore"> 确认删除 </el-button
        ><el-button size="small" type="info" @click="cancelDelete"> 取消 </el-button>
      </div>
    </div>
    <div class="record">
      <div class="card-container" v-if="tableData.length">
        <div class="card" v-for="item in tableData" :key="item.id" @click="goDetail(item)">
          <div class="bg">
            <img :src="bindIcon(item.randomIndex)" alt="" />
            <el-checkbox v-if="item.showSelect" @change="(checked) => handleSelect(checked, item)" :value="selectdDataList.map((v) => v.id).includes(item.id)"></el-checkbox>
          </div>
          <div class="info">
            <div class="project-title">
              <span :title="item.name">{{ item.name }}</span>
            </div>
            <ul>
              <li class="ell" @click.stop="() => {}">
                项目ID: <span>{{ item.id }}</span>
              </li>
              <li class="ell">
                发起人： <span>{{ item.owner }}</span>
              </li>
              <li>
                创建时间： <span>{{ item.createTime }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <el-empty v-else :image-size="120" description="暂无数据">
        <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
      </el-empty>
      <we-pagination
        :pageSizesOption="[8, 12, 16, 24, 32]"
        :total="total"
        :page_offset="pageData.page_offset"
        :page_size="pageData.page_size"
        @paginationChange="paginationHandle"
      ></we-pagination>
    </div>
  </div>
</template>
<script>
import { projectManageServer, jupyterManageServer } from 'Api'
import wePagination from '@/components/wePagination.vue'
import { handleParamsValid } from 'Utils/index.js'
import { mapGetters } from 'vuex'
export default {
  name: 'dataManage',
  components: {
    wePagination
  },
  data() {
    return {
      searchForm: {
        createTime: [],
        name: ''
      },
      searchQuery: {
        createTime: [],
        name: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 8
      },
      total: -1,
      queryFlag: false,
      tableData: [],
      loadingFlag: false,
      showAddModal: false,
      projectNameSelectList: [],
      selectdDataList: [],
      showDeleteSelect: false
    }
  },
  computed: {
    ...mapGetters(['authorization'])
  },
  created() {
    this.queryProject()
  },
  methods: {
    handleSelect(checked, row) {
      const { id } = row
      if (checked) {
        this.selectdDataList.push({ ...row })
      } else {
        this.selectdDataList = this.selectdDataList.filter((v) => v.id !== id)
      }
      console.log(this.selectdDataList, 'selectdDataList')
    },
    cancelDelete() {
      this.showDeleteSelect = false
      this.selectdDataList = []
      this.tableData = this.tableData.map((v) => {
        return {
          ...v,
          showSelect: false
        }
      })
    },
    startDelete() {
      this.showDeleteSelect = true
      this.selectdDataList = []
      this.filterDeleteAbleSelectData()
    },
    // 删除项目
    showDeleteMore() {
      this.$confirm('确认批量删除项目吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          // const params = JSON.stringify(this.selectdDataList.map((v) => v.id))
          this.deleteProject(this.selectdDataList.map((v) => v.id))
        })
        .catch(() => {})
    },
    async getJupterLink(params) {
      const res = await jupyterManageServer.getJupterLink(params)
      if (res.code === 0 && res.data) {
        const { jupyters = [] } = res.data
        if (jupyters.length) {
          const { jupyterAccessUrl = 'http://139.159.202.235:9401/lab' } = jupyters[0]
          window.open(jupyterAccessUrl + 'Authorization=' + this.authorization)
        } else {
          window.open('http://139.159.202.235:9401/lab?Authorization=' + this.authorization)
        }
      }
    },
    bindIcon(randomIndex) {
      return require('../../assets/images/cover/pro' + randomIndex + '.png')
    },
    async allocate(params) {
      const res = await jupyterManageServer.allocate(params)
      console.log(res)
    },
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.queryProject()
        } else {
          return false
        }
      })
    },
    goDetail(row) {
      if (!this.showDeleteSelect) {
        this.$router.push({ path: '/projectDetail', query: { projectId: row.id } })
      }
    },
    async getProjectNameSelect(projectName) {
      if (!projectName) {
        this.projectNameSelectList = []
        return
      }
      const res = await projectManageServer.queryProject({ project: { id: '', name: projectName }, onlyMeta: true, pageNum: 1, pageSize: 9999 })
      if (res.code === 0 && res.data) {
        const { dataList = [] } = res.data
        this.projectNameSelectList = dataList.map((v) => {
          return {
            label: v.name,
            value: v.name
          }
        })
      } else {
        this.projectNameSelectList = []
      }
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.queryProject()
    },
    filterDeleteAbleSelectData() {
      this.tableData = this.tableData.map((v) => {
        return {
          ...v,
          showSelect: v.isOwner
        }
      })
    },
    async queryProject() {
      console.log(this.searchQuery, 'this.searchQuery')
      const { page_offset, page_size } = this.pageData
      const { createTime, name } = this.searchQuery
      const params = handleParamsValid({ name })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      this.loadingFlag = true
      console.log(params)
      // FIXME:
      const res = await projectManageServer.queryProject({ project: { id: '', ...params }, onlyMeta: false, pageNum: page_offset, pageSize: page_size })
      this.loadingFlag = false
      if (res.code === 0 && res.data) {
        const { dataList = [], total } = res.data
        this.tableData = dataList.map((v) => {
          return {
            ...v,
            randomIndex: (v.id % 7) + 1,
            isOwner: v.ownerAgencyName === this.agencyId && v.ownerUserName === this.userId,
            showSelect: false
          }
        })
        // 翻页后初始化选择按钮
        if (this.showDeleteSelect) {
          this.filterDeleteAbleSelectData()
        }
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    async deleteProject(params) {
      const res = await projectManageServer.deleteProject(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('项目批量删除成功')
        this.showDeleteSelect = false
        this.selectdDataList = []
        this.queryProject()
      }
    },
    reset() {
      this.$refs.searchForm.resetFields()
    },
    createProject() {
      this.$router.push({ path: '/projectCreate' })
    }
  }
}
</script>
<style lang="less" scoped>
.project-manage {
  div.card-container {
    overflow: hidden;
    margin-left: -16px;
    margin-right: -16px;
    div.card {
      float: left;
      background: #f6fcf9;
      height: auto;
      border: 1px solid #e0e4ed;
      border-radius: 12px;
      margin: 16px;
      width: calc(25% - 32px);
      box-sizing: border-box;
      min-width: 220px;
      position: relative;
      ::v-deep .el-checkbox {
        position: absolute;
        right: 10px;
        top: 10px;
      }
      ::v-deep .el-checkbox__inner {
        border-radius: 50%;
        width: 20px;
        height: 20px;
        line-height: 20px;
        font-size: 16px;
        border: 1px solid #3071f2;
        box-shadow: 0 0 3px #3071f2;
      }
      ::v-deep .el-checkbox__inner::after {
        left: 7px;
        width: 4px;
        height: 8px;
        top: 3px;
      }
      div.bg {
        position: relative;
        overflow: hidden;
        img {
          width: 100%;
          height: auto;
        }
      }
      div.info {
        padding: 20px;
      }
      div.project-title {
        font-size: 16px;
        line-height: 24px;
        font-family: PingFang SC;
        margin-bottom: 24px;
        color: #262a32;
        span {
          display: inline-block;
          width: 100%;
          text-align: left;
          font-weight: bold;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
      }
      div.count-detail {
        display: flex;
        justify-content: space-between;
        margin-bottom: 16px;
        dl {
          color: #787b84;
          dt {
            font-size: 12px;
            line-height: 20px;
          }
          dd {
            color: #262a32;
            font-size: 16px;
            line-height: 24px;
            font-weight: 500;
          }
        }
      }
      ul {
        li {
          font-size: 12px;
          line-height: 20px;
          margin-bottom: 4px;
          color: #787b84;
          display: flex;
          align-items: center;
          span {
            text-align: right;
            color: #262a32;
            flex: 1;
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
          }
        }
      }
    }
  }
}

div.card:hover {
  box-shadow: 0px 2px 10px 2px #00000014;
  cursor: pointer;
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
