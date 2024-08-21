<template>
  <div class="pag-con" v-if="total !== 0">
    <el-pagination
      background
      @size-change="sizeChange"
      @current-change="currentChange"
      :current-page="page_offset"
      :page-sizes="pageSizesOption"
      :page-size="page_size"
      :layout="layout"
      :total="total"
    >
    </el-pagination>
  </div>
</template>

<script>
export default {
  name: 'wePagination',
  props: {
    page_offset: {
      type: Number
    },
    pageSizesOption: {
      type: Array,
      default: () => [5, 10, 20, 30, 50]
    },
    page_size: {
      type: Number
    },
    layout: {
      type: String,
      default: 'total, sizes, prev, pager, next, jumper'
    },
    total: {
      type: Number
    }
  },
  methods: {
    sizeChange(page_size) {
      console.log(page_size, 'page_size')
      const { page_offset } = this
      this.$emit('paginationChange', { page_size, page_offset })
    },
    currentChange(page_offset) {
      const { page_size } = this
      this.$emit('paginationChange', { page_size, page_offset })
    }
  }
}
</script>

<style scoped lang="less">
div.pag-con {
  width: 100%;
  text-align: right;
  padding: 12px;
  padding-right: 0;
}
</style>
