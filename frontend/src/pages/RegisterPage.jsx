import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Card, Form, Input, Button, Typography, message } from 'antd'
import { UserOutlined, LockOutlined, MailOutlined, SafetyCertificateOutlined } from '@ant-design/icons'
import { useAuth } from '../context/AuthContext'

const { Title, Text } = Typography

export default function RegisterPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [otpStep, setOtpStep] = useState(false)
  const { requestRegistrationOtp, verifyRegistration, loading } = useAuth()

  const onFinish = async (values) => {
    try {
      await requestRegistrationOtp(values.name, values.email, values.password)
      setEmail(values.email)
      setOtpStep(true)
      message.success('Ma OTP da duoc gui den email cua ban')
    } catch (err) {
      message.error(err.response?.data?.message || 'Dang ky that bai')
    }
  }

  const onVerifyOtp = async (values) => {
    try {
      await verifyRegistration(email, values.otp)
      message.success('Dang ky thanh cong!')
      navigate('/vocabulary')
    } catch (err) {
      message.error(err.response?.data?.message || 'Xac thuc OTP that bai')
    }
  }

  return (
    <div className="auth-container">
      <Card className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2} style={{ color: '#c41e3a' }}>Dang ky</Title>
          <Text type="secondary">Tao tai khoan moi</Text>
        </div>
        {!otpStep ? (
          <Form onFinish={onFinish} size="large">
            <Form.Item name="name" rules={[
              { required: true, message: 'Nhap ho ten' },
              { min: 2, message: 'Toi thieu 2 ky tu' },
              { max: 100, message: 'Toi da 100 ky tu' },
            ]}>
              <Input prefix={<UserOutlined />} placeholder="Ho ten" />
            </Form.Item>
            <Form.Item name="email" rules={[
              { required: true, message: 'Nhap email' },
              { type: 'email', message: 'Email khong hop le' },
            ]}>
              <Input prefix={<MailOutlined />} placeholder="Email" />
            </Form.Item>
            <Form.Item name="password" rules={[
              { required: true, message: 'Nhap mat khau' },
              { min: 6, message: 'Toi thieu 6 ky tu' },
            ]}>
              <Input.Password prefix={<LockOutlined />} placeholder="Mat khau" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#c41e3a' }}>
                Gui ma OTP
              </Button>
            </Form.Item>
          </Form>
        ) : (
          <Form onFinish={onVerifyOtp} size="large">
            <Form.Item name="otp" rules={[
              { required: true, message: 'Nhap ma OTP' },
              { pattern: /^\d{6}$/, message: 'OTP gom 6 chu so' },
            ]}>
              <Input prefix={<SafetyCertificateOutlined />} placeholder="Ma OTP" maxLength={6} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#c41e3a' }}>
                Xac nhan dang ky
              </Button>
            </Form.Item>
            <Button type="link" block onClick={() => setOtpStep(false)}>
              Doi email dang ky
            </Button>
          </Form>
        )}
        <div style={{ textAlign: 'center' }}>
          <Text>Da co tai khoan? <Link to="/login">Dang nhap</Link></Text>
        </div>
      </Card>
    </div>
  )
}
