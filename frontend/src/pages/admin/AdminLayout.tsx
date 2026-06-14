import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'

const links = [
  { to: 'users', label: 'Users' },
  { to: 'sessions', label: 'Sessions' },
  { to: 'recordings', label: 'Recordings' },
  { to: 'policies', label: 'Policies' },
  { to: 'audit', label: 'Audit Log' },
]

export default function AdminLayout() {
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()

  return (
    <div className="min-h-screen flex bg-slate-900">
      <aside className="w-56 bg-slate-800 border-r border-slate-700 flex flex-col">
        <div className="px-5 py-5 border-b border-slate-700">
          <p className="text-white font-bold">IWAP Admin</p>
        </div>
        <nav className="flex-1 py-4 space-y-1 px-3">
          {links.map((l) => (
            <NavLink
              key={l.to}
              to={l.to}
              className={({ isActive }) =>
                `block px-4 py-2 rounded-lg text-sm transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-400 hover:text-white hover:bg-slate-700'
                }`
              }
            >
              {l.label}
            </NavLink>
          ))}
        </nav>
        <div className="p-4 border-t border-slate-700 space-y-2">
          <button
            onClick={() => navigate('/dashboard')}
            className="w-full text-left text-slate-400 hover:text-white text-sm px-4 py-2 rounded-lg hover:bg-slate-700 transition-colors"
          >
            Dashboard
          </button>
          <button
            onClick={() => { logout(); navigate('/login') }}
            className="w-full text-left text-red-400 hover:text-red-300 text-sm px-4 py-2 rounded-lg hover:bg-slate-700 transition-colors"
          >
            Logout
          </button>
        </div>
      </aside>
      <main className="flex-1 overflow-auto p-8">
        <Outlet />
      </main>
    </div>
  )
}
