<template>
  <div class="empty" :style="{ height: emptyHeight + 'px' }">
    <div class="img-con">
      <img src="~Assets/images/pic_empty_news.png" />
      <p>暂无数据</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'emptyCom',
  props: {
    emptyHeight: {
      type: Number,
      default: 120
    }
  },
  data() {
    return {
      // checked: false
    }
  },
  methods: {}
}
</script>

<style scoped lang="less">
div.empty {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  justify-content: center;
  .img-con {
    img {
      margin-bottom: 24px;
      width: 120px;
      height: auto;
    }
    p {
      font-size: 14px;
      color: #b3b5b9;
      text-align: center;
    }
  }
}
</style>
