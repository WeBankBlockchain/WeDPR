<template>
  <div>
    <el-tabs v-model="activeName">
      <el-tab-pane key="建模结果" label="建模结果" name="建模结果">
        <el-form label-position="right" label-width="120px" class="form">
          <el-form-item label="任务耗时：">
            <span>{{ jobStatusInfo.timeCostMs / 1000 }}s</span>
          </el-form-item>
          <el-form-item v-if="outputModelResult.length" label="结果文件：">
            <div>
              <el-button type="text" @click="downloadResult(resultLink.modelResultPath, 'mpc_model.json')">{{ 'mpc_model.json' }}</el-button>
            </div>
          </el-form-item>
        </el-form>
        <el-card v-if="iterationGraph.length" class="info-card">
          <div slot="header" class="clearfix">
            <span>评估指标迭代图</span>
          </div>
          <div class="pd20">
            <div class="image-area">
              <div v-for="item in iterationGraph" :key="item.metricsName">
                <el-image style="width: 100%; height: auto" :preview-src-list="iterationGraphImgList" :src="item.data" />
                <div>{{ item.title }}</div>
              </div>
            </div>
          </div>
        </el-card>
        <el-card v-if="FeatureImportance" class="info-card">
          <div slot="header" class="clearfix">
            <span> 特征重要性 </span>
          </div>
          <el-table ref="dynamicTable" :data="FeatureImportance.data" border fit stripe max-height="385">
            <el-table-column v-for="col in FeatureImportance.columns" :key="col.dataItem" sortable :show-overflow-tooltip="true" :prop="col.dataItem" :label="col.dataName" />
            <el-table-column fixed="left" width="80" align="center" type="index">
              <template slot="header">
                <colFilter
                  v-if="FeatureImportance.columnsOrigin"
                  :columns="FeatureImportance.columnsOrigin"
                  @handleCheckedChange="(data) => handleCheckedChange(data, 'FeatureImportance')"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        <el-card v-if="outputModelResult.length" class="info-card">
          <div slot="header" class="clearfix">
            <span>决策树</span>
          </div>
          <el-row type="flex">
            <el-col :span="24">
              <div v-for="item in outputModelResult" :key="item.ModelPlotName" class="img-con">
                <el-image style="width: 100%; height: auto" :src="item.ModelPlotData" :preview-src-list="outputModelImageList" />
                <div>{{ item.title }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>
      <el-tab-pane v-if="outputTrainPreviewTableData || outputPreviewTableData" key="预测结果" label="预测结果" name="预测结果">
        <el-card v-if="outputTrainPreviewTableData" class="info-card">
          <div slot="header" class="clearfix">
            <span>训练集预测结果</span>
          </div>
          <div class="pd20">
            <el-form label-position="right" label-width="'80px' " class="form">
              <el-form-item label="结果文件">
                <div>
                  <el-button type="text" @click="downloadResult(resultLink.trainResultPath, 'train_result.csv')">
                    {{ 'train_result.csv' }}
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
            <el-table ref="dynamicTable" :data="outputTrainPreviewTableData.data" border fit stripe max-height="385">
              <el-table-column
                v-for="col in outputTrainPreviewTableData.columns"
                :key="col.dataItem"
                sortable
                :show-overflow-tooltip="true"
                :prop="col.dataItem"
                :label="col.dataName"
              />
            </el-table>
          </div>
        </el-card>
        <el-card v-if="outputPreviewTableData" class="info-card">
          <div slot="header" class="clearfix">
            <span>{{ jobAlgorithmType === jobEnum.XGB_TRAINING ? '测试集预测结果' : '验证集预测结果' }}</span>
          </div>
          <div class="pd20">
            <el-form label-position="right" label-width="80px" class="form">
              <el-form-item label="结果文件">
                <div>
                  <el-button type="text" @click="downloadResult(resultLink.testResultPath, 'test_result.csv')">
                    {{ 'test_result.csv' }}
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
            <el-table ref="dynamicTable" :data="outputPreviewTableData.data" border fit stripe max-height="385">
              <el-table-column
                v-for="col in outputPreviewTableData.columns"
                :key="col.dataItem"
                sortable
                :show-overflow-tooltip="true"
                :prop="col.dataItem"
                :label="col.dataName"
              />
            </el-table>
          </div>
        </el-card>
      </el-tab-pane>
      <el-tab-pane
        key="效果评估"
        label="效果评估"
        name="效果评估"
        v-if="TrainKSTableData || KSTableData || EvaluationTable || outputTrainMetricsGraphs.length || outputMetricsGraphs.length"
      >
        <el-card v-if="EvaluationTable" class="info-card">
          <div slot="header" class="clearfix">
            <span>模型评估汇总</span>
          </div>
          <el-table ref="dynamicTable" :data="EvaluationTable.data" border fit stripe max-height="385">
            <el-table-column v-for="col in EvaluationTable.columns" :key="col.dataItem" sortable :show-overflow-tooltip="true" :prop="col.dataItem" :label="col.dataName" />
            <el-table-column fixed="left" width="80" align="center" type="index">
              <template slot="header">
                <colFilter
                  v-if="EvaluationTable.columnsOrigin"
                  :columns="EvaluationTable.columnsOrigin"
                  @handleCheckedChange="(data) => handleCheckedChange(data, 'EvaluationTable')"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        <el-card v-if="TrainKSTableData || outputTrainMetricsGraphs.length" class="info-card">
          <div slot="header" class="clearfix">
            <span>训练集效果评估</span>
          </div>

          <div class="pd20">
            <el-table v-if="TrainKSTableData" ref="dynamicTable" :data="TrainKSTableData.data" border fit stripe max-height="385">
              <el-table-column v-for="col in TrainKSTableData.columns" :key="col.dataItem" sortable :show-overflow-tooltip="true" :prop="col.dataItem" :label="col.dataName" />
              <el-table-column fixed="left" width="80" align="center" type="index">
                <template slot="header">
                  <colFilter
                    v-if="TrainKSTableData.columnsOrigin"
                    :columns="TrainKSTableData.columnsOrigin"
                    @handleCheckedChange="(data) => handleCheckedChange(data, 'TrainKSTableData')"
                  />
                </template>
              </el-table-column>
            </el-table>
            <div v-if="outputTrainMetricsGraphs.length" class="image-area">
              <div v-for="item in outputTrainMetricsGraphs" :key="item.metricsName" class="img-con">
                <el-image style="width: 100%; height: auto" :preview-src-list="outputTrainMetricsGraphsDataList" :src="item.data" />
                <div>{{ item.title }}</div>
              </div>
            </div>
          </div>
        </el-card>
        <el-card v-if="KSTableData || outputMetricsGraphs.length" class="info-card">
          <div slot="header" class="clearfix">
            <span> {{ jobAlgorithmType === jobEnum.XGB_TRAINING ? '测试集效果评估' : '验证集效果评估' }}</span>
          </div>
          <div class="pd20">
            <el-table v-if="KSTableData" ref="dynamicTable" :data="KSTableData.data" border fit stripe max-height="385">
              <el-table-column v-for="col in KSTableData.columns" :key="col.dataItem" sortable :show-overflow-tooltip="true" :prop="col.dataItem" :label="col.dataName" />
              <el-table-column fixed="left" width="80" align="center" type="index">
                <template slot="header">
                  <colFilter v-if="KSTableData.columnsOrigin" :columns="KSTableData.columnsOrigin" @handleCheckedChange="(data) => handleCheckedChange(data, 'KSTableData')" />
                </template>
              </el-table-column>
            </el-table>
            <div v-if="outputMetricsGraphs.length" class="image-area">
              <div v-for="item in outputMetricsGraphs" :key="item.metricsName" class="img-con">
                <el-image style="width: 100%; height: auto" :preview-src-list="outputMetricsGraphsDataList" :src="item.data" />
                <div>{{ item.title }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
      <el-tab-pane v-if="FEPreviewData || PRPreviewTableData" key="特征工程" label="特征工程" name="特征工程">
        <el-card v-if="PRPreviewTableData" class="info-card">
          <div slot="header" class="clearfix">
            <span>预处理结果 </span>
          </div>
          <div class="pd20">
            <el-table ref="dynamicTable" :data="PRPreviewTableData.data" border fit stripe max-height="385">
              <el-table-column v-for="col in PRPreviewTableData.columns" :key="col.dataItem" sortable :show-overflow-tooltip="true" :prop="col.dataItem" :label="col.dataName" />
              <el-table-column fixed="left" width="80" align="center" type="index">
                <template slot="header">
                  <colFilter
                    v-if="PRPreviewTableData.columnsOrigin"
                    :columns="PRPreviewTableData.columnsOrigin"
                    @handleCheckedChange="(data) => handleCheckedChange(data, 'PRPreviewTableData')"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
        <el-card v-if="FEPreviewData" class="info-card">
          <el-form label-position="right" label-width="80px" class="form">
            <el-form-item label="结果文件">
              <div>
                <el-button type="text" @click="downloadResult(resultLink.woeIVResultPath, 'woe_iv.csv')">
                  {{ 'woe_iv.csv' }}
                </el-button>
              </div>
            </el-form-item>
          </el-form>
          <el-table ref="FEPreviewData" :data="FEPreviewData.data" fit stripe max-height="385">
            <el-table-column v-for="col in FEPreviewData.columns" :key="col.dataItem" sortable :show-overflow-tooltip="true" :prop="col.dataItem" :label="col.dataName" />
            <el-table-column fixed="left" width="80" align="center" type="index">
              <template slot="header">
                <colFilter v-if="FEPreviewData.columnsOrigin" :columns="FEPreviewData.columnsOrigin" @handleCheckedChange="(data) => handleCheckedChange(data, 'FEPreviewData')" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { toDynamicTableData } from 'Utils/index.js'
import { downloadLargeFile } from 'Mixin/downloadLargeFile.js'
import { jobEnum } from 'Utils/constant.js'
export default {
  name: 'AiResultNew',
  mixins: [downloadLargeFile],
  props: {
    jobID: {
      type: String,
      default: () => {
        return ''
      }
    },
    jobType: {
      type: String,
      default: () => {
        return ''
      }
    },
    jobStatusInfo: {
      type: Object,
      default: () => {
        return {}
      }
    },
    modelResultDetail: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      jobResult: {},
      metricsGraphList: [],
      activeName: '建模结果',
      dynamicTableData: {},
      outputModelResult: [],
      outputModelImageList: [],
      jobAlgorithmSubtype: '',
      iterationGraph: [],
      iterationGraphImgList: [],
      FeatureImportance: null,
      outputTrainPreviewTableData: null,
      outputPreviewTableData: null,
      TrainKSTableData: null,
      KSTableData: null,
      EvaluationTable: null,
      outputTrainMetricsGraphs: [],
      outputMetricsGraphs: [],
      FEPreviewData: null,
      PRPreviewTableData: null,
      resultLink: {},
      jobEnum
    }
  },
  watch: {
    jobID() {
      this.handleResult()
    }
  },
  created() {
    this.handleResult()
    console.log(this.modelResultDetail, 'modelResultDetail')
    console.log(this.jobStatusInfo, 'jobStatusInfo')
  },
  methods: {
    downloadResult(path, fileName) {
      path && this.downloadLargeFile({ filePath: path }, fileName)
    },
    async handleResult() {
      this.loadingFlag = true
      const { modelResultDetail } = this
      console.log(modelResultDetail, 'modelResultDetail')
      const {
        TrainKSTable,
        KSTable,
        outputMetricsGraphs = [],
        EvaluationTable,
        IterationGraph = [],
        FeatureImportance,
        outputModelResult = [],
        outputTrainPreview,
        outputPreview,
        outputTrainMetricsGraphs,
        FEPreview,
        PRPreview,
        ModelResult
      } = modelResultDetail
      this.resultLink = { ...ModelResult }
      // 评估指标迭代图
      this.iterationGraph = IterationGraph.map((v) => {
        return {
          title: v.ModelPlotName,
          data: v.ModelPlotData
        }
      })
      this.iterationGraphImgList = this.iterationGraph.map((v) => v.data)
      // 决策树
      this.outputModelResult = outputModelResult
      this.outputModelImageList = this.outputModelResult.map((v) => v.ModelPlotData)
      // 特征迭代重要性
      this.FeatureImportance = FeatureImportance && FeatureImportance.metricsData && toDynamicTableData(FeatureImportance.metricsData)
      // 预测结果
      outputTrainPreview && (this.outputTrainPreviewTableData = toDynamicTableData(outputTrainPreview))
      outputPreview && (this.outputPreviewTableData = toDynamicTableData(outputPreview))
      // 评估
      TrainKSTable && (this.TrainKSTableData = toDynamicTableData(TrainKSTable))
      KSTable && (this.KSTableData = toDynamicTableData(KSTable))
      this.EvaluationTable = EvaluationTable && EvaluationTable.metricsData && toDynamicTableData(EvaluationTable.metricsData)
      this.outputMetricsGraphs = outputMetricsGraphs.map((v) => {
        return {
          title: v.metricsName,
          data: v.metricsData
        }
      })
      this.outputMetricsGraphsDataList = this.outputMetricsGraphs.map((v) => v.data)
      this.outputTrainMetricsGraphs = outputTrainMetricsGraphs.map((v) => {
        return {
          title: v.metricsName,
          data: v.metricsData
        }
      })
      // 特征工程
      FEPreview && (this.FEPreviewData = toDynamicTableData(FEPreview))
      PRPreview && (this.PRPreviewTableData = toDynamicTableData(PRPreview))
    },
    handleCheckedChange(data, name) {
      this[name] && (this[name].columns = [...data])
    },
    downloadFileResult() {}
  }
}
</script>

<style lang="scss" scoped>
body {
  margin: 0;
}
.form {
  margin-bottom: 10px;
  .el-form-item {
    margin-bottom: 3px;
  }
}
div.img-con {
  width: 50%;
  float: left;
  text-align: center;
}
div.info-card {
  margin-bottom: 30px;
  padding-bottom: 30px;
  .image-area {
    margin-top: 20px;
    text-align: center;
  }
}
div.pd20 {
  padding: 20px;
  padding-top: 0;
  box-sizing: border-box;
}
div.img-con {
  width: 50%;
  float: left;
  text-align: center;
}
</style>
