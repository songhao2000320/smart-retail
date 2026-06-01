import request from './request'

// ==================== 行为记录 ====================

/** 分页查询 */
export function getBehaviorList(params) {
  return request.get('/behaviors', { params })
}

/** 获取详情（含区域停留） */
export function getBehaviorDetail(id) {
  return request.get(`/behaviors/${id}`)
}

/** 创建行为记录 */
export function createBehavior(data) {
  return request.post('/behaviors', data)
}

/** 更新行为记录 */
export function updateBehavior(id, data) {
  return request.put(`/behaviors/${id}`, data)
}

/** 删除行为记录 */
export function deleteBehavior(id) {
  return request.delete(`/behaviors/${id}`)
}

/** 批量导入 Excel */
export function importBehaviors(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/behaviors/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 下载导入模板 */
export function downloadTemplate() {
  return request.get('/behaviors/template', { responseType: 'blob' })
}

// ==================== 数据分析 ====================

/** 客流统计 */
export function getTraffic(params) {
  return request.get('/analytics/traffic', { params })
}

/** 热力图数据 */
export function getHeatmap(params) {
  return request.get('/analytics/heatmap', { params })
}

/** 停留时长分析 */
export function getDuration(params) {
  return request.get('/analytics/duration', { params })
}

/** 购买转化分析 */
export function getConversion(params) {
  return request.get('/analytics/conversion', { params })
}

/** 仪表盘数据 */
export function getDashboard(params) {
  return request.get('/analytics/dashboard', { params })
}

/** 多门店对比 */
export function getCompare(params) {
  return request.get('/analytics/compare', { params })
}
