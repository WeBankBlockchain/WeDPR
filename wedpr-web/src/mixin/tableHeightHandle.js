export const tableHeightHandle = {
  data() {
    return {
      tableHeight: null
    }
  },
  methods: {
    calcTableHeight() {
      this.$nextTick(() => {
        const el = document.getElementsByClassName('autoTableWrap')[0]
        if (el) {
          const topHeight = el.getAttribute('data-topHeight') || 110
          el && (this.tableHeight = document.body.clientHeight - el.getBoundingClientRect().top - topHeight)
        }
      })
    }
  },
  mounted() {
    this.calcTableHeight()
    window.onresize = () => {
      this.calcTableHeight()
    }
  },
  destroyed() {
    window.onresize = null
  }
}
