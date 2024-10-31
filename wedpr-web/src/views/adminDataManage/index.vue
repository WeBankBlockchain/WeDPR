<template>
  <div class="group-manage">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item prop="ownerAgencyName" label="所属机构：">
          <el-select clearable size="small" style="width: 160px" v-model="searchForm.ownerAgencyName" placeholder="请选择">
            <el-option :key="item" v-for="item in agencyList" multiple :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="datasetTitle" label="资源名称：">
          <el-input style="width: 160px" v-model="searchForm.datasetTitle" placeholder="请输入"> </el-input>
        </el-form-item>
        <el-form-item prop="createTime" label="创建时间：">
          <el-date-picker
            style="width: 360px"
            value-format="yyyy-MM-dd HH:mm:ss"
            v-model="searchForm.createTime"
            type="datetimerange"
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
          <el-button type="default" :loading="queryFlag" @click="reset"> 重置 </el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="card-container" v-if="total">
      <dataCard :showOwner="false" :showAuthTags="false" :showEdit="false" @getDetail="getDetail(item)" v-for="item in dataList" :dataInfo="item" :key="item.datasetId" />
    </div>
    <el-empty v-if="!total" :image-size="120" description="暂无数据">
      <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
    </el-empty>
    <we-pagination
      :pageSizesOption="[12, 24, 36, 48]"
      :total="total"
      :page_offset="pageData.page_offset"
      :page_size="pageData.page_size"
      @paginationChange="paginationHandle"
    ></we-pagination>
  </div>
</template>
<script>
import { dataManageServer } from 'Api'
import wePagination from '@/components/wePagination.vue'
import dataCard from '@/components/dataCard.vue'
import { uploadFile } from 'Mixin/uploadFile.js'
import { mapGetters } from 'vuex'
import { handleParamsValid } from 'Utils/index.js'
export default {
  name: 'adminDataManage',
  mixins: [uploadFile],
  components: {
    wePagination,
    dataCard
  },
  data() {
    return {
      searchForm: {
        ownerAgencyName: '',
        datasetTitle: '',
        createTime: ''
      },
      searchQuery: {
        ownerAgencyName: '',
        datasetTitle: '',
        createTime: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 12
      },
      total: -1,
      queryFlag: false,
      dataList: [],
      loadingFlag: false
    }
  },
  created() {
    this.getListDataset()
    console.log(this.agencyList, 'agencyList')
  },
  computed: {
    ...mapGetters(['agencyList', 'userId', 'agencyId'])
  },
  methods: {
    getDetail(row) {
      this.$router.push({ path: '/dataDetail', query: { datasetId: row.datasetId } })
    },
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.getListDataset()
        } else {
          return false
        }
      })
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getListDataset()
    },

    // 获取数据集列表
    async getListDataset() {
      const { page_offset, page_size } = this.pageData
      const { ownerAgencyName = '', datasetTitle = '', createTime = '' } = this.searchQuery
      let params = handleParamsValid({ ownerAgencyName, datasetTitle })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      params = { ...params, pageNum: page_offset, pageSize: page_size }
      this.loadingFlag = true
      const res = await dataManageServer.adminListDataset(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { content = [], totalCount } = res.data
        this.dataList = content.map((v) => {
          return {
            ...v,
            permissions: v.permissions || {},
            isOwner: v.ownerAgencyName === this.agencyId && v.ownerUserName === this.userId
          }
        })
        this.total = totalCount
      } else {
        this.dataList = []
        this.total = 0
      }
    },
    reset() {
      this.$refs.searchForm.resetFields()
    }
  }
}
</script>
<style lang="less" scoped>
div.card-container {
  overflow: hidden;
  margin-left: -16px;
  margin-right: -16px;
}
</style>
