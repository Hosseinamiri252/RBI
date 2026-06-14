import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createSession, listSessions, closeSession } from '../api/sessions'
import { listRecordings } from '../api/recordings'
import { useAuthStore } from '../store/authStore'
import { Session } from '../types/session'
import { Recording } from '../types/recording'

function StatusBadge({ status }: { status: Session['status'] }) {
  const colors = {
    STARTING: 'bg-yellow-500/20 text-yellow-300',
    ACTIVE: 'bg-green-500/20 text-green-300',
    CLOSED: 'bg-slate-500/20 text-slate-400',
    ERROR: 'bg-red-500/20 text-red-300',
  }
  return (
    <span className={`px-2 py-0.5 rounded text-xs font-medium ${colors[status]}`}>
      {status}
    </span>
  )
}

function formatDuration(start: string, end?: string) {
  const ms = new Date(end || Date.now()).getTime() - new Date(start).getTime()
  const secs = Math.floor(ms / 1000)
  const mins = Math.floor(secs / 60)
  const hrs = Math.floor(mins / 60)
  if (hrs > 0) return `${hrs}h ${mins % 60}m`
  if (mins > 0) return `${mins}m ${secs % 60}s`
  return `${secs}s`
}

export default function DashboardPage() {
  const [sessions, setSessions] = useState<Session[]>([])
  const [recordings, setRecordings] = useState<Recording[]>([])
  const [loading, setLoading] = useState(false)
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()

  const load = async () => {
    const [sRes, rRes] = await Promise.all([listSessions(), listRecordings()])
    setSessions(sRes.data)
    setRecordings(rRes.data)
  }

  useEffect(() => { load() }, [])

  const handleStart = async () => {
    setLoading(true)
    try {
      const res = await createSession()
      navigate(`/browser/${res.data.id}`)
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { error?: string } } }
      alert(axiosErr.response?.data?.error || 'Failed to start session')
    } finally {
      setLoading(false)
    }
  }

  const handleClose = async (id: string) => {
    await closeSession(id)
    load()
  }

  const activeSessions = sessions.filter((s) => s.status === 'ACTIVE' || s.status === 'STARTING')
  const historySessions = sessions.filter((s) => s.status === 'CLOSED' || s.status === 'ERROR')

  return (
    <div className="min-h-screen bg-slate-900">
      <header className="bg-slate-800 border-b border-slate-700 px-6 py-4 flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-white">IWAP Dashboard</h1>
          <p className="text-slate-400 text-sm">Welcome, {user?.displayName || user?.username}</p>
        </div>
        <div className="flex items-center gap-3">
          {user?.role === 'ADMIN' && (
            <button
              onClick={() => navigate('/admin')}
              className="bg-slate-700 hover:bg-slate-600 text-white px-4 py-2 rounded-lg text-sm transition-colors"
            >
              Admin Panel
            </button>
          )}
          <button
            onClick={() => { logout(); navigate('/login') }}
            className="bg-red-700 hover:bg-red-600 text-white px-4 py-2 rounded-lg text-sm transition-colors"
          >
            Logout
          </button>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-6 py-8 space-y-8">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold text-white">Active Sessions</h2>
          <button
            onClick={handleStart}
            disabled={loading}
            className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white px-5 py-2 rounded-lg font-medium transition-colors flex items-center gap-2"
          >
            {loading ? 'Starting...' : '+ New Session'}
          </button>
        </div>

        {activeSessions.length === 0 ? (
          <div className="bg-slate-800 rounded-xl p-8 text-center text-slate-400">
            No active sessions. Start one above.
          </div>
        ) : (
          <div className="grid gap-4">
            {activeSessions.map((s) => (
              <div key={s.id} className="bg-slate-800 rounded-xl p-5 flex items-center justify-between">
                <div className="space-y-1">
                  <div className="flex items-center gap-3">
                    <StatusBadge status={s.status} />
                    <span className="text-slate-400 text-sm">
                      Started {formatDuration(s.startedAt)} ago
                    </span>
                  </div>
                  <p className="text-slate-500 text-xs">{s.id}</p>
                </div>
                <div className="flex gap-2">
                  {s.status === 'ACTIVE' && (
                    <button
                      onClick={() => navigate(`/browser/${s.id}`)}
                      className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-1.5 rounded-lg text-sm transition-colors"
                    >
                      Open
                    </button>
                  )}
                  <button
                    onClick={() => handleClose(s.id)}
                    className="bg-red-800 hover:bg-red-700 text-white px-4 py-1.5 rounded-lg text-sm transition-colors"
                  >
                    Close
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        <div>
          <h2 className="text-lg font-semibold text-white mb-4">Recent Sessions</h2>
          <div className="bg-slate-800 rounded-xl overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-700">
                  <th className="text-left px-5 py-3 text-slate-400 font-medium">Date</th>
                  <th className="text-left px-5 py-3 text-slate-400 font-medium">Duration</th>
                  <th className="text-left px-5 py-3 text-slate-400 font-medium">Status</th>
                  <th className="text-left px-5 py-3 text-slate-400 font-medium">Recording</th>
                </tr>
              </thead>
              <tbody>
                {historySessions.slice(0, 10).map((s) => {
                  const rec = recordings.find((r) => r.session?.id === s.id)
                  return (
                    <tr key={s.id} className="border-b border-slate-700/50 hover:bg-slate-700/30">
                      <td className="px-5 py-3 text-slate-300">{new Date(s.startedAt).toLocaleString()}</td>
                      <td className="px-5 py-3 text-slate-300">{s.endedAt ? formatDuration(s.startedAt, s.endedAt) : '-'}</td>
                      <td className="px-5 py-3"><StatusBadge status={s.status} /></td>
                      <td className="px-5 py-3">
                        {rec ? (
                          <span className="text-blue-400 text-xs">Available</span>
                        ) : (
                          <span className="text-slate-500 text-xs">None</span>
                        )}
                      </td>
                    </tr>
                  )
                })}
                {historySessions.length === 0 && (
                  <tr>
                    <td colSpan={4} className="px-5 py-8 text-center text-slate-500">No session history</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  )
}
