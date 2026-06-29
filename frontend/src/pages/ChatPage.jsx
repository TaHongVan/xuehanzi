import { useEffect, useRef, useState } from 'react'
import { Alert, Button, Card, Input, Space, Spin, Tag, Typography, message } from 'antd'
import { AudioMutedOutlined, AudioOutlined, MessageOutlined, SendOutlined } from '@ant-design/icons'

const { Title, Text } = Typography

const quickStarts = [
  'Hãy bắt đầu chào hỏi HSK1 với tôi.',
  'Đóng vai nhân viên nhà hàng và hỏi tôi muốn gọi món gì.',
  'Đóng vai người bán hàng và luyện mua sắm với tôi.',
]

export default function ChatPage() {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [connected, setConnected] = useState(false)
  const [connecting, setConnecting] = useState(false)
  const [muted, setMuted] = useState(false)
  const [status, setStatus] = useState('Chưa kết nối')
  const pcRef = useRef(null)
  const dcRef = useRef(null)
  const localStreamRef = useRef(null)
  const remoteAudioRef = useRef(null)
  const assistantDraftRef = useRef('')
  const userDraftRef = useRef('')

  useEffect(() => {
    return () => disconnectRealtime()
  }, [])

  const addMessage = (sender, content) => {
    if (!content?.trim()) return
    setMessages(prev => [...prev, { sender, content: content.trim() }])
  }

  const updateAssistantDraft = (delta) => {
    assistantDraftRef.current += delta
    setMessages(prev => {
      const next = [...prev]
      const last = next[next.length - 1]
      if (last?.sender === 'AI' && last.draft) {
        next[next.length - 1] = { ...last, content: assistantDraftRef.current }
      } else {
        next.push({ sender: 'AI', content: assistantDraftRef.current, draft: true })
      }
      return next
    })
  }

  const finishAssistantDraft = (fallbackText) => {
    const text = (fallbackText || assistantDraftRef.current).trim()
    if (!text) return
    assistantDraftRef.current = ''
    setMessages(prev => {
      const next = [...prev]
      const last = next[next.length - 1]
      if (last?.sender === 'AI' && last.draft) {
        next[next.length - 1] = { sender: 'AI', content: text }
      } else {
        next.push({ sender: 'AI', content: text })
      }
      return next
    })
  }

  const sendRealtimeEvent = (event) => {
    const dc = dcRef.current
    if (!dc || dc.readyState !== 'open') {
      message.warning('Realtime chưa sẵn sàng')
      return false
    }
    dc.send(JSON.stringify(event))
    return true
  }

  const startRealtime = async () => {
    if (connected || connecting) return

    setConnecting(true)
    setStatus('Đang xin quyền micro...')

    try {
      const pc = new RTCPeerConnection()
      pcRef.current = pc

      pc.onconnectionstatechange = () => {
        setStatus(connectionStatusLabel(pc.connectionState))
        if (pc.connectionState === 'connected') setConnected(true)
        if (['failed', 'closed', 'disconnected'].includes(pc.connectionState)) setConnected(false)
      }

      pc.ontrack = (event) => {
        if (remoteAudioRef.current) {
          remoteAudioRef.current.srcObject = event.streams[0]
        }
      }

      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      localStreamRef.current = stream
      stream.getTracks().forEach(track => pc.addTrack(track, stream))

      const dc = pc.createDataChannel('oai-events')
      dcRef.current = dc
      dc.onopen = () => {
        setStatus('Đã kết nối. Bạn có thể nói trực tiếp.')
        setConnected(true)
        requestAssistantStart()
      }
      dc.onmessage = (event) => handleRealtimeEvent(JSON.parse(event.data))
      dc.onerror = () => setStatus('Data channel lỗi')

      const offer = await pc.createOffer()
      await pc.setLocalDescription(offer)

      setStatus('Đang kết nối OpenAI Realtime...')
      const token = localStorage.getItem('token')
      const response = await fetch('/api/realtime/session', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/sdp',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: offer.sdp,
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(readApiError(errorText) || `Realtime session failed: ${response.status}`)
      }

      await pc.setRemoteDescription({
        type: 'answer',
        sdp: await response.text(),
      })
    } catch (error) {
      disconnectRealtime()
      setStatus('Kết nối thất bại')
      addMessage('AI', `Không thể mở hội thoại realtime. ${error.message}`)
    } finally {
      setConnecting(false)
    }
  }

  const disconnectRealtime = () => {
    dcRef.current?.close()
    pcRef.current?.close()
    localStreamRef.current?.getTracks().forEach(track => track.stop())
    dcRef.current = null
    pcRef.current = null
    localStreamRef.current = null
    setConnected(false)
    setConnecting(false)
    setMuted(false)
    setStatus('Chưa kết nối')
  }

  const requestAssistantStart = (text = 'Hãy chủ động bắt đầu một cuộc hội thoại luyện nói tiếng Trung HSK1 với tôi. Hỏi một câu ngắn, kèm pinyin và nghĩa tiếng Việt.') => {
    sendRealtimeEvent({
      type: 'conversation.item.create',
      item: {
        type: 'message',
        role: 'user',
        content: [{ type: 'input_text', text }],
      },
    })
    sendRealtimeEvent({ type: 'response.create' })
  }

  const sendText = (text = input) => {
    const content = text.trim()
    if (!content) return
    if (!connected) {
      message.warning('Bấm Kết nối realtime trước')
      return
    }

    addMessage('USER', content)
    setInput('')
    sendRealtimeEvent({
      type: 'conversation.item.create',
      item: {
        type: 'message',
        role: 'user',
        content: [{ type: 'input_text', text: content }],
      },
    })
    sendRealtimeEvent({ type: 'response.create' })
  }

  const toggleMute = () => {
    const track = localStreamRef.current?.getAudioTracks()[0]
    if (!track) return
    track.enabled = !track.enabled
    setMuted(!track.enabled)
  }

  const handleRealtimeEvent = (event) => {
    switch (event.type) {
      case 'session.created':
      case 'session.updated':
        setStatus('Realtime sẵn sàng')
        break
      case 'input_audio_buffer.speech_started':
        setStatus('Đang nghe bạn nói...')
        userDraftRef.current = ''
        break
      case 'input_audio_buffer.speech_stopped':
        setStatus('AI đang suy nghĩ...')
        break
      case 'conversation.item.input_audio_transcription.completed':
        userDraftRef.current = event.transcript || ''
        addMessage('USER', userDraftRef.current)
        break
      case 'response.audio_transcript.delta':
      case 'response.text.delta':
      case 'response.output_text.delta':
        updateAssistantDraft(event.delta || '')
        break
      case 'response.audio_transcript.done':
      case 'response.text.done':
      case 'response.output_text.done':
        finishAssistantDraft(event.transcript || event.text || '')
        setStatus('Đã kết nối. Bạn có thể nói tiếp.')
        break
      case 'response.done':
        finishAssistantDraft('')
        setStatus('Đã kết nối. Bạn có thể nói tiếp.')
        break
      case 'error':
        addMessage('AI', event.error?.message || 'Realtime API báo lỗi.')
        setStatus('Realtime lỗi')
        break
      default:
        break
    }
  }

  return (
    <div className="page-container speaking-page">
      <Title level={3}>Luyện nói realtime với AI</Title>
      <Text type="secondary">
        Kết nối micro trực tiếp với AI. AI nghe giọng bạn và trả lời bằng giọng nói, hội thoại hiển thị ngay bên dưới.
      </Text>

      <Alert
        type="info"
        showIcon
        style={{ marginTop: 16 }}
        message="Yêu cầu OPENAI_API_KEY và trình duyệt hỗ trợ WebRTC"
        description="Micro chỉ hoạt động trên localhost hoặc HTTPS. Nếu kết nối thất bại, kiểm tra backend và biến OPENAI_API_KEY."
      />

      <Card className="speaking-card realtime-card" bodyStyle={{ padding: 0 }}>
        <div className="speaking-toolbar">
          <Tag color={connected ? 'green' : connecting ? 'blue' : 'default'}>
            {status}
          </Tag>
          <Space wrap>
            {!connected ? (
              <Button type="primary" icon={<AudioOutlined />} loading={connecting} onClick={startRealtime}>
                Kết nối realtime
              </Button>
            ) : (
              <>
                <Button icon={muted ? <AudioMutedOutlined /> : <AudioOutlined />} onClick={toggleMute}>
                  {muted ? 'Bật mic' : 'Tắt mic'}
                </Button>
                <Button onClick={() => requestAssistantStart()}>AI nói tiếp</Button>
                <Button danger onClick={disconnectRealtime}>Ngắt</Button>
              </>
            )}
          </Space>
        </div>

        <div className="quick-starts">
          {quickStarts.map(item => (
            <Button key={item} disabled={!connected} onClick={() => sendText(item)}>
              {item}
            </Button>
          ))}
        </div>

        <div className="chat-messages realtime-messages">
          {messages.length === 0 && (
            <div className="speaking-empty">
              <MessageOutlined />
              <Text>Bấm “Kết nối realtime”, cấp quyền micro, rồi nói trực tiếp với AI.</Text>
            </div>
          )}

          {messages.map((msg, i) => (
            <div key={`${msg.sender}-${i}`} style={{ display: 'flex', justifyContent: msg.sender === 'USER' ? 'flex-end' : 'flex-start' }}>
              <div className={`message-bubble ${msg.sender === 'USER' ? 'message-user' : 'message-ai'}`}>
                <Text strong className="speaker-label">{msg.sender === 'USER' ? 'Bạn' : 'AI'}</Text>
                <div style={{ whiteSpace: 'pre-wrap' }}>{msg.content}</div>
              </div>
            </div>
          ))}

          {connecting && (
            <div className="message-bubble message-ai">
              <Spin size="small" /> Đang kết nối realtime...
            </div>
          )}
        </div>

        <div className="speaking-input">
          <Space.Compact style={{ width: '100%' }}>
            <Input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onPressEnter={() => sendText()}
              placeholder="Có thể nhập text nếu không muốn nói..."
              disabled={!connected}
              size="large"
            />
            <Button
              type="primary"
              icon={<SendOutlined />}
              size="large"
              disabled={!connected}
              onClick={() => sendText()}
              style={{ background: '#c41e3a' }}
            >
              Gửi
            </Button>
          </Space.Compact>
        </div>
      </Card>

      <audio ref={remoteAudioRef} autoPlay />
    </div>
  )
}

function connectionStatusLabel(state) {
  switch (state) {
    case 'new':
      return 'Đang tạo kết nối...'
    case 'connecting':
      return 'Đang kết nối...'
    case 'connected':
      return 'Đã kết nối realtime'
    case 'disconnected':
      return 'Mất kết nối'
    case 'failed':
      return 'Kết nối thất bại'
    case 'closed':
      return 'Đã ngắt kết nối'
    default:
      return state
  }
}

function readApiError(rawText) {
  if (!rawText) return ''
  try {
    const parsed = JSON.parse(rawText)
    return parsed.message || parsed.error?.message || rawText
  } catch {
    return rawText
  }
}
