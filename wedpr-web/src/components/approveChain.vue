<template>
  <div class="chain">
    <div class="user-con" v-for="(item, i) in approveChainList" :key="item.name + '_' + item.agency">
      <div :class="isCurrent(item) ? 'current user' : 'user'">
        {{ item.name }} <span v-if="item.agency"> ({{ item.agency }})</span>
        <span class="current" v-if="isCurrent(item)">审批中</span>
        <span v-if="item.deleteAble" @click="deleteUser(i)" class="close">x</span>
      </div>
      <div class="connect" v-if="showAdd && !item.addNextUserDisbaled">
        <span class="point"></span> <span class="line"></span>
        <el-popover v-model="item.visible" placement="top-start" width="363" trigger="click">
          <p style="margin-bottom: 20px">添加审批节点</p>
          <el-form :inline="false" :rules="userInfoRules" :model="addUserInfo" ref="addUserInfo" size="small">
            <el-form-item label-width="116px" label="本机构用户名:" prop="user">
              <el-select loading-text="搜索中" filterable style="width: 209px" v-model="addUserInfo.user" remote :remote-method="getUserNameSelect" placeholder="请选择">
                <el-option v-for="item in userNameSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
              </el-select>
            </el-form-item>
            <div style="text-align: right">
              <el-button @click="cancelAdd(item)">取消</el-button>
              <el-button type="primary" @click="addUserToChain(i, item)">确定</el-button>
            </div>
          </el-form>
          <img slot="reference" src="~Assets/images/add.png" alt="" />
        </el-popover>
        <span class="line"></span> <span class="point right"></span>
      </div>
      <div class="connect" v-else><span class="point"></span> <span class="line"></span> <span class="line"></span> <span class="point right"></span></div>
    </div>
  </div>
</template>

<script>
import { userSelect } from 'Mixin/userSelect.js'
import { mapGetters } from 'vuex'
export default {
  name: 'dataCard',
  mixins: [userSelect],
  props: {
    approveChainList: {
      type: Array,
      default: () => []
    },
    showAdd: {
      type: Boolean,
      default: false
    },
    currentApply: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      user: '',
      addUserInfo: {
        user: ''
      },
      userInfoRules: {
        agency: [{ required: true, message: '机构不能为空', trigger: 'blur' }],
        user: [{ required: true, trigger: 'blur', validator: this.userValidate }]
      },
      statusMap: {
        Agree: '已审批',
        Reject: '已拒绝'
      }
    }
  },
  computed: {
    ...mapGetters(['agencyId']),
    agencyList() {
      const data = this.approveChainList.map((v) => {
        return JSON.stringify({
          label: v.agency,
          value: v.agency
        })
      })
      return Array.from(new Set(data)).map((v) => JSON.parse(v))
    }
  },
  methods: {
    isCurrent(item) {
      console.log(this.currentApply)
      return item.agency + '_' + item.name === this.currentApply
    },
    userValidate(rule, value, callback) {
      if (value) {
        if (this.approveChainList.some((v) => v.name === value && v.agency === this.agencyId)) {
          return callback(new Error('审批人已存在当前审批链'))
        }
        callback()
      } else {
        return callback(new Error('请选择审批人'))
      }
    },
    addUserToChain(index, item) {
      console.log(this.$refs.addUserInfo)
      this.$refs.addUserInfo[0].validate((valid) => {
        if (valid) {
          const { user } = this.addUserInfo
          const data = [...this.approveChainList]
          data.splice(index + 1, 0, { agency: this.agencyId, name: user, deleteAble: true, visible: false })
          this.$emit('addUserToChain', data)
          this.user = ''
          item.visible = false
        }
      })
    },
    deleteUser(index) {
      const data = [...this.approveChainList]
      data.splice(index, 1)
      this.$emit('deleteUser', data)
    },
    cancelAdd(item) {
      item.visible = false
    }
  }
}
</script>

<style scoped lang="less">
.chain {
  display: flex;
  flex-wrap: wrap;
  .user-con {
    display: flex;
    align-items: center;
    span.close {
      position: absolute;
      right: 8px;
      top: 0px;
      color: #3071f2b2;
      cursor: pointer;
      font-size: 16px;
    }
    .user {
      padding: 22px 15px 22px 15px;
      border-radius: 12px;
      border: 1px dashed #3071f2b2;
      font-size: 14px;
      line-height: 22px;
      text-align: center;
      margin-bottom: 12px;
      position: relative;
    }
    .user.current {
      border: 2px solid #3071f2b2;
    }
    .connect {
      display: flex;
      align-items: center;
      transform: translateY(-4px);
      img {
        width: 14px;
        height: 14px;
        transform: translateY(2px);
      }
      span.line {
        width: 12px;
        height: 0;
        border-bottom: 1px dashed #3071f2b2;
      }
      span.point {
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background-color: #3071f2b2;
        transform: translateX(-2px);
      }
      span.point.right {
        transform: translateX(2px);
      }
    }
  }
  .user-con:last-child {
    .connect {
      display: none;
    }
  }
  span.current {
    color: #3071f2b2;
  }
}
</style>
