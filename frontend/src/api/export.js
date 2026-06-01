import request from './request'

/**
 * 通过 window.open 直接触发下载（最可靠方案）
 * 后端已设置 Content-Disposition: attachment，浏览器会自动弹出下载
 */
function openDownloadUrl(path, params = {}) {
  const token = localStorage.getItem('token')
  const qs = Object.entries(params)
    .filter(([, v]) => v != null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&')

  let url = `/api/v1${path}`
  if (qs) url += '?' + qs

  fetch(url, {
    headers: { Authorization: `Bearer ${token}` }
  })
  .then(res => {
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    
    // 提取响应头中的文件名
    const cd = res.headers.get('Content-Disposition') || ''
    const match = /filename=(.+)/i.exec(cd)
    const filename = match ? decodeURIComponent(match[1].replace(/^"|"$/g,)) : 'download'

    return res.blob().then(blob => ({ blob, filename }))
  })
  .then(({ blob, filename }) => {
    if (blob.size === 0) throw new Error('文件为空')

    const a = document.createElement('a')
    const url2 = URL.createObjectURL(blob)
    a.style.cssText = 'position:absolute;left:-9999px'
    a.href = url2
    a.download = filename
    document.body.appendChild(a)
    a.click()
    setTimeout(() => {
      document.body.removeChild(a)
      URL.revokeObjectURL(url2)
    }, 2000)
  })
  .catch(err => {
    console.error('[download]', err.message)
  })
}

/**
 * 导出统计报表 Excel
 * @param {Object} params - { type, storeId, startDate, endDate, granularity }
 */
export function exportReport(params) {
  return openDownloadUrl('/export/report', params)
}

/**
 * 导出仪表盘 PDF
 * @param {Object} params - { storeId }
 */
export function exportDashboardPdf(params) {
  return openDownloadUrl('/export/dashboard', params)
}
