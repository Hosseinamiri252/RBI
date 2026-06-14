import { useEffect, useState } from 'react'
import { adminListRecordings, deleteRecording } from '../../api/admin'
import { Recording } from '../../types/recording'
import { useAuthStore } from '../../store/authStore'

function formatBytes(bytes?: number) {
  if (!bytes) return '—'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

export default function RecordingsPage() {
  const [recordings, setRecordings] = useState<Recording[]>([])
  const token = useAuthStore((s) => s.token)

  const load = async () => {
    const res = await adminListRecordings()
    setRecordings(res.data.content || res.data)
  }

  useEffect(() => { load() }, [])

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this recording?')) return
    await deleteRecording(id)
    load()
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-white">Recordings</h1>
      <div className="bg-slate-800 rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-700">
              {['User', 'Date', 'Duration', 'Size', 'Actions'].map((h) => (
                <th key={h} className="text-left px-5 py-3 text-slate-400 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {recordings.map((r) => (
              <tr key={r.id} className="border-b border-slate-700/50 hover:bg-slate-700/30">
                <td className="px-5 py-3 text-slate-300 text-xs">{r.user?.username || '—'}</td>
                <td className="px-5 py-3 text-slate-400 text-xs">{new Date(r.createdAt).toLocaleString()}</td>
                <td className="px-5 py-3 text-slate-400 text-xs">
                  {r.durationSeconds ? `${Math.floor(r.durationSeconds / 60)}m ${r.durationSeconds % 60}s` : '—'}
                </td>
                <td className="px-5 py-3 text-slate-400 text-xs">{formatBytes(r.fileSizeBytes)}</td>
                <td className="px-5 py-3">
                  <div className="flex gap-3">
                    <a
                      href={`/api/recordings/${r.id}/stream?token=${token}`}
                      download
                      className="text-blue-400 hover:text-blue-300 text-xs underline"
                    >
                      Download
                    </a>
                    <button
                      onClick={() => handleDelete(r.id)}
                      className="text-red-400 hover:text-red-300 text-xs underline"
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {recordings.length === 0 && (
              <tr><td colSpan={5} className="px-5 py-8 text-center text-slate-500">No recordings</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
