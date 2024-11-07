<template>
  <div class="create-server-data">
    <el-form :inline="false" @submit="checkService" :model="serverForm" :rules="serverRules" ref="serverForm" size="small">
      <formCard title="基础信息">
        <el-form-item label-width="96px" label="服务名称：" prop="serviceName">
          <el-input style="width: 480px" placeholder="请输入" v-model="serverForm.serviceName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label-width="96px" label="服务简介：" prop="serviceDesc">
          <el-input type="textarea" style="width: 480px" placeholder="请输入" v-model="serverForm.serviceDesc" autocomplete="off"></el-input>
        </el-form-item>
      </formCard>
      <formCard title="选择发布数据" v-if="type !== 'edit'">
        <div class="card-container">
          <dataCard
            @selected="(checked) => selected(checked, item)"
            :selected="selectedData.datasetId === item.datasetId"
            :showTags="false"
            :showEdit="false"
            v-for="item in dataList"
            :dataInfo="item"
            :key="item.datasetId"
          />
        </div>
        <we-pagination
          :pageSizesOption="[8, 12, 16, 24, 32]"
          :total="total"
          :page_offset="pageData.page_offset"
          :page_size="pageData.page_size"
          @paginationChange="paginationHandle"
        ></we-pagination>
      </formCard>
      <formCard title="设置访问凭证">
        <el-form-item label-width="108px" label="访问凭证：" prop="grantedAccessKeyList">
          <el-select style="width: 480px" multiple v-model="serverForm.grantedAccessKeyList" placeholder="请选择" clearable>
            <el-option :title="item.value" :label="item.label" :value="item.value" :key="item" v-for="item in accessKeyList"></el-option>
          </el-select>
        </el-form-item>
      </formCard>
      <formCard title="设置查询规则">
        <el-form-item label-width="108px" label="查询主键：" prop="idField">
          <el-select v-model="serverForm.idField" placeholder="请选择" clearable>
            <el-option :label="item" :value="item" :key="item" v-for="item in selectedData.fieldsList"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label-width="108px" label="查询类型：" prop="searchType">
          <el-checkbox-group v-model="serverForm.searchType">
            <el-checkbox :label="searchTypeEnum.SearchExist">查询存在性</el-checkbox>
            <el-checkbox :label="searchTypeEnum.SearchValue">查询字段值</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <div v-if="serverForm.searchType.includes(searchTypeEnum.SearchValue)">
          <el-form-item label-width="108px" label="查询字段值：" prop="searchFieldsType">
            <el-select v-model="serverForm.searchFieldsType" placeholder="请输入" clearable>
              <el-option label="全部字段" value="all"></el-option>
              <el-option label="指定字段" value="some"> </el-option>
            </el-select>
          </el-form-item>
          <el-form-item v-if="serverForm.searchFieldsType === 'some'" label-width="108px" label="指定字段：" prop="accessibleValueQueryFields">
            <el-select multiple style="width: 480px" v-model="serverForm.accessibleValueQueryFields" placeholder="请选择" clearable>
              <el-option :label="item" :value="item" :key="item" v-for="item in selectedData.fieldsList"></el-option>
            </el-select>
          </el-form-item>
        </div>
      </formCard>
    </el-form>
    <div>
      <el-button size="medium" type="primary" @click="checkService" v-if="type === 'edit'"> 编辑服务 </el-button>
      <el-button size="medium" type="primary" @click="checkService" v-else> 发布服务 </el-button>
    </div>
  </div>
</template>
<script>
import formCard from '@/components/formCard.vue'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import dataCard from '@/components/dataCard.vue'
import { serviceManageServer, dataManageServer, accessKeyManageServer } from 'Api'
import wePagination from '@/components/wePagination.vue'
import { searchTypeEnum, serviceTypeEnum } from 'Utils/constant.js'
import { mapGetters } from 'vuex'
export default {
  name: 'pirServerCreate',
  mixins: [tableHeightHandle],
  components: {
    formCard,
    dataCard,
    wePagination
  },
  data() {
    return {
      serverForm: {
        serviceName: '',
        serviceDesc: '',
        searchType: [],
        searchFieldsType: '',
        idField: '',
        accessibleValueQueryFields: [],
        grantedAccessKeyList: []
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
      dataForm: {
        setting: []
      },
      type: '',
      dataList: [],
      accessKeyList: [],
      selectedData: {},
      serviceId: '',
      searchTypeEnum
    }
  },

  created() {
    const { type, serviceId } = this.$route.query
    console.log(searchTypeEnum, 'searchTypeEnum')
    this.type = type
    this.queryAccessKeyList()
    if (this.type === 'edit') {
      this.serviceId = serviceId
      this.queryService()
    } else {
      this.getListDataset()
    }
  },
  computed: {
    ...mapGetters(['userinfo', 'userId']),
    serverRules() {
      return {
        serviceName: [
          {
            required: true,
            message: '请输入服务名称',
            trigger: 'blur'
          }
        ],
        serviceDesc: [
          {
            required: true,
            message: '请输入服务描述',
            trigger: 'blur'
          }
        ],
        grantedAccessKeyList: [
          {
            required: true,
            message: '请选择访问凭证',
            trigger: 'blur'
          }
        ],
        idField: [
          {
            required: true,
            message: '请选择查询主键',
            trigger: 'blur'
          }
        ],
        searchType: [
          {
            required: true,
            message: '请选择查询类型',
            trigger: 'blur'
          }
        ],
        searchFieldsType: [
          {
            required: true,
            message: '请选择查询字段值',
            trigger: 'blur'
          }
        ],
        accessibleValueQueryFields: [
          {
            required: this.serverForm.searchFieldsType === 'some',
            message: '请输入查询字段',
            trigger: 'blur'
          }
        ]
      }
    }
  },
  methods: {
    // 获取服务详情回显
    async queryService() {
      this.loadingFlag = true
      const { serviceId } = this
      const params = { condition: {}, serviceIdList: [serviceId], pageNum: 1, pageSize: 1 }
      const res = await serviceManageServer.getPublishList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { wedprPublishedServiceList = [] } = res.data
        const { serviceConfig = '', serviceName, serviceDesc, grantedAccessKeyList } = wedprPublishedServiceList[0] || {}
        const { accessibleValueQueryFields = [], datasetId, idField, searchType } = JSON.parse(serviceConfig)
        await this.getDetail({ datasetId })
        const handledSearchType = searchTypeEnum.ALL ? [searchTypeEnum.SearchExist, searchTypeEnum.SearchValue] : [searchType]
        const searchFieldsType = accessibleValueQueryFields.length === this.selectedData.fieldsList.length ? 'all' : 'some'
        this.serverForm = {
          ...this.serverForm,
          serviceName,
          idField,
          serviceDesc,
          searchType: handledSearchType,
          searchFieldsType,
          accessibleValueQueryFields,
          grantedAccessKeyList
        }
        console.log(this.serverForm, 'serverForm')
      }
    },
    // 获取数据集详情
    async getDetail(params) {
      this.loadingFlag = true
      const res = await dataManageServer.queryDataset({ ...params })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { datasetFields } = res.data
        const fieldsList = datasetFields.split(', ')
        this.selectedData = { ...res.data, fieldsList }
      }
    },
    async createService(params) {
      const res = await serviceManageServer.publishService(params)
      if (res.code === 0 && res.data) {
        console.log(res)
        this.$message.success('服务发布成功')
        this.$router.push({ path: 'serverManage' })
      }
    },
    async updateService(params) {
      const res = await serviceManageServer.updateService(params)
      if (res.code === 0) {
        console.log(res)
        this.$message.success('服务编辑成功')
        history.go(-1)
      }
    },
    selected(checked, row) {
      this.serverForm.idField = ''
      this.serverForm.searchType = []
      this.serverForm.searchFieldsType = ''
      this.serverForm.accessibleValueQueryFields = []
      if (checked) {
        const fieldsList = row.datasetFields.split(', ')
        this.selectedData = { ...row, fieldsList }
      } else {
        this.selectedData = {}
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
            label: v.desc + '-' + v.accessKeyID,
            value: v.accessKeyID
          }
        })
      }
    },
    checkService() {
      this.$refs.serverForm.validate((valid) => {
        if (valid) {
          if (!this.selectedData.datasetId) {
            this.$message.error('请选择数据集')
            return
          }
          const { serviceName, serviceDesc, searchFieldsType, idField, grantedAccessKeyList } = this.serverForm
          let { accessibleValueQueryFields, searchType } = this.serverForm
          accessibleValueQueryFields = searchFieldsType === 'all' ? [...this.selectedData.fieldsList] : accessibleValueQueryFields
          searchType = searchType.length === 2 ? searchTypeEnum.ALL : searchType[0]
          const { datasetId } = this.selectedData
          const serviceConfig = { searchType, datasetId, accessibleValueQueryFields, idField }
          if (this.type === 'edit') {
            this.updateService({
              serviceId: this.serviceId,
              serviceName,
              serviceDesc,
              grantedAccessKeyList,
              serviceConfig: JSON.stringify(serviceConfig),
              serviceType: serviceTypeEnum.PIR
            })
          } else {
            this.createService({ serviceName, serviceDesc, grantedAccessKeyList, serviceConfig: JSON.stringify(serviceConfig), serviceType: serviceTypeEnum.PIR })
          }
        } else {
          return false
        }
      })
    },
    async getListDataset() {
      const { page_offset, page_size } = this.pageData
      // 仅选择自己的数据
      const params = { pageNum: page_offset, pageSize: page_size, ownerUserName: this.userId, permissionType: 'usable' }
      this.loadingFlag = true
      const res = await dataManageServer.listDataset(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { content = [], totalCount } = res.data
        this.dataList = content.map((v) => {
          return {
            ...v,
            showSelect: true,
            isOwner: v.ownerAgencyName === this.agencyId && v.ownerUserName === this.userId
          }
        })
        console.log(content, 'content', totalCount)
        this.total = totalCount
      } else {
        this.dataList = []
        this.total = 0
      }
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getAccountList()
    }
  }
}
</script>
<style lang="less" scoped>
.create-server-data {
  .card-container {
    overflow: hidden;
  }
}
</style>
