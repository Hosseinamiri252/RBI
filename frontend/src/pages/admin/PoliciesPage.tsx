import { useEffect, useState } from 'react'
import { listPolicies, createPolicy, updatePolicy } from '../../api/admin'

interface Policy {
  id?: string
  name: string
  description: string
  appliesToRole: string
  maxSessionDurationMinutes: number
  maxConcurrentSessions: number
  recordingEnabled: boolean
  enabled: boolean
  blockedUrlPatterns: string[]
  allowedUrlPatterns: string[]
}

const emptyPolicy: Policy = {
  name: '',
  description: '',
  appliesToRole: 'USER',
  maxSessionDurationMinutes: 480,
  maxConcurrentSessions: 1,
  recordingEnabled: true,
  enabled: true,
  blockedUrlPatterns: [],
  allowedUrlPatterns: [],
}

export default function PoliciesPage() {
  const [policies, setPolicies] = useState<Policy[]>([])
  const [editing, setEditing] = useState<Policy | null>(null)

  const load = async () => {
    const res = await listPolicies()
    setPolicies(res.data.content || res.data)
  }

  useEffect(() => { load() }, [])

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!editing) return
    if (editing.id) {
      await updatePolicy(editing.id, editing as Record<string, unknown>)
    } else {
      await createPolicy(editing as Record<string, unknown>)
    }
    setEditing(null)
    load()
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Access Policies</h1>
        <button
          onClick={() => setEditing({ ...emptyPolicy })}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm"
        >
          New Policy
        </button>
      </div>

      <div className="grid gap-4">
        {policies.map((p) => (
          <div key={p.id} className="bg-slate-800 rounded-xl p-5 flex items-center justify-between">
            <div>
              <p className="text-white font-medium">{p.name}</p>
              <p className="text-slate-400 text-sm mt-1">{p.description || 'No description'}</p>
              <div className="flex gap-4 mt-2 text-xs text-slate-500">
                <span>Role: {p.appliesToRole}</span>
                <span>Max sessions: {p.maxConcurrentSessions}</span>
                <span>Max duration: {p.maxSessionDurationMinutes}m</span>
                <span>Recording: {p.recordingEnabled ? 'On' : 'Off'}</span>
              </div>
            </div>
            <button
              onClick={() => setEditing({ ...p })}
              className="text-blue-400 hover:text-blue-300 text-sm underline"
            >
              Edit
            </button>
          </div>
        ))}
        {policies.length === 0 && (
          <div className="bg-slate-800 rounded-xl p-8 text-center text-slate-500">No policies defined</div>
        )}
      </div>

      {editing && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="bg-slate-800 rounded-xl p-6 w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
            <h2 className="text-lg font-semibold text-white mb-4">
              {editing.id ? 'Edit Policy' : 'New Policy'}
            </h2>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-sm text-slate-300 mb-1">Name</label>
                <input type="text" value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" required />
              </div>
              <div>
                <label className="block text-sm text-slate-300 mb-1">Description</label>
                <textarea value={editing.description} onChange={(e) => setEditing({ ...editing, description: e.target.value })}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 h-20 resize-none" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-slate-300 mb-1">Applies to Role</label>
                  <select value={editing.appliesToRole} onChange={(e) => setEditing({ ...editing, appliesToRole: e.target.value })}
                    className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm">
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm text-slate-300 mb-1">Max Concurrent Sessions</label>
                  <input type="number" min={1} value={editing.maxConcurrentSessions}
                    onChange={(e) => setEditing({ ...editing, maxConcurrentSessions: Number(e.target.value) })}
                    className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm" />
                </div>
              </div>
              <div>
                <label className="block text-sm text-slate-300 mb-1">Max Duration (minutes)</label>
                <input type="number" min={1} value={editing.maxSessionDurationMinutes}
                  onChange={(e) => setEditing({ ...editing, maxSessionDurationMinutes: Number(e.target.value) })}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm" />
              </div>
              <div className="flex items-center gap-3">
                <input type="checkbox" id="recording" checked={editing.recordingEnabled}
                  onChange={(e) => setEditing({ ...editing, recordingEnabled: e.target.checked })}
                  className="w-4 h-4" />
                <label htmlFor="recording" className="text-sm text-slate-300">Enable recording</label>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setEditing(null)}
                  className="flex-1 bg-slate-700 hover:bg-slate-600 text-white py-2 rounded-lg text-sm">Cancel</button>
                <button type="submit"
                  className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg text-sm">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
