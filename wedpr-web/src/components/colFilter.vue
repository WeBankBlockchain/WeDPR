<template>
  <el-popover>
    <i slot="reference" style="font-size: 20px; cursor: pointer" class="el-icon-setting" />
    <div class="tips">请选择展示列</div>
    <el-checkbox-group v-model="checkedCities" :min="1" @change="handleCheckedChange">
      <el-checkbox v-for="column in columns" :key="column.dataName" :label="column.dataName">
        {{ column.dataName }}
      </el-checkbox>
    </el-checkbox-group>
  </el-popover>
</template>

<script>
export default {
  name: 'ColFilter',
  props: {
    columns: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      checkedCities: [],
      orginalCol: []
    }
  },
  computed: {},
  created() {
    this.orginalCol = [...this.columns]
    this.checkedCities = this.columns.map((v) => v.dataName)
  },
  methods: {
    handleCheckedChange() {
      const columnsDataList = this.orginalCol.filter((v) => this.checkedCities.includes(v.dataName))
      this.$emit('handleCheckedChange', columnsDataList)
    }
  }
}
</script>
<style scoped>
.el-checkbox {
  display: block;
  padding: 4px;
}
div.tips {
  padding: 10px 6px;
  font-weight: bolder;
}
</style>
