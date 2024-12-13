<template>
  <div class="group-manage">
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
      <el-tab-pane label="任务信息" name="jobInfo">
        <div v-loading="loading" v-show="activeName === 'jobInfo'">
          <div class="info-container">
            <div class="whole">
              <div class="half">
                <span class="title">任务ID：</span>
                <span class="info" :title="jobInfo.id"> {{ jobInfo.id }} </span>
              </div>
            </div>
            <div class="whole" v-if="jobInfo.jobType === jobEnum.PIR">
              <div class="half">
                <span class="title">所选服务：</span>
                <span class="info link" :title="jobInfo.serviceId" @click="goServiceDetail(jobInfo.serviceId, jobInfo.serviceType)"> {{ jobInfo.serviceId }} </span>
              </div>
            </div>
            <div class="whole" v-if="jobInfo.jobType === jobEnum.PIR">
              <div class="half">
                <span class="title">数据集：</span>
                <span class="info link" :title="jobInfo.datasetId" @click="goDatasetDetail(jobInfo.datasetId)"> {{ jobInfo.datasetId }} </span>
              </div>
            </div>
            <div class="whole" v-if="jobInfo.jobType === jobEnum.PIR">
              <div class="half">
                <span class="title">查询类型：</span>
                <span class="info" :title="searchTypeDesEnum[jobInfo.searchType]"> {{ searchTypeDesEnum[jobInfo.searchType] }} </span>
              </div>
            </div>
            <div class="whole" v-if="jobInfo.jobType === jobEnum.PIR && jobInfo.searchType !== searchTypeEnum.SearchExist">
              <div class="half">
                <span class="title">查询字段：</span>
                <span class="info" :title="jobInfo.queriedFields"> {{ jobInfo.queriedFields }} </span>
              </div>
            </div>
            <div class="whole" v-if="jobInfo.jobType === jobEnum.PIR">
              <div class="half">
                <span class="title">查询字段值：</span>
                <span class="info" :title="jobInfo.searchIdList"> {{ jobInfo.searchIdList }} </span>
              </div>
            </div>
            <div class="whole" v-if="![jobEnum.PIR, jobEnum.PSI, jobEnum.MPC, jobEnum.SQL].includes(jobInfo.jobType)">
              <div class="half">
                <span class="title">标签提供方：</span>
                <span class="info" :title="jobInfo.labelProvider"> {{ jobInfo.labelProvider }} </span>
              </div>
            </div>
            <div class="whole" v-if="jobInfo.jobType !== jobEnum.PIR">
              <div class="half">
                <span class="title">参与方：</span>
                <span class="info" :title="jobInfo.particapate"> {{ jobInfo.particapate }} </span>
              </div>
            </div>
            <div class="whole" v-if="jobInfo.jobType !== jobEnum.PIR">
              <div class="half">
                <span class="title">结果接收方：</span>
                <span class="info" :title="jobInfo.receiver"> {{ jobInfo.receiver || jobInfo.ownerAgency }} </span>
              </div>
            </div>
            <div class="whole">
              <div class="half">
                <span class="title">创建时间：</span>
                <span class="info" :title="jobInfo.createTime"> {{ jobInfo.createTime }} </span>
              </div>
            </div>
            <div class="whole">
              <div class="half">
                <span class="title">算法模板：</span>
                <span class="info" :title="jobInfo.jobType"> {{ jobInfo.jobType }} </span>
              </div>
            </div>
            <div class="whole">
              <div class="half">
                <span class="title">任务状态：</span>
                <span class="info" :title="jobInfo.status">
                  <el-tag size="small" v-if="jobInfo.status === 'RunSuccess'" effect="dark" color="#52B81F">成功</el-tag>
                  <el-tag size="small" v-else-if="jobInfo.status === 'RunFailed'" effect="dark" color="#FF4D4F">失败</el-tag>
                  <el-tag size="small" v-else-if="jobInfo.status === 'Running'" effect="dark" color="#3071F2">运行中</el-tag>
                  <el-tag size="small" v-else effect="dark" color="#3071F2">{{ jobStatusMap[jobInfo.status] }}</el-tag>
                </span>
              </div>
            </div>
            <div class="whole" v-if="jobInfo.status === 'RunFailed'">
              <div class="half" style="width: 90%">
                <span class="title">诊断信息：</span>
                <span class="error-conatiner">
                  {{ jobInfo.statusDetail }}
                </span>
              </div>
            </div>

            <div class="whole" v-if="false">
              <div class="half">
                <span class="title">运行进度：</span>
                <span class="info" :title="jobInfo.projectDesc"> {{ jobInfo.projectDesc }} </span>
              </div>
            </div>
            <div class="whole" v-if="false">
              <div class="half">
                <span class="title">链上存证：</span>
                <span class="info" :title="jobInfo.projectDesc"> {{ jobInfo.projectDesc }} </span>
              </div>
            </div>
          </div>
          <div class="con" v-if="dataList.length">
            <div class="title-radius">参与数据资源</div>
            <div class="tableContent autoTableWrap">
              <el-table size="small" :data="dataList" :border="true" class="table-wrap">
                <el-table-column label="数据资源ID" prop="datasetId" />
                <el-table-column label="数据资源名称" prop="datasetTitle" />
                <el-table-column label="所属机构" prop="ownerAgencyName" />
                <el-table-column label="所属用户" prop="ownerUserName" />
                <el-table-column label="数据集字段" prop="datasetFields" show-overflow-tooltip />
                <el-table-column label="创建时间" prop="createAt" />
                <el-table-column label="操作">
                  <template v-slot="scope">
                    <el-button size="small" type="text" @click="getDetail(scope.row.datasetId)">查看详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="con" v-if="jobEnum.MPC === jobInfo.jobType">
            <div class="title-radius">Python语句</div>
            <div class="modify-container" style="height: 300px">
              <editorCom v-model="jobInfo.mpcContent" :readOnly="true" lang="python" />
            </div>
          </div>
          <div class="con" v-if="jobEnum.SQL === jobInfo.jobType">
            <div class="title-radius">SQL语句</div>
            <div class="modify-container" style="height: 300px">
              <editorCom v-model="jobInfo.sql" :readOnly="true" lang="sql" />
            </div>
          </div>
          <div class="con" v-if="[jobEnum.LR_TRAINING, jobEnum.LR_PREDICTING, jobEnum.XGB_TRAINING, jobEnum.XGB_PREDICTING].includes(jobInfo.jobType)">
            <div class="title-radius">配置信息</div>
            <div class="tableContent autoTableWrap">
              <el-table :max-height="300" size="small" :data="settingTableData" :border="true" class="table-wrap">
                <el-table-column label="参数" prop="label" />
                <el-table-column label="取值" prop="value" />
              </el-table>
            </div>
          </div>
          <div class="con" v-if="xgbJobSavedModelData.length || xgbJobOriginModelData.length">
            <div class="title-radius">模型信息</div>
            <div class="tableContent autoTableWrap">
              <el-table v-if="xgbJobSavedModelData.length" size="small" :span-method="objectSpanMethodSaved" :data="xgbJobSavedModelData" :border="true" class="table-wrap">
                <el-table-column label="模型名称" prop="modelName" />
                <el-table-column label="所属机构" prop="modelAgency" />
                <el-table-column label="所属用户" prop="modelOwner" />
                <el-table-column label="模型任务来源" prop="jobID">
                  <template v-slot="scope">
                    <el-button @click="goJobDetail(scope.row.jobID)" size="small" type="text">{{ scope.row.jobID }}</el-button>
                  </template>
                </el-table-column>
                <el-table-column label="标签提供方" prop="label_provider" />
                <el-table-column label="标签字段" prop="label_column" />
                <el-table-column label="参与机构" prop="agency" />
                <el-table-column label="数据集字段" prop="fields" show-overflow-tooltip>
                  <template v-slot="scope">
                    {{ scope.row.fields.join(',') }}
                  </template>
                </el-table-column>
                <el-table-column label="操作">
                  <template v-slot="scope">
                    <el-button @click="copyModel(scope.row.modelString)" size="small" type="text">复制模型</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-table v-if="xgbJobOriginModelData.length" :span-method="objectSpanMethodOrigin" size="small" :data="xgbJobOriginModelData" :border="true" class="table-wrap">
                <el-table-column label="模型类型" prop="model_type" />
                <el-table-column label="标签提供方" prop="label_provider" />
                <el-table-column label="标签字段" prop="label_column" />
                <el-table-column label="参与机构" prop="agency" />
                <el-table-column label="数据集字段" prop="fields" show-overflow-tooltip>
                  <template v-slot="scope">
                    {{ scope.row.fields.join(',') }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-if="xgbJobOriginModelData.length">
              <div style="margin-top: 20px; padding-bottom: 20px">
                <el-button type="primary" @click="showSettingSaveModal"> 保存配置 </el-button>
                <el-button type="primary" @click="showSettingSaveModelModal"> 保存模型 </el-button>
                <el-button @click="reBuild"> 调参重跑 </el-button>
              </div>
              <div class="tips">* 保存配置：后续可选择基于该配置进行建模与调参重跑</div>
              <div class="tips">* 保存模型：后续可选择基于该模型进行预测</div>
            </div>
          </div>
          <div
            v-if="
              jobInfo.status === 'RunSuccess' &&
              [jobEnum.PIR, jobEnum.PSI, jobEnum.MPC, jobEnum.SQL].includes(jobInfo.jobType) &&
              jobInfo.owner === userId &&
              jobInfo.ownerAgency === agencyId
            "
          >
            <div class="title-radius">任务结果</div>
            <div style="padding-left: 10px">
              <baseResult :jobType="jobInfo.jobType" :jobID="jobID" :resultFileInfo="resultFileInfo" :jobStatusInfo="jobStatusInfo" />
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="运行日志" name="log" v-if="[jobEnum.XGB_TRAINING, jobEnum.XGB_PREDICTING, jobEnum.LR_TRAINING, jobEnum.LR_PREDICTING].includes(jobInfo.jobType)">
        <logComp :jobID="jobID" :jobType="jobInfo.jobType" />
      </el-tab-pane>
      <el-tab-pane
        label="查看结果"
        name="result"
        v-if="
          [jobEnum.XGB_TRAINING, jobEnum.XGB_PREDICTING, jobEnum.LR_TRAINING, jobEnum.LR_PREDICTING].includes(jobInfo.jobType) &&
          jobInfo.status === 'RunSuccess' &&
          receiverList.includes(agencyId)
        "
      >
        <xgbResult v-if="jobID" :jobID="jobID" :jobType="jobInfo.jobType" :jobStatusInfo="jobStatusInfo" :modelResultDetail="modelResultDetail" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>
import { jobManageServer, dataManageServer, settingManageServer, serviceManageServer } from 'Api'
import xgbResult from './result/xgbResult.vue'
import baseResult from './result/baseResult.vue'
import logComp from './log/index.vue'
import { jobStatusMap, jobEnum, searchTypeEnum, searchTypeDesEnum, settingMap, modelSettingMap } from 'Utils/constant.js'
import { copy } from 'Utils'
import { mapGetters } from 'vuex'
import { downloadLargeFile } from 'Mixin/downloadLargeFile.js'
import editorCom from '@/components/editorCom.vue'
export default {
  name: 'groupManage',
  mixins: [downloadLargeFile],
  components: {
    xgbResult,
    baseResult,
    editorCom,
    logComp
  },
  data() {
    return {
      jobInfo: {},
      queryFlag: false,
      tableData: [],
      showAddModal: false,
      activeName: 'jobInfo',
      jobID: '',
      jobResult: {},
      dataList: [],
      jobStatusMap,
      modelResultDetail: {}, // xgbresult
      jobStatusInfo: {},
      resultFileInfo: {}, // psiresult
      receiverList: [],
      labelProvider: [],
      particapate: [],
      jobEnum,
      modelSetting: {},
      settingTableData: [],
      serviceData: {},
      searchTypeDesEnum,
      searchTypeEnum,
      model: '',
      loading: false,
      xgbJobSavedModelData: [],
      xgbJobOriginModelData: [],
      timer: null
    }
  },
  created() {
    const { id } = this.$route.query
    this.jobID = id
    id && this.getJobInfo()
  },
  computed: {
    ...mapGetters(['agencyId', 'userId'])
  },
  watch: {
    // 监听路由对象($route)的变化
    $route: {
      handler: function (to, from) {
        // 如果路由参数有所更新，比如参数id变化了
        const { id } = to.query
        if (id !== from.query.id) {
          this.jobID = id
          this.xgbJobSavedModelData = []
          this.xgbJobOriginModelData = []
          id && this.getJobInfo()
        }
      },
      // 设置为深度监听
      deep: true
    }
  },
  methods: {
    goJobDetail(id) {
      this.$router.push({ path: '/jobDetail', query: { id } })
    },
    copyModel(modelStr) {
      copy(modelStr, '模型复制成功')
    },
    goServiceDetail(serviceId, type) {
      this.$router.push({ path: '/serverDetail', query: { serviceId, serverType: type } })
    },
    goDatasetDetail(datasetId) {
      this.$router.push({ path: '/dataDetail', query: { datasetId } })
    },
    handleXgbJobSavedModelData(modelString, job) {
      const { setting, name, ...rest } = JSON.parse(modelString)
      const { owner, ownerAgency } = job
      const settingData = JSON.parse(setting)
      const { participant_agency_list, ...restSetting } = settingData
      this.xgbJobSavedModelData = participant_agency_list.map((v) => {
        return {
          ...rest,
          ...restSetting,
          ...v,
          modelName: name,
          modelAgency: ownerAgency,
          modelOwner: owner,
          modelString
        }
      })
      console.log(this.xgbJobSavedModelData, 'xgbJobSavedModelData')
    },
    handleXgbJobOriginModelData(modelString) {
      const settingData = JSON.parse(modelString)
      const { participant_agency_list, ...restSetting } = settingData
      this.xgbJobOriginModelData = participant_agency_list.map((v) => {
        return {
          ...restSetting,
          ...v
        }
      })
    },
    handleSettingTableData(modelSetting) {
      this.settingTableData = []
      Object.keys(modelSetting).forEach((key) => {
        this.settingTableData.push({
          label: key,
          value: modelSetting[key]
        })
      })
    },
    async getJobInfo(params) {
      this.loading = !this.timer
      const { jobID } = this
      const res = await jobManageServer.queryJobDetail({ jobID, ...params })
      this.loading = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { job = {}, model, resultFileInfo } = res.data
        const { param } = job
        const { jobStatusInfo = {} } = job
        this.jobInfo = { ...job }
        const { dataSetList, modelSetting, serviceId, searchType, queriedFields, searchIdList, mpcContent, sql } = JSON.parse(param)
        this.modelSetting = modelSetting
        this.jobStatusInfo = jobStatusInfo
        // 展示建模和预测任务参数
        if (this.modelSetting) {
          this.handleSettingTableData(this.modelSetting)
        }

        // 获取任务关联数据集列表
        if (dataSetList && dataSetList.length) {
          this.receiverList = dataSetList.filter((v) => v.receiveResult).map((v) => v.dataset.ownerAgency)
          this.labelProvider = dataSetList.filter((v) => v.labelProvider).map((v) => v.dataset.ownerAgency)
          this.particapate = dataSetList.filter((v) => !v.labelProvider).map((v) => v.dataset.ownerAgency)
          this.jobInfo.receiver = this.receiverList.join('，')
          this.jobInfo.labelProvider = this.labelProvider.join('，')
          this.jobInfo.particapate = this.particapate.join('，')
          const ids = dataSetList
            .map((v) => {
              return v.dataset && v.dataset.datasetID
            })
            .filter((v) => v)
          ids.length && this.getListDetail({ datasetIdList: ids })
          console.log(JSON.parse(model), 'model')
        }

        // pir服务需要获取服务详情
        if (serviceId) {
          this.jobInfo = { ...this.jobInfo, searchType, queriedFields, searchIdList }
          this.queryService(serviceId)
        }
        // 展示诊断信息
        if (jobStatusInfo.status === 'RunFailed') {
          this.jobInfo.statusDetail = jobStatusInfo.statusDetail
        }
        if (mpcContent) {
          this.jobInfo.mpcContent = mpcContent
        }
        if (sql) {
          this.jobInfo.sql = sql
        }
        // 查询任务模型和结果
        if (this.jobStatusInfo.status === 'RunSuccess') {
          if (
            [jobEnum.XGB_TRAINING, jobEnum.XGB_PREDICTING, jobEnum.LR_TRAINING, jobEnum.LR_PREDICTING].includes(this.jobInfo.jobType) &&
            this.receiverList.includes(this.agencyId)
          ) {
            this.getJobResult()
          }
          if (
            [jobEnum.PIR, jobEnum.PSI, jobEnum.MPC, jobEnum.SQL].includes(this.jobInfo.jobType) &&
            this.jobInfo.owner === this.userId &&
            this.jobInfo.ownerAgency === this.agencyId
          ) {
            // pir psi sql mpc 直接展示任务结果
            if (resultFileInfo) {
              this.resultFileInfo = resultFileInfo
            }
          }
        }

        this.startInterVal()
      } else {
        this.jobInfo = {}
      }
    },
    async getJobResult() {
      const { jobID } = this
      const res = await jobManageServer.queryJobDetail({ jobID, fetchJobResult: true, fetchJobDetail: false })
      if (res.code === 0 && res.data) {
        const { modelResultDetail = {}, job = {}, model } = res.data
        const { param = '{}' } = job
        const { modelPredictAlgorithm = '' } = JSON.parse(param)
        this.model = model
        // 展示建模任务模型
        if (model) {
          this.handleXgbJobOriginModelData(model)
        }
        // 展示建模任务保存后的模型
        if (modelPredictAlgorithm) {
          this.handleXgbJobSavedModelData(modelPredictAlgorithm, job)
        }
        this.modelResultDetail = modelResultDetail
      }
    },
    startInterVal() {
      this.timer && clearInterval(this.timer)
      this.timer = setInterval(() => {
        const pendingStatus = ['Submitted', 'Handshaking', 'HandshakeSuccess', 'Running']
        if (pendingStatus.includes(this.jobInfo.status)) {
          this.getJobInfo()
        } else {
          this.timer && clearInterval(this.timer)
        }
      }, 5000)
    },
    // 获取服务详情
    async queryService(serviceId) {
      this.loading = true
      const params = { condition: {}, serviceIdList: [serviceId], pageNum: 1, pageSize: 1 }
      const res = await serviceManageServer.getPublishList(params)
      this.loading = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { wedprPublishedServiceList = [] } = res.data
        const { serviceConfig = '', serviceType, agency, serviceId } = wedprPublishedServiceList[0] || {}
        this.receiverList = [agency]
        console.log(serviceType, 'serviceType')
        const { datasetId } = JSON.parse(serviceConfig)
        this.jobInfo = { ...this.jobInfo, datasetId, serviceId, serviceType }
      }
    },
    // 获取数据集详情
    async getListDetail(params) {
      const res = await dataManageServer.queryDatasetList(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { data = [] } = res
        this.dataList = data
      } else {
        this.dataList = []
      }
    },
    showSettingSaveModal() {
      this.$prompt('请输入保存的配置名称', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValidator(value) {
          console.log(value, 'value')
          try {
            return !!value.trim()
          } catch {
            return false
          }
        },
        inputErrorMessage: '配置名称不能为空'
      })
        .then(({ value }) => {
          this.saveModelConf(value)
        })
        .catch(() => {})
    },
    showSettingSaveModelModal() {
      this.$prompt('请输入保存的模型名称', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValidator(value) {
          console.log(value, 'value')
          try {
            return !!value.trim()
          } catch {
            return false
          }
        },
        inputErrorMessage: '模型名称不能为空'
      })
        .then(({ value }) => {
          this.saveModel(value)
        })
        .catch(() => {})
    },
    async saveModelConf(name) {
      const { modelSetting } = this
      const { jobType } = this.jobInfo
      const params = {
        templateList: [{ name, type: settingMap[jobType], setting: JSON.stringify({ ...modelSetting }) }]
      }
      const res = await settingManageServer.insertSettings(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { data } = res
        console.log(data)
        this.$message.success('保存配置成功！')
      }
    },
    async saveModel(name) {
      const { model, jobID } = this
      const { jobType } = this.jobInfo
      const modelData = JSON.parse(model)
      const params = {
        templateList: [{ name, type: modelSettingMap[jobType], setting: JSON.stringify({ ...modelData, jobID }) }]
      }
      const res = await settingManageServer.insertSettings(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { data } = res
        console.log(data)
        this.$message.success('保存模型成功！')
      }
    },
    reBuild() {
      const { jobID = '' } = this
      const { jobType } = this.jobInfo
      this.$router.push({
        path: 'resetParams',
        query: { jobID, jobType }
      })
    },
    objectSpanMethodOrigin({ row, column, rowIndex, columnIndex }) {
      const length = this.xgbJobOriginModelData.length
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
    objectSpanMethodSaved({ row, column, rowIndex, columnIndex }) {
      const length = this.xgbJobSavedModelData.length
      if (columnIndex < 6 || columnIndex > 7) {
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
    getDetail(datasetId) {
      this.$router.push({ path: '/dataDetail', query: { datasetId } })
    }
  },
  destroyed() {
    this.timer && clearInterval(this.timer)
  }
}
</script>
<style lang="less" scoped>
::v-deep .el-tabs--card > .el-tabs__header {
  border-color: #ccc;
}
div.whole {
  display: flex;
  margin-bottom: 16px;
  .error-conatiner {
    max-width: 80%;
    word-break: break-all;
  }
}
div.half {
  width: 50%;
  display: flex;
}
div.con {
  margin-bottom: 30px;
}
.tips {
  color: #b3b5b9;
  margin-bottom: 10px;
}
.log-container {
  .log {
    line-height: 20px;
    border: 1px solid #ccc;
    padding: 10px;
    ::v-deep .error {
      display: inline-block;
      background-color: rgb(255, 77, 79);
    }
    ::v-deep .warn {
      display: inline-block;
      background-color: #e6a23c;
    }
  }
}
div.info-container {
  margin-bottom: 44px;
  span.title {
    float: left;
    width: 106px;
    text-align: right;
    color: #525660;
    font-size: 14px;
    line-height: 22px;
  }
  span.info {
    flex: 1;
    color: #262a32;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-size: 14px;
    line-height: 22px;
  }
  span.error-conatiner {
    color: #262a32;
    font-size: 14px;
    line-height: 22px;
    word-break: break-all;
  }
  span.info.link {
    cursor: pointer;
    color: #3071f2;
  }
  .el-row {
    margin-bottom: 16px;
  }
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
  }
  span.right {
    width: 118px;
    font-size: 14px;
    line-height: 22px;
    color: #525660;
  }
}
</style>
