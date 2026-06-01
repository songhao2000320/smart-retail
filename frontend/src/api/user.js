import request from './request'

export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

export function updateUserRole(userId, role) {
  return request({
    url: `/users/${userId}/role`,
    method: 'put',
    data: { role }
  })
}

export function updateUserStatus(userId, status) {
  return request({
    url: `/users/${userId}/status`,
    method: 'put',
    data: { status }
  })
}

// ==================== 用户门店授权 ====================

/** 获取用户授权门店列表 */
export function getUserAuths(userId) {
  return request.get(`/user-auth/${userId}`)
}

/** 为用户授权门店 */
export function saveUserAuths(userId, storeIds) {
  return request.post('/user-auth', { userId, storeIds })
}
