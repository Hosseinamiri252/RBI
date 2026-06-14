import api from './client'

export interface LoginResponse {
  token: string
  user: { id: string; username: string; displayName: string; role: 'USER' | 'ADMIN' }
}

export const login = (username: string, password: string) =>
  api.post<LoginResponse>('/auth/login', { username, password })

export const logout = () => api.post('/auth/logout')
export const getMe = () => api.get('/auth/me')
