<template>
  <div class="data-manage">
    <div class="tip" v-if="fileUploadTask && fileUploadTask.datasetId">
      <el-alert center show-icon title="当前有文件正在上传中，请不要刷新或关闭本页面，否则上传会失败。" type="warning" effect="light" :closable="false"> </el-alert>
    </div>
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item prop="ownerAgencyName" label="所属机构：">
          <el-select size="small" style="width: 160px" v-model="searchForm.ownerAgencyName" placeholder="请选择">
            <el-option :key="item" v-for="item in agencyList" multiple :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="dataSourceType" label="数据类型：">
          <el-select size="small" style="width: 120px" v-model="searchForm.dataSourceType" placeholder="请选择">
            <el-option :key="item" v-for="item in typeList" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="ownerUserName" label="所属用户：" v-if="searchForm.ownerAgencyName !== agencyId">
          <el-input style="width: 120px" v-model="searchForm.ownerUserName" placeholder="请输入"> </el-input>
        </el-form-item>
        <el-form-item label="所属用户：" prop="ownerUserName" v-if="searchForm.ownerAgencyName === agencyId">
          <el-select
            loading-text="搜索中"
            filterable
            style="width: 120px"
            v-model="searchForm.ownerUserName"
            remote
            :remote-method="getUserNameSelect"
            placeholder="请选择"
            clearable
          >
            <el-option v-for="item in userNameSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="datasetTitle" label="资源名称：">
          <el-input style="width: 120px" v-model="searchForm.datasetTitle" placeholder="请输入"> </el-input>
        </el-form-item>

        <el-form-item prop="status" label="上传状态：">
          <el-select size="small" style="width: 120px" v-model="searchForm.status" placeholder="请选择">
            <el-option label="成功" :value="0"></el-option>
            <el-option label="失败" :value="-1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="createTime" label="创建时间：">
          <el-date-picker
            style="width: 280px"
            value-format="yyyy-MM-dd"
            v-model="searchForm.createTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button :disabled="showApplySelect || showDeleteSelect" type="primary" :loading="queryFlag" @click="queryHandle">
            {{ queryFlag ? '查询中...' : '查询' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="default" :disabled="showApplySelect || showDeleteSelect" :loading="queryFlag" @click="reset"> 重置 </el-button>
        </el-form-item>
        <div v-if="!showApplySelect && !showDeleteSelect">
          <el-form-item>
            <el-button icon="el-icon-plus" type="primary" @click="createAccount"> 新建数据资源 </el-button>
            <el-button icon="el-icon-plus" type="primary" @click="startApplySelect"> 批量申请授权 </el-button>
            <el-button icon="el-icon-delete" style="color: red" @click="startDelete"> 批量删除 </el-button>
          </el-form-item>
        </div>
      </el-form>
      <div class="handle" v-if="showApplySelect">
        <span>已选{{ selectdDataList.length }}项</span><el-button size="small" :disabled="!selectdDataList.length" type="primary" @click="applyAuth"> 确认申请 </el-button
        ><el-button size="small" type="info" @click="cancelApply"> 取消 </el-button>
      </div>
      <div class="handle" v-if="showDeleteSelect">
        <span>已选{{ selectdDataList.length }}项</span><el-button style="color: red" size="small" :disabled="!selectdDataList.length" @click="showDeleteMore"> 确认删除 </el-button
        ><el-button size="small" type="info" @click="cancelDelete"> 取消 </el-button>
      </div>
    </div>
    <div class="card-container" v-if="total">
      <dataCard
        @deleteDataset="showDelModal(item)"
        @getDetail="getDetail(item)"
        @deleteData="showDelModal(item)"
        @dataApply="dataApply(item)"
        @selected="(checked) => selected(checked, item)"
        :selected="selectdDataList.map((v) => v.datasetId).includes(item.datasetId)"
        showEdit
        showStatus
        v-for="item in dataList"
        :dataInfo="item"
        :key="item.datasetId"
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
import { dataManageServer } from 'Api'
import wePagination from '@/components/wePagination.vue'
import dataCard from '@/components/dataCard.vue'
import { uploadFile } from 'Mixin/uploadFile.js'
import { mapGetters, mapMutations } from 'vuex'
import { SET_FILEUPLOADTASK } from 'Store/mutation-types.js'
import { userSelect } from 'Mixin/userSelect.js'
import { handleParamsValid } from 'Utils/index.js'
import { dataStatusEnum } from 'Utils/constant.js'
export default {
  name: 'dataManage',
  mixins: [uploadFile, userSelect],
  components: {
    wePagination,
    dataCard
  },
  data() {
    return {
      activeName: 'first',
      searchForm: {
        ownerAgencyName: '',
        ownerUserGroupId: '',
        ownerUserName: '',
        datasetTitle: '',
        createTime: '',
        dataSourceType: '',
        status: ''
      },
      searchQuery: {
        ownerAgencyName: '',
        ownerUserGroupId: '',
        ownerUserName: '',
        datasetTitle: '',
        createTime: '',
        dataSourceType: '',
        status: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 8
      },
      total: -1,
      queryFlag: false,
      dataList: [],
      loadingFlag: false,
      showAddModal: false,
      selectdDataList: [],
      showApplySelect: false,
      showDeleteSelect: false,
      typeList: [],
      timer: ''
    }
  },
  created() {
    this.getListDataset()
    this.getDataUploadType()
    this.checkTask()
    this.updateDataStatusInterver()
  },
  computed: {
    ...mapGetters(['fileUploadTask', 'agencyList', 'userId', 'agencyId', 'groupList']),
    deleteDisabled() {
      const { selectdDataList } = this
      return !(selectdDataList.length && selectdDataList.every((v) => v.isOwner))
    },
    authDisabled() {
      const { selectdDataList } = this
      return !(selectdDataList.length && selectdDataList.every((v) => !v.permissions.usable))
    }
  },
  watch: {
    activeName() {
      this.$refs.searchForm.resetFields()
      this.pageData = {
        page_offset: 1,
        page_size: 8
      }
      this.query()
    }
  },
  methods: {
    ...mapMutations([SET_FILEUPLOADTASK]),
    handleClick() {},
    query() {
      const { activeName } = this
      switch (activeName) {
        case 'first':
          this.queryFollowerAuthList()
          break
        case 'second':
          this.queryMyApplyList()
          break
        case 'third':
          this.queryMyFollowList()
          break
      }
    },
    checkTask() {
      const { fileUploadTask } = this
      const that = this
      if (!fileUploadTask) {
        return
      }
      const { dataFile, datasetId, status } = fileUploadTask
      if (datasetId && status === 'waitting') {
        this.handleFile({
          userData: dataFile,
          datasetId,
          onFail() {
            console.log('upload Fail')
            that.$message.error('上传文件失败')
            that.getListDataset()
          },
          onSuccess() {
            console.log('upload success')
            that.$message.success('上传文件成功')
            that.getListDataset()
            that.SET_FILEUPLOADTASK(null)
          }
        })
      }
    },
    // 存在非终态数据轮询
    updateDataStatusInterver() {
      this.timer = setInterval(() => {
        const hasNotFinish = this.dataList.filter((v) => {
          return ![dataStatusEnum.Success, dataStatusEnum.Failure, dataStatusEnum.Fatal].includes(v.status)
        })
        hasNotFinish && this.getListDataset()
      }, 5000)
    },
    async getDataUploadType() {
      const res = await dataManageServer.getDataUploadType()
      console.log(res)
      if (res.code === 0 && res.data) {
        this.typeList = res.data
      }
    },
    getDetail(row) {
      if (!this.showDeleteSelect && !this.showApplySelect) {
        this.$router.push({ path: '/dataDetail', query: { datasetId: row.datasetId } })
      }
    },
    startApplySelect() {
      this.showApplySelect = true
      this.selectdDataList = []
      this.filterApplyAbleSelectData()
    },
    filterApplyAbleSelectData() {
      this.dataList = this.dataList.map((v) => {
        return {
          ...v,
          showSelect: !v.permissions.usable
        }
      })
    },
    filterDeleteAbleSelectData() {
      this.dataList = this.dataList.map((v) => {
        return {
          ...v,
          showSelect: v.isOwner
        }
      })
    },
    cancelApply() {
      this.showApplySelect = false
      this.selectdDataList = []
      this.dataList = this.dataList.map((v) => {
        return {
          ...v,
          showSelect: false
        }
      })
    },
    cancelDelete() {
      this.showDeleteSelect = false
      this.selectdDataList = []
      this.dataList = this.dataList.map((v) => {
        return {
          ...v,
          showSelect: false
        }
      })
    },
    startDelete() {
      this.showDeleteSelect = true
      this.filterDeleteAbleSelectData()
    },
    applyAuth() {
      const { selectdDataList } = this
      const ids = selectdDataList.map((v) => v.datasetId).join(',')
      this.$router.push({ path: '/dataApply', query: { selectdDataStr: encodeURIComponent(ids), applyType: 'wedpr_data_auth' } })
    },
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.getListDataset()
        } else {
          return false
        }
      })
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getListDataset()
    },

    // 获取数据集列表
    async getListDataset() {
      const { page_offset, page_size } = this.pageData
      const { ownerAgencyName = '', ownerUserGroupId = '', ownerUserName = '', datasetTitle = '', createTime = '', dataSourceType = '', status = '' } = this.searchQuery
      let params = handleParamsValid({ ownerAgencyName, ownerUserGroupId, ownerUserName, datasetTitle, dataSourceType, status })
      if (createTime && createTime.length) {
        params.startTime = createTime[0]
        params.endTime = createTime[1]
      }
      params = { ...params, pageNum: page_offset, pageSize: page_size }
      this.loadingFlag = true
      const res = await dataManageServer.listDataset(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { content = [], totalCount } = res.data
        this.dataList = content.map((v) => {
          return {
            ...v,
            isOwner: v.ownerAgencyName === this.agencyId && v.ownerUserName === this.userId,
            showSelect: false
          }
        })
        if (this.showApplySelect) {
          this.filterApplyAbleSelectData()
        }
        if (this.showDeleteSelect) {
          this.filterDeleteAbleSelectData()
        }
        console.log(content, 'content', totalCount)
        this.total = totalCount
      } else {
        this.dataList = []
        this.total = 0
      }
    },
    // 删除账户
    showDelModal(data) {
      const { datasetId = '', datasetTitle } = data
      this.$confirm(`确认删除数据--'${datasetTitle}'?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.deleteDataset({ datasetId })
        })
        .catch(() => {})
    },
    // 删除账户
    showDeleteMore() {
      this.$confirm('确认批量删除数据吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.deleteDatasetList({ datasetIdList: this.selectdDataList.map((v) => v.datasetId) })
        })
        .catch(() => {})
    },
    dataApply(row) {
      this.$router.push({ path: '/dataApply', query: { selectdDataStr: encodeURIComponent(row.datasetId), applyType: 'wedpr_data_auth' } })
    },
    selected(checked, row) {
      const { datasetId } = row
      if (checked) {
        this.selectdDataList.push({ ...row })
      } else {
        this.selectdDataList = this.selectdDataList.filter((v) => v.datasetId !== datasetId)
      }
    },
    async deleteDataset(params) {
      const res = await dataManageServer.deleteDataset(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('数据删除成功')
        this.getListDataset()
      }
    },
    async deleteDatasetList(params) {
      const res = await dataManageServer.deleteDatasetList(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('数据批量删除成功')
        this.showDeleteSelect = false
        this.selectdDataList = []
        this.getListDataset()
      }
    },
    reset() {
      this.$refs.searchForm.resetFields()
    },
    createAccount() {
      this.$router.push({ path: '/dataCreate' })
    },
    closeModal() {
      this.showAddModal = false
    },
    handlOK() {
      this.showAddModal = false
      this.getListDataset()
    }
  },
  beforeDestroy() {
    this.timer && clearInterval(this.timer)
  }
}
</script>
<style lang="less" scoped>
div.data-manage {
  div.tip {
    position: absolute;
    top: 20px;
    left: 50%;
    width: 80%;
    transform: translateX(-50%);
    z-index: 999;
  }
}
div.card-container {
  overflow: hidden;
  margin-left: -16px;
  margin-right: -16px;
}

div.handle {
  span {
    width: 75px;
    color: #787b84;
    float: left;
    line-height: 32px;
  }
  ::v-deep .el-button--small {
    margin-left: 10px;
  }
}
</style>
