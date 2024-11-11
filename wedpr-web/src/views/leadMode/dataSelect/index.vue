<template>
  <div class="select-data">
    <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
      <el-form-item prop="datasetTitle" label="资源名称：">
        <el-input style="width: 160px" v-model="searchForm.datasetTitle" placeholder="请输入"> </el-input>
      </el-form-item>
      <el-form-item prop="ownerUserName" label="所属用户：">
        <el-input style="width: 160px" v-model="searchForm.ownerUserName" placeholder="请输入"> </el-input>
      </el-form-item>
      <el-form-item prop="createTime" label="创建时间：">
        <el-date-picker
          style="width: 360px"
          value-format="yyyy-MM-dd"
          v-model="searchForm.createTime"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loadingFlag" @click="queryHandle">
          {{ queryFlag ? '查询中...' : '查询' }}
        </el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="default" :loading="loadingFlag" @click="reset"> 重置 </el-button>
      </el-form-item>
    </el-form>
    <div class="card-container">
      <dataCard
        :showTags="false"
        :showEdit="false"
        @selected="(checked) => selected(checked, item)"
        :selected="selectdDataId === item.datasetId"
        showSelect
        v-for="item in dataList"
        :dataInfo="item"
        :key="item.datasetId"
      />
    </div>
    <el-empty v-if="!total" :image-size="120" description="暂无数据">
      <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
    </el-empty>
    <we-pagination
      :pageSizesOption="[4, 8, 12, 16, 24]"
      :total="total"
      :page_offset="pageData.page_offset"
      :page_size="pageData.page_size"
      @paginationChange="paginationHandle"
    ></we-pagination>
  </div>
</template>
<script>
import { dataManageServer } from 'Api'
import dataCard from '@/components/dataCard.vue'
import { mapGetters } from 'vuex'
import { handleParamsValid } from 'Utils/index.js'
export default {
  name: 'participateSelect',
  props: {
    showTagsModal: {
      type: Boolean,
      default: false
    },
    ownerAgencyName: {
      type: String,
      default: ''
    }
  },
  components: {
    dataCard
  },
  data() {
    return {
      searchForm: {
        datasetTitle: '',
        ownerUserName: '',
        tag: '',
        createTime: ''
      },
      searchQuery: {
        datasetTitle: '',
        ownerUserName: '',
        tag: '',
        createTime: ''
      },
      formLabelWidth: '112px',
      loadingFlag: false,
      groupId: '',
      pageData: { page_offset: 1, page_size: 4 },
      dataList: [],
      selectdDataId: '',
      selectedData: {},
      fieldList: []
    }
  },
  created() {
    this.selectdDataId = ''
    this.getListDataset()
  },
  computed: {
    ...mapGetters(['userId', 'agencyId'])
  },
  watch: {
    ownerAgencyName(nv, ov) {
      console.log(nv)
      this.dataList = []
      this.$emit('selected', null)
      nv && this.getListDataset()
    }
  },
  methods: {
    reset() {
      this.$refs.searchForm.resetFields()
    },
    // 查询
    queryHandle() {
      this.searchQuery = { ...this.searchForm }
      this.pageData.page_offset = 1
      this.getListDataset()
    },
    // 单选 选中后更新标签字段选项下拉
    selected(checked, row) {
      const { datasetId } = row
      if (checked) {
        this.selectdDataId = datasetId
      } else {
        this.selectdDataId = ''
      }
      if (this.selectdDataId) {
        this.$emit('selected', row)
      } else {
        this.$emit('selected', null)
      }
    },
    async getListDataset() {
      const { page_offset, page_size } = this.pageData
      const { ownerAgencyName = '' } = this
      const { ownerUserName = '', datasetTitle = '', createTime = '' } = this.searchQuery
      let params = handleParamsValid({ ownerAgencyName, ownerUserName, datasetTitle })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      params = { ...params, pageNum: page_offset, pageSize: page_size, permissionType: 'usable' }
      this.loadingFlag = true
      const res = await dataManageServer.listDataset(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { content = [], totalCount } = res.data
        this.dataList = content.map((v) => {
          return {
            ...v,
            isOwner: v.ownerAgencyName === this.agencyId && v.ownerUserName === this.userId,
            showSelect: true
          }
        })
        console.log(content, 'content', totalCount)
        this.total = totalCount
      } else {
        this.dataList = []
        this.total = 0
      }
    },
    paginationHandle(pageData) {
      this.pageData = { ...pageData }
      this.getListDataset()
    }
  }
}
</script>
<style lang="less" scoped>
.select-data {
  border: 1px solid #e0e4ed;
  border-radius: 4px;
  padding: 20px;
  height: auto;
  .el-empty {
    margin-top: 0;
  }
}
.card-container {
  margin: -10px -10px;
  height: auto;
  overflow: auto;
}

::v-deep div.data-card {
  margin: 10px;
  ul li:first-child {
    line-height: 26px;
    margin-bottom: 8px;
  }
  ul li span.data-size {
    i {
      font-size: 18px;
      line-height: 26px;
    }
  }
}
</style>
