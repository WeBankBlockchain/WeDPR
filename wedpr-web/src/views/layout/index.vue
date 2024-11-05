<template>
  <el-container class="layout-contaioner">
    <el-header height="64px"><HeaderTop @menuToggleClick="menuToggleClick" :isCollapse="isCollapse" /></el-header>
    <el-container class="main-container">
      <el-aside :width="isCollapse ? '68px' : '240px'">
        <MenuLeft :isCollapse="isCollapse" />
        <div class="guide" v-if="!isCollapse && !hiddenFeed">
          <img class="close" @click="closeFeed" src="~Assets/images/icon_close.png" alt="" />
          <img src="~Assets/images/guide.png" alt="" />
          <el-button type="primary" size="small" @click="feed">点击反馈</el-button>
        </div>
      </el-aside>
      <el-main class="elmain">
        <div class="elmain-container" v-if="!isHome">
          <div class="bread-con">
            <el-breadcrumb separator="/" separator-class="el-icon-arrow-right">
              <el-breadcrumb-item :key="item.text" v-for="item in bread" :to="item.link || ''">{{ item.text }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="scroll" v-if="!isHome">
            <router-view v-slot="{ Component }">
              <transition name="fade-transform" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </div>
        <router-view v-slot="{ Component }" v-if="isHome">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import HeaderTop from './headerTop'
import MenuLeft from './menuLeft'
import { mapGetters } from 'vuex'
export default {
  components: {
    HeaderTop,
    MenuLeft
  },
  data() {
    return {
      isCollapse: false,
      isHome: false,
      hiddenFeed: false
    }
  },
  created() {
    this.isHome = this.$route.name === 'home'
  },
  computed: {
    ...mapGetters(['permission', 'bread', 'userinfo'])
  },
  methods: {
    menuToggleClick() {
      this.isCollapse = !this.isCollapse
    },
    feed() {
      // this.$router.push({ path: 'feed' })
      window.open('https://wj.qq.com/s2/15263371/ce6c/')
    },
    closeFeed() {
      this.hiddenFeed = true
    }
  },
  watch: {
    $route(to) {
      const { name } = to
      this.isHome = name === 'home'
    }
  }
}
</script>
<style lang="less" scoped>
.layout-contaioner {
  height: 100%;
  .el-aside {
    padding: 0 10px;
    box-sizing: border-box;
  }
  .main-container {
    height: 100%;
    overflow: auto;
  }

  .elmain {
    height: 100%;
    padding: 0;
    padding-bottom: 24px;
    padding-right: 24px;
    background-color: #f6f8fc;
    .elmain-container {
      padding: 30px 0;
      background-color: white;
      border-radius: 24px;
      overflow-y: auto;
      height: 100%;
      min-width: 960px;
      overflow-x: auto;
      position: relative;
      .bread-con {
        height: 50px;
        line-height: 20px;
        padding-left: 20px;
      }
      div.scroll {
        height: calc(100% - 50px);
        overflow-y: auto;
        padding: 0 20px;
        box-sizing: border-box;
      }
    }
  }
  .el-menu {
    border-right: none;
  }
  .el-header {
    padding: 0;
  }
  div.guide {
    text-align: center;
    position: absolute;
    bottom: 32px;
    left: 0;
    width: 100%;
    padding: 0 24px;
    box-sizing: border-box;
    .close {
      position: absolute;
      right: 24px;
      top: -16px;
      width: 16px;
      height: 16px;
      cursor: pointer;
    }
    img {
      width: 120px;
      height: 120px;
      display: block;
      margin: 0 auto;
      margin-bottom: 12px;
    }
  }
}
</style>
