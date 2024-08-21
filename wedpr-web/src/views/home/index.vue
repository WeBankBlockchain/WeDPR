<template>
  <div class="app-container">
    <div class="left">
      <div class="top container">
        <p class="title">常用功能</p>
        <div class="job-entry">
          <dl @click="goPage('/dataManage')">
            <dt><img src="~Assets/images/add_data.png" /></dt>
            <dd>新增数据</dd>
          </dl>
          <dl @click="goPage('/serverManage')">
            <dt><img src="~Assets/images/pir.png" /></dt>
            <dd>匿踪查询</dd>
          </dl>
          <dl @click="goPage('/projectManage')">
            <dt><img src="~Assets/images/psi_circle.png" /></dt>
            <dd>隐私求交</dd>
          </dl>
          <dl @click="goPage('/projectManage')">
            <dt><img src="~Assets/images/model.png" /></dt>
            <dd>联合建模</dd>
          </dl>
          <dl @click="goPage('/projectManage')">
            <dt><img src="~Assets/images/mpc.png" /></dt>
            <dd>联合计算</dd>
          </dl>
        </div>
      </div>
      <div class="bottom container">
        <div class="block">
          <p class="title">数据概览</p>
          <div :class="!permission.includes('groupManage') ? 'widther general' : 'general'">
            <ul v-if="permission.includes('groupManage')">
              <li><img src="~Assets/images/users.png" alt="" /></li>
              <li class="count ell" :title="groupCount">{{ groupCount }}</li>
              <li class="des">用户组数量</li>
            </ul>
            <ul v-if="permission.includes('groupManage')">
              <li><img src="~Assets/images/user_count.png" alt="" /></li>
              <li class="count ell" :title="userCount">{{ userCount }}</li>
              <li class="des">用户数量</li>
            </ul>
            <ul>
              <li><img src="~Assets/images/datasets.png" alt="" /></li>
              <li class="count ell" :title="dataTotal">{{ dataTotal }}</li>
              <li class="des">数据资源数量</li>
            </ul>
            <ul>
              <li><img src="~Assets/images/projects.png" alt="" /></li>
              <li class="count ell" :title="projectTotal">{{ projectTotal }}</li>
              <li class="des">项目数量</li>
            </ul>
            <ul>
              <li><img src="~Assets/images/services.png" alt="" /></li>
              <li class="count ell" :title="userCount">11</li>
              <li class="des">发布服务数量</li>
            </ul>
          </div>
        </div>
        <div class="chart-con">
          <div class="sort">
            <p class="title">任务类型排序</p>
            <div class="chart">
              <el-table size="small" v-loading="loadingFlag" :data="jobOverviewList" :border="false">
                <el-table-column width="126px" label="分类" prop="type">
                  <template v-slot="scope"> {{ handleData(scope.row.type).label }} </template>
                </el-table-column>
                <el-table-column width="96px" label="数量" prop="count" />
                <el-table-column width="180px" label="占比">
                  <template v-slot="scope">
                    <el-progress v-if="scope.row.progress" :stroke-width="8" :percentage="scope.row.progress"></el-progress>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="sort">
            <p class="title">任务数量走势</p>
            <div style="display: flex; flex-wrap: wrap">
              <div>
                <el-radio-group v-model="searchTabIndex" style="margin-right: 14px" size="small" @input="searchTabInput">
                  <el-radio-button :label="0"> 近7天 </el-radio-button>
                  <el-radio-button :label="1"> 近1月 </el-radio-button>
                  <el-radio-button :label="2"> 近3月 </el-radio-button>
                </el-radio-group>
              </div>
              <el-date-picker
                v-model="searchDateVal"
                type="daterange"
                size="small"
                style="width: 260px"
                align="right"
                unlink-panels
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :picker-options="pickerOptions"
                @change="searchDateChange"
              />
            </div>
            <div class="chart" id="chart"></div>
          </div>
        </div>
      </div>
    </div>
    <div class="right">
      <div class="top container">
        <p class="title">我的信息<span @click="modifyShow">修改信息</span></p>
        <div class="info-con">
          <img src="~Assets/images/avatar_male.png" />
          <ul>
            <li>{{ userId }}</li>
            <li>{{ userinfo.roleName === 'admin_user' ? '机构管理员' : '普通用户' }}</li>
            <li class="info">
              <span class="ell" style="margin-right: 10px" :title="agencyName"> 所属机构：{{ agencyName }} </span>
              <span class="ell" style="margin-right: 10px" :title="agencyAdmin"> 机构管理员：{{ agencyAdmin }}</span>
            </li>
            <li class="info" v-if="false">
              <span class="ell" style="margin-right: 10px" :title="userinfo.groupName">所属用户组：{{ userinfo.groupName }}</span>
              <span class="ell" style="margin-right: 10px" :title="userinfo.groupAdminName">用户组管理员：{{ userinfo.groupAdminName }}</span>
            </li>
          </ul>
        </div>
      </div>
      <div class="center container card">
        <p class="title">数据目录<span v-if="dataList.length" @click="goDataListPage">查看更多＞</span></p>
        <ul class="data" v-if="dataList.length">
          <li :title="item.datasetTitle" :key="item.datasetId" v-for="item in dataList" @click="goDataDetail(item)">
            <span class="ell dataName" :title="item.datasetTitle">{{ item.datasetTitle }}</span>
            <span>{{ item.updateAt }}</span>
          </li>
        </ul>
        <el-empty v-else :image-size="120" description="暂无数据">
          <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
        </el-empty>
      </div>
      <div class="bottom container card">
        <p class="title">消息通知<span v-if="tableData.length" @click="moreJob">查看更多＞</span></p>
        <div v-if="tableData.length">
          <ul class="msg" v-for="item in tableData" :key="item.id">
            <li>[任务]</li>
            <li>任务执行</li>
            <li @click="goTaskDetail(item)" class="ell" :tiitle="'任务ID' + item.id + jobStatusMap[item.status]">
              任务ID <span class="link">{{ item.id }}</span
              >{{ jobStatusMap[item.status] }}
            </li>
          </ul>
        </div>
        <el-empty v-else :image-size="120" description="暂无数据">
          <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
        </el-empty>
      </div>
    </div>
    <modifyUser :key="userinfo.email" :userinfo="userinfo" :showModifyModal="showModifyModal" @closeModal="closeModal" @handlOK="handlOK" />
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { mapGetters } from 'vuex'
import { dataManageServer, accountManageServer, projectManageServer, jobManageServer } from 'Api'
import dayjs from 'dayjs'
import { jobStatusMap, jobEnum } from 'Utils/constant.js'
import modifyUser from './modifyUser/index.vue'
const channelColors = {
  PSI: '#2F89F3',
  XGB_TRAINING: '#FFA927',
  XGB_PREDICTING: '#69CB92'
}
export default {
  name: 'HomePage',
  components: {
    modifyUser
  },
  props: {},
  data() {
    return {
      jobOverviewList: [],
      loadingFlag: false,
      option: {},
      dataList: [],
      dataTotal: 0,
      userCount: 0,
      groupCount: 0,
      projectTotal: 0,
      searchDateVal: [],
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        }
      },
      searchTabIndex: 0,
      tableData: [],
      jobStatusMap,
      showModifyModal: false,
      myChart: null
    }
  },
  computed: {
    ...mapGetters(['userId', 'agencyName', 'userinfo', 'algList', 'agencyAdmin', 'permission'])
  },
  created() {
    this.getListDataset()
    this.getUserCount()
    this.queryProject()
    this.queryJobOverview()
    this.getGroupCount()
    this.queryFollowerJobByCondition()
  },
  mounted() {
    this.searchTabInput()
    const that = this
    window.onresize = function () {
      that.myChart && that.myChart.resize()
    }
  },
  methods: {
    handlOK() {
      this.showModifyModal = false
    },
    closeModal() {
      this.showModifyModal = false
    },
    modifyShow() {
      this.showModifyModal = true
    },
    handleData(key) {
      const data = this.algList.filter((v) => v.value === key)
      return data[0] || {}
    },
    initData() {
      const chartDom = document.getElementById('chart')
      if (chartDom) {
        this.myChart = echarts.init(chartDom)
        this.option && this.myChart.setOption(this.option)
      }
    },
    moreJob() {
      this.$router.push({ path: 'messageManage' })
    },
    goTaskDetail(item) {
      this.$router.push({ path: 'jobDetail', query: { id: item.id } })
    },
    async queryFollowerJobByCondition() {
      const res = await jobManageServer.queryFollowerJobByCondition({
        job: {
          id: ''
        },
        pageNum: 1,
        pageSize: 6
      })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { jobs = [] } = res.data
        this.tableData = jobs
      } else {
        this.tableData = []
      }
    },
    searchDateChange() {
      const that = this
      const params = {
        startTime: dayjs(that.searchDateVal[0]).format('YYYY-MM-DD') || '',
        endTime: dayjs(that.searchDateVal[1]).format('YYYY-MM-DD') || ''
      }
      this.queryJobLine({ statTime: params, jobTypeList: [jobEnum.PSI, jobEnum.XGB_PREDICTING, jobEnum.XGB_TRAINING] })
      console.log(params)
    },
    searchTabInput() {
      const that = this
      const end = new Date()
      const start = new Date()
      switch (that.searchTabIndex) {
        case 1:
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
          break
        case 2:
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
          break
        default:
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 6)
          break
      }
      // yyyy-MM-dd
      that.searchDateVal = [start, end]
      that.searchDateChange()
    },
    goPage(path) {
      this.$router.push({ path })
    },
    // 获取前五条数据集列表
    async getListDataset() {
      const params = { pageOffset: 0, pageSize: 6 }
      const res = await dataManageServer.listDataset(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { content = [], totalCount } = res.data
        this.dataList = content
        this.dataTotal = totalCount
      } else {
        this.dataList = []
        this.dataTotal = 0
      }
    },
    // 获取前六条数据集列表
    async getUserCount() {
      const res = await accountManageServer.userCount()
      console.log(res)
      if (res.code === 0 && res.data) {
        const { userCount } = res.data
        this.userCount = userCount
      } else {
        this.userCount = 0
      }
    },
    // 获取管理组count
    async getGroupCount() {
      const res = await accountManageServer.getGroupCount()
      console.log(res)
      if (res.code === 0 && res.data) {
        const { groupCount } = res.data
        this.groupCount = groupCount
      } else {
        this.groupCount = 0
      }
    },
    async queryProject() {
      const res = await projectManageServer.queryProject({ project: { id: '' }, pageNum: 1, pageSize: 1 })
      this.loadingFlag = false
      if (res.code === 0 && res.data) {
        const { total } = res.data
        this.projectTotal = total
      } else {
        this.projectTotal = 0
      }
    },
    async queryJobOverview() {
      const res = await projectManageServer.queryJobOverview({ jobTypeList: [jobEnum.PSI, jobEnum.XGB_PREDICTING, jobEnum.XGB_TRAINING] })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { jobOverviewList, totalCount } = res.data
        this.jobOverviewList = jobOverviewList.map((v) => {
          return {
            type: v.jobType,
            count: v.count,
            progress: Math.floor((v.count * 100) / totalCount)
          }
        })
        console.log(this.jobOverviewList)
      } else {
        this.jobOverviewList = []
      }
    },
    async queryJobLine(params) {
      const res = await projectManageServer.queryJobOverview(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        console.log(res)
        const { statResults = [] } = res.data

        const xData = statResults.map((v) => {
          return v.timeRange.startTime
        })
        const yData = statResults.map((k) => k.jobTypeStats)
        console.log(yData, 'yData')
        const PSIData = yData.map((v) => v.filter((k) => k.jobType === jobEnum.PSI)[0].count)
        const XGB_PREDICTINGData = yData.map((v) => v.filter((k) => k.jobType === jobEnum.XGB_PREDICTING)[0].count)
        const XGB_TRAININGData = yData.map((v) => v.filter((k) => k.jobType === jobEnum.XGB_TRAINING)[0].count)
        const seriesYData = [
          { name: this.handleData(jobEnum.PSI).label, dataList: PSIData },
          { name: this.handleData(jobEnum.XGB_PREDICTING).label, dataList: XGB_PREDICTINGData },
          { name: this.handleData(jobEnum.XGB_TRAINING).label, dataList: XGB_TRAININGData }
        ]
        const series = seriesYData.map((item) => {
          return {
            data: item.dataList, // 具体数据
            type: 'line', // 设置图表类型为折线图
            name: item.name, // 图表名称
            smooth: true, // 是否将折线设置为平滑曲线
            // 设置折线样式
            itemStyle: {
              color: channelColors[item.name] // 设置折线颜色
            }
          }
        })
        console.log(series, 'series')
        this.option = {
          xAxis: {
            type: 'category',
            data: xData
          },
          grid: {
            show: true, // 是否显示图表背景网格
            left: 0, // 图表距离容器左侧多少距离
            right: 16, // 图表距离容器右侧侧多少距离
            bottom: 30, // 图表距离容器上面多少距离
            top: 30, // 图表距离容器下面多少距离
            containLabel: true // 防止标签溢出
          },
          smooth: true,
          legend: {
            data: [this.handleData(jobEnum.PSI).label, this.handleData(jobEnum.XGB_PREDICTING).label, this.handleData(jobEnum.XGB_TRAINING).label],
            left: 60,
            bottom: -6
          },
          yAxis: { type: 'value' },
          series
        }
        this.initData()
      }
    },
    goDataDetail(data) {
      const { datasetId } = data
      this.$router.push({ path: 'dataDetail', query: { datasetId } })
    },
    goDataListPage() {
      this.$router.push({ path: 'dataManage' })
    }
  }
}
</script>

<style lang="less" scoped>
div.app-container {
  width: 100%;
  height: auto;
  box-sizing: border-box;
  overflow: hidden;
  ::v-deep input.el-range-input {
    transform: translateX(6px);
  }
  .container {
    padding: 32px;
    border-radius: 24px;
    background-color: white;
    .el-empty {
      margin-top: 0;
    }
  }
  .card {
    height: 324px;
    position: relative;
  }
  p.title {
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    text-align: left;
    color: #262a32;
    margin-bottom: 24px;
    span {
      cursor: pointer;
      color: #3071f2;
      float: right;
      font-size: 14px;
      line-height: 22px;
    }
  }
  div.left {
    width: 65%;
    float: left;
    div.top {
      margin-bottom: 24px;
      .job-entry {
        display: flex;
        justify-content: space-between;
        margin-top: 4px;
        dl {
          width: 72px;
          text-align: center;
          cursor: pointer;
          img {
            width: 72px;
            height: auto;
            border-radius: 50%;
            margin-bottom: 12px;
          }
        }
      }
    }
    div.bottom {
      div.block {
        margin-bottom: 32px;
      }
      .general {
        display: flex;
        justify-content: space-between;
        ul {
          background: #f5f7fb;
          padding: 16px 24px;
          border-radius: 12px;
          width: calc(20% - 24px);
          img {
            width: 32px;
            height: 32px;
            margin-bottom: 20px;
          }
          li.count {
            font-size: 28px;
            font-weight: 600;
            line-height: 28px;
            letter-spacing: 0.25px;
            text-align: left;
            margin-bottom: 4px;
            color: #262a32;
          }
          li.des {
            font-size: 12px;
            font-weight: 400;
            line-height: 20px;
            text-align: left;
            color: #787b84;
          }
        }
      }
      .general.widther {
        ul {
          width: calc(33% - 24px);
        }
      }
      .chart-con {
        display: flex;
        justify-content: space-between;
        .sort {
          width: calc(50% - 15px);
          #chart {
            width: 100%;
            height: 308px;
          }
        }
      }
    }
  }
  div.right {
    width: calc(35% - 24px);
    float: left;
    margin-left: 24px;
    .container {
      margin-bottom: 24px;
      background-color: white;
      div.info-con {
        height: 85px;
        padding-top: 20px;
        padding-bottom: 4px;
        box-sizing: content-box;
        display: flex;
        align-items: center;
        img {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          margin-right: 24px;
        }
        ul {
          flex: 1;
          li:first-child {
            font-size: 18px;
            line-height: 26px;
            color: #262a32;
          }
          li:nth-child(2) {
            font-size: 12px;
            line-height: 20px;
            color: #b3b5b9;
            margin-bottom: 12px;
          }
          li.info {
            font-size: 12px;
            line-height: 20px;
            color: #787b84;
            span {
              display: inline-block;
              width: calc(50% - 10px);
            }
          }
        }
      }
      ul.data {
        li {
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
          text-align: left;
          margin-bottom: 16px;
          color: #525660;
          cursor: pointer;
          display: flex;
          span.dataName {
            flex: 1;
          }
        }
      }
      ul.msg {
        display: flex;
        margin-bottom: 16px;
        li {
          color: #787b84;
          margin-right: 10px;
        }
        li:nth-child(2) {
          flex: 1;
        }
        li:nth-child(3) {
          color: #b3b5b9;
          width: calc(100% - 140px);
          text-align: right;
          span {
            cursor: pointer;
            color: #3071f2;
          }
        }
      }
    }
  }
}
</style>
