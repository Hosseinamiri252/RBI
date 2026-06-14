import { useEffect, useState } from 'react'
import { listAuditLogs } from '../../api/admin'

interface AuditLog {
  id: string
  action: string
  resourceType?: string
  resourceId?: string
  clientIp?: string
  createdAt: string
  user?: { username: string }
  details?: Record<string, unknown>
}

export default function AuditLogPage() {
  const [logs, setLogs] = useState<AuditLog[]>([])
  const [expanded, setExpanded] = useState<string | null>(null)

  const load = async () => {
    const res = await listAuditLogs()
    setLogs(res.data.content || res.data)
  }

  useEffect(() => { load() }, [])

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-white">Audit Log</h1>
      <div className="bg-slate-800 rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-700">
              {['Timestamp', 'User', 'Action', 'Resource', 'Client IP', 'Details'].map((h) => (
                <th key={h} className="text-left px-5 py-3 text-slate-400 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {logs.map((l) => (
              <>
                <tr key={l.id} className="border-b border-slate-700/50 hover:bg-slate-700/30">
                  <td className="px-5 py-3 text-slate-400 text-xs">{new Date(l.createdAt).toLocaleString()}</td>
                  <td className="px-5 py-3 text-slate-300 text-xs">{l.user?.username || '—'}</td>
                  <td className="px-5 py-3">
                    <span className="bg-blue-500/20 text-blue-300 px-2 py-0.5 rounded text-xs">{l.action}</span>
                  </td>
                  <td className="px-5 py-3 text-slate-400 text-xs">{l.resourceType || '—'}</td>
                  <td className="px-5 py-3 text-slate-400 text-xs">{l.clientIp || '—'}</td>
                  <td className="px-5 py-3">
                    {l.details && (
                      <button
                        onClick={() => setExpanded(expanded === l.id ? null : l.id)}
                        className="text-blue-400 hover:text-blue-300 text-xs underline"
                      >
                        {expanded === l.id ? 'Hide' : 'Show'}
                      </button>
                    )}
                  </td>
                </tr>
                {expanded === l.id && (
                  <tr key={`${l.id}-detail`} className="bg-slate-900/50">
                    <td colSpan={6} className="px-5 py-3">
                      <pre className="text-xs text-slate-300 overflow-x-auto">
                        {JSON.stringify(l.details, null, 2)}
                      </pre>
                    </td>
                  </tr>
                )}
              </>
            ))}
            {logs.length === 0 && (
              <tr><td colSpan={6} className="px-5 py-8 text-center text-slate-500">No audit logs</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
