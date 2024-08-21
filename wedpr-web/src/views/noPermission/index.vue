<template>
  <div class="errPage-container">
    <el-row>
      <el-col :span="12">
        <h1 class="text-jumbo">Oops!</h1>
        <h2 style="text-align: center; margin: 20px 0">您没有权限访问该页面</h2>
        <div @click="backLogin" class="apply-auth">
          <a>返回登录页</a>
        </div>
      </el-col>
      <el-col :span="12">
        <img src="~Assets/imgs/401.gif" width="313" height="428" alt="401" />
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  methods: {
    backLogin () {
      this.$router.push('/login')
    }
  }
}
</script>

<style lang="less" scoped>
.errPage-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  .text-jumbo {
    font-size: 60px;
    font-weight: 700;
    color: #484848;
    text-align: center;
  }
  .apply-auth {
    display: flex;
    justify-content: center;
    align-items: center;
    a {
      text-decoration: none;
      display: block;
      width: 110px;
      height: 36px;
      line-height: 36px;
      background: #1482f0;
      border-radius: 10px;
      text-align: center;
      color: #ffffff;
    }
  }
}
</style>
