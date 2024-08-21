import { dataManageServer } from 'Api'
import SparkMD5 from 'spark-md5'
import { Message } from 'element-ui'
import { mapMutations, mapGetters } from 'vuex'
import { SET_FILEUPLOADTASK } from 'Store/mutation-types.js'
const DefualtChunkSize = 0.5 * 1024 * 1024
const maxPoorLength = 3
let pendingDatasetId = ''
export const uploadFile = {
  data() {
    return {}
  },
  computed: {
    ...mapGetters(['fileUploadTask'])
  },
  methods: {
    ...mapMutations([SET_FILEUPLOADTASK]),
    handleFile(datasetParams) {
      const that = this
      const { userData: file, datasetId, onFail } = datasetParams
      pendingDatasetId = datasetId
      const { chunkList, chunkMount } = this.getChunkList(file)
      const spark = new SparkMD5.ArrayBuffer()
      const fileReader = new FileReader()
      let chunkIndex = 0
      fileReader.onload = function (e) {
        const chunkDataBuffer = e.target.result
        spark.append(chunkDataBuffer)
        chunkIndex++
        const chunkBlob = chunkList[chunkIndex]
        if (chunkIndex < chunkMount) {
          fileReader.readAsArrayBuffer(chunkBlob)
        } else {
          const identifier = spark.end()
          console.info('finished computed hash', identifier, chunkList)
          const totalCount = chunkList.length
          const reqList = chunkList.map((filesChunk, index) => {
            return {
              identifier,
              index,
              filesChunk,
              totalCount
            }
          })
          that.limitRequest(reqList, maxPoorLength, identifier, datasetParams)
        }
      }

      fileReader.onerror = function () {
        onFail && onFail()
        console.warn('oops, something went wrong.')
      }
      fileReader.readAsArrayBuffer(chunkList[chunkIndex])
    },
    getChunkList(file) {
      console.log(file, 'file')
      const chunkMount = Math.ceil(file.size / DefualtChunkSize)
      const chunkList = []
      const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
      for (let i = 0; i < chunkMount; i++) {
        const start = i * DefualtChunkSize
        const end = start + DefualtChunkSize >= file.size ? file.size : start + DefualtChunkSize
        chunkList.push(blobSlice.call(file, start, end))
      }
      return { chunkList, chunkMount }
    },

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

    uploadChunkData(params) {
      const { identifier, index, filesChunk, totalCount } = params
      const form = new FormData()
      form.append('filesChunk', filesChunk) // 切片流
      form.append('totalCount', totalCount) // 总片数
      form.append('index', index) // 当前是第几片
      form.append('identifier', identifier)
      form.append('datasetId', pendingDatasetId)
      return new Promise((resolve, reject) => {
        dataManageServer
          .uploadChunkData(form)
          .then((res) => {
            if (res.code === 0) {
              resolve()
            } else {
              reject(new Error('upload chunk Failed'))
            }
          })
          .catch(() => {
            reject(new Error('upload chunk Failed'))
          })
      })
    },
    limitRequest(requests, limit, identifier, datasetParams) {
      const { onSuccess, onFail } = datasetParams
      const pool = [] // 正处于请求中的函数
      let index = 0 // 待请求函数数组的索引
      const that = this
      const requestQueue = Math.min(requests.length, limit) // 判断请求队列和个数的大小

      for (let key = 0; key < requestQueue; key++) {
        pool.push(run(requests[index++]))
      }

      function awaitRequest() {
        const percentage = parseInt((index / requests.length) * 100)
        that.SET_FILEUPLOADTASK({ ...that.fileUploadTask, percentage })
        console.log(percentage, 'percentage')
        if (index === requests.length) {
          console.log('所有请求已进入promise.all')
          return Promise.resolve()
        }
        return run(requests[index++])
      }

      function cancelRequest() {
        return Promise.reject(new Error('upload chunk Failed'))
      }

      function run(params) {
        return that.retryablePromise(that.uploadChunkData, params, { maxRetries: 5, retryDelay: 2000 }).then(awaitRequest).catch(cancelRequest) // 每完成一个请求，就执行resolve回调，从而向请求队列中添加新的请求
      }
      Promise.all(pool)
        .then((res) => {
          console.log(res)
          dataManageServer
            .mergeChunkData({
              identifier,
              totalCount: requests.length,
              datasetId: pendingDatasetId
            })
            .then((response) => {
              if (response.code === 0) {
                // Message.success('上传文件成功')
                onSuccess && onSuccess()
              } else {
                // Message.error('上传文件失败！')
                onFail && onFail()
              }
            })
            .catch((error) => {
              // Message.error('上传文件失败！')
              console.log(error)
              onFail && onFail()
            })
        })
        .catch((err) => {
          onFail && onFail()
          Message.error('上传文件失败！')
          console.log(err)
        })
    }
  }
}
