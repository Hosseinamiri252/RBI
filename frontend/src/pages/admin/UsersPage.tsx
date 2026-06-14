import { useEffect, useState } from 'react'
import { listUsers, createUser, toggleUser, deleteUser } from '../../api/admin'
import { User } from '../../types/user'

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([])
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState({ username: '', password: '', displayName: '', email: '', role: 'USER' })

  const load = async () => {
    const res = await listUsers()
    setUsers(res.data.content || res.data)
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    await createUser(form)
    setShowCreate(false)
    setForm({ username: '', password: '', displayName: '', email: '', role: 'USER' })
    load()
  }

  const handleToggle = async (id: string) => {
    await toggleUser(id)
    load()
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this user?')) return
    await deleteUser(id)
    load()
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Users</h1>
        <button
          onClick={() => setShowCreate(true)}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm transition-colors"
        >
          Add User
        </button>
      </div>

      <div className="bg-slate-800 rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-700">
              {['Username', 'Display Name', 'Role', 'Source', 'Status', 'Last Login', 'Actions'].map((h) => (
                <th key={h} className="text-left px-5 py-3 text-slate-400 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id} className="border-b border-slate-700/50 hover:bg-slate-700/30">
                <td className="px-5 py-3 text-white font-mono text-xs">{u.username}</td>
                <td className="px-5 py-3 text-slate-300">{u.displayName || '-'}</td>
                <td className="px-5 py-3">
                  <span className={`px-2 py-0.5 rounded text-xs ${u.role === 'ADMIN' ? 'bg-purple-500/20 text-purple-300' : 'bg-blue-500/20 text-blue-300'}`}>
                    {u.role}
                  </span>
                </td>
                <td className="px-5 py-3 text-slate-400 text-xs">{u.authSource}</td>
                <td className="px-5 py-3">
                  <span className={`px-2 py-0.5 rounded text-xs ${u.enabled ? 'bg-green-500/20 text-green-300' : 'bg-red-500/20 text-red-300'}`}>
                    {u.enabled ? 'Active' : 'Disabled'}
                  </span>
                </td>
                <td className="px-5 py-3 text-slate-400 text-xs">
                  {u.lastLoginAt ? new Date(u.lastLoginAt).toLocaleString() : 'Never'}
                </td>
                <td className="px-5 py-3">
                  <div className="flex gap-2">
                    <button onClick={() => handleToggle(u.id)} className="text-slate-400 hover:text-white text-xs underline">
                      {u.enabled ? 'Disable' : 'Enable'}
                    </button>
                    <button onClick={() => handleDelete(u.id)} className="text-red-400 hover:text-red-300 text-xs underline">
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showCreate && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="bg-slate-800 rounded-xl p-6 w-full max-w-md mx-4">
            <h2 className="text-lg font-semibold text-white mb-4">Create User</h2>
            <form onSubmit={handleCreate} className="space-y-4">
              {[
                { label: 'Username', key: 'username', type: 'text' },
                { label: 'Password', key: 'password', type: 'password' },
                { label: 'Display Name', key: 'displayName', type: 'text' },
                { label: 'Email', key: 'email', type: 'email' },
              ].map(({ label, key, type }) => (
                <div key={key}>
                  <label className="block text-sm text-slate-300 mb-1">{label}</label>
                  <input
                    type={type}
                    value={form[key as keyof typeof form]}
                    onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                    className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              ))}
              <div>
                <label className="block text-sm text-slate-300 mb-1">Role</label>
                <select
                  value={form.role}
                  onChange={(e) => setForm({ ...form, role: e.target.value })}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowCreate(false)}
                  className="flex-1 bg-slate-700 hover:bg-slate-600 text-white py-2 rounded-lg text-sm transition-colors">
                  Cancel
                </button>
                <button type="submit"
                  className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg text-sm transition-colors">
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
