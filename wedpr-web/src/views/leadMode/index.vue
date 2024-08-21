<template>
  <div class="lead-mode">
    <ul>
      <li><span>项目名称：</span>{{ dataInfo.name }}</li>
      <li><span>项目简介：</span>{{ dataInfo.projectDesc }}</li>
    </ul>
    <div class="step-container">
      <el-steps :active="active" finish-status="success">
        <el-step title="选择模型"></el-step>
        <el-step title="选择数据资源"></el-step>
        <el-step title="配置并运行"></el-step>
        <el-step title="查看结果"></el-step>
      </el-steps>
    </div>

    <formCard key="1" title="请选择模型" v-show="active === 0">
      <div class="alg-container">
        <div :class="selectedAlg === item.value ? 'alg active' : 'alg'" v-for="item in algListFull" @click="selectAlg(item.value)" :key="item.value">
          <img :src="item.jobSrc" alt="" />
          <span class="title">{{ item.label }}</span>
        </div>
      </div>
    </formCard>
    <div v-show="active === 1">
      <div class="tags data-container" v-if="selectedAlg === jobEnum.XGB_TRAINING">
        <p>
          选择标签数据 <span class="btn" @click="removeTag" v-if="tagSelectList.length"><img src="~Assets/images/icon_delete.png" alt="" /> 移除 </span>
        </p>
        <div class="area" @click="addTag" v-if="!tagSelectList.length">
          <img src="~Assets/images/add_dataset.png" alt="" />
          <div>点击选择数据</div>
        </div>
        <div class="area table-area" v-else>
          <el-table size="small" :data="tagSelectList" :border="true" class="table-wrap">
            <el-table-column label="机构ID" prop="ownerAgencyId" show-overflow-tooltip />
            <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
            <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
            <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
            <el-table-column label="包含字段" prop="datasetFields" show-overflow-tooltip />
            <el-table-column label="已选标签字段" prop="selectedTagFields" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <div class="participates data-container" v-for="(item, index) in paticipateSelectList" :key="item">
        <p>
          选择参与方数据 {{ selectedAlg === jobEnum.PSI ? index + 1 : '' }}
          <span class="btn" @click="removeParticipate(item.datasetId)" v-if="item.datasetId"><img src="~Assets/images/icon_delete.png" alt="" /> 移除 </span>
        </p>
        <div class="area" @click="showAddParticipate" v-if="!item.datasetId">
          <img src="~Assets/images/add_dataset.png" alt="" />
          <div>点击选择数据</div>
        </div>
        <div class="area table-area" v-else>
          <el-table size="small" :data="[item]" :border="true" class="table-wrap">
            <el-table-column label="机构ID" prop="ownerAgencyId" show-overflow-tooltip />
            <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
            <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
            <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
            <el-table-column label="包含字段" prop="datasetFields" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <div class="add" @click="showAddParticipate">
        <span><img src="~Assets/images/add_participate.png" alt="" />增加参与方</span>
      </div>
    </div>
    <div v-show="active === 2">
      <formCard v-if="selectedAlg === jobEnum.XGB_TRAINING" key="3" title="配置并运行">
        <el-form label-width="200px" :model="xgbSettingForm" ref="xgbSettingForm" :rules="xgbSettingFormRules">
          <div class="participates data-container">
            <p>已选数据</p>
            <div class="area table-area">
              <el-table size="small" :data="xgbSettingForm.selectedData" :border="true" class="table-wrap">
                <el-table-column label="角色" prop="ownerAgencyId" show-overflow-tooltip>
                  <template v-slot="scope">
                    <el-tag color="#4384ff" style="color: white" v-if="scope.row.selectedTagFields" size="small">标签方</el-tag>
                    <el-tag color="#4CA9EC" style="color: white" v-if="!scope.row.selectedTagFields" size="small">参与方</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="机构ID" prop="ownerAgencyId" show-overflow-tooltip />
                <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
                <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
                <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
                <el-table-column label="已选标签字段" prop="selectedTagFields" show-overflow-tooltip />
              </el-table>
            </div>
          </div>
          <formCard key="set" title="请设置参数">
            <div class="alg-container">
              <el-form-item v-if="false" label="选择历史参数" prop="template">
                <el-input size="small" style="width: 360px" v-model="xgbSettingForm.template" placeholder="请选择"> </el-input>
              </el-form-item>

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
            </div>
          </formCard>
          <el-form-item label="结果接收方：" prop="receiver" label-width="120px">
            <el-select size="small" style="width: 360px" v-model="xgbSettingForm.receiver" multiple placeholder="请选择">
              <el-option :key="item" v-for="item in agencyList" multiple :label="item.lable" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </formCard>
      <div v-if="selectedAlg === jobEnum.PSI">
        <el-form label-width="200px" :model="psiForm" ref="psiForm" :rules="psiFormRules">
          <div class="participates data-container">
            <p>选择数据字段</p>
            <div class="area table-area">
              <el-table size="small" :data="psiForm.selectedData" :border="true" class="table-wrap">
                <el-table-column label="机构ID" prop="ownerAgencyId" show-overflow-tooltip />
                <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
                <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
                <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
                <el-table-column label="包含字段" prop="datasetFields" show-overflow-tooltip>
                  <template v-slot="scope">
                    <el-select size="small" v-model="scope.row.datasetFieldsSelected" placeholder="请选择" multiple>
                      <el-option :key="item" v-for="item in scope.row.datasetFields.trim().split(',')" :label="item" :value="item"></el-option>
                    </el-select>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <el-form-item label="结果接收方：" prop="receiver" label-width="120px">
            <el-select size="small" style="width: 360px" v-model="psiForm.receiver" multiple placeholder="请选择">
              <el-option :key="item" v-for="item in agencyList" :label="item.lable" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div>
      <el-button size="medium" v-if="active > 0" @click="pre"> 上一步 </el-button>
      <el-button size="medium" v-if="active < 2" type="primary" @click="next" :disabled="nextDisabaled"> 下一步 </el-button>
      <el-button size="medium" v-if="active === 2 && selectedAlg === jobEnum.PSI" type="primary" @click="runPSIJob" :disabled="runDisabaled"> 运行 </el-button>
      <el-button size="medium" v-if="active === 2 && selectedAlg === jobEnum.XGB_TRAINING" type="primary" @click="runXGBJob" :disabled="runXGBTrainDisabaled"> 运行 </el-button>
    </div>
    <tagSelect :showTagsModal="showTagsModal" @closeModal="closeModal" @tagSelected="tagSelected"></tagSelect>
    <participateSelect :showParticipateModal="showParticipateModal" @closeModal="closeModal" @participateSelected="participateSelected"></participateSelect>
  </div>
</template>
<script>
import formCard from '@/components/formCard.vue'
// import dataCard from '@/components/dataCard.vue'
import { settingManageServer, projectManageServer } from 'Api'
import tagSelect from './tagSelect/index.vue'
import participateSelect from './participateSelect/index.vue'
import { mapGetters } from 'vuex'
import { algListFull, jobEnum } from 'Utils/constant.js'
export default {
  name: 'leadMode',
  components: {
    formCard,
    tagSelect,
    participateSelect
    // dataCard
  },
  data() {
    return {
      active: 0,
      activeName: 'first',
      dataForm: {
        setting: []
      },
      xgbSettingForm: {
        template: '',
        receiver: [],
        selectedData: []
      },
      xgbSettingFormRules: {
        receiver: [{ required: true, message: '结果接收方不能为空', trigger: 'blur' }],
        selectedData: [{ required: true, message: '参与方不能为空', trigger: 'blur' }]
      },
      psiForm: {
        receiver: [],
        selectedData: []
      },
      psiFormRules: {
        receiver: [{ required: true, message: '结果接收方不能为空', trigger: 'blur' }],
        selectedData: [{ required: true, message: '参与方不能为空', trigger: 'blur' }]
      },
      modelModule: [
        {
          type: 'float',
          min_value: 0,
          max_value: 1,
          value: 0.5,
          label: '测试值：',
          description: '我是测试值'
        },
        {
          type: 'float',
          min_value: 0,
          max_value: 1,
          value: 0.5,
          label: '测试值1：',
          description: '我是测试值'
        }
      ],
      selectedAlg: '',
      algListFull,
      showTagsModal: false,
      showParticipateModal: false,
      pageData: {},
      tagSelectList: [],
      paticipateSelectList: [{}],
      dataInfo: {},
      jobEnum
    }
  },
  created() {
    const { projectId } = this.$route.query
    this.projectId = projectId
    projectId && this.queryProject()
  },
  watch: {
    selectedAlg(value) {
      console.log(value)
      this.querySettings({
        onlyMeta: false,
        condition: {
          id: '',
          name: value,
          owner: '*'
        }
      })
      switch (value) {
        case jobEnum.XGB_TRAINING:
          this.paticipateSelectList = [{}]
          break
        case jobEnum.PSI:
          this.paticipateSelectList = [{}, {}]
          break
        default:
          break
      }
    },
    selectedData(v) {
      console.log(v, 'selectedData=================')
      if (this.selectedAlg === jobEnum.PSI) {
        this.psiForm.selectedData = v.map((v) => {
          return { ...v, datasetFieldsSelected: [] }
        })
      }
      if (this.selectedAlg === jobEnum.XGB_TRAINING) {
        this.xgbSettingForm.selectedData = v.map((v) => {
          return { ...v }
        })
      }
    }
  },
  computed: {
    ...mapGetters(['agencyList', 'algList']),
    nextDisabaled() {
      if (this.active === 0) {
        return !this.selectedAlg
      }
      if (this.active === 1) {
        const validpaticipateSelectList = this.paticipateSelectList.filter((v) => v.datasetId)
        if (this.selectedAlg === jobEnum.PSI) {
          return !(validpaticipateSelectList.length >= 2)
        }
        if (this.selectedAlg === jobEnum.XGB_TRAINING) {
          return !(this.tagSelectList.length && validpaticipateSelectList.length)
        }
      }
      return false
    },
    selectedData() {
      const paticipateData = this.paticipateSelectList.map((v) => {
        return { ...v, datasetFields: v.datasetFields || '', datasetFieldsSelected: [] }
      })
      return [...this.tagSelectList, ...paticipateData]
    },
    runDisabaled() {
      const selectedFields = this.psiForm.selectedData.every((v) => v.datasetFieldsSelected.length)
      return !(this.psiForm.receiver.length && selectedFields)
    },
    runXGBTrainDisabaled() {
      return !this.xgbSettingForm.receiver.length
    }
  },
  methods: {
    checkPSIData() {
      this.$refs.psiForm.validate((valid) => {
        if (valid) {
          console.log(valid)
          this.handlePsiJobData()
        }
      })
    },
    checkXGBData() {
      this.$refs.xgbSettingForm.validate((valid) => {
        if (valid) {
          console.log(valid)
          this.handleXGBdata()
        }
      })
    },
    runPSIJob() {
      console.log('run start')
      this.checkPSIData()
    },
    runXGBJob() {
      console.log('run start')
      this.checkXGBData()
    },
    // 获取项目详情
    async queryProject() {
      this.loadingFlag = true
      const { projectId } = this
      const res = await projectManageServer.queryProject({ project: { id: projectId } })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { dataList = [] } = res.data
        this.dataInfo = dataList[0] || {}
      } else {
        this.dataInfo = []
      }
    },

    handlePsiJobData() {
      const { selectedAlg } = this
      const { selectedData, receiver } = this.psiForm
      const ownerAgencyIdList = selectedData.map((v) => v.ownerAgencyId)
      if (Array.from(new Set(ownerAgencyIdList)).length < 2) {
        this.$message.error('参与方至少为2方')
        return
      }
      const { name } = this.dataInfo
      console.log(selectedData, 'selectedData')
      const dataSetList = selectedData.map((v) => {
        console.log(v, v.datasetStoragePath, JSON.parse(v.datasetStoragePath))
        const dataset = {
          owner: v.ownerUserId,
          ownerAgency: v.ownerAgencyId,
          path: JSON.parse(v.datasetStoragePath).filePath,
          storageTypeStr: v.datasetStorageType,
          datasetID: v.datasetId
        }
        return {
          idFields: v.datasetFieldsSelected,
          dataset,
          receiveResult: receiver.includes(v.ownerAgencyId)
        }
      })
      const param = { dataSetList }
      const params = { jobType: selectedAlg, projectName: name, param: JSON.stringify(param) }
      const taskParties = selectedData.map((v) => {
        return {
          userName: v.ownerUserId,
          agency: v.ownerAgencyId
        }
      })
      console.log({ job: params, taskParties }, receiver)
      this.submitJob({ job: params, taskParties })
    },
    handleXGBdata() {
      const { selectedAlg, modelModule } = this
      const { selectedData, receiver } = this.xgbSettingForm
      const ownerAgencyIdList = selectedData.map((v) => v.ownerAgencyId)
      if (Array.from(new Set(ownerAgencyIdList)).length < 2) {
        this.$message.error('参与方至少为2方')
        return
      }
      const { name } = this.dataInfo
      console.log(selectedData, 'selectedData')
      const dataSetList = selectedData.map((v) => {
        console.log(v, v.datasetStoragePath, JSON.parse(v.datasetStoragePath))
        const dataset = {
          owner: v.ownerUserId,
          ownerAgency: v.ownerAgencyId,
          path: JSON.parse(v.datasetStoragePath).filePath,
          storageTypeStr: v.datasetStorageType,
          datasetID: v.datasetId
        }
        return {
          idFields: v.datasetFieldsSelected,
          dataset,
          labelProvider: !!v.selectedTagFields,
          receiveResult: receiver.includes(v.ownerAgencyId)
        }
      })
      const modelSetting = {}
      modelModule.forEach((v) => {
        const key = v.label
        modelSetting[key] = v.value
      })
      const param = { dataSetList, modelSetting }
      console.log(param, 'modelSettingmodelSettingmodelSettingmodelSettingmodelSettingmodelSettingmodelSetting')
      const params = { jobType: selectedAlg, projectName: name, param: JSON.stringify(param) }
      const taskParties = selectedData.map((v) => {
        return {
          userName: v.ownerUserId,
          agency: v.ownerAgencyId
        }
      })
      console.log({ job: params, taskParties }, receiver)
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
      } else {
        this.dataInfo = []
      }
    },
    async querySettings(params) {
      const res = await settingManageServer.querySettings(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { setting = '' } = res.data[0]
        console.log(setting, 'JSON.parse(setting)')
        this.modelModule = JSON.parse(setting)
      }
    },
    showAddParticipate() {
      this.showParticipateModal = true
    },
    closeModal() {
      this.showTagsModal = false
      this.showParticipateModal = false
    },
    tagSelected(data) {
      this.showTagsModal = false
      console.log(data, 'data')
      this.tagSelectList = [...data]
    },
    removeTag() {
      this.tagSelectList = []
    },
    setArea() {
      if (this.paticipateSelectList.some((v) => v.datasetId)) {
        this.paticipateSelectList = this.paticipateSelectList.filter((v) => v.datasetId)
      } else {
        this.paticipateSelectList = [{}]
      }
    },
    removeParticipate(datasetId) {
      this.paticipateSelectList = this.paticipateSelectList.filter((v) => datasetId !== v.datasetId)
      this.setArea()
    },
    participateSelected(data) {
      this.showParticipateModal = false
      this.paticipateSelectList.push({
        ...data
      })
      this.setArea()
    },
    handleClick() {},
    addTag() {
      this.showTagsModal = true
    },

    next() {
      this.active++
    },
    pre() {
      this.active--
    },
    selectAlg(value) {
      console.log(value, 'algV')
      this.selectedAlg = value
    }
  }
}
</script>
<style lang="less" scoped>
div.lead-mode {
  ul {
    li {
      color: #525660;
      font-size: 14px;
      line-height: 22px;
      margin-bottom: 12px;
      span {
        color: #787b84;
      }
    }
  }
  .step-container {
    margin-top: 42px;
    margin-bottom: 42px;
    width: 732px;
  }
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
  }
  .data-container {
    width: 872px;
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
  div.add {
    border: 1px solid #e0e4ed;
    width: 872px;
    height: 36px;
    padding: 7px 12px;
    text-align: center;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    justify-content: center;
    color: #3071f2;
    font-size: 14px;
    line-height: 22px;
    margin-bottom: 44px;
    span {
      display: flex;
      align-items: center;
    }
    img {
      width: 18px;
      height: 18px;
      margin-right: 8px;
    }
  }
  span.tips {
    font-size: 14px;
    color: #787b84;
    margin-left: 16px;
    font-weight: 500;
  }
  ::v-deep .el-step__head.is-success {
    color: #3071f2;
    border-color: #3071f2;
  }
  ::v-deep .el-step__title.is-success {
    color: #3071f2;
  }
  ::v-deep .el-step__head.is-process {
    color: white;
    .el-step__icon {
      background-color: #3071f2;
      border-color: #3071f2;
    }
  }
  ::v-deep .el-step__title.is-process {
    color: #3071f2;
  }
  ::v-deep .el-step.is-horizontal .el-step__line {
    top: 18px;
  }
  ::v-deep .el-step__icon {
    width: 36px;
    height: 36px;
  }
}
</style>
