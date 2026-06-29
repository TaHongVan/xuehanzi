import { useState, useEffect } from 'react'
import { Card, Select, Button, Input, Typography, Progress, Space, Tag, message, Empty } from 'antd'
import { CheckCircleOutlined, CloseCircleOutlined, ReloadOutlined } from '@ant-design/icons'
import { testApi, topicApi } from '../api/services'

const { Title, Text, Paragraph } = Typography

export default function TestPage() {
  const [questions, setQuestions] = useState([])
  const [current, setCurrent] = useState(0)
  const [answer, setAnswer] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [topics, setTopics] = useState([])
  const [filters, setFilters] = useState({ hsk: null, topic: null })
  const [wrongQueue, setWrongQueue] = useState([])

  useEffect(() => {
    topicApi.list().then(r => setTopics(r.data.data || [])).catch(() => setTopics([]))
  }, [])

  const loadQuestions = async () => {
    setLoading(true)
    try {
      const params = {}
      if (filters.hsk) params.hsk = filters.hsk
      if (filters.topic) params.topic = filters.topic
      const { data: res } = await testApi.getQuestions(params)
      setQuestions(res.data || [])
      setCurrent(0)
      setAnswer('')
      setResult(null)
      setWrongQueue([])
    } catch {
      message.error('Không thể tải câu hỏi')
    } finally {
      setLoading(false)
    }
  }

  const submitAnswer = async () => {
    if (!answer.trim()) {
      message.warning('Nhập câu trả lời')
      return
    }
    const q = questions[current]
    try {
      const { data: res } = await testApi.submit({
        vocabularyId: q.vocabularyId,
        answer: answer.trim(),
      })
      setResult(res.data)
      if (!res.data.correct) {
        setWrongQueue(prev => [...new Set([...prev, current])])
      } else {
        setWrongQueue(prev => prev.filter(i => i !== current))
      }
    } catch {
      message.error('Gửi câu trả lời thất bại')
    }
  }

  const nextQuestion = () => {
    setAnswer('')
    setResult(null)
    if (wrongQueue.length > 0) {
      setCurrent(wrongQueue[0])
    } else if (current < questions.length - 1) {
      setCurrent(current + 1)
    } else {
      message.success('Hoàn thành tất cả câu hỏi!')
    }
  }

  const handleEnter = () => {
    if (result?.correct) {
      nextQuestion()
    } else {
      submitAnswer()
    }
  }

  const q = questions[current]
  const progress = questions.length ? ((current + 1) / questions.length) * 100 : 0
  const mastered = questions.length - wrongQueue.length

  return (
    <div className="page-container">
      <Title level={3}>Kiểm tra từ vựng</Title>
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <Select
            placeholder="HSK Level"
            allowClear
            style={{ width: 120 }}
            onChange={(v) => setFilters(f => ({ ...f, hsk: v }))}
            options={[1, 2, 3, 4, 5, 6].map(n => ({ value: n, label: `HSK ${n}` }))}
          />
          <Select
            placeholder="Chủ đề"
            allowClear
            style={{ width: 180 }}
            onChange={(v) => setFilters(f => ({ ...f, topic: v }))}
            options={topics.map(t => ({ value: t.id, label: t.name }))}
          />
          <Button
            type="primary"
            icon={<ReloadOutlined />}
            onClick={loadQuestions}
            loading={loading}
            style={{ background: '#c41e3a' }}
          >
            Bắt đầu
          </Button>
        </Space>
      </Card>

      {!q ? (
        <Empty description="Chọn bộ lọc và nhấn Bắt đầu" />
      ) : (
        <Card>
          <Progress percent={Math.round(progress)} status="active" style={{ marginBottom: 16 }} />
          <Text type="secondary">Câu {current + 1}/{questions.length} | Đúng: {mastered}</Text>

          <div style={{ textAlign: 'center', margin: '32px 0' }}>
            <Tag color="red">HSK {q.hskLevel}</Tag>
            <Tag>{q.topicName}</Tag>
            <Title level={2} style={{ marginTop: 16 }}>{q.meaning}</Title>
            <Paragraph type="secondary">Viết chữ Hán tương ứng</Paragraph>
          </div>

          <Input
            className="chinese-text"
            size="large"
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            onPressEnter={handleEnter}
            placeholder="Nhập chữ Hán..."
            readOnly={result?.correct}
            style={{ textAlign: 'center', fontSize: 24, marginBottom: 16 }}
          />

          {result && (
            <div style={{ textAlign: 'center', marginBottom: 16 }}>
              {result.correct ? (
                <Text type="success"><CheckCircleOutlined /> {result.feedback}</Text>
              ) : (
                <Text type="danger">
                  <CloseCircleOutlined /> {result.feedback} - Đáp án: <span className="chinese-text">{result.correctAnswer}</span>
                </Text>
              )}
            </div>
          )}

          <div style={{ textAlign: 'center' }}>
            {!result ? (
              <Button type="primary" size="large" onClick={submitAnswer} style={{ background: '#c41e3a' }}>
                Kiểm tra
              </Button>
            ) : (
              <Button type="primary" size="large" onClick={nextQuestion} style={{ background: '#c41e3a' }}>
                {wrongQueue.length > 0 && !result.correct ? 'Thử lại câu sai' : 'Câu tiếp theo'}
              </Button>
            )}
          </div>
        </Card>
      )}
    </div>
  )
}
