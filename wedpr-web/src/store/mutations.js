import {
  SET_AGENCYADMIN,
  SET_ALGLIST,
  SET_USERID,
  SET_AGENCYLIST,
  SET_AUTHORIZATION,
  SET_PERMISSION,
  SET_USERINFO,
  SET_FILEUPLOADTASK,
  SET_AGENCYID,
  SET_AGENCYNAME,
  SET_BREAD,
  SET_GROUPLIST,
  SET_PBKEY
} from './mutation-types'
import { setStore } from 'Utils/sessionstore'
// 必须是同步代码
const mutation = {
  [SET_AUTHORIZATION](state, authorization) {
    state.authorization = authorization
    setStore('authorization', authorization)
  },
  [SET_PERMISSION](state, permission) {
    state.permission = permission
    setStore('permission', permission)
  },
  [SET_USERINFO](state, userinfo) {
    state.userinfo = userinfo
    setStore('userinfo', userinfo)
  },
  [SET_AGENCYID](state, agencyId) {
    state.agencyId = agencyId
    setStore('agencyId', agencyId)
  },
  [SET_AGENCYNAME](state, agencyName) {
    state.agencyName = agencyName
    setStore('agencyName', agencyName)
  },
  [SET_AGENCYADMIN](state, agencyAdmin) {
    state.agencyAdmin = agencyAdmin
    setStore('agencyAdmin', agencyAdmin)
  },
  [SET_FILEUPLOADTASK](state, fileUploadTask) {
    state.fileUploadTask = fileUploadTask
  },
  [SET_AGENCYLIST](state, agencyList) {
    state.agencyList = agencyList
    setStore('agencyList', agencyList)
  },
  [SET_USERID](state, userId) {
    state.userId = userId
    setStore('userId', userId)
  },
  [SET_ALGLIST](state, algList) {
    state.algList = algList
    setStore('algList', algList)
  },
  [SET_BREAD](state, bread) {
    state.bread = bread
    setStore('bread', bread)
  },
  [SET_GROUPLIST](state, groupList) {
    state.groupList = groupList
    setStore('groupList', groupList)
  },
  [SET_PBKEY](state, pbKey) {
    state.pbKey = pbKey
    setStore('pbKey', pbKey)
  }
}
export default mutation
