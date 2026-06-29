import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { Button, Space, Typography } from 'antd'
import {
  BookOutlined,
  FireOutlined,
  FormOutlined,
  MessageOutlined,
  OrderedListOutlined,
  SettingOutlined,
  SwapOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { useAuth } from '../context/AuthContext'
import { vocabularyApi } from '../api/services'

const { Text } = Typography

const navItems = [
    { key: '/vocabulary', icon: <BookOutlined />, label: 'Từ vựng' },
  { key: '/test', icon: <SwapOutlined />, label: 'Flashcard' },
  { key: '/sentence', icon: <OrderedListOutlined />, label: 'Sắp xếp câu' },
  { key: '/handwriting', icon: <FormOutlined />, label: 'Luyện viết' },
  // { key: '/chat', icon: <MessageOutlined />, label: 'Luyen noi' },
]

export default function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { logout, isAdmin, user } = useAuth()
  const [stats, setStats] = useState({ masteredCount: 0, todayCount: 0 })
  const visibleNavItems = isAdmin
    ? [...navItems, { key: '/admin', icon: <SettingOutlined />, label: 'Quan tri' }]
    : navItems

  useEffect(() => {
    vocabularyApi.stats()
      .then(({ data }) => setStats(data.data || { masteredCount: 0, todayCount: 0 }))
      .catch(() => setStats({ masteredCount: 0, todayCount: 0 }))
  }, [location.pathname])

  return (
    <div className="app-shell">
      <header className="app-topbar">
        <button className="brand-lockup" onClick={() => navigate('/vocabulary')} type="button">
          <span className="brand-mark">Han</span>
          <span>
            <strong>HanLearn</strong>
          </span>
        </button>

        <Space size={16} className="header-stats">
          <span className="stat-pill">Từ đã học: <strong>{stats.masteredCount || 0}</strong></span>
          <span className="stat-pill">Hôm nay: <strong>{stats.todayCount || 0}</strong></span>
          {/*<span className="streak-pill"><FireOutlined /> 7 ngay</span>*/}
        </Space>
      </header>

      <nav className="feature-nav" aria-label="Chức năng hoc">
        {visibleNavItems.map(item => {
          const active = location.pathname === item.key || location.pathname.startsWith(`${item.key}/`)
          return (
            <button
              key={item.key}
              type="button"
              className={`feature-nav-item${active ? ' active' : ''}`}
              disabled={item.disabled}
              onClick={() => !item.disabled && navigate(item.key)}
            >
              {item.icon}
              <span>{item.label}</span>
              {item.badge && <Text className="ai-badge">{item.badge}</Text>}
            </button>
          )
        })}
        <span className="user-inline"><UserOutlined /> {user?.username}</span>
        <Button className="logout-inline" type="text" onClick={() => { logout(); navigate('/login') }}>
          Dang xuat
        </Button>
      </nav>

      <main className="app-content">
        <Outlet />
      </main>
    </div>
  )
}
