<template>
  <div class="group-manage" style="position: relative; height: 100%">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item prop="ownerAgency" label="创建机构：">
          <el-select clearable style="width: 160px" v-model="searchForm.ownerAgency" placeholder="请选择">
            <el-option v-for="item in agencyList" :label="item.label" :value="item.value" :key="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="name" label="项目名称：">
          <el-select filterable clearable style="width: 160px" v-model="searchForm.name" remote :remote-method="getProjectNameSelect" placeholder="请输入">
            <el-option v-for="item in projectNameSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="createTime" label="创建时间：">
          <el-date-picker
            value-format="yyyy-MM-dd HH:mm:ss"
            v-model="searchForm.createTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          >
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
      </el-form>
    </div>
    <div class="record">
      <div class="card-container" v-if="tableData.length">
        <div class="card" v-for="item in tableData" :key="item.id" @click="goDetail(item)">
          <div class="bg">
            <img :src="bindIcon(item.randomIndex)" alt="" />
          </div>
          <div class="info">
            <div class="title">
              <span :title="item.name">{{ item.name }}</span>
            </div>
            <ul>
              <li>
                所属机构 <span>{{ item.ownerAgency }}</span>
              </li>
              <li>
                所属用户 <span>{{ item.owner }}</span>
              </li>
              <li>
                创建时间 <span>{{ item.createTime }}</span>
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
import { projectManageServer } from 'Api'
import wePagination from '@/components/wePagination.vue'
import { handleParamsValid } from 'Utils/index.js'
import { mapGetters } from 'vuex'
export default {
  name: 'adminProjectManage',
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
      projectNameSelectList: []
    }
  },
  computed: {
    ...mapGetters(['agencyList'])
  },
  created() {
    this.queryProject()
  },
  methods: {
    bindIcon(randomIndex) {
      return require('../../assets/images/cover/pro' + randomIndex + '.png')
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
      this.$router.push({ path: '/projectDetail', query: { projectId: row.id } })
    },
    async getProjectNameSelect(projectName) {
      if (!projectName) {
        this.projectNameSelectList = []
        return
      }
      const res = await projectManageServer.adminQueryProject({ pageNum: 1, pageSize: 9999 })
      if (res.code === 0 && res.data) {
        const { projectList = [] } = res.data
        this.projectNameSelectList = projectList.map((v) => {
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
    async queryProject() {
      console.log(this.searchQuery, 'this.searchQuery')
      const { page_offset, page_size } = this.pageData
      const { createTime, name, ownerAgency } = this.searchQuery
      const params = handleParamsValid({ name, ownerAgency })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      this.loadingFlag = true
      console.log(params)
      // FIXME:
      const res = await projectManageServer.adminQueryProject({ ...params, pageNum: page_offset, pageSize: page_size })
      this.loadingFlag = false
      if (res.code === 0 && res.data) {
        const { projectList = [], total } = res.data
        this.tableData = projectList.map((v) => {
          return {
            ...v,
            randomIndex: (v.id % 7) + 1
          }
        })
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
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
    div.bg {
      img {
        width: 100%;
        height: auto;
      }
    }
    div.info {
      padding: 20px;
    }
    div.title {
      font-size: 16px;
      line-height: 24px;
      font-family: PingFang SC;
      display: flex;
      align-items: center;
      margin-bottom: 24px;
      color: #262a32;
      img {
        width: 24px;
        height: 24px;
      }
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
        width: 33%;
        dt {
          font-size: 12px;
          line-height: 20px;
        }
        dd {
          color: #262a32;
          font-size: 14px;
          line-height: 24px;
          font-weight: 500;
          width: 100%;
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
        span.data-size {
          i {
            font-size: 28px;
            font-style: normal;
          }
        }
      }
      li:first-child {
        line-height: 28px;
      }
    }
  }
}
div.card:hover {
  box-shadow: 0px 2px 10px 2px #00000014;
  cursor: pointer;
}
</style>
