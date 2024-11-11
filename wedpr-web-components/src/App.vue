<template>
  <div id="app">
    <formCard title="详情信息">
      <el-table ref="FEPreviewData" :data="tableData" max-height="385" border fit stripe>
        <el-table-column :key="item.dataName" v-for="item in columnsShow" :show-overflow-tooltip="true" :prop="item.dataName" :label="item.dataName" />
        <el-table-column fixed="left" width="80" align="center" type="index">
          <template slot="header">
            <colFilter v-if="columnsOrigin.length" :columns="columnsOrigin" @handleCheckedChange="(data) => handleCheckedChange(data)" />
          </template>
        </el-table-column>
      </el-table>
      <wePagination
        :pageSizesOption="[10, 20, 30, 40, 50]"
        :total="total"
        :page_offset="pageData.page_offset"
        :page_size="pageData.page_size"
        @paginationChange="paginationHandle"
      ></wePagination>
    </formCard>
  </div>
</template>

<script>
// import wePagination from '../packages/wePagination/src/index.vue'
// import formCard from '../packages/formCard/src/index.vue'
export default {
  name: 'App',
  // components: {
  //   // wePagination,
  //   formCard
  //   // wePagination
  // },
  data() {
    return {
      total: 10,
      pageData: {
        page_offset: 1,
        page_size: 10
      },
      columnsOrigin: [
        {
          dataItem: 'age',
          dataName: 'age'
        },
        {
          dataItem: 'name',
          dataName: 'name'
        },
        {
          dataItem: 'sex',
          dataName: 'sex'
        }
      ],
      columnsShow: [
        {
          dataItem: 'age',
          dataName: 'age'
        },
        {
          dataItem: 'name',
          dataName: 'name'
        },
        {
          dataItem: 'sex',
          dataName: 'sex'
        }
      ],
      tableData: [
        {
          name: '张三',
          age: 18,
          sex: 'male'
        },
        {
          name: '李四',
          age: 28,
          sex: 'male'
        }
      ]
    }
  },
  methods: {
    paginationHandle(pageData) {
      console.log(pageData)
    },
    handleCheckedChange(data) {
      this.columnsShow = [...data]
    }
  }
}
</script>

<style lang="less">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
