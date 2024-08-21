import { accountManageServer } from 'Api'
export const userSelect = {
  data() {
    return {
      userNameSelectList: []
    }
  },
  methods: {
    async getUserNameSelect(username) {
      if (!username) {
        this.userNameSelectList = []
        return
      }
      const res = await accountManageServer.getUser({ pageNum: 1, pageSize: 9999, username })
      if (res.code === 0 && res.data) {
        const { userList = [] } = res.data
        this.userNameSelectList = userList.map((v) => {
          return {
            label: v.username,
            value: v.username
          }
        })
      } else {
        this.userNameSelectList = []
      }
    }
  }
}
