<template>
  <div class="form-card">
    <span class="card-title">{{ title }}</span>
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: 'formCard',
  props: {
    title: {
      type: String
    }
  },
  methods: {}
}
</script>

<style scoped lang="less">
div.form-card {
  width: 100%;
  position: relative;
  border-radius: 12px;
  border: 1px solid #e0e4ed;
  padding: 32px 20px;
  margin-top: 10px;
  height: auto;
  margin-bottom: 32px;
  display: inline-block;
  span.card-title {
    position: absolute;
    font-size: 16px;
    line-height: 24px;
    padding: 0 8px;
    background-color: white;
    top: -12px;
    left: 10px;
    font-weight: 500;
    color: #262a32;
    z-index: 88;
  }
}
</style>
