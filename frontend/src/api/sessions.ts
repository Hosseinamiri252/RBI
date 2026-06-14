import api from './client'
import { Session } from '../types/session'

export const createSession = () => api.post<Session>('/sessions')
export const listSessions = () => api.get<Session[]>('/sessions')
export const getSession = (id: string) => api.get<Session>(`/sessions/${id}`)
export const closeSession = (id: string) => api.delete(`/sessions/${id}`)
