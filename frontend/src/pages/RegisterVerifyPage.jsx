import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { Button, Card, Result, Spin, Typography, message } from 'antd'
import { useAuth } from '../context/AuthContext'

const { Text } = Typography

export default function RegisterVerifyPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const hasVerified = useRef(false)
  const [status, setStatus] = useState(token ? 'loading' : 'missing')
  const [errorMessage, setErrorMessage] = useState('')
  const { verifyRegistrationLink } = useAuth()

  useEffect(() => {
    if (!token || hasVerified.current) return
    hasVerified.current = true

    const verify = async () => {
      try {
        await verifyRegistrationLink(token)
        setStatus('success')
        message.success('Kích hoạt tài khoản thành công!')
        setTimeout(() => navigate('/vocabulary'), 900)
      } catch (err) {
        setErrorMessage(err.response?.data?.message || 'Liên kết kích hoạt không hợp lệ hoặc đã hết hạn')
        setStatus('error')
      }
    }

    verify()
  }, [navigate, token, verifyRegistrationLink])

  return (
    <div className="auth-container">
      <Card className="auth-card">
        {status === 'loading' && (
          <div style={{ textAlign: 'center', padding: '32px 0' }}>
            <Spin size="large" />
            <Text type="secondary" style={{ display: 'block', marginTop: 16 }}>
              Đang kích hoạt tài khoản...
            </Text>
          </div>
        )}

        {status === 'success' && (
          <Result
            status="success"
            title="Tài khoản đã được kích hoạt"
            subTitle="Bạn đang được chuyển vào Hanzii."
            extra={<Button type="primary" onClick={() => navigate('/vocabulary')}>Vào học ngay</Button>}
          />
        )}

        {status === 'error' && (
          <Result
            status="error"
            title="Không thể kích hoạt"
            subTitle={errorMessage}
            extra={<Link to="/register"><Button type="primary">Quay lại đăng ký</Button></Link>}
          />
        )}

        {status === 'missing' && (
          <Result
            status="warning"
            title="Thiếu token kích hoạt"
            subTitle="Vui lòng mở đúng liên kết trong email kích hoạt."
            extra={<Link to="/register"><Button type="primary">Quay lại đăng ký</Button></Link>}
          />
        )}
      </Card>
    </div>
  )
}
