<template>
  <div class="server-con" :key="serviceInfo">
    <div class="img-con">
      <img class="type" v-if="serviceInfo.serviceType === serviceTypeEnum.PIR" src="~Assets/images/icon_service1.png" alt="" />
      <img class="type" v-else src="~Assets/images/icon_service2.png" alt="" />

      <el-checkbox v-if="serviceInfo.showSelect" @change="handleSelect" :value="selected"></el-checkbox>
    </div>
    <span v-if="serviceInfo.isOnwer" class="auth" :style="{ backgroundColor: colorMap[serviceInfo.status] }">{{ servicePulishStatus[serviceInfo.status] }}</span>
    <span class="auth" v-if="!serviceInfo.isOnwer && serviceInfo.serviceAuthStatus === serviceAuthStatus.Authorized">已授权</span>
    <dl @click="getDetail(serviceInfo)">
      <dt class="ell" :title="serviceInfo.serviceName">
        {{ serviceInfo.serviceName }}
      </dt>
      <dd>
        发布用户：<span class="count">{{ serviceInfo.owner }}</span>
      </dd>
      <dd>
        发布机构：<span class="count">{{ serviceInfo.agency }}</span>
      </dd>
      <dd>
        服务类型：<span class="count">{{ serviceInfo.serviceType }}</span>
      </dd>
      <dd>
        创建时间：<span>{{ serviceInfo.createTime }}</span>
      </dd>
    </dl>
    <div class="edit" v-if="showEdit">
      <div class="op-con" v-if="serviceInfo.isOnwer">
        <img src="~Assets/images/icon_edit.png" alt="" @click.stop="modifyData(serviceInfo)" />
        <img @click.stop="deleteService(serviceInfo)" src="~Assets/images/icon_delete.png" alt="" />
      </div>
      <div v-if="serviceInfo.serviceAuthStatus !== serviceAuthStatus.Authorized && !serviceInfo.isOnwer" class="apply" @click.stop="applyData(serviceInfo)">
        <span>申请使用</span>
      </div>
      <div v-if="serviceInfo.serviceAuthStatus === serviceAuthStatus.Authorized && !serviceInfo.isOnwer" class="apply authed">
        <span>申请使用</span>
      </div>
    </div>
  </div>
</template>

<script>
import { serviceTypeEnum, serviceAuthStatus, servicePulishStatus } from 'Utils/constant.js'
import { mapGetters } from 'vuex'
export default {
  name: 'serviceCard',
  props: {
    serviceInfo: {
      type: Object,
      default: () => {}
    },
    selected: {
      type: Boolean,
      default: false
    },
    showEdit: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      serviceTypeEnum,
      serviceAuthStatus,
      servicePulishStatus,
      colorMap: {
        Publishing: '#3071F2',
        PublishSuccess: '#52B81F',
        PublishFailed: '#FF4D4F'
      }
    }
  },
  computed: {
    ...mapGetters(['userId', 'agencyId'])
  },
  methods: {
    handleSelect(checked) {
      this.$emit('selected', checked)
    },
    getDetail(data) {
      const { serviceId, type } = data
      this.$router.push({ path: '/serverDetail', query: { serviceId, serverType: type } })
    },
    applyData(item) {
      const serviceId = encodeURIComponent(item.serviceId)
      this.$router.push({ path: '/dataApply', query: { serviceId, applyType: 'wedpr_service_auth' } })
    },
    deleteService(data) {
      this.$emit('deleteService', data)
    },
    modifyData(data) {
      this.$emit('modifyData', data)
    }
  }
}
</script>

<style scoped lang="less">
div.server-con {
  border: 1px solid #e0e4ed;
  padding: 20px;
  box-sizing: border-box;
  border-radius: 12px;
  float: left;
  width: calc(25% - 32px);
  background-color: #f6f8fc;
  margin: 20px 16px;
  min-width: 240px;
  // max-height: 306px;
  // overflow: hidden;
  position: relative;
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
  }
  ::v-deep .el-checkbox__inner {
    border-radius: 50%;
    width: 20px;
    height: 20px;
    line-height: 20px;
    font-size: 16px;
    border: 1px solid #3071f2;
    box-shadow: 0 0 3px #3071f2;
  }
  ::v-deep .el-checkbox__inner::after {
    left: 7px;
    width: 4px;
    height: 8px;
    top: 3px;
  }
  span.auth {
    position: absolute;
    right: 0;
    top: 0;
    color: white;
    padding: 2px 6px;
    font-size: 12px;
    line-height: 16px;
    border-radius: 4px;
    background: #52b81f;
  }
  div.img-con {
    display: flex;
    justify-content: space-between;
    align-items: center;
    img.type {
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      float: left;
    }
  }

  dl {
    cursor: pointer;
    dt {
      font-size: 16px;
      line-height: 24px;
      color: #262a32;
      font-weight: bolder;
      margin-bottom: 16px;
      span {
        float: right;
      }
    }
    dd {
      font-size: 12px;
      line-height: 24px;
      margin-bottom: 4px;
      color: #787b84;
      span {
        float: right;
      }
    }
  }
  div.edit {
    margin-top: 28px;
    div.apply.authed {
      cursor: default;
      border: 1px solid #e0e4ed;
      color: #b3b5b9;
    }
  }
  .op-con {
    display: flex;
    justify-content: space-between;
    height: auto;
    img {
      width: 24px;
      height: 24px;
      cursor: pointer;
      margin-bottom: 0;
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
  }
}
div.server-con:hover {
  box-shadow: 0px 2px 10px 2px #00000014;
}
</style>
