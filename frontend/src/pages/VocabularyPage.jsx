import { useEffect, useMemo, useState } from 'react'
import { Button, Empty, Input, message, Pagination, Select, Spin, Tag, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import {
  FormOutlined,
  SoundOutlined,
  SwapOutlined,
} from '@ant-design/icons'
import { topicApi, vocabularyApi } from '../api/services'

const { Title, Text } = Typography

const statusClass = { NEW: 'new', LEARNING: 'learning', MASTERED: 'mastered' }
const statusOptions = [
  { value: 'NEW', label: 'Mới' },
  { value: 'LEARNING', label: 'Đang học' },
  { value: 'MASTERED', label: 'Đã thuộc' },
]

export default function VocabularyPage() {
  const navigate = useNavigate()
  const [data, setData] = useState([])
  const [topics, setTopics] = useState([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({ current: 1, pageSize: 12, total: 0 })
  const [filters, setFilters] = useState({ status: null, hsk: null, topic: null, keyword: '' })

  const fetchTopics = async () => {
    try {
      const { data: res } = await topicApi.list()
      setTopics(res.data || [])
    } catch {
      setTopics([])
    }
  }

  useEffect(() => {
    fetchTopics()
  }, [])

  const sidebarTopics = useMemo(() => {
    const apiTopics = topics.map((topic) => ({
      id: topic.id,
      name: topic.name,
      icon: topic.icon || '📚',
      count: Number(topic.vocabularyCount ?? topic.count ?? topic.total ?? 0),
    }))

    return [
      {
        id: null,
        name: 'Tất cả',
        icon: '🌐',
        count: apiTopics.reduce((sum, topic) => sum + topic.count, 0) || pagination.total,
      },
      ...apiTopics,
    ]
  }, [topics, pagination.total])

  const fetchData = async (page = 0, size = pagination.pageSize) => {
    setLoading(true)
    try {
      const params = { page, size }
      if (filters.status) params.status = filters.status
      if (filters.hsk) params.hsk = filters.hsk
      if (filters.topic) params.topic = filters.topic
      if (filters.keyword) params.keyword = filters.keyword

      const { data: res } = await vocabularyApi.list(params)
      const pageData = res.data
      setData(pageData.content || [])
      setPagination({
        current: (pageData.page ?? page) + 1,
        pageSize: pageData.size ?? size,
        total: pageData.totalElements ?? 0,
      })
    } catch {
      message.error('Không thể tải từ vựng')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData(0, pagination.pageSize)
  }, [filters])

  const handleStatusChange = async (word, status) => {
    const previousData = data
    setData(current => current.map(item => item.id === word.id ? { ...item, status } : item))
    try {
      await vocabularyApi.updateStatus(word.id, status)
      message.success('Đã cập nhật trạng thái')
      fetchData(pagination.current - 1, pagination.pageSize)
    } catch {
      setData(previousData)
      message.error('Cập nhật thất bại')
    }
  }

  const speakWord = (word) => {
    if (!window.speechSynthesis) {
      message.warning('Trình duyệt không hỗ trợ phát âm')
      return
    }

    const utterance = new SpeechSynthesisUtterance(word.chineseWord)
    utterance.lang = 'zh-CN'
    window.speechSynthesis.speak(utterance)
  }

  const currentTopic = sidebarTopics.find(topic => topic.id === filters.topic)
  const learnedCount = data.filter(word => word.status === 'MASTERED').length
  const nextStatus = { NEW: 'LEARNING', LEARNING: 'MASTERED', MASTERED: 'NEW' }

  const rotateStatus = (word) => {
    handleStatusChange(word, nextStatus[word.status] || 'LEARNING')
  }

  const openHandwriting = (word) => {
    navigate(`/handwriting${word.chineseWord ? `?word=${encodeURIComponent(word.chineseWord)}` : ''}`)
  }

  return (
    <div className="vocab-workspace">
      <aside className="topic-sidebar">
        <Text className="sidebar-label">CHỦ ĐỀ</Text>
        <div className="topic-list">
          {sidebarTopics.map(topic => (
            <button
              key={topic.id ?? 'all'}
              type="button"
              className={`topic-item${filters.topic === topic.id ? ' active' : ''}`}
              onClick={() => setFilters(f => ({ ...f, topic: topic.id }))}
            >
              <span className="topic-name">
                <span>{topic.icon}</span>
                {topic.name}
              </span>
              <span className="topic-count">{topic.count}</span>
            </button>
          ))}
        </div>
      </aside>

      <section className="vocab-main">
        <div className="vocab-heading">
          <div>
            <Title level={2}>Từ vựng · <span>{currentTopic?.name || 'Tất cả'}</span></Title>
            <Text type="secondary">{learnedCount} từ đã thuộc trong trang hiện tại</Text>
          </div>
          <div className="vocab-actions">
            <Button icon={<SwapOutlined />} onClick={() => navigate('/test')}>
              Ôn tập Flashcard
            </Button>
          </div>
        </div>

        <div className="vocab-filters">
          <Input
            placeholder="Tìm từ... (#拼音 hoặc nghĩa)"
            value={filters.keyword}
            onChange={(e) => setFilters(f => ({ ...f, keyword: e.target.value }))}
            allowClear
          />
          <Select
            placeholder="Tất cả trạng thái"
            allowClear
            value={filters.status}
            onChange={(v) => setFilters(f => ({ ...f, status: v }))}
            options={statusOptions}
          />
          <Select
            placeholder="Tất cả HSK"
            allowClear
            value={filters.hsk}
            onChange={(v) => setFilters(f => ({ ...f, hsk: v }))}
            options={[1, 2, 3, 4, 5, 6].map(n => ({ value: n, label: `HSK ${n}` }))}
          />
        </div>

        <Spin spinning={loading}>
          {data.length === 0 ? (
            <Empty className="vocab-empty" description="Chưa có từ vựng phù hợp" />
          ) : (
            <div className="vocab-grid">
              {data.map(word => (
                <article key={word.id} className={`word-card ${statusClass[word.status] || 'new'}`}>
                  <div className="word-chinese">{word.chineseWord}</div>
                  <div className="word-pinyin">{word.pinyin}</div>
                  <Text className="word-meaning">{word.meaning}</Text>
                  <div className="word-tags">
                    <Tag color="blue">HSK {word.hskLevel}</Tag>
                    <Select
                      className={`status-select ${statusClass[word.status] || 'new'}`}
                      size="small"
                      value={word.status || 'NEW'}
                      onChange={(value) => handleStatusChange(word, value)}
                      options={statusOptions}
                    />
                  </div>
                  <div className="word-tools">
                    <div>
                      <Button size="small" icon={<SoundOutlined />} onClick={() => speakWord(word)} />
                      <Button size="small" icon={<SwapOutlined />} onClick={() => rotateStatus(word)} />
                      <Button size="small" icon={<FormOutlined />} onClick={() => openHandwriting(word)} />
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </Spin>

        {pagination.total > pagination.pageSize && (
          <Pagination
            className="vocab-pagination"
            current={pagination.current}
            pageSize={pagination.pageSize}
            total={pagination.total}
            showSizeChanger
            onChange={(page, size) => fetchData(page - 1, size)}
          />
        )}
      </section>
    </div>
  )
}
