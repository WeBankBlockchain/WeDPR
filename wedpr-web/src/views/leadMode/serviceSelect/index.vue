<template>
  <div class="select-data">
    <div class="card-container">
      <serviceCard
        @selected="(checked) => selected(checked, item)"
        :selected="selectdserviceId === item.serviceId"
        @deleteService="showDeleteModal(item)"
        @modifyData="modifyData(item)"
        v-for="item in tableData"
        :serviceInfo="item"
        :key="item.serviceId"
        :showEdit="false"
      />
    </div>
    <el-empty v-if="!total" :image-size="120" description="暂无数据">
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
</template>
<script>
import { mapGetters } from 'vuex'
import serviceCard from '@/components/serviceCard.vue'
import { serviceManageServer } from 'Api'
import { serviceAuthStatus } from 'Utils/constant.js'
export default {
  name: 'participateSelect',
  props: {
    showTagsModal: {
      type: Boolean,
      default: false
    },
    serviceType: {
      type: String,
      default: ''
    }
  },
  components: {
    serviceCard
  },
  data() {
    return {
      formLabelWidth: '112px',
      loadingFlag: false,
      groupId: '',
      pageData: { page_offset: 1, page_size: 4 },
      total: -1,
      dataList: [],
      selectdserviceId: '',
      selectedData: {},
      fieldList: [],
      tableData: []
    }
  },
  created() {
    this.getPublishList()
  },
  computed: {
    ...mapGetters(['userId', 'agencyId'])
  },
  methods: {
    reset() {
      this.$refs.searchForm.resetFields()
    },
    // 单选 选中后更新标签字段选项下拉
    selected(checked, row) {
      const { serviceId } = row
      if (checked) {
        this.selectdserviceId = serviceId
      } else {
        this.selectdserviceId = ''
      }
      if (this.selectdserviceId) {
        this.$emit('selected', row)
      } else {
        this.$emit('selected', null)
      }
    },
    // 获取服务列表
    async getPublishList() {
      const { page_offset, page_size } = this.pageData
      const { serviceType } = this
      const params = {
        condition: { serviceId: '', serviceType, authStatus: serviceAuthStatus.Authorized, status: 'PublishSuccess' },
        serviceIdList: [],
        pageNum: page_offset,
        pageSize: page_size
      }
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
            showSelect: true
          }
        })
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    paginationHandle(pageData) {
      this.pageData = { ...pageData }
      this.getPublishList()
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
  margin-bottom: 42px;
  .el-empty {
    margin-top: 0;
  }
}
.card-container {
  margin: -10px;
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
