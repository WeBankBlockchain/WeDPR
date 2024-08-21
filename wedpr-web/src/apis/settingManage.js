import http from '../utils/http'

// 获取配置
const querySettings = (params) => http.post('/setting/querySettings', params)
const getConfig = (params) => http.get('/config/getConfig', params)
const queryAgencyMetas = (params) => http.post('/meta/agency/queryAgencyMetas', params)
// 获取公钥
const getPub = (params) => http.get('/pub', params)
const getImageCode = (params) => http.get('/image-code', params)
export default { getPub, getImageCode, querySettings, getConfig, queryAgencyMetas }
