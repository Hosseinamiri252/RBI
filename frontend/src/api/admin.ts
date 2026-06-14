import api from './client'

export const listUsers = (page = 0) =>
  api.get('/admin/users', { params: { page, size: 20 } })
export const createUser = (data: Record<string, unknown>) =>
  api.post('/admin/users', data)
export const updateUser = (id: string, data: Record<string, unknown>) =>
  api.put(`/admin/users/${id}`, data)
export const deleteUser = (id: string) => api.delete(`/admin/users/${id}`)
export const toggleUser = (id: string) => api.patch(`/admin/users/${id}/toggle`)
export const resetPassword = (id: string, password: string) =>
  api.patch(`/admin/users/${id}/password`, { password })

export const adminListSessions = (page = 0) =>
  api.get('/admin/sessions', { params: { page, size: 20 } })
export const forceCloseSession = (id: string) => api.delete(`/admin/sessions/${id}`)

export const listPolicies = (page = 0) =>
  api.get('/admin/policies', { params: { page, size: 20 } })
export const createPolicy = (data: Record<string, unknown>) =>
  api.post('/admin/policies', data)
export const updatePolicy = (id: string, data: Record<string, unknown>) =>
  api.put(`/admin/policies/${id}`, data)

export const listAuditLogs = (page = 0) =>
  api.get('/admin/audit', { params: { page, size: 20 } })

export const getStats = () => api.get('/admin/stats')
