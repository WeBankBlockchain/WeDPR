import http from '../utils/http'

// 获取群组列表
const getGroupList = (params) => http.get('/admin/groups', params)
// 获取群组人员列表
const getGroupDetail = (params) => http.get('/admin/groups/' + params.groupId, params)
// 获取管理的用户组个数
const getGroupCount = (params) => http.get('/admin/groupCount', params)
// 新增群组
const addGroup = (params) => http.post('/admin/groups', params)
// 删除群组
const deleteGroup = (params) => http.delete('/admin/groups/' + params.groupId)
// 新增用户
const addUser = (params) => http.post('/admin/groups/' + params.groupId + '/users', params)
// 编辑用户信息
const getUserInfo = (params) => http.get('/users-info', params)
// 编辑用户信息
const modifyUser = (params) => http.patch('/users-info', params)
// 编辑用户密码
const modifyPassword = (params) => http.patch('/users-password', params)
// 查询机构下用户
const getUser = (params) => http.get('/users', params)
// 查询机构下用户数量
const userCount = (params) => http.get('/userCount', params)
// 设置管理员
const setAdminUser = (params) => http.patch('/admin/groups/adminuser', params)
// 删除用户
const delUser = (params) => http.delete('/admin/groups/' + params.groupId + '/users/' + params.username, params)

export default {
  getGroupCount,
  getUserInfo,
  modifyPassword,
  getGroupList,
  getGroupDetail,
  addGroup,
  deleteGroup,
  addUser,
  setAdminUser,
  delUser,
  getUser,
  userCount,
  modifyUser
}
