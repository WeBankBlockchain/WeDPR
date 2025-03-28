export const permissionMap = {
  admin_user: [
    'home',
    'feed',
    'groupManage',
    'tenantManage',
    'accessKeyManage',
    'accountManage',
    'dataManage',
    'projectManage',
    'messageManage',
    'logManage',
    'approveManage',
    'serverManage'
  ],
  original_user: ['home', 'feed', 'tenantManage', 'accountManage', 'accessKeyManage', 'dataManage', 'projectManage', 'messageManage', 'logManage', 'approveManage', 'serverManage'],
  group_admin: [
    'home',
    'feed',
    'groupManage',
    'tenantManage',
    'accountManage',
    'accessKeyManage',
    'dataManage',
    'projectManage',
    'messageManage',
    'logManage',
    'approveManage',
    'serverManage'
  ],
  agency_admin: ['agencyManage', 'agencyCreate', 'feed', 'dataManage', 'projectManage', 'logManage', 'certificateManage', 'addCertificate', 'screen']
}
