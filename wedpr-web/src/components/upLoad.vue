<template>
  <div>
    <el-upload
      v-if="!fileList.length"
      action=""
      :before-upload="beforeUpload"
      class="upload-area"
      :accept="accept"
      :drag="drag"
      :http-request="uploadCsvHandler"
      :file-list="fileList"
      :multiple="false"
    >
      <el-button v-if="drag" type="text" size="small" class="dragtips"><img src="~Assets/images/upload.png" alt="" /> {{ tips }}</el-button>
      <el-button v-else size="small" type="primary">上传附件</el-button>
    </el-upload>
    <el-upload v-if="fileList.length" action="" class="upload-demo" :before-upload="beforeUpload" :on-remove="onRemove" :http-request="uploadCsvHandler" :file-list="fileList">
      <el-button size="small" type="primary">重新上传</el-button>
    </el-upload>
  </div>
</template>

<script>
export default {
  name: 'weUpload',
  model: {
    prop: 'value'
  },
  props: {
    value: {
      type: Object,
      default: () => {
        return null
      }
    },
    drag: {
      type: Boolean,
      default: true
    },
    tips: {
      type: String,
      default: ''
    },
    accept: {
      type: String,
      default: ''
    },
    beforeUpload: {
      type: Function,
      default: () => {}
    }
  },
  data() {
    return {
      fileList: []
    }
  },
  created() {
    // this.init()
  },
  methods: {
    // form 表单双向绑定
    uploadCsvHandler({ file }) {
      this.$emit('input', file)
    },
    init() {
      // 回显初始化
      // if (this.value) {
      //   const [name, url] = this.value.split(',')
      //   this.fileList = [{ name, url }]
      // }
    },
    onRemove() {
      console.log(this.fileList)
      this.$emit('input', null)
    }
  },
  watch: {
    value(newValue) {
      console.log(newValue)
      if (newValue) {
        const { name } = newValue
        this.fileList = [{ name, newValue }]
      } else {
        this.fileList = []
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
::v-deep .el-upload-dragger {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 480px;
  .dragtips {
    color: #262a32;
    img {
      width: 16px;
      height: 16px;
      transform: translateY(4px);
    }
  }
}
</style>
