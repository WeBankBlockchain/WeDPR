import http from '../utils/http'

// 上传文件
const uploadFile = (params) => http.post('/resources', params)
const getFileStream = (params) => http.getStream('/resources', params)

// 快捷登录

export default { uploadFile, getFileStream }
