import { useEffect, useState } from 'react'
import { adminListSessions, forceCloseSession } from '../../api/admin'
import { Session } from '../../types/session'

export default function SessionsPage() {
  const [sessions, setSessions] = useState<Session[]>([])

  const load = async () => {
    const res = await adminListSessions()
    setSessions(res.data.content || res.data)
  }

  useEffect(() => {
    load()
    const t = setInterval(load, 10000)
    return () => clearInterval(t)
  }, [])

  const handleForceClose = async (id: string) => {
    if (!confirm('Force close this session?')) return
    await forceCloseSession(id)
    load()
  }

  const activeCount = sessions.filter((s) => s.status === 'ACTIVE').length

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Sessions</h1>
        <span className="bg-green-500/20 text-green-300 px-3 py-1 rounded-lg text-sm">
          {activeCount} active
        </span>
      </div>

      <div className="bg-slate-800 rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-700">
              {['User', 'Started', 'Status', 'Client IP', 'Container', 'Actions'].map((h) => (
                <th key={h} className="text-left px-5 py-3 text-slate-400 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {sessions.map((s) => (
              <tr key={s.id} className="border-b border-slate-700/50 hover:bg-slate-700/30">
                <td className="px-5 py-3 text-slate-300 text-xs">{s.user?.username || '—'}</td>
                <td className="px-5 py-3 text-slate-400 text-xs">{new Date(s.startedAt).toLocaleString()}</td>
                <td className="px-5 py-3">
                  <span className={`px-2 py-0.5 rounded text-xs ${s.status === 'ACTIVE' ? 'bg-green-500/20 text-green-300' : 'bg-slate-500/20 text-slate-400'}`}>
                    {s.status}
                  </span>
                </td>
                <td className="px-5 py-3 text-slate-400 text-xs">{s.clientIp || '—'}</td>
                <td className="px-5 py-3 text-slate-400 text-xs font-mono">{s.containerName?.slice(-20) || '—'}</td>
                <td className="px-5 py-3">
                  {(s.status === 'ACTIVE' || s.status === 'STARTING') && (
                    <button
                      onClick={() => handleForceClose(s.id)}
                      className="text-red-400 hover:text-red-300 text-xs underline"
                    >
                      Force Close
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {sessions.length === 0 && (
              <tr><td colSpan={6} className="px-5 py-8 text-center text-slate-500">No sessions</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
