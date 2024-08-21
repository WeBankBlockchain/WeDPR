<template>
  <el-upload
    class="upload-area"
    :multiple="true"
    :action="uploadUrl"
    :headers="headers"
    :on-remove="onRemove"
    :on-success="handleSuccess"
    :on-error="handleError"
    :before-upload="beforeUpload"
    :on-change="handleChange"
    :on-exceed="handleExceed"
    ref="upload"
    :beforeUpload="beforeUploadFileAndVideo"
    :limit="limitNumber"
    :file-list="fileList"
    accept=".pdf,.mp4"
  >
    <el-button size="small" type="primary">点击上传</el-button>
    <div slot="tip" class="el-upload__tip">仅支持上传pdf/mp4文件，附件大小不超过10M(doc,excel和ppt文件可以另存为pdf文件上传)</div>
  </el-upload>
</template>

<script>
import { uploadUrl } from 'Utils/config.js'
import { mapGetters } from 'vuex'
export default {
  name: 'weUpload',
  model: {
    prop: 'value'
  },
  props: {
    value: {
      type: String,
      default: ''
    },
    tips: {
      type: String,
      default: ''
    },
    beforeUpload: {
      type: Function,
      default: () => {}
    },
    limit: {
      type: Number,
      default: 0
    }
  },
  data () {
    return {
      uploadUrl,
      fileList: [],
      limitNumber: 10
    }
  },
  created () {
    console.log('enter', this.value)
    this.init()
  },
  methods: {
    init () {
      if (this.value) {
        const fileListData = this.value.split(';')
        this.fileList = fileListData.map((v) => {
          const [name, url] = v.split(',')
          return {
            name,
            url
          }
        })
      } else {
        this.fileList = []
      }
    },
    handleChange (file, fileList) {
      console.log('change', fileList)
      this.fileList = fileList.filter((v) => !(v.response && (v.response.code !== '0' || !v.response.data)))
      const fileListArray = this.fileList.map((fileData) => {
        const { name, url } = fileData
        if (name && url) {
          return name + ',' + url
        }
        if (fileData && fileData.response && fileData.response.data) {
          const { file_name, file_url } = fileData.response.data
          return file_name + ',' + file_url
        }
        return ''
      })
      const dataStr = fileListArray.length ? fileListArray.join(';') : ''
      this.$emit('input', dataStr)
    },
    handleSuccess (res, file, fileList) {
      const { code } = res
      if (code !== '0') {
        console.log(res, file, fileList, 'fail')
        this.$message.error(res.msg || '上传资源失败')
      }
    },
    // remove 不触发onchange
    onRemove (file, fileList) {
      const fileListData = fileList && fileList.length ? fileList : []
      const fileListArray = fileListData.map((fileData) => {
        const { name, url } = fileData
        return name + ',' + url
      })
      this.$emit('input', fileListArray.length ? fileListArray.join(';') : '')
    },
    beforeUploadFileAndVideo (file) {
      const fileName = file.name
      const fileType = file.name.substring(file.name.lastIndexOf('.') + 1)
      const supportType = ['pdf', 'mp4']
      const isSupport = supportType.includes(fileType)
      const isLt10M = file.size / 1024 / 1024 < 10
      const isNull = file.size === 0
      const fileNameHasComma = fileName.indexOf(',') > -1
      if (fileNameHasComma) {
        this.$message.error('活动附件名称不能包含","，请修改名称！')
        this.init()
        return false
      }
      if (!isSupport) {
        this.$message.error('上传活动附件格式不支持!')
        this.init()
        return false
      }
      if (!isLt10M) {
        this.$message.error('上传活动附件大小不能超过 10MB!')
        this.init()
        return false
      }
      if (isNull) {
        this.$message.error('附件内容不能为空!')
        this.init()
        return false
      }
      return true
    },
    handleError (error) {
      this.$message.error(error.msg || '上传资源失败')
    },
    handleExceed (files, fileList) {
      this.$message.error(`当前限制选择${this.limitNumber}个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`)
    }
  },
  computed: {
    ...mapGetters(['authorization']),
    headers () {
      return {
        Authorization: this.authorization
      }
    }
  }
}
</script>

<style scoped lang="less">
::v-deep .el-upload-list {
  width: 400px;
}
::v-deep .el-upload-list__item.is-success .el-upload-list__item-status-label {
  right: -55px;
  display: none;
}
::v-deep .el-upload-list__item .el-icon-close {
  display: inline-block;
}
::v-deep .el-icon-close:before {
  content: '删除';
  color: #777;
  position: absolute;
  right: -40px;
}
::v-deep .el-icon-close-tip {
  display: none;
  visibility: hidden;
}
</style>
