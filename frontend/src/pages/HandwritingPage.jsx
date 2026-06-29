import { useEffect, useMemo, useRef, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Button, Card, message, Select, Space, Tag, Tooltip, Typography } from 'antd'
import {
  ClearOutlined,
  EyeInvisibleOutlined,
  EyeOutlined,
  HeartOutlined,
  HighlightOutlined,
  InfoCircleOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  SoundOutlined,
} from '@ant-design/icons'
import HanziWriter from 'hanzi-writer'
import { topicApi, vocabularyApi } from '../api/services'

const { Title, Text } = Typography

const WRITER_SIZE = 260
const WRITING_STROKE_COLOR = '#0077c8'

function HanziPracticeTile({ character, index, showTemplate, wordInfo, onHideTemplate }) {
  const writerHostRef = useRef(null)
  const writerRef = useRef(null)
  const quizRef = useRef(false)
  const [isQuizMode, setIsQuizMode] = useState(false)
  const [result, setResult] = useState(null)

  useEffect(() => {
    if (!writerHostRef.current) return

    writerHostRef.current.innerHTML = ''
    quizRef.current = false
    setIsQuizMode(false)
    setResult(null)

    writerRef.current = HanziWriter.create(writerHostRef.current, character, {
      width: WRITER_SIZE,
      height: WRITER_SIZE,
      padding: 18,
      showOutline: showTemplate,
      showCharacter: showTemplate,
      strokeColor: WRITING_STROKE_COLOR,
      radicalColor: WRITING_STROKE_COLOR,
      outlineColor: '#e8dfd6',
      drawingColor: WRITING_STROKE_COLOR,
      highlightColor: WRITING_STROKE_COLOR,
      drawingWidth: 30,
      strokeAnimationSpeed: 1,
      delayBetweenStrokes: 120,
    })
  }, [character])

  useEffect(() => {
    const writer = writerRef.current
    if (!writer) return

    if (showTemplate) {
      writer.showOutline({ duration: 150 })
      writer.showCharacter({ duration: 150 })
    } else {
      writer.hideOutline({ duration: 150 })
      writer.hideCharacter({ duration: 150 })
    }
  }, [showTemplate])

  const speakCharacter = () => {
    if (!('speechSynthesis' in window)) {
      message.warning('Trinh duyet khong ho tro phat am')
      return
    }
    window.speechSynthesis.cancel()
    const utterance = new SpeechSynthesisUtterance(character)
    utterance.lang = 'zh-CN'
    utterance.rate = 0.75
    window.speechSynthesis.speak(utterance)
  }

  const animateCharacter = () => {
    const writer = writerRef.current
    if (!writer) return
    quizRef.current = false
    setIsQuizMode(false)
    setResult(null)
    writer.cancelQuiz()
    writer.animateCharacter()
  }

  const startQuiz = () => {
    const writer = writerRef.current
    if (!writer) return

    quizRef.current = true
    setIsQuizMode(true)
    setResult({ mistakes: 0, strokes: 0, complete: false })
    writer.quiz({
      showHintAfterMisses: 2,
      highlightOnComplete: true,
      leniency: 1.15,
      onCorrectStroke: (summaryData) => {
        setResult(current => ({
          mistakes: current?.mistakes || 0,
          strokes: summaryData?.totalStrokes || current?.strokes || 0,
          complete: false,
        }))
      },
      onMistake: (strokeData) => {
        setResult(current => ({
          mistakes: strokeData?.totalMistakes || (current?.mistakes || 0) + 1,
          strokes: current?.strokes || 0,
          complete: false,
        }))
      },
      onComplete: (summaryData) => {
        quizRef.current = false
        setIsQuizMode(false)
        setResult({
          mistakes: summaryData?.totalMistakes || 0,
          strokes: summaryData?.totalStrokes || 0,
          complete: true,
        })
        message.success(`Da viet dung chu ${character}`)
      },
    })
  }

  const resetPractice = () => {
    writerRef.current?.cancelQuiz()
    writerRef.current?.setCharacter(character)
    onHideTemplate()
    setTimeout(() => startQuiz(), 0)
  }

  return (
    <div className="writer-tile">
      <div className="writer-tile-heading">
        <span className="writer-tile-character">{character}</span>
        <Text type="secondary">Chu {index + 1}</Text>
      </div>
      <div className="writer-board">
        <div className="writer-grid" />
        <div ref={writerHostRef} className="hanzi-writer-host" />
      </div>
      <div className="writer-icon-row">
        <Tooltip title="Hien cach viet tung net">
          <Button icon={<PlayCircleOutlined />} onClick={animateCharacter} />
        </Tooltip>
        <Tooltip title="Luyen viet theo net chu mau">
          <Button className={isQuizMode ? 'writer-icon-active' : ''} icon={<HighlightOutlined />} onClick={startQuiz} />
        </Tooltip>
        <Tooltip title="Nghe phat am chu dang viet">
          <Button icon={<SoundOutlined />} onClick={speakCharacter} />
        </Tooltip>
        <Tooltip title="Xoa va viet lai">
          <Button icon={<ClearOutlined />} onClick={resetPractice} />
        </Tooltip>
        <Tooltip title={wordInfo ? `${wordInfo.pinyin} - ${wordInfo.meaning}` : 'Thong tin chu'}>
          <Button icon={<InfoCircleOutlined />} />
        </Tooltip>
        <Tooltip title="Danh dau yeu thich">
          <Button icon={<HeartOutlined />} />
        </Tooltip>
      </div>
      {result && (
        <div className="writer-tile-result">
          <Tag color={result.complete ? 'success' : 'processing'}>
            {result.complete ? 'Hoan thanh' : 'Dang luyen'}
          </Tag>
          <Text type="secondary">Dung: {result.strokes || 0} net</Text>
          <Text type="secondary">Sai: {result.mistakes || 0}</Text>
        </div>
      )}
    </div>
  )
}

export default function HandwritingPage() {
  const [searchParams] = useSearchParams()
  const queryText = searchParams.get('word') || searchParams.get('char') || '你'
  const [practiceText, setPracticeText] = useState(queryText)
  const [vocabList, setVocabList] = useState([])
  const [topics, setTopics] = useState([])
  const [filters, setFilters] = useState({ hsk: null, topic: null })
  const [showTemplate, setShowTemplate] = useState(true)

  useEffect(() => {
    topicApi.list()
      .then(r => setTopics(r.data.data || []))
      .catch(() => {})
  }, [])

  useEffect(() => {
    const params = { page: 0, size: 100 }
    if (filters.hsk) params.hsk = filters.hsk
    if (filters.topic) params.topic = filters.topic

    vocabularyApi.list(params)
      .then(r => {
        const words = r.data.data.content || []
        setVocabList(words)
        if (words.length && !words.some(v => v.chineseWord === practiceText)) {
          setPracticeText(words[0].chineseWord)
        }
      })
      .catch(() => message.error('Khong the tai danh sach chu luyen viet'))
  }, [filters])

  useEffect(() => {
    const nextText = searchParams.get('word') || searchParams.get('char')
    if (nextText) setPracticeText(nextText)
  }, [searchParams])

  const selectedWord = useMemo(
    () => vocabList.find(v => v.chineseWord === practiceText) || vocabList.find(v => v.chineseWord?.startsWith(practiceText)),
    [practiceText, vocabList]
  )

  const practiceChars = useMemo(
    () => [...(practiceText || '')].filter(char => /\p{Script=Han}/u.test(char)),
    [practiceText]
  )

  const randomWord = () => {
    if (vocabList.length === 0) return
    const random = vocabList[Math.floor(Math.random() * vocabList.length)]
    setPracticeText(random.chineseWord)
  }

  const speakWord = () => {
    if (!('speechSynthesis' in window)) {
      message.warning('Trinh duyet khong ho tro phat am')
      return
    }
    window.speechSynthesis.cancel()
    const utterance = new SpeechSynthesisUtterance(practiceText)
    utterance.lang = 'zh-CN'
    utterance.rate = 0.75
    window.speechSynthesis.speak(utterance)
  }

  const toggleTemplate = () => {
    setShowTemplate(current => !current)
    message.info('Van nhan dien theo du lieu chu mau du mau dang an')
  }

  return (
    <div className="page-container handwriting-page">
      <div className="page-title-row">
        <div>
          <Title level={3}>Luyen viet chu Han</Title>
          <Text type="secondary">
            Tu ghep se hien du tung chu Han. Moi chu co khung rieng va net viet co mau rieng de de nhin.
          </Text>
        </div>
        <Space wrap>
          <Button icon={<SoundOutlined />} onClick={speakWord}>Doc tu</Button>
          <Button icon={showTemplate ? <EyeOutlined /> : <EyeInvisibleOutlined />} onClick={toggleTemplate}>
            {showTemplate ? 'Tat mau' : 'Hien mau'}
          </Button>
          <Button icon={<ReloadOutlined />} onClick={randomWord}>Ngau nhien</Button>
        </Space>
      </div>

      <div className="handwriting-layout">
        <Card className="practice-card">
          <div className="practice-header">
            <div>
              <Text type="secondary">Tu dang luyen</Text>
              <div className="practice-character practice-word">{practiceText}</div>
              {selectedWord && (
                <Text className="practice-meaning">
                  {selectedWord.pinyin} - {selectedWord.meaning}
                </Text>
              )}
            </div>
            <Space wrap className="practice-selectors">
              <Select
                placeholder="Tat ca HSK"
                allowClear
                value={filters.hsk}
                onChange={(hsk) => setFilters(current => ({ ...current, hsk }))}
                style={{ width: 130 }}
                options={[1, 2, 3, 4, 5, 6].map(n => ({ value: n, label: `HSK ${n}` }))}
              />
              <Select
                placeholder="Tat ca chu de"
                allowClear
                value={filters.topic}
                onChange={(topic) => setFilters(current => ({ ...current, topic }))}
                style={{ width: 170 }}
                options={topics.map(topic => ({ value: topic.id, label: topic.name }))}
              />
              <Select
                value={practiceText}
                onChange={(value) => setPracticeText(value)}
                style={{ width: 260 }}
                showSearch
                optionFilterProp="label"
                options={vocabList.map(v => ({
                  value: v.chineseWord,
                  label: `${v.chineseWord} - ${v.meaning}`,
                }))}
              />
            </Space>
          </div>

          <div className="writer-tile-grid">
            {practiceChars.map((char, index) => (
              <HanziPracticeTile
                key={`${practiceText}-${index}-${char}`}
                character={char}
                index={index}
                showTemplate={showTemplate}
                wordInfo={selectedWord}
                onHideTemplate={() => setShowTemplate(false)}
              />
            ))}
          </div>
        </Card>

        <Card className="practice-result" title="Huong dan">
          <Text type="secondary">
            Moi o dai dien cho mot chu Han trong tu. Bam play de xem thu tu net, bam icon but de viet theo net mau.
            Khi tat mau, he thong van cham dung theo form chu mau cua tung chu.
          </Text>
        </Card>
      </div>
    </div>
  )
}
