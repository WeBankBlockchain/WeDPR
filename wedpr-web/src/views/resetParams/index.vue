<template>
  <div class="create-data">
    <div class="participates data-container">
      <p>已选数据</p>
      <div class="area table-area">
        <el-table size="small" :data="tableData" :border="true" class="table-wrap">
          <el-table-column label="角色" prop="ownerAgencyId" show-overflow-tooltip>
            <template v-slot="scope">
              <el-tag color="#4384ff" style="color: white" v-if="scope.row.labelProvider" size="small">标签方</el-tag>
              <el-tag color="#4CA9EC" style="color: white" v-if="!scope.row.labelProvider" size="small">参与方</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="机构ID" prop="ownerAgencyId" show-overflow-tooltip />
          <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
          <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
          <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
          <el-table-column v-if="false" label="已选标签字段" prop="selectedTagFields" show-overflow-tooltip />
        </el-table>
      </div>
    </div>

    <formCard key="set" title="请设置参数">
      <div class="alg-container">
        <el-form label-width="200px" :model="xgbSettingForm" ref="xgbSettingForm" :rules="xgbSettingFormRules">
          <el-form-item v-for="item in modelModule" :key="item.label" :label="item.label">
            <el-input-number size="small" v-if="item.type === 'float'" v-model="item.value" :step="0.1" style="width: 140px" :min="item.min_value" :max="item.max_value" />
            <el-input size="small" v-if="item.type === 'string'" v-model="item.value" style="width: 140px" />
            <el-input-number
              size="small"
              v-if="item.type === 'int'"
              v-model="item.value"
              :step="1"
              :min="item.min_value"
              :max="item.max_value"
              step-strictly
              style="width: 140px"
            />
            <el-radio-group v-if="item.type === 'bool'" v-model="item.value">
              <el-radio :label="1"> true </el-radio>
              <el-radio :label="0"> false </el-radio>
            </el-radio-group>
            <span v-if="item.type !== 'bool'" class="tips">{{ item.description }}</span>
          </el-form-item>
        </el-form>
      </div>
    </formCard>
    <div>结果接收方： {{ receiverStr }}</div>
    <div style="margin-top: 30px">
      <el-button size="medium" type="primary" @click="handleXGBdata"> 重新运行 </el-button>
    </div>
  </div>
</template>
<script>
import formCard from '@/components/formCard.vue'
import { jobManageServer, settingManageServer, projectManageServer, dataManageServer } from 'Api'
import { jobEnum } from 'Utils/constant.js'
export default {
  name: 'serverCreate',
  components: {
    formCard
  },
  data() {
    return {
      dataForm: {
        setting: []
      },
      jobID: '',
      jobInfo: {},
      modelModule: [],
      modelSetting: {},
      xgbSettingForm: {},
      xgbSettingFormRules: [],
      parties: [],
      projectName: '',
      tableData: [],
      receiverStr: ''
    }
  },
  created() {
    const { jobID } = this.$route.query
    this.jobID = jobID
    this.querySettings({
      onlyMeta: false,
      condition: {
        id: '',
        name: jobEnum.XGB_TRAINING,
        owner: '*'
      }
    })
  },
  methods: {
    handleXGBdata() {
      const { modelModule, dataSetList, parties, projectName } = this
      const modelSetting = {}
      modelModule.forEach((v) => {
        const key = v.label
        modelSetting[key] = v.value
      })
      const param = { dataSetList, modelSetting }
      const params = { jobType: jobEnum.XGB_TRAINING, projectName, param: JSON.stringify(param) }
      const taskParties = parties.map((v) => {
        return {
          userName: v.userName,
          agency: v.agency
        }
      })
      console.log({ job: params, taskParties })
      this.submitJob({ job: params, taskParties })
    },
    // 创建JOB
    async submitJob(params) {
      this.loadingFlag = true
      const res = await projectManageServer.submitJob(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        this.$message.success('任务创建成功')
        this.$router.push({ path: '/jobDetail', query: { id: res.data } })
      }
    },
    async querySettings(params) {
      const res = await settingManageServer.querySettings(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { setting = '' } = res.data[0]
        console.log('JSON.parse(setting)', JSON.parse(setting))
        this.modelModule = JSON.parse(setting)
        this.queryJobByCondition()
      }
    },
    // 获取数据集详情
    async getListDetail(params) {
      this.loadingFlag = true
      const res = await dataManageServer.queryDatasetList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { data = [] } = res
        this.tableData = data.map((v, i) => {
          const { labelProvider } = this.dataSetList[i]
          return { ...v, labelProvider }
        })
      } else {
        this.tableData = []
      }
    },
    // 获取任务信息
    async queryJobByCondition() {
      this.loadingFlag = true
      const { jobID } = this
      const res = await jobManageServer.queryJobByCondition({ job: { id: jobID } })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { jobs = [] } = res.data
        this.jobInfo = jobs[0]
        const { parties, param, projectName } = this.jobInfo
        const { modelSetting = {}, dataSetList = {} } = JSON.parse(param)
        console.log(dataSetList, 'dataSetList', JSON.parse(param))
        this.modelSetting = modelSetting
        this.dataSetList = dataSetList
        this.modelModule.forEach((v) => {
          v.value = this.modelSetting[v.label]
        })
        this.parties = JSON.parse(parties)
        this.projectName = projectName
        const datasetIdList = dataSetList.map((v) => {
          return v.dataset.datasetID
        })
        this.receiverStr = dataSetList
          .filter((v) => v.receiveResult)
          .map((v) => v.dataset.ownerAgency)
          .join('，')
        this.getListDetail({ datasetIdList })
        console.log(this.parties, 'parties')
      } else {
        this.jobInfo = {}
      }
    }
  }
}
</script>
<style lang="less" scoped>
.alg-container {
  overflow: hidden;
  div.alg {
    float: left;
    text-align: center;
    height: 54px;
    background: #eff3fa;
    margin: 16px;
    width: calc(16% - 32px);
    line-height: 74px;
    color: #262a32;
    display: flex;
    align-items: center;
    min-width: 220px;
    border-radius: 16px;
    cursor: pointer;
    border: 2px solid white;
    box-sizing: content-box;
    img {
      height: 54px;
      width: auto;
      border-top-left-radius: 16px;
      border-bottom-left-radius: 16px;
    }
    .title {
      flex: 1;
      text-align: center;
      font-size: 16px;
      color: #262a32;
    }
  }
  .alg.active {
    border-color: #3071f2;
  }
  span.tips {
    margin-left: 10px;
  }
}
.data-container {
  width: 100%;
  height: auto;
  border: 1px solid #e0e4ed;
  border-radius: 12px;
  margin-bottom: 42px;
  overflow: hidden;
  p {
    background: #d6e3fc;
    color: #262a32;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    text-align: left;
    padding: 13px 24px;
    span {
      float: right;
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      color: #262a32;
      padding: 3px 12px;
      background-color: white;
      border-radius: 4px;
      transform: translateY(-4px);
      cursor: pointer;
      img {
        width: 16px;
        height: 16px;
        transform: translateY(2px);
      }
    }
  }
  div.area {
    text-align: center;
    height: 126px;
    display: flex;
    align-items: center;
    flex-direction: column;
    justify-content: center;
    cursor: pointer;
    img {
      display: inline-block;
      width: 16px;
      height: 16px;
      margin-bottom: 8px;
    }
  }
  div.area.table-area {
    height: auto;
  }
}
</style>
