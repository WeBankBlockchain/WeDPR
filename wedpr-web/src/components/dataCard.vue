<template>
  <div :class="dataInfo.isOwner ? 'data-card' : 'others data-card'">
    <div class="title">
      <img src="~Assets/images/icon_data.png" alt="" />
      <span :title="dataInfo.datasetTitle">{{ dataInfo.datasetTitle }}</span>
      <!-- <div class="tag" v-if="dataInfo.isOwner && showStatus">{{ dataInfo.statusDesc }}</div> -->
      <el-checkbox v-if="dataInfo.showSelect" @change="handleSelect" :value="selected"></el-checkbox>
      <span class="auth" v-if="!dataInfo.isOwner && dataInfo.permissions.usable">已授权</span>
    </div>
    <ul @click="goDetail">
      <li>
        数据量<span class="data-size">
          <i>{{ dataInfo.recordCount }}</i
          >*<i>{{ dataInfo.columnCount }}</i></span
        >
      </li>
      <li>
        数据属主 <span title="dataInfo.ownerUserName">{{ dataInfo.ownerUserName }}</span>
      </li>
      <li>
        所属机构 <span title="dataInfo.ownerAgencyName">{{ dataInfo.ownerAgencyName }}</span>
      </li>
      <li>
        数据来源 <span title="dataInfo.ownerAgencyName">{{ dataInfo.dataSourceType }}</span>
      </li>
    </ul>
    <div class="edit" v-if="dataInfo.permissions && showEdit && dataInfo.status !== 2">
      <div class="op-con" v-if="dataInfo.isOwner">
        <img v-if="dataInfo.permissions.writable" src="~Assets/images/icon_edit.png" alt="" @click.stop="modifyData(dataInfo)" />
        <img v-if="dataInfo.permissions.usable" src="~Assets/images/icon_download.png" alt="" @click.stop="downLoadData(dataInfo)" />
        <img @click.stop="deleteData" v-if="dataInfo.permissions.writable" src="~Assets/images/icon_delete.png" alt="" />
      </div>
      <div class="apply" @click.stop="applyData" v-if="!dataInfo.permissions.usable">
        <span><img src="~Assets/images/apply.png" alt="" />申请使用</span>
      </div>
      <div class="apply authed" @click.stop="applyData" v-if="!dataInfo.isOwner && dataInfo.permissions.usable">
        <span><img src="~Assets/images/apply_disabled.png" alt="" />申请使用</span>
      </div>
    </div>
    <div class="edit" v-if="dataInfo.status === 2 && dataInfo.isOwner">
      <div class="apply" @click.stop="reUpload(dataInfo)">
        <span><img src="~Assets/images/upload.png" alt="" />重新上传</span>
      </div>
    </div>
  </div>
</template>

<script>
import { downloadLargeFile } from 'Mixin/downloadLargeFile.js'
export default {
  name: 'dataCard',
  mixins: [downloadLargeFile],
  props: {
    showEdit: {
      type: Boolean,
      default: true
    },
    showTags: {
      type: Boolean,
      default: true
    },
    showStatus: {
      type: Boolean,
      default: false
    },
    dataInfo: {
      type: Object,
      default: () => {}
    },
    selected: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // checked: false
    }
  },
  methods: {
    applyData() {
      this.$emit('dataApply')
    },
    goDetail() {
      if (!this.dataInfo.status) {
        this.$emit('getDetail')
      }
    },
    reUpload(data) {
      const { datasetId } = data
      this.$router.push({ path: 'dataCreate', query: { datasetId, type: 'reUpload' } })
    },
    deleteData() {
      this.$emit('deleteData')
    },
    handleSelect(checked) {
      this.$emit('selected', checked)
    },
    downFile(url) {
      const a = document.createElement('a')
      a.style.display = 'none'
      a.download = 'download'
      a.href = url
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
    },
    downLoadData(data) {
      const { datasetStoragePath = '', datasetTitle, dataSourceType } = data
      try {
        const { filePath = '' } = JSON.parse(datasetStoragePath)
        let fileName = datasetTitle
        switch (dataSourceType) {
          case 'CSV':
            fileName += '.csv'
            break
          case 'EXCEL':
            fileName += '.xlsx'
            break
          default:
            return
        }
        this.downloadLargeFile({ filePath }, fileName)
      } catch (error) {
        this.$message.error('存储路径解析失败')
      }
    },
    modifyData(data) {
      const { datasetId } = data
      this.$router.push({ path: 'modifyData', query: { datasetId } })
    }
  }
}
</script>

<style scoped lang="less">
div.data-card {
  background: #f6fcf9;
  // width: 314px;
  max-height: 256px;
  border: 1px solid #e0e4ed;
  border-radius: 12px;
  margin: 16px;
  width: calc(25% - 32px);
  box-sizing: border-box;
  min-width: 220px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  float: left;
  ::v-deep .el-checkbox__inner {
    border-radius: 50%;
    width: 20px;
    height: 20px;
    line-height: 20px;
    font-size: 16px;
  }
  ::v-deep .el-checkbox__inner::after {
    left: 7px;
    width: 4px;
    height: 8px;
    top: 3px;
  }
  div.title {
    font-size: 16px;
    line-height: 24px;
    font-family: PingFang SC;
    display: flex;
    align-items: center;
    margin-bottom: 24px;
    color: #262a32;
    img {
      width: 24px;
      height: 24px;
    }
    span {
      text-align: left;
      flex: 1;
      font-weight: bold;
      overflow: hidden;
      text-overflow: ellipsis;
      text-indent: 8px;
      white-space: nowrap;
    }
    span.auth {
      position: absolute;
      right: 0;
      top: 0;
      background: #52b81f;
      color: white;
      padding: 2px 6px;
      font-size: 12px;
      line-height: 16px;
      border-radius: 4px;
    }
  }
  ul {
    li {
      font-size: 12px;
      line-height: 24px;
      margin-bottom: 6px;
      color: #787b84;
      display: flex;
      span {
        flex: 1;
        text-align: right;
        color: #262a32;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding-left: 5px;
      }
      span.data-size {
        i {
          font-size: 12px;
          font-style: normal;
        }
      }
    }
    li:last-child {
      margin-bottom: 0;
    }
  }
  div.edit {
    margin-top: 28px;
  }
  .op-con {
    display: flex;
    justify-content: space-between;
    height: auto;
    img {
      width: 24px;
      height: 24px;
      cursor: pointer;
    }
  }
  div.apply {
    border: 1px solid #b3b5b9;
    margin-bottom: -4px;
    margin-top: -4px;
    height: 32px;
    padding: 5px 12px;
    text-align: center;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    justify-content: center;
    span {
      display: flex;
      align-items: center;
    }
    img {
      width: 16px;
      height: 16px;
      margin-right: 8px;
    }
  }
  div.apply.authed {
    cursor: default;
    border: 1px solid #e0e4ed;
    color: #b3b5b9;
  }
  div.tag {
    width: 52px;
    height: 20px;
    line-height: 20px;
    font-size: 12px;
    background-color: #52b81f;
    color: white;
    border-top-right-radius: 4px;
    border-bottom-left-radius: 4px;
    text-align: center;
    margin-right: 12px;
    margin-left: 6px;
  }
}
div.data-card.others {
  background: #f6f8fc;
  border: 1px solid #e0e4ed;
}
div.data-card:hover {
  box-shadow: 0px 2px 10px 2px #00000014;
  cursor: pointer;
}
</style>
