import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { closeSession } from '../api/sessions'
import { useAuthStore } from '../store/authStore'
import GuacamoleViewer from '../components/GuacamoleViewer'

export default function BrowserPage() {
  const { sessionId } = useParams<{ sessionId: string }>()
  const token = useAuthStore((s) => s.token)
  const navigate = useNavigate()
  const [showToolbar, setShowToolbar] = useState(true)
  const [elapsed, setElapsed] = useState(0)
  const [showEndConfirm, setShowEndConfirm] = useState(false)
  const toolbarTimer = useRef<ReturnType<typeof setTimeout>>()
  const elapsedTimer = useRef<ReturnType<typeof setInterval>>()

  useEffect(() => {
    elapsedTimer.current = setInterval(() => setElapsed((e) => e + 1), 1000)
    return () => clearInterval(elapsedTimer.current)
  }, [])

  const resetToolbarTimer = () => {
    setShowToolbar(true)
    clearTimeout(toolbarTimer.current)
    toolbarTimer.current = setTimeout(() => setShowToolbar(false), 3000)
  }

  useEffect(() => {
    resetToolbarTimer()
    return () => clearTimeout(toolbarTimer.current)
  }, [])

  const handleEnd = async () => {
    if (sessionId) await closeSession(sessionId)
    navigate('/dashboard')
  }

  const formatTime = (secs: number) => {
    const h = Math.floor(secs / 3600)
    const m = Math.floor((secs % 3600) / 60)
    const s = secs % 60
    return h > 0
      ? `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
      : `${m}:${String(s).padStart(2, '0')}`
  }

  if (!sessionId || !token) return null

  return (
    <div
      className="relative w-screen h-screen bg-black overflow-hidden"
      onMouseMove={resetToolbarTimer}
    >
      <GuacamoleViewer
        sessionId={sessionId}
        token={token}
        onDisconnect={() => navigate('/dashboard')}
      />

      {showToolbar && (
        <div className="absolute top-0 left-0 right-0 bg-slate-900/90 backdrop-blur-sm px-6 py-3 flex items-center justify-between z-50 transition-opacity duration-300">
          <div className="flex items-center gap-4">
            <span className="text-white font-mono text-sm bg-slate-800 px-3 py-1 rounded">
              {formatTime(elapsed)}
            </span>
            <span className="text-slate-400 text-sm">Isolated Browser Session</span>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => document.documentElement.requestFullscreen?.()}
              className="text-slate-300 hover:text-white text-sm px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 transition-colors"
            >
              Fullscreen
            </button>
            <button
              onClick={() => setShowEndConfirm(true)}
              className="bg-red-700 hover:bg-red-600 text-white text-sm px-4 py-1 rounded transition-colors"
            >
              End Session
            </button>
          </div>
        </div>
      )}

      {showEndConfirm && (
        <div className="absolute inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="bg-slate-800 rounded-xl p-6 max-w-sm w-full mx-4">
            <h3 className="text-lg font-semibold text-white mb-2">End Session?</h3>
            <p className="text-slate-400 text-sm mb-5">
              This will close your browser session. The recording will be saved.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowEndConfirm(false)}
                className="flex-1 bg-slate-700 hover:bg-slate-600 text-white py-2 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleEnd}
                className="flex-1 bg-red-700 hover:bg-red-600 text-white py-2 rounded-lg transition-colors"
              >
                End Session
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
