export interface User {
  id: string
  username: string
  displayName?: string
  email?: string
  role: 'USER' | 'ADMIN'
  authSource: 'LOCAL' | 'LDAP'
  enabled: boolean
  maxConcurrentSessions: number
  createdAt: string
  lastLoginAt?: string
}
