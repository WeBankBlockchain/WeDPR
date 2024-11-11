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
import { Popover, CheckboxGroup, Checkbox } from 'element-ui'
import 'element-ui/lib/theme-chalk/popover.css'
import 'element-ui/lib/theme-chalk/icon.css'
import 'element-ui/lib/theme-chalk/pagination.css'
import 'element-ui/lib/theme-chalk/checkbox-group.css'
import 'element-ui/lib/theme-chalk/checkbox-button.css'
import 'element-ui/lib/theme-chalk/checkbox.css'
export default {
  name: 'ColFilter',
  components: {
    'el-popover': Popover,
    'el-checkbox-group': CheckboxGroup,
    'el-checkbox': Checkbox
  },
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
