import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import BrowserPage from './pages/BrowserPage'
import AdminLayout from './pages/admin/AdminLayout'
import UsersPage from './pages/admin/UsersPage'
import SessionsPage from './pages/admin/SessionsPage'
import RecordingsPage from './pages/admin/RecordingsPage'
import PoliciesPage from './pages/admin/PoliciesPage'
import AuditLogPage from './pages/admin/AuditLogPage'

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore((s) => s.token)
  return token ? <>{children}</> : <Navigate to="/login" replace />
}

function AdminRoute({ children }: { children: React.ReactNode }) {
  const user = useAuthStore((s) => s.user)
  if (!user) return <Navigate to="/login" replace />
  if (user.role !== 'ADMIN') return <Navigate to="/dashboard" replace />
  return <>{children}</>
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/dashboard" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
        <Route path="/browser/:sessionId" element={<PrivateRoute><BrowserPage /></PrivateRoute>} />
        <Route path="/admin" element={<AdminRoute><AdminLayout /></AdminRoute>}>
          <Route index element={<Navigate to="users" replace />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="sessions" element={<SessionsPage />} />
          <Route path="recordings" element={<RecordingsPage />} />
          <Route path="policies" element={<PoliciesPage />} />
          <Route path="audit" element={<AuditLogPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
