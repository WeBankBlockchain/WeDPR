<template>
  <div class="screen">
    <div class="top">
      <div class="back"><img @click="backHome" src="~Assets/images/back.png" alt="" /></div>
      <div class="title">管理监控平台</div>
      <div class="time">
        <p>{{ dateStr }}</p>
      </div>
    </div>
    <div class="bottom">
      <div class="left slide-con">
        <div class="title">
          <div class="img-container">
            <img src="~Assets/images/lead-data.png" alt="" />
          </div>
          <div class="percent-conatiner">
            <el-progress style="width: 160px" :stroke-width="8" :percentage="Number(datasetUseInfo.usedProportion)" :show-text="false"></el-progress
            ><span class="ell rate" :title="`已使用${datasetUseInfo.usedCount}/${datasetUseInfo.totalCount}`">
              已使用{{ datasetUseInfo.usedCount }}/{{ datasetUseInfo.totalCount }}&nbsp;&nbsp;{{ Math.floor((datasetUseInfo.usedCount * 100) / datasetUseInfo.totalCount) }}%</span
            >
          </div>
        </div>
        <div class="chart-container">
          <div class="chart-con circle">
            <div id="circle-chart" class="chart"></div>
            <div class="data-table">
              <ul class="head">
                <li class="type">分类</li>
                <li>个数</li>
                <li>使用率</li>
              </ul>
              <ul v-for="item in datasetTypeStatisticTableData" :key="item.datasetType">
                <li class="type"><span :style="{ backgroundColor: item.color }"></span>{{ item.datasetType }}</li>
                <li>{{ item.count }}</li>
                <li>{{ item.usedProportion }}%</li>
              </ul>
            </div>
          </div>
          <div class="chart-con">
            <div id="bar-chart" class="chart"></div>
          </div>
          <div class="chart-con line-chart">
            <p>数据资源总数走势</p>
            <div id="line-chart" class="chart"></div>
          </div>
        </div>
      </div>
      <div class="center">
        <div class="agency-info">
          <p><span></span>共接入机构：{{ totalAgencyCount }}</p>
          <p class="error"><span></span>故障机构：{{ faultAgencyCount }}</p>
        </div>
        <div id="graph-chart" ref="main" class="chart"></div>
        <div class="notice-con">
          <div class="msg-table">
            <ul class="head">
              <li>网络实时动态</li>
            </ul>
            <div v-if="logInfoList.length">
              <ul v-for="item in logInfoList" :key="item.key">
                <li class="msg">{{ item.des }}</li>
                <li class="time">{{ item.createTime }}</li>
              </ul>
            </div>
            <div class="empty" v-else>暂无动态</div>
          </div>
          <div class="msg-table">
            <ul class="head">
              <li>告警通知</li>
            </ul>
            <div v-if="warningList.length">
              <ul v-for="item in warningList" :key="item.name">
                <li class="msg">节点{{ item.name }} 网络异常</li>
                <li class="time">{{ item.time }}</li>
              </ul>
            </div>
            <div class="empty" v-else>暂无警告</div>
          </div>
        </div>
      </div>
      <div class="right slide-con">
        <div class="title">
          <div class="img-container">
            <img style="width: 108px" src="~Assets/images/job_sat.png" alt="" />
          </div>
          <div class="percent-container">
            <el-progress style="width: 160px" :stroke-width="8" :percentage="Number(jobOverview.successProportion)" :show-text="false"></el-progress
            ><span class="ell rate" :title="`成功执行任务${jobOverview.successCount}/${jobOverview.totalCount}`">
              成功执行任务{{ jobOverview.successCount }}/{{ jobOverview.totalCount }}&nbsp;&nbsp;{{ Math.floor((jobOverview.successCount * 100) / jobOverview.totalCount) }}%</span
            >
          </div>
        </div>
        <div class="chart-container">
          <div class="chart-con circle">
            <div id="job-circle-chart" class="chart"></div>
          </div>
          <div class="chart-con bar-chart-con">
            <div id="job-bar-chart" class="chart"></div>
          </div>
          <div class="chart-con line-chart">
            <p>任务数量走势</p>
            <div id="job-line-chart" class="chart"></div>
          </div>
        </div>
      </div>
    </div>
    <div class="copyRight">
      <img src="~Assets/images/copyRight.png" />
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { mapGetters } from 'vuex'
import { dashboardManageServer, logManageServer } from 'Api'
import { jobStatusMap, actionMap, actionScreenStatus } from 'Utils/constant.js'
import dayjs from 'dayjs'
import { colorList, circleOption, barOption, lineOption, circleJobOption, barJobOption, lineJobOption, graphChartOption, spliceLegend } from './chartsSetting.js'
import nodeUsable from '../../assets/images/node-usable.png'
import nodeDisable from '../../assets/images/node-disable.png'
import notConnected from '../../assets/images/notconnected.png'
export default {
  name: 'HomePage',
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
      myDataCircleChart: null,
      myDataLineChart: null,
      myDataBarChart: null,
      myJobCircleChart: null,
      myJobineChart: null,
      myJobBarChart: null,
      graphChart: null,
      resourceTypeMap: {},
      dataReferOverviewList: [
        {
          type: 'CSV文件',
          count: 200,
          useRate: '40%'
        },
        {
          type: 'EXCEL文件',
          count: 200,
          useRate: '40%'
        },
        {
          type: '数据库',
          count: 200,
          useRate: '40%'
        },
        {
          type: 'HIVE',
          count: 200,
          useRate: '40%'
        },
        {
          type: 'HDFS',
          count: 200,
          useRate: '40%'
        }
      ],
      datasetUseInfo: {
        usedCount: 0,
        totalCount: 0,
        usedProportion: 0
      },
      jobOverview: {
        successCount: 0,
        totalCount: 0,
        successProportion: 0
      },
      datasetTypeStatisticTableData: [],
      logInfoList: [],
      dateStr: '',
      getTimeTimer: null,
      getDataTimer: null,
      faultAgencyCount: 0,
      totalAgencyCount: 0,
      warningList: []
    }
  },
  computed: {
    ...mapGetters(['userId', 'agencyName', 'userinfo', 'algList', 'agencyAdmin', 'permission']),
    algListMap() {
      const data = {}
      this.algList.forEach((v) => {
        data[v.value] = v.label
      })
      return data
    }
  },
  created() {
    this.getCurrentDateTimeChinese()
  },
  mounted() {
    this.initData()
    this.fullScreen()
    const that = this
    window.onresize = function () {
      that.myDataCircleChart && that.myDataCircleChart.resize()
      that.myDataBarChart && that.myDataBarChart.resize()
      that.myDataLineChart && that.myDataLineChart.resize()
      that.myJobCircleChart && that.myJobCircleChart.resize()
      that.myJobineChart && that.myJobineChart.resize()
      that.myJobBarChart && that.myJobBarChart.resize()
      that.graphChart && that.graphChart.resize()
    }
    this.getTimeTimer = setInterval(this.getCurrentDateTimeChinese, 1000)
    this.getDataTimer = setInterval(this.initData, 20000)
  },
  methods: {
    getCurrentDateTimeChinese() {
      const date = new Date()
      const year = date.getFullYear()
      const month = date.getMonth() + 1 // 月份从0开始
      const day = date.getDate()
      const dayOfWeek = date.getDay()
      let hour = date.getHours()
      hour = hour < 10 ? '0' + hour : hour
      let minute = date.getMinutes()
      minute = minute < 10 ? '0' + minute : minute
      let second = date.getSeconds()
      second = second < 10 ? '0' + second : second
      const daysOfWeek = ['星期天', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      this.dateStr = `${year} 年 ${month} 月 ${day} 日 ${daysOfWeek[dayOfWeek]} ${hour}:${minute}:${second}`
    },
    initData() {
      this.getDatasetInfo()
      this.getDatasetLineData()
      this.getJobInfo()
      this.getJobLineData()
      this.getAgencyInfo()
      this.queryRecordSyncStatus()
    },
    fullScreen() {
      const full = document.getElementById('app')
      if (full.RequestFullScreen) {
        full.RequestFullScreen()
      } else if (full.mozRequestFullScreen) {
        full.mozRequestFullScreen()
      } else if (full.webkitRequestFullScreen) {
        full.webkitRequestFullScreen()
      } else if (full.msRequestFullscreen) {
        full.msRequestFullscreen()
      }
    },
    exitFullScreen() {
      if (document.exitFullScreen) {
        document.exitFullScreen()
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen()
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen()
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen()
      }
    },
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
    initCircleData() {
      if (this.myDataCircleChart) {
        this.myDataCircleChart.setOption(circleOption)
      } else {
        const chartDom = document.getElementById('circle-chart')
        if (chartDom) {
          this.myDataCircleChart = echarts.init(chartDom)
          this.myDataCircleChart && this.myDataCircleChart.setOption(circleOption)
        }
      }
    },
    initBarData() {
      if (this.myDataBarChart) {
        this.myDataBarChart.setOption(barOption)
      } else {
        const chartDom = document.getElementById('bar-chart')
        if (chartDom) {
          this.myDataBarChart = echarts.init(chartDom)
          this.myDataBarChart && this.myDataBarChart.setOption(barOption)
        }
      }
    },
    initLineData() {
      if (this.myDataLineChart) {
        this.myDataLineChart.setOption(lineOption)
      } else {
        const chartDom = document.getElementById('line-chart')
        if (chartDom) {
          this.myDataLineChart = echarts.init(chartDom)
          this.myDataLineChart && this.myDataLineChart.setOption(lineOption)
        }
      }
    },
    initJobCircleData() {
      if (this.myJobCircleChart) {
        this.myJobCircleChart.setOption(circleJobOption)
      } else {
        const chartDom = document.getElementById('job-circle-chart')
        if (chartDom) {
          this.myJobCircleChart = echarts.init(chartDom)
          this.myJobCircleChart && this.myJobCircleChart.setOption(circleJobOption)
        }
      }
    },
    initJobBarData() {
      if (this.myJobBarChart) {
        this.myJobBarChart.setOption(barJobOption)
      } else {
        const chartDom = document.getElementById('job-bar-chart')
        if (chartDom) {
          this.myJobBarChart = echarts.init(chartDom)
          this.myJobBarChart && this.myJobBarChart.setOption(barJobOption)
        }
      }
    },
    initJobLineData() {
      if (this.myJobineChart) {
        this.myJobineChart.setOption(lineJobOption)
      } else {
        const chartDom = document.getElementById('job-line-chart')
        if (chartDom) {
          this.myJobineChart = echarts.init(chartDom)
          this.myJobineChart && this.myJobineChart.setOption(lineJobOption)
        }
      }
    },
    initGraphChart() {
      if (this.graphChart) {
        this.graphChart.setOption(graphChartOption)
      } else {
        const chartDom = document.getElementById('graph-chart')
        if (chartDom) {
          this.graphChart = echarts.init(chartDom)
          this.graphChart && this.graphChart.setOption(graphChartOption)
        }
      }
    },
    backHome() {
      this.exitFullScreen()
      history.go(-1)
    },
    moreJob() {
      this.$router.push({ path: 'messageManage' })
    },
    goTaskDetail(item) {
      this.$router.push({ path: 'jobDetail', query: { id: item.id } })
    },
    goPage(path) {
      this.$router.push({ path })
    },
    // 获取数据集使用信息
    async getDatasetInfo() {
      const res = await dashboardManageServer.getDatasetInfo()
      if (res.code === 0 && res.data) {
        const { datasetOverview = {}, datasetTypeStatistic = [], agencyDatasetTypeStatistic = [] } = res.data
        this.datasetUseInfo = { ...datasetOverview } // progeress
        const sortedData = datasetTypeStatistic.sort((a, b) => b.count - a.count)
        this.datasetTypeStatisticTableData = sortedData.map((v, i) => {
          return {
            ...v,
            color: colorList[i % colorList.length]
          }
        })
        // 饼图
        circleOption.series[0].data = sortedData
          .map((v) => {
            return {
              name: v.datasetType,
              value: v.count
            }
          })
          .filter((v) => v.value)
        this.initCircleData()
        // 去除数据集总数为0的机构
        const dataFiltered = agencyDatasetTypeStatistic.filter((v) => v.totalCount).reverse()
        const agencyNameList = dataFiltered.map((v) => v.agencyName)
        barOption.yAxis.data = agencyNameList
        const datasetTypeList = datasetTypeStatistic.map((v) => v.datasetType)
        barOption.series = datasetTypeList.map((dataType) => {
          const countList = []
          dataFiltered.forEach((agencyData) => {
            const { datasetTypeStatistic } = agencyData
            const data = datasetTypeStatistic.filter((data) => data.datasetType === dataType)
            if (data.length) {
              countList.push(data[0].count)
            } else {
              countList.push(0)
            }
          })
          return {
            name: dataType,
            type: 'bar',
            stack: 'total',
            barWidth: 12,
            emphasis: {
              focus: 'series'
            },
            data: countList
          }
        })
        this.initBarData()
      }
    },
    // 数据集趋势图
    async getDatasetLineData() {
      const start = new Date()
      const end = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 6)
      const res = await dashboardManageServer.getDatasetLineData({ startTime: dayjs(start).format('YYYY-MM-DD'), endTime: dayjs(end).format('YYYY-MM-DD') })
      this.loadingFlag = false
      if (res.code === 0 && res.data) {
        const { agencyDatasetStat = [] } = res.data
        const dateList = agencyDatasetStat[0].dateList
        const agencylist = agencyDatasetStat.map((v) => v.agencyName)
        const series = agencyDatasetStat.map((v) => {
          return {
            data: v.countList, // 具体数据
            type: 'line', // 设置图表类型为折线图
            name: v.agencyName, // 图表名称
            smooth: true // 是否将折线设置为平滑曲线
          }
        })
        lineOption.xAxis.data = dateList
        lineOption.legend.data = agencylist
        lineOption.series = series
        this.initLineData()
      }
    },
    // 获取任务信息
    async getJobInfo() {
      const res = await dashboardManageServer.getJobInfo()
      if (res.code === 0 && res.data) {
        let { jobOverview = {}, jobTypeStatistic = [], agencyJobTypeStatistic = [] } = res.data
        // 算法中英文切换
        console.log(this.algListMap, 'this.algListMap')
        jobTypeStatistic = jobTypeStatistic.map((v) => {
          return { ...v, jobType: this.algListMap[v.jobType] || v.jobType }
        })
        this.jobOverview = { ...jobOverview } // progeress
        circleJobOption.series[0].data = jobTypeStatistic
          .map((v) => {
            return {
              name: v.jobType,
              value: v.count
            }
          })
          .filter((v) => v.value)
        const seriesData = circleJobOption.series[0].data
        circleJobOption.legend.formatter = function (name) {
          let tarValue
          for (let i = 0; i < seriesData.length; i++) {
            if (seriesData[i].name === name) {
              tarValue = seriesData[i].value
            }
          }
          return `${name}       ${tarValue}个`
        }
        this.initJobCircleData()
        // 去除数据集总数为0的机构
        const dataFiltered = agencyJobTypeStatistic.filter((v) => v.totalCount).reverse()
        const agencyNameList = dataFiltered.map((v) => v.agencyName)
        barJobOption.yAxis.data = agencyNameList
        const jobTypeList = jobTypeStatistic.map((v) => v.jobType)
        barJobOption.series = jobTypeList.map((jobType) => {
          const countList = []
          dataFiltered.forEach((agencyData) => {
            const { jobTypeStatistic } = agencyData
            const data = jobTypeStatistic.filter((data) => (this.algListMap[data.jobType] || data.jobType) === jobType)
            if (data.length) {
              countList.push(data[0].count)
            } else {
              countList.push(0)
            }
          })
          return {
            name: jobType,
            type: 'bar',
            barWidth: 12,
            stack: 'total',
            emphasis: {
              focus: 'series'
            },
            itemStyle: {
              barWidth: 6 // 设置柱子粗细
            },
            data: countList
          }
        })
        barJobOption.legend = spliceLegend(jobTypeList)
        barJobOption.grid.bottom = (Math.ceil(jobTypeList.length / 4) + 1) * 22
        this.initJobBarData()
      }
    },
    // 任务趋势图
    async getJobLineData() {
      const start = new Date()
      const end = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 6)
      const res = await dashboardManageServer.getJobLineData({ startTime: dayjs(start).format('YYYY-MM-DD'), endTime: dayjs(end).format('YYYY-MM-DD') })
      this.loadingFlag = false

      if (res.code === 0 && res.data) {
        const { jobTypeStat = [] } = res.data
        const dateList = jobTypeStat[0].dateList
        const jobTypeList = jobTypeStat.map((v) => this.algListMap[v.jobType] || v.jobType)
        const series = jobTypeStat.map((v) => {
          return {
            data: v.countList, // 具体数据
            type: 'line', // 设置图表类型为折线图
            name: this.algListMap[v.jobType] || v.jobType, // 图表名称
            smooth: true // 是否将折线设置为平滑曲线
          }
        })
        lineJobOption.xAxis.data = dateList
        lineJobOption.legend = spliceLegend(jobTypeList)
        lineJobOption.grid.bottom = (Math.ceil(jobTypeList.length / 4) + 1) * 22
        lineJobOption.series = series
        this.initJobLineData()
      }
    },
    async queryRecordSyncStatus() {
      const params = { pageNum: 1, pageSize: 5 }
      const res = await logManageServer.queryRecordSyncStatus(params)
      if (res.code === 0 && res.data) {
        const { dataList = [] } = res.data
        this.logInfoList = dataList
          .map((v) => {
            const { resourceAction, createTime, status, transactionHash } = v
            const des = actionMap[resourceAction] + actionScreenStatus[status]
            return {
              des,
              createTime,
              key: transactionHash
            }
          })
          .splice(0, 3)
      }
    },
    async getAgencyInfo() {
      const res = await dashboardManageServer.getAgencyInfo()
      if (res.code === 0 && res.data) {
        const { agencyAdmin, agencyFaultList, agencyPeerList, faultAgencyCount, totalAgencyCount } = res.data
        this.faultAgencyCount = faultAgencyCount
        this.totalAgencyCount = totalAgencyCount
        const centerPosition = this.getCenterPos()
        console.log(centerPosition, 'centerPosition')
        // 中心节点
        const centerData = {
          name: agencyAdmin,
          symbol: 'image://' + nodeUsable,
          symbolSize: [100, 80],
          x: centerPosition.x,
          y: centerPosition.y
        }
        // 定位节点（隐藏）
        const leftTopData = {
          name: '待接入1',
          symbol: 'image://' + notConnected,
          symbolSize: [54, 59],
          x: centerPosition.maxX,
          y: 0
        }
        // 定位节点（隐藏）
        const rightBottomData = {
          name: '待接入2',
          symbol: 'image://' + notConnected,
          symbolSize: [54, 59],
          x: 0,
          y: centerPosition.maxY
        }

        // 问题节点
        const FaultData = agencyFaultList.map((v) => {
          return {
            name: v,
            symbol: 'image://' + nodeDisable,
            symbolSize: [100, 80]
          }
        })
        const time = dayjs().format('YYYY-MM-DD hh:mm:ss')
        this.warningList = [...agencyFaultList].splice(0, 3).map((v) => {
          return {
            name: v,
            time
          }
        })
        // 正常节点
        const PeerData = agencyPeerList.map((v) => {
          return {
            name: v,
            symbol: 'image://' + nodeUsable,
            symbolSize: [100, 80]
          }
        })
        const circleNodes = [...FaultData, ...PeerData].filter((v) => v.name !== agencyAdmin)
        const calcedCircleNodes = circleNodes.map((v, i) => {
          const { x, y } = this.circledNodesPosition(centerPosition, i + 1, circleNodes.length, centerPosition.maxX / 2 - 50)
          console.log(x, y, 'x, y')
          return {
            ...v,
            x,
            y
          }
        })
        graphChartOption.series[0].data = [leftTopData, rightBottomData, centerData, ...calcedCircleNodes]
        graphChartOption.series[0].links = this.pairwise([...agencyPeerList])
        this.initGraphChart()
      }
    },
    pairwise(arr) {
      const data = []
      for (let i = 0; i < arr.length; i++) {
        for (let j = i + 1; j < arr.length; j++) {
          data.push({
            source: arr[i],
            target: arr[j],
            label: {
              show: false
            },
            lineStyle: {
              curveness: -0.2,
              color: '#60a9ff',
              type: 'dotted'
            }
          })
          data.push({
            source: arr[j],
            target: arr[i],
            label: {
              show: false
            },
            lineStyle: {
              curveness: 0.2,
              color: '#60a9ff',
              type: 'dotted'
            }
          })
        }
      }
      return data
    },
    getCenterPos() {
      const maxX = this.$refs.main.clientWidth
      const maxY = this.$refs.main.clientHeight
      return {
        x: maxX * 0.5,
        y: maxY * 0.5,
        maxX,
        maxY
      }
    },
    circledNodesPosition({ x, y }, index, nodesLen, radius = 300) {
      const avd = 360 / nodesLen
      const ahd = (avd * Math.PI) / 180
      return {
        x: Math.sin(ahd * index) * radius + x,
        y: Math.cos(ahd * index) * radius + y
      }
    },
    goDataDetail(data) {
      const { datasetId } = data
      this.$router.push({ path: 'dataDetail', query: { datasetId } })
    },
    goDataListPage() {
      this.$router.push({ path: 'dataManage' })
    }
  },
  destroyed() {
    this.getTimeTimer && clearInterval(this.getTimeTimer)
    this.getDataTimer && clearInterval(this.getDataTimer)
  }
}
</script>

<style lang="less" scoped>
div.screen {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  background-color: rgba(17, 17, 17);
  color: white;
  padding: 0 24px;
  div.top {
    display: flex;
    width: 100%;
    height: 64px;
    text-align: center;
    line-height: 64px;
    div.back {
      width: 30%;
      text-align: left;
      display: flex;
      align-items: center;
      img {
        cursor: pointer;
        width: 84px;
        height: auto;
      }
    }
    div.time {
      width: 30%;
      color: #95989d;
      font-size: 16px;
      p {
        width: 276px;
        text-align: left;
        float: right;
      }
    }
    div.title {
      width: 40%;
      font-size: 26px;
      font-weight: 500;
      background: linear-gradient(90deg, rgba(35, 40, 52, 0) 12.37%, rgba(103, 118, 154, 0.3) 49.53%, rgba(35, 40, 52, 0) 86.69%);
    }
  }
  div.bottom {
    height: calc(100% - 128px);
    width: 100%;
    display: flex;
    background: url('~Assets/images/bg_screen.jpg');
    background-size: cover;
    div.slide-con {
      width: 30%;
      .title {
        height: 68px;
        display: flex;
        align-items: center;
        padding: 20px 0;
        margin-bottom: 28px;
        .img-container {
          flex: 1;
          img {
            width: 148px;
            height: auto;
          }
        }
        .el-progress {
          width: 160px;
          display: inline-block;
        }
        .el-progress__text {
          display: none;
        }
        span.ell.rate {
          margin-left: 12px;
          max-width: 160px;
        }
      }
      div.chart-container {
        height: calc(100% - 96px);
        width: 100%;
      }
      .data-table {
        ul {
          display: flex;
          justify-content: space-between;
          li {
            height: 34px;
            font-size: 14px;
            line-height: 34px;
            text-align: center;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            width: 33%;
          }
          li.type {
            text-align: left;
            text-indent: 8px;
            span {
              display: inline-block;
              width: 6px;
              height: 6px;
              border-radius: 50%;
              transform: translateY(-2px);
              margin-right: 4px;
            }
          }
        }
        .head {
          background-color: rgba(38, 43, 55);
        }
      }
      .chart-con {
        width: 100%;
        height: calc(33%);
        padding-bottom: 45px;
      }
      .bar-chart-con {
        padding-bottom: 20px;
      }
      .line-chart {
        padding-bottom: 0;
        p {
          text-align: center;
          color: #edf0f6;
          font-size: 14px;
          margin-bottom: 14px;
        }
        div {
          height: calc(100% - 28px);
        }
      }
      .circle {
        display: flex;
        padding-bottom: 10px;
        div.data-table {
          width: 60%;
          background-color: rgba(17, 17, 17);
        }
        #circle-chart {
          width: 40%;
          height: 100%;
        }
        #job-circle-chart {
          width: 100%;
          height: 100%;
        }
      }
    }
    .chart {
      height: 100%;
      width: 100%;
    }
    div.center {
      width: 40%;
      height: 100%;
      padding: 0 24px;
      position: relative;
      #graph-chart {
        height: calc(100% - 134px);
      }
      .agency-info {
        position: absolute;
        left: 44px;
        top: 32px;
        p {
          color: #95989d;
          span {
            display: inline-block;
            background: #467de8;
            width: 6px;
            height: 6px;
            border-radius: 50%;
            transform: translateY(-2px);
            margin-right: 4px;
          }
        }
        p.error span {
          background-color: #ff4d4f;
        }
      }
      .notice-con {
        height: 133px;
        display: flex;
        justify-content: space-between;
        div.msg-table {
          width: calc(50% - 13px);
          overflow: hidden;
          ul {
            display: flex;
            justify-content: space-between;
            padding: 0 10px;
            background: rgb(14, 14, 14);
            li {
              height: 34px;
              font-size: 14px;
              line-height: 34px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
            li.msg {
              flex: 1;
              padding-right: 10px;
              text-align: left;
            }
            li.time {
              width: auto;
            }
          }
          .head {
            background-color: rgba(38, 43, 55);
          }
          .empty {
            padding-top: 35px;
            text-align: center;
            color: #95989d;
            background-color: #0e0e0e;
            height: 100%;
            box-sizing: border-box;
          }
        }
      }
    }
  }
  div.copyRight {
    height: 64px;
    width: 100%;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    img {
      width: 124px;
      height: auto;
    }
  }
}
</style>
