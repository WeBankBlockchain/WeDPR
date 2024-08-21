<template>
  <div class="info-container">
    <ul>
      <li>
        <span class="label">任务耗时：</span> <span class="info"> {{ jobStatusInfo.timeCostMs / 1000 }}s </span>
      </li>
      <li>
        <span class="label">结果文件：</span> <el-button type="text" @click="getLink('psi_result.json')">{{ 'psi_result.json' }}</el-button>
      </li>
    </ul>
  </div>
</template>

<script>
import { downloadLargeFile } from 'Mixin/downloadLargeFile.js'
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
    jobStatusInfo: {
      type: Object,
      default: () => {
        return {}
      }
    },
    resultFileInfo: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      jobResult: {},
      activeName: '任务结果'
    }
  },
  watch: {
    jobID() {
      this.handleResult()
    }
  },
  created() {
    this.handleResult()
  },
  methods: {
    handleResult() {
      this.jobResult = this.resultFileInfo
    },
    getLink(fileName) {
      const { path } = this.jobResult
      console.log(path, 'path')
      this.downloadLargeFile({ filePath: path }, fileName)
    }
  }
}
</script>

<style lang="scss" scoped>
ul {
  padding: 10px 0;
}
li {
  padding: 8px;
}
span.label {
  margin-right: 6px;
  font-size: 14px;
  line-height: 22px;
}
</style>
