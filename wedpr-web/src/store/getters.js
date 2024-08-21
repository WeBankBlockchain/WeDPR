// 相当于store的计算属性
// Getter 也可以接受其他 getter 作为第二个参数：
const getters = {
  authorization(state) {
    return state.authorization
  },
  permission(state) {
    return state.permission
  },
  userinfo(state) {
    return state.userinfo
  },
  fileUploadTask(state) {
    return state.fileUploadTask
  },
  agencyId(state) {
    return state.agencyId
  },
  agencyName(state) {
    return state.agencyName
  },
  agencyAdmin(state) {
    return state.agencyAdmin
  },
  agencyList(state) {
    return state.agencyList
  },
  userId(state) {
    return state.userId
  },
  algList(state) {
    return state.algList
  },
  bread(state) {
    return state.bread
  },
  groupList(state) {
    return state.groupList
  },
  pbKey(state) {
    return state.pbKey
  }
}
export default getters
