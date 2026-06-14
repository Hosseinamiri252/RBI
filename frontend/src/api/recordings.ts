import api from './client'
import { Recording } from '../types/recording'

export const listRecordings = () => api.get<Recording[]>('/recordings')
export const adminListRecordings = (page = 0) =>
  api.get('/admin/recordings', { params: { page, size: 20 } })
export const deleteRecording = (id: string) => api.delete(`/recordings/${id}`)
export const streamRecordingUrl = (id: string, token: string) =>
  `/api/recordings/${id}/stream?token=${token}`
