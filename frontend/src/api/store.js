import request from './request'

export function getStoreList(params) {
  return request({
    url: '/stores',
    method: 'get',
    params
  })
}

export function getAllStores() {
  return request({
    url: '/stores/all',
    method: 'get'
  })
}

export function getStoreById(id) {
  return request({
    url: `/stores/${id}`,
    method: 'get'
  })
}

export function createStore(data) {
  return request({
    url: '/stores',
    method: 'post',
    data
  })
}

export function updateStore(id, data) {
  return request({
    url: `/stores/${id}`,
    method: 'put',
    data
  })
}

export function deleteStore(id) {
  return request({
    url: `/stores/${id}`,
    method: 'delete'
  })
}

// ===== 区域管理 =====

export function getZones(storeId) {
  return request({
    url: `/stores/${storeId}/zones`,
    method: 'get'
  })
}

export function createZone(storeId, data) {
  return request({
    url: `/stores/${storeId}/zones`,
    method: 'post',
    data
  })
}

export function updateZone(storeId, zoneId, data) {
  return request({
    url: `/stores/${storeId}/zones/${zoneId}`,
    method: 'put',
    data
  })
}

export function deleteZone(storeId, zoneId) {
  return request({
    url: `/stores/${storeId}/zones/${zoneId}`,
    method: 'delete'
  })
}
