import { getStore } from 'Utils/sessionstore'
const state = {
  authorization: getStore('authorization') || '',
  permission: getStore('permission') || [],
  userinfo: getStore('userinfo') || '',
  fileUploadTask: null,
  agencyId: getStore('agencyName') || '',
  agencyName: getStore('agencyName') || '',
  agencyAdmin: getStore('agencyAdmin') || '',
  agencyList: getStore('agencyList') || [],
  userId: getStore('userId') || '',
  algList: getStore('algList') || [],
  bread: getStore('bread') || [],
  groupList: getStore('groupList') || [],
  pbKey: getStore('pbKey') || ''
}
export default state
