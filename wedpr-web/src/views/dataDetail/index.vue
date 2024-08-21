<template>
  <div class="create-data">
    <div class="title-radius">资源详情</div>
    <div class="info-container">
      <div class="whole">
        <div class="half">
          <span class="title">资源ID：</span>
          <span class="info" :title="dataInfo.datasetId"> {{ dataInfo.datasetId }} </span>
        </div>
        <div class="half">
          <span class="title right">资源名称：</span>
          <span class="info" :title="dataInfo.datasetTitle"> {{ dataInfo.datasetTitle }} </span>
        </div>
      </div>
      <div class="whole">
        <div class="half">
          <span class="title">所属机构：</span>
          <span class="info" :title="dataInfo.ownerAgencyName"> {{ dataInfo.ownerAgencyName }} </span>
        </div>
        <div class="half">
          <span class="title right">所属用户：</span>
          <span class="info" :title="dataInfo.ownerUserName"> {{ dataInfo.ownerUserName }} </span>
        </div>
      </div>
      <div class="whole">
        <div class="half">
          <span class="title">样本量：</span>
          <span class="info" :title="dataInfo.recordCount"> {{ dataInfo.recordCount }} </span>
        </div>
        <div class="half">
          <span class="title right">特征量：</span>
          <span class="info" :title="dataInfo.columCount"> {{ dataInfo.columCount }} </span>
        </div>
      </div>
      <div class="whole">
        <div class="half">
          <span class="title right">上传时间：</span>
          <span class="info" :title="dataInfo.createTcreateAtime"> {{ dataInfo.createAt }} </span>
        </div>
        <div class="half">
          <span class="title">特征：</span>
          <span class="info" :title="dataInfo.datasetFields"> {{ dataInfo.datasetFields }} </span>
        </div>
      </div>
      <div class="whole">
        <div class="half">
          <span class="title">链上存证：</span>
          <span class="info" :title="dataInfo.datasetHash"> {{ dataInfo.datasetHash }} </span>
        </div>
        <div class="half">
          <span class="title right">数据格式：</span>
          <span class="info" :title="dataInfo.dataSourceType"> {{ dataInfo.dataSourceType }} </span>
        </div>
      </div>
      <div class="whole">
        <div class="half all">
          <span class="title">可见范围：</span>
          <span class="info">
            <div class="tags-con">
              <el-tag style="margin-left: 10px; margin-bottom: 10px" size="small" type="info" v-for="item in dataInfo.permissionDes" :key="item">{{ item }}</el-tag>
            </div>
          </span>
        </div>
      </div>
    </div>
    <div class="title-radius" v-if="dataInfo.isOwner">使用记录</div>
    <div class="tableContent autoTableWrap" v-if="dataInfo.isOwner && total">
      <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="tableData" :border="true" class="table-wrap">
        <el-table-column label="项目ID" prop="projectId" />
        <el-table-column label="项目名称" prop="projectName" />
        <el-table-column label="发起用户" prop="initAgency">
          <template v-slot="scope"> {{ scope.row.initAgency }} | {{ scope.row.creater }} </template>
        </el-table-column>
        <el-table-column label="发起用户" prop="initAgency">
          <template v-slot="scope"> {{ scope.row.initAgency }} | {{ scope.row.creater }} </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="projectId" />
        <el-table-column label="项目状态" prop="projectName" />
        <el-table-column label="操作">
          <template>
            <el-button size="small" type="text">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
    </div>
    <el-empty v-if="!total && dataInfo.isOwner" :image-size="120" description="暂无数据">
      <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
    </el-empty>
    <div class="sub-con">
      <el-button size="medium" type="primary" v-if="!dataInfo.permissions.usable" @click="subApply"> 申请使用 </el-button>
    </div>
  </div>
</template>
<script>
import { dataManageServer } from 'Api'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { mapGetters } from 'vuex'
export default {
  name: 'dataDetail',
  mixins: [tableHeightHandle],
  data() {
    return {
      dataInfo: {
        permissions: {}
      },
      pageData: {
        page_offset: 0,
        page_size: 10
      },
      tableData: [],
      total: 0,
      rangeMap: {
        selfUserGroup: '本用户组内',
        selfAgency: '本机构内',
        agencyList: '指定机构',
        userList: '指定用户',
        global: '全局'
      }
    }
  },
  created() {
    const { datasetId } = this.$route.query
    this.datasetId = datasetId
    datasetId && this.getDetail()
  },
  computed: {
    ...mapGetters(['userId', 'agencyId'])
  },
  methods: {
    // 获取数据集详情
    async getDetail() {
      this.loadingFlag = true
      const { datasetId } = this
      const res = await dataManageServer.queryDataset({ datasetId })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        this.dataInfo = { ...res.data, isOwner: res.data.ownerUserId === this.userId && res.data.ownerAgencyId === this.agencyId }
        const { visibilityDetails } = this.dataInfo
        const { permissionDes = [] } = JSON.parse(visibilityDetails)
        this.dataInfo.permissionDes = permissionDes
        console.log(permissionDes)
      } else {
        this.dataInfo = {}
      }
    },
    paginationHandle() {},
    subApply() {
      const { datasetId } = this.dataInfo
      this.$router.push({ path: '/dataApply', query: { selectdDataStr: encodeURIComponent(datasetId) } })
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
div.all {
  width: 100%;
  display: flex;
}

.el-empty {
  margin-top: 0;
}
div.info-container {
  margin-bottom: 44px;
  span {
    font-size: 14px;
    line-height: 22px;
    color: #525660;
  }
  span.title {
    float: left;
    width: 106px;
    text-align: right;
    color: #525660;
  }
  span.info {
    flex: 1;
    color: #262a32;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    .tags-con {
      white-space: normal;
      word-break: break-all;
    }
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
</style>
