<template>
  <div class="group-manage">
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
      <el-tab-pane label="任务信息" name="first">
        <div class="info-container">
          <div class="whole">
            <div class="half">
              <span class="title">任务ID：</span>
              <span class="info" :title="jobInfo.id"> {{ jobInfo.id }} </span>
            </div>
          </div>
          <div class="whole">
            <div class="half">
              <span class="title">参与方：</span>
              <span class="info" :title="jobInfo.particapate"> {{ jobInfo.particapate }} </span>
            </div>
          </div>
          <div class="whole">
            <div class="half">
              <span class="title">结果接收方：</span>
              <span class="info" :title="jobInfo.receiver"> {{ jobInfo.receiver }} </span>
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
        <div class="con">
          <div class="title-radius">参与数据资源</div>
          <div class="tableContent autoTableWrap">
            <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="dataList" :border="true" class="table-wrap">
              <el-table-column label="数据资源ID" prop="datasetId" />
              <el-table-column label="数据资源名称" prop="datasetTitle" />
              <el-table-column label="所属机构" prop="ownerAgencyId" />
              <el-table-column label="所属用户" prop="ownerUserId" />
              <el-table-column label="创建时间" prop="createAt" />
              <el-table-column label="操作">
                <template v-slot="scope">
                  <el-button size="small" type="text" @click="getDetail(scope.row.datasetId)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="工作流视图" name="second" v-if="false"> </el-tab-pane>
      <el-tab-pane label="审计信息" name="third" v-if="false"> </el-tab-pane>
      <el-tab-pane label="查看结果" name="four" v-if="jobInfo.status === 'RunSuccess' && receiverList.includes(agencyId)">
        <xgbResult v-if="jobInfo.jobType === jobEnum.XGB_TRAINING" :jobID="jobID" :jobStatusInfo="jobStatusInfo" :modelResultDetail="modelResultDetail" />
        <baseResult v-if="jobInfo.jobType === jobEnum.PSI" :jobID="jobID" :jobStatusInfo="jobStatusInfo" :resultFileInfo="resultFileInfo" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>
import { jobManageServer, dataManageServer } from 'Api'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import xgbResult from './result/xgbResult.vue'
import baseResult from './result/baseResult.vue'
import { jobStatusMap, jobEnum } from 'Utils/constant.js'
import { mapGetters } from 'vuex'
export default {
  name: 'groupManage',
  mixins: [tableHeightHandle],
  components: {
    xgbResult,
    baseResult
  },
  data() {
    return {
      jobInfo: {},
      queryFlag: false,
      tableData: [],
      loadingFlag: false,
      showAddModal: false,
      activeName: 'first',
      jobID: '',
      jobResult: {},
      dataList: [],
      jobStatusMap,
      modelResultDetail: {}, // xgbresult
      jobStatusInfo: {},
      resultFileInfo: {}, // psiresult
      receiverList: [],
      jobEnum
    }
  },
  created() {
    const { id } = this.$route.query
    this.jobID = id
    id && this.getJobInfo()
  },
  computed: {
    ...mapGetters(['agencyId'])
  },
  methods: {
    async getJobInfo() {
      this.loadingFlag = true
      const { jobID } = this
      const res = await jobManageServer.queryJobDetail({ jobID })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { job = {}, modelResultDetail = {}, resultFileInfo } = res.data
        const { parties, param } = job
        const { jobStatusInfo = {} } = job
        this.jobInfo = { ...job }
        this.jobInfo.particapate = JSON.parse(parties)
          .map((v) => v.agency)
          .join('，')
        const dataSetList = JSON.parse(param).dataSetList
        this.receiverList = dataSetList.filter((v) => v.receiveResult).map((v) => v.dataset.ownerAgency)
        this.jobInfo.receiver = this.receiverList.join('，')
        const ids = dataSetList
          .map((v) => {
            return v.dataset && v.dataset.datasetID
          })
          .filter((v) => v)
        ids.length && this.getListDetail({ datasetIdList: ids })
        console.log(JSON.parse(parties), 'parties')
        this.modelResultDetail = modelResultDetail
        this.jobStatusInfo = jobStatusInfo
        this.resultFileInfo = resultFileInfo
      } else {
        this.jobInfo = {}
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
        this.dataList = data
      } else {
        this.dataList = []
      }
    },
    getDetail(datasetId) {
      this.$router.push({ path: '/dataDetail', query: { datasetId } })
    }
  }
}
</script>
<style lang="less" scoped>
div.whole {
  display: flex;
  margin-bottom: 16px;
}
div.half {
  width: 50%;
  display: flex;
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
