<template>
  <div class="header-bar-box">
    <div @click="menuToggleClick" class="toggle-con">
      <el-icon v-if="isCollapse" class="icon el-icon-s-unfold" />
      <el-icon v-if="!isCollapse" class="icon el-icon-s-fold" />
    </div>
    <div class="logo-con">
      <img class="logo" src="~Assets/images/logo.png" alt="" />
    </div>
    <div class="top-con">
      <div class="header-bar">
        <div class="custom-content-con-box">
          <div class="custom-content-con">
            <div class="info-con">
              <el-dropdown>
                <img class="av" src="~Assets/images/avatar_male.png" />
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="showModifyPassword">修改密码</el-dropdown-item>
                  <el-dropdown-item @click.native="logOut">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              {{ userId }}
            </div>
          </div>
          <modifyPassword :userinfo="userinfo" :showModifyModal="showModifyModal" @closeModal="closeModal" @handlOK="handlOK" />
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { passwordHanle } from 'Mixin/passwordHandle.js'
import { SET_USERINFO, SET_AUTHORIZATION, SET_PERMISSION, SET_USERID, SET_AGENCYNAME, SET_AGENCYID, SET_FILEUPLOADTASK } from 'Store/mutation-types.js'
import { mapMutations, mapGetters } from 'vuex'
import modifyPassword from '../modifyPassword/index.vue'
export default {
  name: 'HeaderBar',
  mixins: [passwordHanle],
  components: {
    modifyPassword
  },
  data() {
    return {
      showModifyModal: false
    }
  },
  props: {
    isCollapse: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    ...mapGetters(['userinfo', 'userId'])
  },
  methods: {
    ...mapMutations([SET_USERINFO, SET_AUTHORIZATION, SET_PERMISSION, SET_USERID, SET_AGENCYNAME, SET_AGENCYID, SET_FILEUPLOADTASK]),
    logOut() {
      this.SET_USERID('')
      this.SET_AGENCYNAME('')
      this.SET_AGENCYID('')
      this.SET_PERMISSION([])
      this.SET_AUTHORIZATION('')
      this.SET_FILEUPLOADTASK(null)
      this.SET_USERINFO({})
      this.$router.push('/login')
    },
    showModifyPassword() {
      this.showModifyModal = true
    },
    closeModal() {
      this.showModifyModal = false
    },
    handlOK() {
      this.showModifyModal = false
      this.logOut()
      this.$router.push({ path: '/login' })
    },
    handleCollpasedChange() {},
    menuToggleClick() {
      this.$emit('menuToggleClick')
    },
    fullScreen() {
      this.$router.push({ path: 'screen' })
    }
  }
}
</script>
<style lang="less" scoped>
.header-bar-box {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  background-color: #f6f8fc;
  width: 100%;
  align-items: center;
  .logo-con {
    height: 100px;
    display: flex;
    align-items: center;
    img {
      width: auto;
      height: 22px;
      display: block;
      margin: 0 auto;
    }
  }
  div.toggle-con {
    display: flex;
    align-items: center;
    width: 64px;
    justify-content: center;
    .icon {
      font-size: 26px;
    }
  }
  div.top-con {
    padding: 0 50px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
  }
  .header-bar {
    flex: 1;
    display: flex;
    align-items: center;
    height: 100%;
    padding-left: 30px;
    .trans {
      transition: transform 0.2s ease;
    }
    @size: 40px;
    .sider-trigger-a {
      // padding: 6px;
      // width: @size;
      // height: @size;
      display: flex;
      align-items: center;
      text-align: center;
      color: #5c6b77;
      // margin-top: 12px;
      i {
        .trans;
        vertical-align: top;
      }
      &.collapsed i {
        transform: rotateZ(90deg);
        .trans;
      }
    }
  }
  .custom-content-con-box {
    position: absolute;
    right: 24px;
    top: 18px;
    .custom-content-con {
      display: flex;
    }
    .info-con {
      display: flex;
      align-items: center;
      .av {
        width: 32px;
        height: 32px;
        border-radius: 50px;
        margin-right: 8px;
      }
      .full {
        width: 86px;
        height: auto;
        cursor: pointer;
        margin-right: 24px;
      }
    }

    .btn {
      cursor: default;
    }
  }
}
.reset-dialog {
  .el-form-item--mini.el-form-item,
  .el-form-item--small.el-form-item {
    margin-bottom: 0;
  }
}
</style>
