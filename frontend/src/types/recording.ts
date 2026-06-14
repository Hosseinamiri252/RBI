export interface Recording {
  id: string
  filePath: string
  fileSizeBytes?: number
  durationSeconds?: number
  createdAt: string
  session?: { id: string }
  user?: { id: string; username: string }
}
