import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Button, Card, Form, Input, Result, Typography, message } from 'antd'
import { LockOutlined } from '@ant-design/icons'
import { authApi } from '../api/services'

const { Title, Text } = Typography

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [loading, setLoading] = useState(false)
  const [completed, setCompleted] = useState(false)

  const onFinish = async ({ newPassword }) => {
    setLoading(true)
    try {
      await authApi.resetPassword({ token, newPassword })
      setCompleted(true)
      message.success('Đặt lại mật khẩu thành công!')
    } catch (err) {
      message.error(err.response?.data?.message || 'Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <Card className="auth-card">
        {!token ? (
          <Result
            status="warning"
            title="Thiếu token đặt lại mật khẩu"
            subTitle="Vui lòng mở đúng liên kết được gửi trong email."
            extra={<Link to="/forgot-password"><Button type="primary">Gửi lại email</Button></Link>}
          />
        ) : completed ? (
          <Result
            status="success"
            title="Mật khẩu đã được thay đổi"
            subTitle="Bạn có thể đăng nhập bằng mật khẩu mới."
            extra={<Link to="/login"><Button type="primary">Đăng nhập</Button></Link>}
          />
        ) : (
          <>
            <div style={{ textAlign: 'center', marginBottom: 24 }}>
              <Title level={2} style={{ color: '#c41e3a' }}>Đặt mật khẩu mới</Title>
              <Text type="secondary">Mật khẩu mới phải có ít nhất 6 ký tự.</Text>
            </div>

            <Form onFinish={onFinish} size="large" layout="vertical">
              <Form.Item
                name="newPassword"
                label="Mật khẩu mới"
                rules={[
                  { required: true, message: 'Nhập mật khẩu mới' },
                  { min: 6, max: 100, message: 'Mật khẩu phải có từ 6 đến 100 ký tự' },
                ]}
                hasFeedback
              >
                <Input.Password prefix={<LockOutlined />} autoComplete="new-password" />
              </Form.Item>
              <Form.Item
                name="confirmPassword"
                label="Nhập lại mật khẩu mới"
                dependencies={['newPassword']}
                hasFeedback
                rules={[
                  { required: true, message: 'Nhập lại mật khẩu mới' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      return !value || getFieldValue('newPassword') === value
                        ? Promise.resolve()
                        : Promise.reject(new Error('Mật khẩu nhập lại không khớp'))
                    },
                  }),
                ]}
              >
                <Input.Password prefix={<LockOutlined />} autoComplete="new-password" />
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" loading={loading} block style={{ background: '#c41e3a' }}>
                  Đổi mật khẩu
                </Button>
              </Form.Item>
            </Form>
          </>
        )}
      </Card>
    </div>
  )
}
