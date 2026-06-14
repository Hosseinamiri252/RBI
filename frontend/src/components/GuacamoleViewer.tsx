import { useEffect, useRef } from 'react'
// @ts-expect-error no types
import Guacamole from 'guacamole-common-js'

interface Props {
  sessionId: string
  token: string
  onDisconnect: () => void
}

export default function GuacamoleViewer({ sessionId, token, onDisconnect }: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const clientRef = useRef<Guacamole.Client | null>(null)

  useEffect(() => {
    const tunnel = new Guacamole.HTTPTunnel(
      `/tunnel?token=${encodeURIComponent(token)}&sessionId=${sessionId}`
    )
    const client = new Guacamole.Client(tunnel)
    clientRef.current = client

    const display = client.getDisplay()
    const element = display.getElement()
    element.style.width = '100%'
    element.style.height = '100%'
    containerRef.current?.appendChild(element)

    client.onstatechange = (state: number) => {
      if (state === Guacamole.Client.State.DISCONNECTED) {
        onDisconnect()
      }
    }

    client.connect()

    const mouse = new Guacamole.Mouse(element)
    mouse.onEach(['mousedown', 'mouseup', 'mousemove'], (e: Guacamole.Mouse.Event) => {
      client.sendMouseState(e.state, true)
    })

    const keyboard = new Guacamole.Keyboard(document)
    keyboard.onkeydown = (keysym: number) => client.sendKeyEvent(1, keysym)
    keyboard.onkeyup = (keysym: number) => client.sendKeyEvent(0, keysym)

    const handleResize = () => {
      const container = containerRef.current
      if (!container) return
      display.scale(container.offsetWidth / display.getWidth())
    }
    window.addEventListener('resize', handleResize)
    setTimeout(handleResize, 500)

    return () => {
      client.disconnect()
      keyboard.reset()
      window.removeEventListener('resize', handleResize)
      if (containerRef.current && element.parentNode === containerRef.current) {
        containerRef.current.removeChild(element)
      }
    }
  }, [sessionId, token, onDisconnect])

  return (
    <div
      ref={containerRef}
      className="w-full h-full bg-black"
      style={{ cursor: 'none' }}
    />
  )
}
