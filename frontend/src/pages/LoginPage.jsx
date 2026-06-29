import { useEffect, useRef } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Card, Form, Input, Button, Typography, message } from 'antd'
import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { useAuth } from '../context/AuthContext'

const { Title, Text } = Typography

export default function LoginPage() {
  const [form] = Form.useForm()
  const googleButtonRef = useRef(null)
  const navigate = useNavigate()
  const { login, googleLogin, loading } = useAuth()
  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID

  const onFinish = async (values) => {
    try {
      await login(values.username, values.password)
      message.success('Dang nhap thanh cong!')
      navigate('/vocabulary')
    } catch (err) {
      message.error(err.response?.data?.message || 'Dang nhap that bai')
    }
  }

  useEffect(() => {
    if (!googleClientId || !googleButtonRef.current) return

    const renderGoogleButton = () => {
      if (!window.google?.accounts?.id || !googleButtonRef.current) return
      googleButtonRef.current.innerHTML = ''
      window.google.accounts.id.initialize({
        client_id: googleClientId,
        callback: async (response) => {
          try {
            await googleLogin(response.credential)
            message.success('Dang nhap Google thanh cong!')
            navigate('/vocabulary')
          } catch (err) {
            message.error(err.response?.data?.message || 'Dang nhap Google that bai')
          }
        },
      })
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        theme: 'outline',
        size: 'large',
        width: googleButtonRef.current.offsetWidth,
        text: 'signin_with',
      })
    }

    if (window.google?.accounts?.id) {
      renderGoogleButton()
      return
    }

    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    script.onload = renderGoogleButton
    document.body.appendChild(script)
  }, [googleClientId, googleLogin, navigate])

  return (
    <div className="auth-container">
      <Card className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2} style={{ color: '#c41e3a' }}>Hanzii</Title>
          <Text type="secondary">Nen tang hoc tieng Trung</Text>
        </div>

        <Form form={form} onFinish={onFinish} size="large">
          <Form.Item name="username" rules={[{ required: true, message: 'Nhap email hoac ten dang nhap' }]}>
            <Input prefix={<UserOutlined />} placeholder="Email hoac ten dang nhap" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: 'Nhap mat khau' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Mat khau" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#c41e3a' }}>
              Dang nhap
            </Button>
          </Form.Item>
        </Form>

        {googleClientId && (
          <div style={{ marginBottom: 16 }}>
            <div ref={googleButtonRef} />
          </div>
        )}

        <div style={{ textAlign: 'center' }}>
          <Text>Chua co tai khoan? <Link to="/register">Dang ky</Link></Text>
        </div>
      </Card>
    </div>
  )
}
