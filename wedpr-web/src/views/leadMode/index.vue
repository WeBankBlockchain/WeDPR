<template>
  <div class="lead-mode">
    <ul>
      <li><span>项目名称：</span>{{ dataInfo.name }}</li>
      <li><span>项目简介：</span>{{ dataInfo.projectDesc }}</li>
    </ul>
    <div class="step-container">
      <el-steps :active="active" finish-status="success">
        <el-step title="选择模板" :description="selectedAlg && selectedAlg.label"></el-step>
        <el-step title="选择数据资源"></el-step>
        <el-step title="配置并运行"></el-step>
        <el-step title="查看结果"></el-step>
      </el-steps>
    </div>
    <!-- step1 ==================================================== -->
    <formCard key="1" title="请选择模板" v-show="active === 0">
      <div class="alg-container">
        <div :class="selectedAlg.value === item.value ? 'alg active' : 'alg'" v-for="item in filteredAlgList" @click="selectAlg(item)" :key="item.value">
          <img :src="item.src" alt="" />
          <span class="title">{{ item.label }}</span>
        </div>
      </div>
    </formCard>
    <!-- step2 ==================================================== -->
    <div v-show="active === 1 && selectedAlg.value !== jobEnum.PIR">
      <formCard style="width: 1124px" title="请选择模型" v-if="[jobEnum.XGB_PREDICTING, jobEnum.LR_PREDICTING].includes(selectedAlg.value)">
        <modelSelect :jobType="selectedAlg.value" @modelSelectedChange="modelSelectedChange" v-model="jobSettingForm.modelPredictAlgorithm" />
      </formCard>
      <div class="tags data-container" v-if="selectedAlg.needTagsProvider">
        <p>
          选择标签数据
          <span class="btn" @click="removeTag" v-if="tagSelectList.length"> <img src="~Assets/images/icon_delete.png" alt="" /> 移除 </span>
        </p>
        <div class="area" @click="addTag" v-if="!tagSelectList.length">
          <img src="~Assets/images/add_dataset.png" alt="" />
          <div>点击选择数据</div>
        </div>
        <div class="area table-area" v-else>
          <el-table size="small" :data="tagSelectList" :border="true" class="table-wrap">
            <el-table-column label="机构ID" prop="ownerAgencyName" show-overflow-tooltip />
            <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
            <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
            <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
            <el-table-column label="包含字段" prop="datasetFields" show-overflow-tooltip />
            <el-table-column v-if="selectedAlg.value === jobEnum.XGB_TRAINING" label="已选标签字段" prop="selectedTagFields" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <div class="participates data-container" v-for="(item, index) in paticipateSelectList" :key="item">
        <p>
          选择参与方数据 {{ selectedAlg.value === jobEnum.XGB_TRAINING ? '' : index + 1 }}
          <span class="btn" @click="removeParticipate(index)" v-if="item.datasetId"><img src="~Assets/images/icon_delete.png" alt="" /> 移除 </span>
        </p>
        <div class="area" @click="showAddParticipate(index)" v-if="!item.datasetId">
          <img src="~Assets/images/add_dataset.png" alt="" />
          <div>点击选择数据</div>
        </div>
        <div class="area table-area" v-else>
          <el-table size="small" :data="[item]" :border="true" class="table-wrap">
            <el-table-column label="机构ID" prop="ownerAgencyName" show-overflow-tooltip />
            <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
            <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
            <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
            <el-table-column label="包含字段" prop="datasetFields" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <div class="add" v-if="selectedAlg" @click="showAddParticipate(paticipateSelectList.length)">
        <span><img src="~Assets/images/add_participate.png" alt="" />增加参与方</span>
      </div>
    </div>
    <div v-show="active === 1 && selectedAlg.value === jobEnum.PIR">
      <serviceSelect :serviceType="serviceTypeEnum.PIR" @selected="handleServiceSelected" />
      <div class="participates data-container" v-if="selectedServiceConfig.datasetId">
        <p>服务详情</p>
        <div class="area table-area">
          <el-table size="small" :data="jobSettingForm.selectedData" :border="true" class="table-wrap">
            <el-table-column label="服务名称" prop="serviceName" show-overflow-tooltip />
            <el-table-column label="数据集" prop="datasetId" show-overflow-tooltip />
            <el-table-column label="主键" prop="idField" show-overflow-tooltip />
            <el-table-column label="支持的查询方式" prop="searchType" show-overflow-tooltip>
              <template v-slot="scope">
                <span v-if="scope.row.searchType === searchTypeEnum.ALL">查询存在性，查询字段值</span>
                <span v-if="scope.row.searchType === searchTypeEnum.SearchExist">查询存在性</span>
                <span v-if="scope.row.searchType === searchTypeEnum.SearchValue">查询字段值</span>
              </template>
            </el-table-column>
            <el-table-column label="可查的字段列表" prop="accessibleValueQueryFields">
              <template v-slot="scope">
                {{ scope.row.accessibleValueQueryFields && scope.row.accessibleValueQueryFields.join(',') }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <!-- step3 ==================================================== -->
    <div v-show="active === 2">
      <!-- XGB_TRAINING LR_TRAINING -->
      <el-form
        v-if="[jobEnum.XGB_TRAINING, jobEnum.LR_TRAINING].includes(selectedAlg.value)"
        label-width="200px"
        :model="jobSettingForm"
        ref="jobSettingForm"
        :rules="jobSettingFormRules"
      >
        <div class="participates data-container">
          <p>已选数据</p>
          <div class="area table-area">
            <el-table size="small" :data="jobSettingForm.selectedData" :border="true" class="table-wrap">
              <el-table-column label="角色" prop="ownerAgencyName" show-overflow-tooltip>
                <template v-slot="scope">
                  <el-tag color="#4384ff" style="color: white" v-if="scope.row.selectedTagFields" size="small">标签方</el-tag>
                  <el-tag color="#4CA9EC" style="color: white" v-if="!scope.row.selectedTagFields" size="small">参与方</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="机构ID" prop="ownerAgencyName" show-overflow-tooltip />
              <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
              <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
              <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
              <el-table-column label="包含字段" prop="datasetFields" show-overflow-tooltip />
              <el-table-column label="已选标签字段" prop="selectedTagFields" show-overflow-tooltip />
            </el-table>
          </div>
        </div>
        <formCard key="set" title="请设置参数">
          <div class="alg-container">
            <el-form-item label="选择历史参数：" prop="setting">
              <el-select size="small" value-key="id" @change="handleSelectSetting" style="width: 360px" v-model="XGB_SETTING" placeholder="请选择">
                <el-option :key="item" v-for="item in modelSettingList" :label="item.label" :value="item.value"></el-option>
              </el-select>
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
              <el-select size="small" v-if="item.type === 'select'" style="width: 140px" v-model="item.value" placeholder="请选择">
                <el-option v-for="selectValue in item.value_list" :label="selectValue" :value="selectValue" :key="selectValue"></el-option>
              </el-select>
              <span v-if="item.type !== 'bool'" class="tips">{{ item.description }}</span>
            </el-form-item>
          </div>
        </formCard>
        <el-form-item label="结果接收方：" prop="receiver" label-width="120px">
          <el-select size="small" style="width: 360px" v-model="jobSettingForm.receiver" multiple placeholder="请选择">
            <el-option :key="item" v-for="item in agencyListAble" multiple :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <!-- XGB_PREDICTING LR_PREDICTING -->
      <el-form
        v-if="selectedAlg.value === jobEnum.XGB_PREDICTING || selectedAlg.value === jobEnum.LR_PREDICTING"
        label-width="200px"
        :model="jobSettingForm"
        ref="jobSettingForm"
        :rules="jobSettingFormRules"
      >
        <div class="participates data-container">
          <p>已选数据</p>
          <div class="area table-area">
            <el-table size="small" :data="jobSettingForm.selectedData" :border="true" class="table-wrap">
              <el-table-column label="角色" prop="ownerAgencyName" show-overflow-tooltip>
                <template v-slot="scope">
                  <el-tag v-if="scope.$index === 0" color="#4CA9EC" style="color: white" size="small">标签方</el-tag>
                  <el-tag v-else color="#4CA9EC" style="color: white" size="small">参与方</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="机构ID" prop="ownerAgencyName" show-overflow-tooltip />
              <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
              <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
              <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
              <el-table-column label="包含字段" prop="datasetFields" show-overflow-tooltip />
            </el-table>
          </div>
        </div>
        <formCard key="set" title="请设置参数">
          <div class="alg-container">
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
              <el-select size="small" v-if="item.type === 'select'" style="width: 140px" v-model="item.value" placeholder="请选择">
                <el-option v-for="selectValue in item.value_list" :label="selectValue" :value="selectValue" :key="selectValue"></el-option>
              </el-select>
              <span v-if="item.type !== 'bool'" class="tips">{{ item.description }}</span>
            </el-form-item>
          </div>
        </formCard>
        <el-form-item label="结果接收方：" prop="receiver" label-width="120px">
          <el-select size="small" style="width: 360px" v-model="jobSettingForm.receiver" multiple placeholder="请选择">
            <el-option :key="item" v-for="item in agencyListAble" multiple :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <!-- SQL MPC -->
      <el-form
        v-if="selectedAlg.value === jobEnum.SQL || selectedAlg.value === jobEnum.MPC"
        key="3"
        label-width="200px"
        :model="jobSettingForm"
        ref="jobSettingForm"
        :rules="jobSettingFormRules"
      >
        <div class="participates data-container">
          <p>已选数据</p>
          <div class="area table-area">
            <el-table size="small" :data="jobSettingForm.selectedData" :border="true" class="table-wrap">
              <el-table-column label="机构ID" prop="ownerAgencyName" show-overflow-tooltip />
              <el-table-column label="数据资源名称" prop="datasetTitle" show-overflow-tooltip />
              <el-table-column label="已选资源ID" prop="datasetId" show-overflow-tooltip />
              <el-table-column label="所属用户" prop="ownerUserName" show-overflow-tooltip />
              <el-table-column label="包含字段" prop="ownerUserName" show-overflow-tooltip />
            </el-table>
          </div>
        </div>
        <formCard key="SQL" class="sql-card" title="编写SQL语句" v-if="selectedAlg.value === jobEnum.SQL">
          <el-form-item label="" prop="sql" label-width="0">
            <div class="sql-container">
              <div class="modify-container">
                <editorCom v-model="jobSettingForm.sql" lang="sql" />
              </div>
              <div class="lead"><img src="~Assets/images/icon_guide.png" /> 语法指引及示例下载</div>
            </div>
          </el-form-item>
        </formCard>
        <formCard key="Python" class="sql-card" title="编写Python语句" v-if="selectedAlg.value === jobEnum.MPC">
          <el-form-item label="" prop="python" label-width="0">
            <div class="sql-container">
              <div class="modify-container">
                <editorCom v-model="jobSettingForm.python" lang="python" />
              </div>
              <div class="lead"><img src="~Assets/images/icon_guide.png" /> 语法指引及示例下载</div>
            </div>
          </el-form-item>
        </formCard>
        <el-form-item label="结果接收方：" prop="receiver" label-width="120px">
          <el-select size="small" style="width: 360px" v-model="jobSettingForm.receiver" multiple placeholder="请选择">
            <el-option :key="item" v-for="item in agencyListAble" multiple :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <!-- PIR -->
      <el-form v-if="selectedAlg.value === jobEnum.PIR" key="3" label-width="200px" :model="jobSettingForm" ref="jobSettingForm" :rules="jobSettingFormRules">
        <div class="participates data-container">
          <p>已选服务</p>
          <div class="area table-area">
            <el-table size="small" :data="jobSettingForm.selectedData" :border="true" class="table-wrap">
              <el-table-column label="服务名称" prop="serviceName" show-overflow-tooltip />
              <el-table-column label="数据集" prop="datasetId" show-overflow-tooltip />
              <el-table-column label="主键" prop="idField" show-overflow-tooltip />
              <el-table-column label="支持的查询方式" prop="searchType" show-overflow-tooltip>
                <template v-slot="scope">
                  <span v-if="scope.row.searchType === searchTypeEnum.ALL">查询存在性，查询字段值</span>
                  <span v-if="scope.row.searchType === searchTypeEnum.SearchExist">查询存在性</span>
                  <span v-if="scope.row.searchType === searchTypeEnum.SearchValue">查询字段值</span>
                </template>
              </el-table-column>
              <el-table-column label="可查的字段列表" prop="accessibleValueQueryFields">
                <template v-slot="scope">
                  {{ scope.row.accessibleValueQueryFields && scope.row.accessibleValueQueryFields.join(',') }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        <formCard key="PIR" title="请设置查询规则">
          <el-form-item label="查询类型：" prop="searchType" label-width="120px">
            <el-radio-group v-model="jobSettingForm.searchType">
              <el-radio
                v-if="selectedServiceConfig.searchType === searchTypeEnum.SearchExist || selectedServiceConfig.searchType === searchTypeEnum.ALL"
                :label="searchTypeEnum.SearchExist"
                >查询存在性</el-radio
              >
              <el-radio
                v-if="selectedServiceConfig.searchType === searchTypeEnum.SearchValue || selectedServiceConfig.searchType === searchTypeEnum.ALL"
                :label="searchTypeEnum.SearchValue"
                >查询字段值</el-radio
              >
              （默认查询主键为id）
            </el-radio-group>
          </el-form-item>
          <el-form-item label="查询字段：" prop="queriedFields" label-width="120px" v-if="jobSettingForm.searchType === searchTypeEnum.SearchValue">
            <el-select size="small" style="width: 360px" v-model="jobSettingForm.queriedFields" multiple placeholder="请选择">
              <el-option :key="item" v-for="item in selectedServiceConfig.accessibleValueQueryFields" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label-width="120px" style="margin-bottom: 10px" prop="searchIdList" label="字段值：">
            <el-input
              size="small"
              :autosize="{ minRows: 6 }"
              type="textarea"
              v-model="jobSettingForm.searchIdList"
              placeholder="请输入查询字段值,多个值用,分割"
              style="width: 480px"
            >
            </el-input>
          </el-form-item>
        </formCard>
      </el-form>
      <!-- PSI -->
      <div v-if="selectedAlg.value === jobEnum.PSI">
        <el-form label-width="200px" :model="jobSettingForm" ref="jobSettingForm" :rules="jobSettingFormRules">
          <div class="participates data-container">
            <p>选择数据字段</p>
            <div class="area table-area">
              <el-table size="small" :data="jobSettingForm.selectedData" :border="true" class="table-wrap">
                <el-table-column label="机构ID" prop="ownerAgencyName" show-overflow-tooltip />
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
            <el-select size="small" style="width: 360px" v-model="jobSettingForm.receiver" multiple placeholder="请选择">
              <el-option :key="item" v-for="item in agencyListAble" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <div>
      <el-button size="medium" v-if="active > 0" @click="pre"> 上一步 </el-button>
      <el-button size="medium" v-if="active < 2" type="primary" @click="next" :disabled="nextDisabaled"> 下一步 </el-button>
      <el-button size="medium" v-if="active === 2" type="primary" @click="runJob" :disabled="runDisabaled"> 运行 </el-button>
    </div>
    <tagSelect
      :showFieldsSelect="selectedAlg.value === jobEnum.XGB_TRAINING || selectedAlg.value === jobEnum.LR_TRAINING"
      :showTagsModal="showTagsModal"
      @closeModal="closeModal"
      @tagSelected="tagSelected"
      :ownerAgencyName="tagAgency"
    ></tagSelect>
    <participateSelect
      :ownerAgencyName="filterPaticipateAgency"
      :showParticipateModal="showParticipateModal"
      @closeModal="closeModal"
      @participateSelected="participateSelected"
    ></participateSelect>
  </div>
</template>
<script>
import formCard from '@/components/formCard.vue'
// import dataCard from '@/components/dataCard.vue'
import { settingManageServer, projectManageServer } from 'Api'
import tagSelect from './tagSelect/index.vue'
import participateSelect from './participateSelect/index.vue'
import modelSelect from './modelSelect/index.vue'
import { mapGetters } from 'vuex'
import { jobEnum, searchTypeEnum, serviceTypeEnum, settingMap } from 'Utils/constant.js'
import editorCom from '@/components/editorCom.vue'
import serviceSelect from './serviceSelect/index.vue'
export default {
  name: 'leadMode',
  components: {
    formCard,
    tagSelect,
    participateSelect,
    modelSelect,
    editorCom,
    serviceSelect
    // dataCard
  },
  data() {
    return {
      active: 0,
      activeName: 'first',
      dataForm: {
        setting: []
      },
      jobSettingForm: {
        receiver: [],
        selectedData: [],
        sql: '',
        queryType: 1,
        dataFields: [],
        fieldsValueList: [],
        modelPredictAlgorithm: {},
        searchType: [],
        queriedFields: [],
        searchIdList: ''
      },

      modelModule: [
        // {
        //   type: 'float',
        //   min_value: 0,
        //   max_value: 1,
        //   value: 0.5,
        //   label: '测试值：',
        //   description: '我是测试值'
        // },
      ],
      selectedAlg: {},
      showTagsModal: false,
      showParticipateModal: false,
      pageData: {},
      tagSelectList: [],
      paticipateSelectList: [{}],
      dataInfo: {},
      jobEnum,
      serviceTypeEnum,
      modelSettingList: [],
      XGB_SETTING: '',
      addContainerIndex: 0,
      modelTableData: [],
      searchTypeEnum,
      selectedServiceConfig: {},
      tagAgency: '',
      filterPaticipateAgency: [],
      agencyListAble: []
    }
  },
  created() {
    const { projectId } = this.$route.query
    this.projectId = projectId
    projectId && this.queryProject()
  },
  watch: {
    selectedAlg(selectedAlg) {
      console.log(selectedAlg)
      const { participateNumber } = selectedAlg
      this.paticipateSelectList = []
      for (let i = 0; i < parseInt(participateNumber); i++) {
        this.paticipateSelectList.push({})
      }
      console.log(participateNumber, this.paticipateSelectList)
      this.tagSelectList = []
      switch (selectedAlg.value) {
        case jobEnum.XGB_TRAINING:
          this.queryDefaultSettings('SYS_' + jobEnum.XGB_TRAINING)
          this.queryModelSettingList(settingMap[selectedAlg.value])
          break
        // FIXME:
        case jobEnum.LR_TRAINING:
          this.queryDefaultSettings('SYS_' + jobEnum.LR_TRAINING)
          this.queryModelSettingList(settingMap[selectedAlg.value])
          break
        case jobEnum.XGB_PREDICTING:
          this.queryDefaultSettings('SYS_PREDICTING')
          // this.queryModelSettingList()
          break
        case jobEnum.LR_PREDICTING:
          this.queryDefaultSettings('SYS_PREDICTING')
          // this.queryModelSettingList()
          break
        default:
          break
      }
    },
    selectedData(v) {
      console.log(v, 'selectedData=================')
      const ownerAgencyNameList = v.map((v) => v.ownerAgencyName)
      this.agencyListAble = this.agencyList.filter((v) => ownerAgencyNameList.includes(v.value))
      if (this.selectedAlg.value === jobEnum.PSI) {
        this.jobSettingForm.selectedData = v.map((v) => {
          return { ...v, datasetFieldsSelected: [] }
        })
      } else {
        this.jobSettingForm.selectedData = v.map((v) => {
          return { ...v }
        })
      }
    }
  },
  computed: {
    ...mapGetters(['agencyList', 'algList']),
    filteredAlgList() {
      console.log(this.algList, '=========================')
      return this.algList.filter((v) => v.enable)
    },
    nextDisabaled() {
      if (this.active === 0) {
        return !this.selectedAlg.value
      }
      return false
    },
    // 组合处理选中的dataset
    selectedData() {
      const paticipateData = this.paticipateSelectList.map((v) => {
        // v.datasetFields 兼容后台服务
        return { ...v, datasetFields: v.datasetFields || '', datasetFieldsSelected: [] }
      })
      return [...this.tagSelectList, ...paticipateData]
    },
    runDisabaled() {
      const type = this.selectedAlg.value
      let disabled = true
      let selectedFields = false
      switch (type) {
        case jobEnum.PSI:
          selectedFields = this.jobSettingForm.selectedData.every((v) => v.datasetFieldsSelected.length)
          disabled = !(this.jobSettingForm.receiver.length && selectedFields)
          break
        case jobEnum.PIR:
          disabled = false
          break
        default:
          disabled = !this.jobSettingForm.receiver.length
          break
      }
      return disabled
    },
    pirOptions() {
      if (this.jobSettingForm.selectedData.length) {
        const { datasetFields = '' } = this.jobSettingForm.selectedData[0]
        const fields = datasetFields.trim().split(',')
        const children = fields.map((v) => {
          return {
            label: v,
            value: v
          }
        })
        return [
          {
            value: 0,
            label: '所有字段',
            children
          }
        ]
      } else {
        return []
      }
    },
    jobSettingFormRules() {
      return {
        receiver: [{ required: true, message: '结果接收方不能为空', trigger: 'blur' }],
        selectedData: [{ required: true, message: '参与方不能为空', trigger: 'blur' }],
        sql: [{ required: this.selectedAlg.value === jobEnum.SQL, message: 'sql内容不能为空', trigger: 'blur' }],
        queryType: [{ required: this.selectedAlg.value === jobEnum.PIR, message: '请选择查询类型', trigger: 'blur' }],
        modelPredictAlgorithm: [
          { required: this.selectedAlg.value === jobEnum.XGB_PREDICTING || this.selectedAlg.value === jobEnum.LR_PREDICTING, message: '请选择模型', trigger: 'blur' }
        ],
        searchType: [{ required: this.selectedAlg.value === jobEnum.PIR, message: '请选择查询类型', trigger: 'blur' }],
        queriedFields: [{ required: this.selectedAlg.value === jobEnum.PIR, message: '请选择查询字段', trigger: 'blur' }],
        searchIdList: [{ required: this.selectedAlg.value === jobEnum.PIR, message: '请输入字段值', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleServiceSelected(service) {
      console.log(service)
      const { serviceConfig } = service
      const { datasetId, searchType, idField, accessibleValueQueryFields } = JSON.parse(serviceConfig)
      this.selectedServiceConfig = JSON.parse(serviceConfig)
      this.jobSettingForm.selectedData = [{ ...service, datasetId, searchType, idField, accessibleValueQueryFields, selectedServiceConfig: this.selectedServiceConfig }]
    },
    checkJobData() {
      this.$refs.jobSettingForm.validate((valid) => {
        if (valid) {
          const { value } = this.selectedAlg
          switch (value) {
            case jobEnum.PSI:
              this.handlePsiJobData()
              break
            case jobEnum.XGB_TRAINING:
              this.handleTraingData()
              break
            case jobEnum.XGB_PREDICTING:
              this.handlePredictingData()
              break
            case jobEnum.LR_TRAINING:
              this.handleTraingData()
              break
            case jobEnum.LR_PREDICTING:
              this.handlePredictingData()
              break
            case jobEnum.PIR:
              this.handlePIRdata()
              break
            default:
              break
          }
        }
      })
    },
    runJob() {
      console.log('run start')
      this.checkJobData()
    },
    // 获取项目详情
    async queryProject() {
      this.loadingFlag = true
      const { projectId } = this
      const res = await projectManageServer.queryProject({ project: { id: projectId }, onlyMeta: false })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { dataList = [] } = res.data
        this.dataInfo = dataList[0] || {}
      } else {
        this.dataInfo = []
      }
    },
    hanleSelectedModel(checked, item) {
      if (checked) {
        this.jobSettingForm.modelPredictAlgorithm = item
      } else {
        this.jobSettingForm.modelPredictAlgorithm = {}
      }
    },
    handlePIRdata() {
      const { searchType, queriedFields = [], searchIdList } = this.jobSettingForm
      const { serviceId } = this.jobSettingForm.selectedData[0]
      const { projectId } = this
      const params = {
        serviceId,
        searchIdList: searchIdList.split(','),
        queriedFields,
        searchType
      }
      this.submitJob({ job: { param: JSON.stringify(params), jobType: jobEnum.PIR, projectId } })
    },
    handlePsiJobData() {
      const { selectedAlg } = this
      const { selectedData, receiver } = this.jobSettingForm
      const { projectId } = this
      const dataSetList = selectedData.map((v) => {
        console.log(v, v.datasetStoragePath, JSON.parse(v.datasetStoragePath))
        const dataset = {
          owner: v.ownerUserName,
          ownerAgency: v.ownerAgencyName,
          path: JSON.parse(v.datasetStoragePath).filePath,
          storageTypeStr: v.datasetStorageType,
          datasetID: v.datasetId
        }
        return {
          idFields: v.datasetFieldsSelected,
          dataset,
          receiveResult: receiver.includes(v.ownerAgencyName)
        }
      })
      const param = { dataSetList }
      const params = { jobType: selectedAlg.value, projectId, param: JSON.stringify(param) }
      const taskParties = selectedData.map((v) => {
        return {
          userName: v.ownerUserName,
          agency: v.ownerAgencyName
        }
      })
      const datasetList = selectedData.map((v) => v.datasetId)
      this.submitJob({ job: params, taskParties, datasetList })
    },
    handleTraingData() {
      const { selectedAlg, modelModule } = this
      const { selectedData, receiver } = this.jobSettingForm
      const { projectId } = this
      console.log(selectedData, 'selectedData')
      const dataSetList = selectedData.map((v) => {
        const dataset = {
          owner: v.ownerUserName,
          ownerAgency: v.ownerAgencyName,
          path: JSON.parse(v.datasetStoragePath).filePath,
          storageTypeStr: v.datasetStorageType,
          datasetID: v.datasetId
        }
        return {
          idFields: v.datasetFieldsSelected,
          dataset,
          labelProvider: !!v.selectedTagFields,
          receiveResult: receiver.includes(v.ownerAgencyName)
        }
      })
      const modelSetting = {}
      modelModule.forEach((v) => {
        const key = v.label
        modelSetting[key] = v.value
      })
      const param = { dataSetList, modelSetting }
      console.log(param, 'modelSettingmodel')
      const params = { jobType: selectedAlg.value, projectId, param: JSON.stringify(param) }
      const taskParties = selectedData.map((v) => {
        return {
          userName: v.ownerUserName,
          agency: v.ownerAgencyName
        }
      })
      const datasetList = selectedData.map((v) => v.datasetId)
      console.log({ job: params, taskParties }, receiver)
      this.submitJob({ job: params, taskParties, datasetList })
    },
    handlePredictingData() {
      const { selectedAlg, modelModule } = this
      const { selectedData, receiver, modelPredictAlgorithm } = this.jobSettingForm
      const { projectId } = this
      console.log(selectedData, 'selectedData')
      const dataSetList = selectedData.map((v, i) => {
        const dataset = {
          owner: v.ownerUserName,
          ownerAgency: v.ownerAgencyName,
          path: JSON.parse(v.datasetStoragePath).filePath,
          storageTypeStr: v.datasetStorageType,
          datasetID: v.datasetId
        }
        return {
          idFields: v.datasetFieldsSelected,
          dataset,
          labelProvider: !!(i === 0), // 先选的是标签方
          receiveResult: receiver.includes(v.ownerAgencyName)
        }
      })
      const modelSetting = {}
      modelModule.forEach((v) => {
        const key = v.label
        modelSetting[key] = v.value
      })
      const param = { dataSetList, modelSetting, modelPredictAlgorithm: JSON.stringify(modelPredictAlgorithm) }
      const params = { jobType: selectedAlg.value, projectId, param: JSON.stringify(param) }
      const taskParties = selectedData.map((v) => {
        return {
          userName: v.ownerUserName,
          agency: v.ownerAgencyName
        }
      })
      const datasetList = selectedData.map((v) => v.datasetId)
      console.log({ job: params, taskParties }, receiver)
      this.submitJob({ job: params, taskParties, datasetList })
    },
    handleSelectSetting(data) {
      const setting = JSON.parse(data.setting)
      this.modelModule.forEach((v) => {
        const key = v.label
        v.value = setting[key]
      })
      console.log(this.modelModule, setting, 'this.modelModule')
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
    // 查询xgb和lr默认模板参数
    async queryDefaultSettings(name) {
      const res = await settingManageServer.querySettings({
        onlyMeta: false,
        condition: {
          id: '',
          name,
          type: 'ALGORITHM_SETTING',
          owner: ''
        }
      })
      if (res.code === 0 && res.data) {
        const { setting = '' } = res.data.dataList[0]
        console.log(setting, 'JSON.parse(setting)')
        this.modelModule = JSON.parse(setting)
      }
    },
    // 查询用户自定义setting模板list
    async queryModelSettingList(type) {
      const res = await settingManageServer.querySettings({
        onlyMeta: false,
        condition: {
          id: '',
          name: '',
          type,
          owner: ''
        }
      })
      console.log(res)
      if (res.code === 0 && res.data) {
        const { dataList } = res.data
        this.modelSettingList = dataList.map((v) => {
          return {
            label: v.name,
            value: v
          }
        })
      }
    },
    // 模型选择变更 影响数据集选择
    modelSelectedChange(item) {
      if (item) {
        const { participant_agency_list } = item
        const tagAgency = participant_agency_list.filter((v) => v.isLabelProvider)[0].agency
        const filterPaticipateAgency = participant_agency_list.filter((v) => !v.isLabelProvider).map((v) => v.agency)
        this.tagAgency = tagAgency
        this.filterPaticipateAgency = filterPaticipateAgency
        this.tagSelectList = []
        this.paticipateSelectList = [{}]
      }
    },
    showAddParticipate(addContainerIndex) {
      this.showParticipateModal = true
      this.addContainerIndex = addContainerIndex // 记录点击添加的是哪个container
    },
    closeModal() {
      this.showTagsModal = false
      this.showParticipateModal = false
    },
    tagSelected(data) {
      this.showTagsModal = false
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
    removeParticipate(index) {
      this.paticipateSelectList.splice(index, 1)
    },
    participateSelected(data) {
      this.showParticipateModal = false
      this.$set(this.paticipateSelectList, this.addContainerIndex, data)
    },
    handleClick() {},
    addTag() {
      this.showTagsModal = true
    },
    calcParticipateNumberMatch(participateNumber, dataLength) {
      if (participateNumber.indexOf('+') > -1) {
        return dataLength >= parseInt(participateNumber)
      } else {
        return dataLength === parseInt(participateNumber)
      }
    },
    next() {
      if (this.active === 1) {
        const { participateNumber } = this.selectedAlg
        const validpaticipateSelect = this.paticipateSelectList.filter((v) => v.datasetId)
        const validDataLength = validpaticipateSelect.length // 有效数据集数量
        const participateAgencyList = validpaticipateSelect.map((v) => v.ownerAgencyName)
        // 参与机构数量
        const uniqueAgencyLength = Array.from(new Set(participateAgencyList)).length
        if ([jobEnum.XGB_TRAINING, jobEnum.LR_TRAINING, jobEnum.LR_PREDICTING, jobEnum.XGB_PREDICTING].includes(this.selectedAlg.value)) {
          const tag = this.tagSelectList[0] || {}
          const xgbValidParticipateLength = validpaticipateSelect.filter((v) => v.ownerAgencyName !== tag.ownerAgencyName).length
          // 建模预测需要选择标签提供方数据集
          const tagSelectListNum = this.tagSelectList.length
          if (!(tagSelectListNum === 1 && this.calcParticipateNumberMatch(participateNumber, xgbValidParticipateLength))) {
            this.$message.error(`请添加标签提供方，并添加至少${parseInt(participateNumber)}个不同机构的参与方`)
            return
          }
          // 预测任务 选择的数据集要和选择的模型内数据集数量和字段相匹配
          if ([jobEnum.LR_PREDICTING, jobEnum.XGB_PREDICTING].includes(this.selectedAlg.value)) {
            const { participant_agency_list } = this.jobSettingForm.modelPredictAlgorithm
            const modelTagsFileds = participant_agency_list.filter((v) => v.isLabelProvider)[0].fields
            const modelPaticipate = participant_agency_list.filter((v) => !v.isLabelProvider)
            if (modelPaticipate.length !== xgbValidParticipateLength) {
              this.$message.error('参与方数据集数量错误，需要等于模型中参与方数量')
              return
            }
            // 标签方字段校验
            const { datasetFields = '' } = tag
            const tagSelectedFields = datasetFields.split(', ')
            if (!modelTagsFileds.every((v) => tagSelectedFields.includes(v))) {
              this.$message.error('标签方数据集选择错误，需要包含模型中标签方数据集所有字段')
              return
            }
            // 参与方字段校验
            let valid = true
            modelPaticipate.forEach((v) => {
              const { fields: modelFields, agency } = v
              const matchPaticipateSelect = validpaticipateSelect.filter((k) => k.ownerAgencyName === agency)
              if (matchPaticipateSelect.length === 1) {
                const { datasetFields = '' } = matchPaticipateSelect[0]
                const selectedFields = datasetFields.split(', ')
                if (!modelFields.every((k) => selectedFields.includes(k))) {
                  valid = false
                }
              } else {
                valid = false
              }
            })
            if (!valid) {
              this.$message.error('参与方数据集选择错误，需要包含模型中参与方数据集所有字段')
              return
            }
          }
        } else if (this.selectedAlg.value === jobEnum.PSI) {
          // psi 可同一机构下多个数据集
          if (!this.calcParticipateNumberMatch(participateNumber, validDataLength)) {
            this.$message.error(`请添加至少${parseInt(participateNumber)}个参与方`)
            return
          }
          // psi 至少有一个自己的数据集
          if (!participateAgencyList.includes(this.agencyId)) {
            this.$message.error('请添加至少一个己方的数据集')
            return
          }
        } else if (this.selectedAlg.value === jobEnum.PIR) {
          // pir 要选择服务
          if (!this.jobSettingForm.selectedData.length) {
            this.$message.error('请选择服务')
            return
          }
        } else {
          // 其他任务只要参与方数量满足
          if (!this.calcParticipateNumberMatch(participateNumber, uniqueAgencyLength)) {
            this.$message.error(`请添加至少${parseInt(participateNumber)}个不同机构的参与方`)
            return
          }
        }
        this.active++
      } else {
        this.active++
      }
    },
    pre() {
      this.active--
    },
    selectAlg(selectedAlg) {
      console.log(selectedAlg, 'selectedAlg')
      this.selectedAlg = { ...selectedAlg }
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
  div.sql-card {
    padding-bottom: 0;
    div.sql-container {
      display: flex;
      div.modify-container {
        height: 500px;
        width: 850px;
      }
      div.lead {
        color: #3071f2;
        padding-left: 16px;
        cursor: pointer;
        img {
          width: 16px;
          height: 16px;
          vertical-align: middle;
        }
      }
    }
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
  ::v-deep .el-step__description.is-success {
    color: #3071f2;
  }
  ::v-deep .el-step__description.is-process {
    color: #3071f2;
  }
  ::v-deep .el-step.is-horizontal .el-step__line {
    top: 18px;
  }
  ::v-deep .el-step__icon {
    width: 36px;
    height: 36px;
  }
  ::v-deep .el-input-group__prepend {
    width: 84px;
    text-align: center;
  }
}
</style>
