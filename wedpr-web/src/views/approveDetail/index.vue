<template>
  <div class="create-data">
    <div class="approveInfo">
      <div class="title-radius">申请单详情</div>
      <div class="info-container">
        <div class="whole">
          <div class="half">
            <span class="title">申请标题：</span>
            <span class="info" :title="dataInfo.applyTitle">
              {{ dataInfo.applyTitle }}
              <span class="status tag">
                <el-tag size="small" v-if="SuccessStatus.includes(dataInfo.status)" effect="dark" color="#52B81F">{{ approveStatusMap[dataInfo.status] }}</el-tag>
                <el-tag size="small" v-else-if="FailStatus.includes(dataInfo.status)" effect="dark" color="#FF4D4F">{{ approveStatusMap[dataInfo.status] }}</el-tag>
                <el-tag size="small" v-else-if="PendingStatus.includes(dataInfo.status)" effect="dark" color="#3071F2">{{ approveStatusMap[dataInfo.status] }}</el-tag>
                <el-tag size="small" v-else effect="dark" color="#3071F2">{{ approveStatusMap[dataInfo.status] }}</el-tag>
              </span>
            </span>
          </div>
        </div>
        <div class="whole">
          <div class="half">
            <span class="title">申请用户：</span>
            <span class="info" :title="dataInfo.applicant"> {{ dataInfo.applicant }} </span>
          </div>
        </div>
        <div class="whole">
          <div class="half">
            <span class="title">所属机构：</span>
            <span class="info" :title="dataInfo.applicantAgency"> {{ dataInfo.applicantAgency }} </span>
          </div>
        </div>
        <div class="whole">
          <div class="half">
            <span class="title">申请时间：</span>
            <span class="info" :title="dataInfo.createTime"> {{ dataInfo.createTime }} </span>
          </div>
        </div>
        <div class="whole">
          <div class="half">
            <span class="title">关注人：</span>
            <span class="info" :title="dataInfo.followers">
              <el-tag size="small" style="margin-right: 5px" :key="item" v-for="item in dataInfo.followers" type="info">{{ item }}</el-tag>
            </span>
          </div>
        </div>
        <div class="whole">
          <div class="half">
            <span class="title">申请背景：</span>
            <span class="info" :title="dataInfo.applyDesc"> {{ dataInfo.applyDesc }} </span>
          </div>
        </div>
        <div class="whole">
          <div class="half">
            <span class="title">申请内容：</span>
            <span class="info">
              <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="dataInfo.applyDataContent" :border="true" class="table-wrap">
                <el-table-column :label="item.title" :prop="item.key" :key="item.key" v-for="item in columns">
                  <template v-slot="scope">
                    <el-date-picker
                      size="small"
                      disabled
                      v-if="item.type === 'date'"
                      value-format="yyyy-MM-dd"
                      style="width: 160px"
                      v-model="scope.row[item.key]"
                      type="date"
                      placeholder="请选择日期"
                    >
                    </el-date-picker>
                    <span v-else> {{ scope.row[item.key] }} </span>
                  </template>
                </el-table-column>
              </el-table>
            </span>
          </div>
        </div>
        <div class="whole">
          <div class="half">
            <span class="title">审批链：</span>
            <span class="info"> <approveChain :currentApply="dataInfo.currentApplyNodeAgency + '_' + dataInfo.currentApplyNode" :approveChainList="dataInfo.applyChain" /></span>
          </div>
        </div>
        <div
          class="whole"
          v-if="(dataInfo.currentApplyNode === userId && dataInfo.currentApplyNodeAgency === agencyId) || (dataInfo.applicant === userId && dataInfo.applicantAgency === agencyId)"
        >
          <div class="operate">
            <span class="title">操作：</span>
            <span class="handle action" v-if="dataInfo.currentApplyNode === userId && dataInfo.currentApplyNodeAgency === agencyId" :title="dataInfo.datasetTitle">
              <img @click="showAgreeConfirm(dataInfo)" src="~Assets/images/agree.png" />
              <img @click="showDisAgreeConfirm(dataInfo)" src="~Assets/images/refuse.png" />
            </span>
            <span class="handle" v-if="dataInfo.applicant === userId && dataInfo.applicantAgency === agencyId">
              <img src="~Assets/images/modifyApply.png" v-if="modifyAbleStatus.includes(dataInfo.status)" size="small" type="primary" @click="reApply" />
              <img src="~Assets/images/giveup.png" size="medium" v-if="giveupStatus.includes(dataInfo.status)" style="color: red" type="text" @click="showCloseConfirm(dataInfo)" />
            </span>
          </div>
        </div>
      </div>
    </div>
    <div class="record" v-if="resultList.length">
      <div class="title-radius">审批记录</div>
      <div class="step-con" v-for="(item, i) in resultList" :key="item.name + item.agency">
        <div class="step">
          <div class="small-circle" v-if="i === 0"></div>
          <div class="circle" v-else>
            <span>{{ i }}</span>
          </div>
          <span class="line" v-if="resultList[i + 1]"> </span>
        </div>
        <ul class="info">
          <li class="ell" :title="item.name + '(' + item.agency + ')'">
            <el-tag size="small" :type="resultTypeMap[item.result]">{{ resultMap[item.result] }}</el-tag>
            {{ item.name + '(' + item.agency + ')' }}
          </li>
          <li>{{ item.time }}</li>
        </ul>
      </div>
    </div>
  </div>
</template>
<script>
import { authManageServer } from 'Api'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import approveChain from '@/components/approveChain.vue'
import { mapGetters } from 'vuex'
import { approveStatusMap } from 'Utils/constant.js'
export default {
  name: 'dataDetail',
  mixins: [tableHeightHandle],
  components: {
    approveChain
  },
  data() {
    return {
      PendingStatus: ['ToConfirm', 'Progressing', 'Approving'],
      FailStatus: ['ApproveFailed', 'ProgressFailed', 'ApproveRejected', 'ApproveCanceled'],
      SuccessStatus: ['ApproveSuccess', 'ProgressSuccess'],
      dataInfo: {},
      id: 0,
      columns: [],
      approveStatusMap,
      giveupStatus: ['Approving', 'ToConfirm', 'ApproveRejected'],
      modifyAbleStatus: ['ToConfirm', 'ApproveRejected'],
      handleDisabled: ['ProgressFailed', 'ProgressSuccess'],
      resultList: [],
      resultMap: {
        Agree: '同意',
        Reject: '驳回',
        Submit: '提出申请'
      },
      resultTypeMap: {
        Agree: 'success',
        Reject: 'danger',
        Submit: ''
      },
      active: 1
    }
  },
  created() {
    const { authID } = this.$route.query
    this.authID = authID
    this.queryAuthTemplateDetails(['wedpr_data_auth'])
    authID && this.getDetail()
  },
  computed: {
    ...mapGetters(['userId', 'agencyId'])
  },
  methods: {
    async queryAuthTemplateDetails(params) {
      const res = await authManageServer.queryAuthTemplateDetails(params)
      console.log(res)
      if (res.code === 0) {
        const { templateSetting } = res.data[0]
        const { columns = [] } = JSON.parse(templateSetting)
        this.columns = columns
      }
    },
    reApply() {
      this.$router.push({ path: 'dataApplyModify', query: { authID: this.authID } })
    },
    // 获取审批单详情
    async getDetail() {
      this.loadingFlag = true
      const { authID } = this
      const res = await authManageServer.queryAuthDetail({ authID })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        this.dataInfo = { ...res.data }
        const { applyChain, applyContent, authResult, currentApplyNode, currentApplyNodeAgency, status } = this.dataInfo
        const authInfo = JSON.parse(applyChain)
        const applyDataContent = JSON.parse(applyContent)
        this.dataInfo.applyChain = authInfo.chain
        this.dataInfo.applyDataContent = applyDataContent
        // 结束后当前节点置空
        if (this.handleDisabled.includes(status)) {
          this.dataInfo.currentApplyNode = ''
          this.dataInfo.currentApplyNodeAgency = ''
        }
        this.resultList = authResult.resultList.map((v) => {
          return {
            ...v,
            result: v.result.result,
            time: v.result.time
          }
        })
        this.resultList.forEach((v, i) => {
          if (v.agency === currentApplyNodeAgency && v.name === currentApplyNode) {
            this.active = i + 1
          }
        })
        console.log(this.dataInfo.applyChain, 'this.dataInfo.applyChain')
        console.log(this.dataInfo, 'this.dataInfo')
      } else {
        this.dataInfo = {}
      }
    },
    showAgreeConfirm(params) {
      this.$confirm('确认通过审批?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.agree(params)
        })
        .catch(() => {})
    },
    showDisAgreeConfirm(params) {
      this.$confirm('确认驳回审批?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.disagree(params)
        })
        .catch(() => {})
    },
    showCloseConfirm(params) {
      this.$confirm('申请单作废后不可恢复，后续审批流程将中断，确认作废吗？', '确认作废', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.closeAuth(params)
        })
        .catch(() => {})
    },
    async agree(params) {
      const { id } = params
      const res = await authManageServer.updateAuthResult({ authID: id, authResultDetail: { result: 'Agree', detail: '' } })
      console.log(res)
      if (res.code === 0) {
        this.$message.success('审批已通过！')
        history.back()
      }
    },
    async disagree(params) {
      const { id } = params
      const res = await authManageServer.updateAuthResult({ authID: id, authResultDetail: { result: 'Reject', detail: '' } })
      console.log(res)
      if (res.code === 0) {
        this.$message.success('审批已驳回！')
        history.back()
      }
    },
    async closeAuth(params) {
      const { id } = params
      const res = await authManageServer.closeAuthList([id])
      console.log(res)
      if (res.code === 0) {
        this.$message.success('审批已废除！')
      }
    },
    paginationHandle() {},
    subApply() {}
  }
}
</script>
<style lang="less" scoped>
.create-data {
  display: flex;
  width: 100%;
  div.approveInfo {
    flex: 1;
  }
  div.record {
    width: 340px;
    padding-left: 40px;

    .step-con {
      display: flex;
      .step {
        transform: translateY(13px);
      }
      .small-circle {
        width: 10px;
        height: 10px;
        border: 1px solid #3071f2;
        background: #d6e3fc;
        border-radius: 50%;
        transform: translateX(7px);
        margin-right: 10px;
      }
      .circle {
        background: #3071f2;
        color: white;
        border-radius: 50%;
        width: 22px;
        height: 22px;
        display: flex;
        align-items: center;
        justify-content: center;
        span {
          font-size: 12px;
          display: inline-block;
          width: 12px;
          height: 12px;
          transform: translate(0, -2px);
          text-align: center;
        }
      }
      span.line {
        height: 88px;
        border-left: 2px dashed #3071f2;
        display: block;
        width: 0;
        transform: translateX(11px);
      }
      ul {
        width: 218px;
        height: 72px;
        background: #f6f7fb;
        border-radius: 4px;
        margin-left: 12px;
        padding: 12px 10px;
        li:first-child {
          color: #525660;
          font-size: 12px;
          line-height: 22px;
        }
        li:last-child {
          color: #787b84;
          font-size: 12px;
          line-height: 20px;
          margin-top: 6px;
        }
      }
    }
  }
  ::v-deep .el-tag {
    padding: 0 12px;
    line-height: 24px;
  }
}
div.whole {
  display: flex;
  margin-bottom: 16px;
}
div.half {
  width: 100%;
  display: flex;
}
div.info-container {
  margin-bottom: 44px;
  span {
    font-size: 14px;
    line-height: 22px;
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
  div.operate {
    display: flex;
    align-items: center;
    width: 100%;
    height: 32px;
  }
  span.handle {
    img {
      width: auto;
      height: 32px;
      margin-right: 12px;
      cursor: pointer;
      vertical-align: middle;
    }
  }
  span.handle.action {
    flex: 1;
  }
  .el-row {
    margin-bottom: 16px;
  }
  span.right {
    width: 118px;
  }
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
  }

  span.info {
    color: #262a32;
  }
  span.status.tag {
    color: white;
    margin-left: 12px;
  }
}
.sub-con {
  margin-top: 32px;
  padding-left: 86px;
}
</style>
