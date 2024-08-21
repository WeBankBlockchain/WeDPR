<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>
<script>
import { mapGetters, mapMutations } from 'vuex'
import { SET_BREAD, SET_PBKEY } from 'Store/mutation-types.js'
import { settingManageServer } from 'Api'
export default {
  name: 'appVue',
  data() {
    return {}
  },
  created() {
    this.getPub()
  },
  computed: {
    ...mapGetters(['permission', 'bread', 'userinfo'])
  },
  watch: {
    $route(to) {
      const { fullPath, meta = {}, name, query } = to
      const { title = '', isParent } = meta
      if (this.bread.some((v) => v.link === fullPath)) {
        // 页面是否包含在bread中
        const bread = []
        const length = this.bread.length
        console.log(this.bread, length)
        for (let i = 0; i < length; i++) {
          console.log(this.bread[i], 'this.bread[i]')
          bread.push(this.bread[i])
          if (this.bread[i].link === fullPath) {
            break
          }
        }
        this.SET_BREAD(bread)
      } else {
        if (isParent) {
          this.SET_BREAD([{ text: title, link: fullPath, name }])
        } else {
          const { type } = query
          const text = type && type === 'edit' ? title.replace('新增', '编辑') : title
          this.SET_BREAD([...this.bread, { text, link: fullPath, name }])
        }
      }
      this.isHome = name === 'home'
    }
  },
  methods: {
    ...mapMutations([SET_PBKEY, SET_BREAD]),
    async getPub() {
      const res = await settingManageServer.getPub()
      if (res.code === 0 && res.data) {
        const { publicKey } = res.data
        this.SET_PBKEY(publicKey)
      }
    }
  }
}
</script>
<style lang="less"></style>
