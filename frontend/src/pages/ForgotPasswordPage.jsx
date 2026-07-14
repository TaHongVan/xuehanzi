import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button, Card, Form, Input, Result, Typography, message } from 'antd'
import { MailOutlined } from '@ant-design/icons'
import { authApi } from '../api/services'

const { Title, Text } = Typography

export default function ForgotPasswordPage() {
  const [loading, setLoading] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  const onFinish = async ({ email }) => {
    setLoading(true)
    try {
      await authApi.forgotPassword({ email })
      setSubmitted(true)
    } catch (err) {
      message.error(err.response?.data?.message || 'Không thể gửi email. Vui lòng thử lại.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <Card className="auth-card">
        {submitted ? (
          <Result
            status="success"
            title="Kiểm tra email của bạn"
            subTitle="Nếu email tồn tại trong hệ thống, Hanzii đã gửi liên kết đặt lại mật khẩu. Liên kết có hiệu lực trong 15 phút."
            extra={<Link to="/login"><Button type="primary">Quay lại đăng nhập</Button></Link>}
          />
        ) : (
          <>
            <div style={{ textAlign: 'center', marginBottom: 24 }}>
              <Title level={2} style={{ color: '#c41e3a' }}>Quên mật khẩu</Title>
              <Text type="secondary">Nhập email đã đăng ký để nhận liên kết đặt lại mật khẩu.</Text>
            </div>

            <Form onFinish={onFinish} size="large" layout="vertical">
              <Form.Item
                name="email"
                label="Email"
                rules={[
                  { required: true, message: 'Nhập email của bạn' },
                  { type: 'email', message: 'Email không hợp lệ' },
                ]}
              >
                <Input prefix={<MailOutlined />} placeholder="email@example.com" autoComplete="email" />
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#c41e3a' }}>
                  Gửi liên kết đặt lại mật khẩu
                </Button>
              </Form.Item>
            </Form>

            <div style={{ textAlign: 'center' }}>
              <Link to="/login">Quay lại đăng nhập</Link>
            </div>
          </>
        )}
      </Card>
    </div>
  )
}
