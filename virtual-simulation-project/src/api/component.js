import request from '@/utils/request'

/**
 * 器件管理 API
 */

// 获取器件列表（分页）
export function getComponentList(params) {
  return request({
    url: '/api/component/list',
    method: 'get',
    params
  })
}

// 获取所有器件（不分页）
export function getAllComponents() {
  return request({
    url: '/api/component/all',
    method: 'get'
  })
}

// 根据ID获取器件详情
export function getComponentById(id) {
  return request({
    url: `/api/component/${id}`,
    method: 'get'
  })
}

// 新增器件
export function addComponent(data) {
  return request({
    url: '/api/component/add',
    method: 'post',
    data
  })
}

// 修改器件
export function updateComponent(data) {
  return request({
    url: '/api/component/update',
    method: 'put',
    data
  })
}

// 删除器件
export function deleteComponent(id) {
  return request({
    url: `/api/component/delete/${id}`,
    method: 'delete'
  })
}

// 批量删除器件
export function batchDeleteComponents(ids) {
  return request({
    url: '/api/component/batch-delete',
    method: 'delete',
    data: { ids }
  })
}

// 根据类型获取器件
export function getComponentsByCategory(category) {
  return request({
    url: '/api/component/category',
    method: 'get',
    params: { category }
  })
}

// 根据协议获取器件
export function getComponentsByProtocol(protocol) {
  return request({
    url: '/api/component/protocol',
    method: 'get',
    params: { protocol }
  })
}

// 验证器件ID是否唯一
export function checkComponentId(id) {
  return request({
    url: '/api/component/check-id',
    method: 'get',
    params: { id }
  })
}

// 导出器件配置
export function exportComponents(ids) {
  return request({
    url: '/api/component/export',
    method: 'post',
    data: { ids },
    responseType: 'blob'
  })
}

// 导入器件配置
export function importComponents(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/component/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
