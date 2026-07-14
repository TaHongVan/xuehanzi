import { useEffect, useRef, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Card, Form, Input, Button, Typography, message, Divider } from 'antd'
import { UserOutlined, LockOutlined, MailOutlined, SafetyCertificateOutlined } from '@ant-design/icons'
import { useAuth } from '../context/AuthContext'

const { Title, Text } = Typography

export default function RegisterPage() {
  const navigate = useNavigate()
  const googleButtonRef = useRef(null)
  const [email, setEmail] = useState('')
  const [otpStep, setOtpStep] = useState(false)
  const { requestRegistrationOtp, verifyRegistration, resendRegistration, googleLogin, loading } = useAuth()
  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID

  const onFinish = async (values) => {
    try {
      await requestRegistrationOtp(values.name, values.email, values.password)
      setEmail(values.email)
      setOtpStep(true)
      message.success('Mã OTP và liên kết kích hoạt đã được gửi đến email của bạn')
    } catch (err) {
      message.error(err.response?.data?.message || 'Đăng ký thất bại')
    }
  }

  const onVerifyOtp = async (values) => {
    try {
      await verifyRegistration(email, values.otp)
      message.success('Đăng ký thành công!')
      navigate('/vocabulary')
    } catch (err) {
      message.error(err.response?.data?.message || 'Xác thực OTP thất bại')
    }
  }

  const onResend = async () => {
    try {
      await resendRegistration(email)
      message.success('Đã gửi lại mã OTP và liên kết kích hoạt')
    } catch (err) {
      message.error(err.response?.data?.message || 'Không thể gửi lại email kích hoạt')
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
            message.success('Đăng ký/Đăng nhập Google thành công!')
            navigate('/vocabulary')
          } catch (err) {
            message.error(err.response?.data?.message || 'Đăng ký Google thất bại')
          }
        },
      })
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        theme: 'outline',
        size: 'large',
        width: googleButtonRef.current.offsetWidth,
        text: 'signup_with',
      })
    }

    if (window.google?.accounts?.id) {
      renderGoogleButton()
      return
    }

    const existingScript = document.querySelector('script[src="https://accounts.google.com/gsi/client"]')
    if (existingScript) {
      existingScript.addEventListener('load', renderGoogleButton, { once: true })
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
          <Title level={2} style={{ color: '#c41e3a' }}>Đăng ký</Title>
          <Text type="secondary">Tạo tài khoản mới</Text>
        </div>
        {!otpStep ? (
          <Form onFinish={onFinish} size="large">
            <Form.Item name="name" rules={[
              { required: true, message: 'Nhập họ tên' },
              { min: 2, message: 'Toi thieu 2 ky tu' },
              { max: 100, message: 'Toi da 100 ky tu' },
            ]}>
              <Input prefix={<UserOutlined />} placeholder="Họ tên" />
            </Form.Item>
            <Form.Item name="email" rules={[
              { required: true, message: 'Nhập email' },
              { type: 'email', message: 'Email không hợp lệ' },
            ]}>
              <Input prefix={<MailOutlined />} placeholder="Email" />
            </Form.Item>
            <Form.Item name="password" rules={[
              { required: true, message: 'Nhập mật khẩu' },
              { min: 6, message: 'Toi thieu 6 ky tu' },
            ]}>
              <Input.Password prefix={<LockOutlined />} placeholder="Mật khẩu" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#c41e3a' }}>
                Gửi mã OTP và liên kết kích hoạt
              </Button>
            </Form.Item>
          </Form>
        ) : (
          <Form onFinish={onVerifyOtp} size="large">
            <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
              Kiểm tra email {email}. Bạn có thể nhập OTP hoặc bấm liên kết kích hoạt trong email.
            </Text>
            <Form.Item name="otp" rules={[
              { required: true, message: 'Nhập mã OTP' },
              { pattern: /^\d{6}$/, message: 'OTP gồm 6 chữ số' },
            ]}>
              <Input prefix={<SafetyCertificateOutlined />} placeholder="Mã OTP" maxLength={6} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#c41e3a' }}>
                Xác nhận đăng ký
              </Button>
            </Form.Item>
            <Button type="link" block onClick={() => setOtpStep(false)}>
              Đổi email đăng ký
            </Button>
            <Button type="link" block loading={loading} onClick={onResend}>
              Gửi lại email kích hoạt
            </Button>
          </Form>
        )}

        {googleClientId && (
          <>
            <Divider plain>Hoac</Divider>
            <div style={{ marginBottom: 16 }}>
              <div ref={googleButtonRef} />
            </div>
          </>
        )}

        <div style={{ textAlign: 'center' }}>
          <Text>Đã có tài khoản? <Link to="/login">Đăng nhập</Link></Text>
        </div>
      </Card>
    </div>
  )
}
