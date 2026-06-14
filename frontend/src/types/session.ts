export interface Session {
  id: string
  status: 'STARTING' | 'ACTIVE' | 'CLOSED' | 'ERROR'
  startedAt: string
  endedAt?: string
  clientIp?: string
  containerName?: string
  vncPort?: number
  recordingPath?: string
  user?: { id: string; username: string }
}
