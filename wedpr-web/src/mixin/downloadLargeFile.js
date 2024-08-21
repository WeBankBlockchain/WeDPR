import { dataManageServer } from 'Api'
export const downloadLargeFile = {
  data() {
    return {
      cipherLoadingDownload: null
    }
  },
  methods: {
    retryablePromise(apiCall, params, retryOptions) {
      return new Promise((resolve, reject) => {
        let retryCount = 0
        const retry = (error) => {
          if (retryCount < retryOptions.maxRetries) {
            retryCount++
            console.log(`重试接口调用，第 ${retryCount} 次尝试`)
            setTimeout(() => {
              apiCall(params).then(resolve).catch(retry)
            }, retryOptions.retryDelay)
          } else {
            reject(error)
          }
        }
        apiCall(params).then(resolve).catch(retry)
      })
    },
    downloadLargeFile(params, fileName) {
      this.cipherLoadingDownload = this.$loading({
        lock: true,
        text: '正在下载文件...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.2)'
      })
      dataManageServer
        .getFileShardsInfo(params)
        .then((res) => {
          if (res.code === 0 && res.data) {
            const { shardCount } = res.data
            this.getResultSlice({ ...params, shardCount }, fileName)
          }
        })
        .catch(() => {
          this.$message.error('数据下载失败')
          this.cipherLoadingDownload && this.cipherLoadingDownload.close()
        })
    },
    getJobsResultSliceData(params) {
      return new Promise((resolve, reject) => {
        dataManageServer
          .downloadFileShardData(params)
          .then((res) => {
            if (res) {
              resolve(res)
            } else {
              reject(new Error('获取文件分片失败'))
            }
          })
          .catch(() => {
            reject(new Error('获取文件分片失败'))
          })
      })
    },
    async getResultSlice(params, fileName) {
      const { shardCount, filePath } = params
      const blobData = []
      try {
        for (let i = 0; i < shardCount; i++) {
          const res = await this.retryablePromise(this.getJobsResultSliceData, { filePath, shardCount, shardIndex: i }, { maxRetries: 5, retryDelay: 2000 })
          if (res) {
            blobData.push(res)
          } else {
            throw new Error('分片获取失败')
          }
        }
      } catch (error) {
        // 捕获uncaught promise
        this.$message.error('数据下载失败')
        this.cipherLoadingDownload && this.cipherLoadingDownload.close()
        return
      }

      this.cipherLoadingDownload && this.cipherLoadingDownload.close()
      this.$message.success('数据下载成功')
      this.downloadDatacontent(blobData, fileName)
      //   this.deleteResultSlice({ jobId, type, shareCount })
    },
    // deleteResultSlice(params) {
    //   deleteJobsResultSlice(params).then((response) => {
    //     if (!handleFetchResponseMsgBox(this, response, false)) {
    //       return
    //     }
    //     console.log(response)
    //   })
    // },
    downloadDatacontent(content, fileName) {
      const a = document.createElement('a')
      const event = document.createEvent('MouseEvents')
      const blob = new Blob([...content], { type: 'text/csv' })
      a.href = URL.createObjectURL(blob)
      a.download = fileName
      event.initEvent('click', true, true)
      a.dispatchEvent(event)
    }
  }
}
