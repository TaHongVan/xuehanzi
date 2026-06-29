import { useState, useEffect } from 'react'

import { Card, Select, Button, Typography, Space, Tag, message, Empty } from 'antd'

import { CheckOutlined, ReloadOutlined, UndoOutlined } from '@ant-design/icons'

import { sentenceApi, topicApi } from '../api/services'



const { Title, Text } = Typography



const isPunctuation = (word) => /^[。，？！、；：""''（）\s]+$/.test(word)



const makeWordItems = (words) =>

  words.map((word, index) => ({ id: `${word}-${index}`, word }))



export default function SentencePage() {

  const [exercises, setExercises] = useState([])

  const [current, setCurrent] = useState(0)

  const [selected, setSelected] = useState([])

  const [shuffled, setShuffled] = useState([])

  const [result, setResult] = useState(null)

  const [loading, setLoading] = useState(false)

  const [topics, setTopics] = useState([])

  const [filters, setFilters] = useState({ hsk: null, topic: null })



  useEffect(() => { topicApi.list().then(r => setTopics(r.data.data)).catch(() => {}) }, [])



  const loadExercises = async () => {

    setLoading(true)

    try {

      const params = {}

      if (filters.hsk) params.hsk = filters.hsk

      if (filters.topic) params.topic = filters.topic

      const { data: res } = await sentenceApi.getExercises(params)

      setExercises(res.data)

      setCurrent(0)

      resetExercise(res.data[0])

    } catch {

      message.error('Không thể tải bài tập')

    } finally {

      setLoading(false)

    }

  }



  const resetExercise = (exercise) => {

    if (!exercise) return

    setShuffled(makeWordItems(exercise.shuffledWords))

    setSelected([])

    setResult(null)

  }



  const selectWord = (item) => {

    setSelected(prev => [...prev, item])

    setShuffled(prev => prev.filter(w => w.id !== item.id))

  }



  const unselectWord = (item) => {

    setShuffled(prev => [...prev, item])

    setSelected(prev => prev.filter(w => w.id !== item.id))

  }



  const checkAnswer = async () => {

    const ex = exercises[current]

    if (!ex || selected.length === 0) return message.warning('Hãy sắp xếp các từ')

    try {

      const { data: res } = await sentenceApi.check({

        sentenceId: ex.sentenceId,

        arrangedSentence: selected.map(s => s.word).join(''),

      })

      setResult(res.data)

      if (res.data.correct) message.success(res.data.feedback)

      else message.warning(res.data.feedback)

    } catch {

      message.error('Kiểm tra thất bại')

    }

  }



  const nextExercise = () => {

    const next = current + 1

    if (next < exercises.length) {

      setCurrent(next)

      resetExercise(exercises[next])

    } else {

      message.success('Hoàn thành tất cả bài tập!')

    }

  }



  const renderWordTag = (item, onClick, inAnswer = false) => {

    const isPunc = isPunctuation(item.word)

    return (

      <Tag

        key={item.id}

        className="word-chip chinese-text"

        color={inAnswer ? (isPunc ? 'default' : 'red') : (isPunc ? 'default' : undefined)}

        onClick={() => onClick(item)}

        style={{

          fontSize: isPunc ? 20 : 22,

          padding: '6px 14px',

          borderStyle: isPunc ? 'dashed' : 'solid',

          opacity: isPunc ? 0.85 : 1,

        }}

      >

        {item.word}

      </Tag>

    )

  }



  const ex = exercises[current]



  return (

    <div className="page-container">

      <Title level={3}>Sắp xếp câu</Title>

      <Card style={{ marginBottom: 16 }}>

        <Space wrap>

          <Select placeholder="HSK Level" allowClear style={{ width: 120 }}

            onChange={(v) => setFilters(f => ({ ...f, hsk: v }))}

            options={[1,2,3,4,5,6].map(n => ({ value: n, label: `HSK ${n}` }))} />

          <Select placeholder="Chủ đề" allowClear style={{ width: 180 }}

            onChange={(v) => setFilters(f => ({ ...f, topic: v }))}

            options={topics.map(t => ({ value: t.id, label: t.name }))} />

          <Button type="primary" icon={<ReloadOutlined />} onClick={loadExercises} loading={loading}

            style={{ background: '#c41e3a' }}>Bắt đầu</Button>

        </Space>

      </Card>



      {!ex ? (

        <Empty description="Chọn bộ lọc và nhấn Bắt đầu" />

      ) : (

        <Card>

          <Text type="secondary">Bài {current + 1}/{exercises.length}</Text>

          <div style={{ textAlign: 'center', margin: '24px 0' }}>

            <Tag color="red">HSK {ex.hskLevel}</Tag>

            <Title level={3}>{ex.vietnameseSentence}</Title>

            <Text type="secondary">Nhấn từng từ Hán để sắp xếp thành câu đúng (dấu câu là ô riêng)</Text>

          </div>



          <Text strong style={{ display: 'block', marginBottom: 8 }}>Câu của bạn:</Text>

          <div style={{

            minHeight: 70, border: '2px dashed #c41e3a', borderRadius: 8,

            padding: 12, marginBottom: 16, display: 'flex', flexWrap: 'wrap', gap: 8,

            background: '#fff9f9',

          }}>

            {selected.length === 0 ? (

              <Text type="secondary">Chọn các từ bên dưới...</Text>

            ) : (

              selected.map(item => renderWordTag(item, unselectWord, true))

            )}

          </div>



          <Text strong style={{ display: 'block', marginBottom: 8 }}>Các từ gợi ý:</Text>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 24 }}>

            {shuffled.map(item => renderWordTag(item, selectWord))}

          </div>



          {result && !result.correct && (

            <div style={{ textAlign: 'center', marginBottom: 16 }}>

              <Text type="danger">

                {result.feedback} — Đáp án: <span className="chinese-text">{result.correctSentence}</span>

              </Text>

            </div>

          )}



          <div style={{ textAlign: 'center' }}>

            <Space>

              <Button icon={<UndoOutlined />} onClick={() => resetExercise(ex)}>Làm lại</Button>

              <Button type="primary" icon={<CheckOutlined />} onClick={checkAnswer}

                style={{ background: '#c41e3a' }}>Kiểm tra</Button>

              {result?.correct && (

                <Button type="primary" onClick={nextExercise} style={{ background: '#52c41a' }}>

                  Tiếp theo

                </Button>

              )}

            </Space>

          </div>

        </Card>

      )}

    </div>

  )

}


