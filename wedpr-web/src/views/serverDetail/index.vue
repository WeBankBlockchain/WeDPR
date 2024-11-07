<template>
  <div class="server-data">
    <div class="con">
      <div class="title-radius">基本信息</div>
    </div>
    <div class="info-container">
      <div class="whole">
        <div class="half">
          <span class="title">服务名称：</span>
          <span class="info" :title="serviceInfo.serviceName"> {{ serviceInfo.serviceName }} </span>
        </div>
      </div>
      <div class="whole">
        <div class="half">
          <span class="title">服务ID：</span>
          <span class="info" :title="serviceInfo.serviceId"> {{ serviceInfo.serviceId }} </span>
        </div>
      </div>
      <div class="whole">
        <div class="half">
          <span class="title">服务状态：</span>
          <span class="info" :title="serviceInfo.push">
            <el-tag size="small" :color="colorMap[serviceInfo.status]">{{ servicePulishStatus[serviceInfo.status] }}</el-tag>
          </span>
        </div>
      </div>
      <div class="whole">
        <div class="half">
          <span class="title">所属机构：</span>
          <span class="info" :title="serviceInfo.agency"> {{ serviceInfo.agency }} </span>
        </div>
      </div>
      <div class="whole">
        <div class="half">
          <span class="title">所属用户：</span>
          <span class="info" :title="serviceInfo.owner"> {{ serviceInfo.owner }} </span>
        </div>
      </div>
      <div class="whole" v-if="serviceInfo.serviceType === serviceTypeEnum.PIR">
        <div class="half">
          <span class="title">发布数据：</span>
          <span class="info link" @click="goDataDetail(serviceInfo.datasetId)">
            {{ serviceInfo.datasetId }}
          </span>
        </div>
      </div>
      <div class="whole">
        <div class="half">
          <span class="title">发布时间：</span>
          <span class="info"> {{ serviceInfo.createTime }} </span>
        </div>
      </div>

      <div class="whole" v-if="serviceInfo.serviceType === serviceTypeEnum.PIR">
        <div class="half tableinfo">
          <span class="title">查询规则：</span>
          <span class="info">
            <el-table size="small" :data="serviceConfigList" :border="true" class="table-wrap">
              <el-table-column label="主键" prop="idField" show-overflow-tooltip />
              <el-table-column label="支持的查询方式" prop="searchType" show-overflow-tooltip>
                <template v-slot="scope">
                  <span v-if="scope.row.searchType === searchTypeEnum.ALL">查询存在性，查询字段值</span>
                  <span v-if="scope.row.searchType === searchTypeEnum.SearchExist">查询存在性</span>
                  <span v-if="scope.row.searchType === searchTypeEnum.SearchValue">查询字段值</span>
                </template>
              </el-table-column>
              <el-table-column label="可查的字段列表" prop="accessibleValueQueryFields" show-overflow-tooltip>
                <template v-slot="scope">
                  {{ scope.row.accessibleValueQueryFields && scope.row.accessibleValueQueryFields.join(',') }}
                </template>
              </el-table-column>
            </el-table>
          </span>
        </div>
      </div>
      <div class="whole" v-if="serviceInfo.serviceType !== serviceTypeEnum.PIR">
        <div class="half tableinfo">
          <span class="title">模型内容：</span>
          <span class="info">
            <el-table size="small" :data="serviceConfigList" :border="true" class="table-wrap" :span-method="objectSpanMethod">
              <el-table-column label="模型类型" prop="model_type" />
              <el-table-column label="标签提供方" prop="label_provider" show-overflow-tooltip />
              <el-table-column label="标签字段" prop="label_column" show-overflow-tooltip />
              <el-table-column label="参与机构" prop="agency" show-overflow-tooltip />
              <el-table-column label="包含标签" prop="fields" show-overflow-tooltip />
            </el-table>
          </span>
        </div>
      </div>
      <div class="whole" v-if="serviceInfo.owner === userId && serviceInfo.agency === agencyId">
        <div class="half tableinfo">
          <span class="title">授权信息：</span>
          <span class="info">
            <el-table size="small" v-if="serviceAuthInfos.length" :data="serviceAuthInfos" :border="true" class="table-wrap">
              <el-table-column label="授权机构" prop="accessibleAgency" />
              <el-table-column label="授权用户" prop="accessibleUser" show-overflow-tooltip />
              <el-table-column label="AccessKey" prop="accessKeyId" show-overflow-tooltip />
              <el-table-column label="申请时间" prop="applyTime" show-overflow-tooltip />
              <el-table-column label="过期时间" prop="expireTime" show-overflow-tooltip />
            </el-table>
            <span v-else>暂无授权</span>
          </span>
        </div>
      </div>
      <div class="whole" v-if="serviceInfo.owner === userId && serviceInfo.agency === agencyId && serviceInfo.status === 'PublishFailed'">
        <div>
          <span class="title">发布日志：</span>
          <span class="info log" :title="serviceInfo.statusMsg">
            <pre><code>{{ serviceInfo.statusMsg }}</code></pre>
          </span>
        </div>
      </div>
    </div>
    <div v-if="serviceInfo.owner === userId && serviceInfo.agency === agencyId">
      <div class="con">
        <div class="title-radius">使用记录</div>
      </div>
      <div class="tableContent autoTableWrap" v-if="total">
        <el-table size="small" v-loading="loadingFlag" :data="tableData" :border="true" class="table-wrap">
          <el-table-column label="调用ID" prop="invokeId" />
          <el-table-column label="调用机构" prop="invokeAgency" />
          <el-table-column label="调用用户" prop="invokeUser" />
          <el-table-column label="调用时间" prop="invokeTime" />
          <el-table-column label="调用状态" prop="invokeStatus">
            <template v-slot="scope">
              <el-tag size="small" v-if="scope.row.invokeStatus === 'InvokeSuccess'" effect="dark" color="#52B81F">调用成功</el-tag>
              <el-tag size="small" v-if="scope.row.invokeStatus == 'InvokeFailed'" effect="dark" color="#FF4D4F">调用失败</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
      </div>
      <el-empty v-if="!total" :image-size="120" description="暂无数据">
        <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
      </el-empty>
    </div>
    <div class="sub-con" v-else>
      <el-button size="medium" type="primary" @click="subApply"> 申请调用 </el-button>
    </div>
    <serverApply :serviceId="serviceId" :showApplyModal="showApplyModal" @closeModal="closeModal" @handlOK="handlOK" />
  </div>
</template>
<script>
import { serviceManageServer } from 'Api'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { jobStatusList, jobStatusMap, searchTypeEnum, serviceTypeEnum, servicePulishStatus } from 'Utils/constant.js'
import { mapGetters } from 'vuex'
import wePagination from '@/components/wePagination.vue'
import serverApply from './serviceApply'
import hljs from 'highlight.js'
import 'highlight.js/styles/a11y-light.min.css'
export default {
  name: 'projectDetail',
  mixins: [tableHeightHandle],
  components: {
    wePagination,
    serverApply
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
      serviceInfo: {},
      pageData: {
        page_offset: 1,
        page_size: 5
      },
      tableData: [],
      total: -1,
      mode: {
        Expert: '向导模式',
        Wizard: '专家模式'
      },
      typeList: [],
      jobStatusList,
      jobStatusMap,
      searchTypeEnum,
      serviceTypeEnum,
      servicePulishStatus,
      pageMode: process.env.VUE_APP_MODE,
      showApplyModal: false,
      serviceId: '',
      serviceConfigList: [],
      colorMap: {
        Publishing: '#3071F2',
        PublishSuccess: '#52B81F',
        PublishFailed: '#FF4D4F'
      },
      serviceAuthInfos: []
    }
  },
  created() {
    const { serviceId, type } = this.$route.query
    this.serviceId = serviceId
    serviceId && this.queryService()
    this.showApplyModal = type === 'apply'
  },
  computed: {
    ...mapGetters(['algList', 'agencyId', 'userId'])
  },
  methods: {
    goDataDetail(datasetId) {
      this.$router.push({ path: '/dataDetail', query: { datasetId } })
    },
    handleData(key) {
      const data = this.algList.filter((v) => v.value === key)
      return data[0] || {}
    },
    createJob() {
      this.$router.push({ path: '/leadMode', query: { serviceId: this.serviceId } })
    },
    modifyProject() {
      this.$router.push({ path: '/projectEdit', query: { serviceId: this.serviceId } })
    },
    goDetail(id) {
      this.$router.push({ path: '/jobDetail', query: { id } })
    },
    reset() {
      this.$refs.searchForm.resetFields()
    },
    // 获取服务详情
    async queryService() {
      this.loadingFlag = true
      const { serviceId } = this
      const params = { condition: {}, serviceIdList: [serviceId], pageNum: 1, pageSize: 1 }
      const res = await serviceManageServer.getPublishList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { wedprPublishedServiceList = [] } = res.data
        const { serviceConfig = '', serviceType, serviceAuthInfos } = wedprPublishedServiceList[0] || {}
        if (serviceType === serviceTypeEnum.PIR) {
          // pir服务
          const { datasetId, searchType, idField, accessibleValueQueryFields } = JSON.parse(serviceConfig)
          this.serviceConfigList = [
            {
              searchType,
              idField,
              accessibleValueQueryFields
            }
          ]
          this.serviceInfo = { ...wedprPublishedServiceList[0], datasetId, serviceType }
          console.log(this.serviceConfigList, 'serviceConfigList')
        } else {
          //  model服务
          const { participant_agency_list = [], ...rest } = JSON.parse(serviceConfig)
          this.serviceConfigList = participant_agency_list.map((v) => {
            return {
              ...v,
              ...rest
            }
          })
          console.log(this.serviceConfigList, 'serviceConfigList')
          this.serviceInfo = { ...wedprPublishedServiceList[0], serviceType }
        }

        // 自己的服务查询使用记录
        if (this.serviceInfo.owner === this.userId && this.serviceInfo.agency === this.agencyId) {
          this.serviceAuthInfos = serviceAuthInfos
          this.getServerUseRecord()
        }
        // 高亮错误日志
        this.$nextTick(() => {
          hljs.highlightAll() // 获取更新后的 DOM 状态
        })
      } else {
        this.serviceInfo = {}
      }
    },
    objectSpanMethod({ row, column, rowIndex, columnIndex }) {
      const length = this.serviceConfigList.length
      if (columnIndex < 3) {
        if (rowIndex === 0) {
          return {
            // 此单元格在列上要占据length个行单元格，1个列单元格
            rowspan: length,
            colspan: 1
          }
        } else {
          return {
            rowspan: 0,
            colspan: 0
          }
        }
      }
    },
    handleParamsValid(params) {
      const validParams = {}
      Object.keys(params).forEach((key) => {
        if (!(params[key] === undefined || params[key] === null || params[key] === '')) {
          validParams[key] = params[key]
        }
      })
      return validParams
    },
    handleRuleDes(exists, values) {
      let existsRulesDes = '查询存在性：'
      if (exists.includes('*')) {
        existsRulesDes += '全部字段'
      } else {
        existsRulesDes += exists.join('、')
      }

      let valuesRulesDes = '查询字段值：'
      if (values.includes('*')) {
        valuesRulesDes += '全部字段'
      } else {
        valuesRulesDes += values.join('、')
      }
      const desList = []
      if (exists.length) {
        desList.push(existsRulesDes)
      }
      if (values.length) {
        desList.push(valuesRulesDes)
      }
      return desList.join(';')
    },
    // 获取任务列表
    async getServerUseRecord() {
      this.loadingFlag = true
      const { serviceId } = this
      const { page_offset, page_size } = this.pageData
      const res = await serviceManageServer.getServerUseRecord({ condition: { serviceId, invokeId: '' }, pageNum: page_offset, pageSize: page_size })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { wedprPublishInvokeList = [], total } = res.data
        this.tableData = wedprPublishInvokeList
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
      this.getServerUseRecord()
    },
    subApply() {
      this.$router.push({ path: '/dataApply', query: { serviceId: this.serviceInfo.serviceId, applyType: 'wedpr_service_auth' } })
      // this.showApplyModal = true
    },
    closeModal() {
      this.showApplyModal = false
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
div.con {
  .el-button {
    float: right;
  }
}
div.whole {
  display: flex;
  margin-bottom: 16px;
}
div.half {
  width: 50%;
  display: flex;
}
div.half.tableinfo {
  width: 75%;
}
div.info-container {
  margin-bottom: 44px;
  span {
    font-size: 14px;
    line-height: 22px;
    color: #525660;
  }
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
    color: white;
  }
  span.title {
    float: left;
    width: 86px;
    text-align: right;
    color: #525660;
  }
  span.info {
    flex: 1;
    color: #262a32;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
  span.log {
    display: inline-block;
    width: calc(100% - 200px);
    border: 1px solid #ccc;
    border-radius: 4px;
  }
  .el-row {
    margin-bottom: 16px;
  }
  span.right {
    width: 118px;
  }
}
.sub-con {
  margin-top: 32px;
}
span.info {
  color: #262a32;
}
div.info-container span.info.link {
  cursor: pointer;
  color: #3071f2;
}
.tableContent {
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
  }
}
</style>
