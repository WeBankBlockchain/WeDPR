import http from '../utils/http'

// 获取随机数
// const getRandom = (params) => http.get('/random_factor', params)
// // 发送验证码
// const sendOtp = (params) => http.post('/send_otp', params)
// // 普通登录
// const normalLogin = (params) => http.post('/login_general', params)
// // um登录
// const umLogin = (params) => http.post('/login_um', params)
// 登出
const loginOut = (params) => http.delete('/logout', params)
// 注册
const register = (params) => http.post('/register', params)
// 登录
const login = (params) => http.post('/login', params)
// 机构信息
const userAgency = (params) => http.get('/userAgency', params)

// 快捷登录

export default { register, login, loginOut, userAgency }
