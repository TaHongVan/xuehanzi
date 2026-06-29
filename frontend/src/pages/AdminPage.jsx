import { useState, useEffect } from 'react'

import {

  Tabs, Card, Upload, Button, Typography, Table, message, Alert, Space,

  Modal, Form, Input, Select, Popconfirm, Tag, InputNumber,

} from 'antd'

import {

  PlusOutlined, EditOutlined, DeleteOutlined, DownloadOutlined,

  InboxOutlined, ReloadOutlined,

} from '@ant-design/icons'

import { adminApi, topicApi } from '../api/services'



const { Title, Text } = Typography

const { Dragger } = Upload



const downloadBlob = (response, filename) => {

  const url = window.URL.createObjectURL(new Blob([response.data]))

  const link = document.createElement('a')

  link.href = url

  link.setAttribute('download', filename)

  document.body.appendChild(link)

  link.click()

  link.remove()

}



const ImportResult = ({ result }) => {

  if (!result) return null

  return (

    <Card title="Kết quả import" size="small" style={{ marginTop: 16 }}>

      <Space direction="vertical">

        <Text>Tổng dòng: <strong>{result.totalRows}</strong></Text>

        <Text type="success">Thêm mới: <strong>{result.imported}</strong></Text>

        <Text type="warning">Ghi đè: <strong>{result.updated || 0}</strong></Text>

        <Text type="secondary">Bỏ qua: <strong>{result.skipped}</strong></Text>

        {result.errors?.length > 0 && (

          <Table size="small" pagination={false}

            dataSource={result.errors.map((e, i) => ({ key: i, error: e }))}

            columns={[{ title: 'Lỗi', dataIndex: 'error' }]} />

        )}

      </Space>

    </Card>

  )

}



// ===================== Vocabulary Tab =====================

function VocabularyTab({ topics }) {

  const [data, setData] = useState([])

  const [loading, setLoading] = useState(false)

  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 })

  const [modalOpen, setModalOpen] = useState(false)

  const [editing, setEditing] = useState(null)

  const [importResult, setImportResult] = useState(null)

  const [uploading, setUploading] = useState(false)

  const [form] = Form.useForm()



  const fetchData = async (page = 0, size = 10) => {

    setLoading(true)

    try {

      const { data: res } = await adminApi.listVocabularies({ page, size })

      const p = res.data

      setData(p.content)

      setPagination({ current: p.page + 1, pageSize: p.size, total: p.totalElements })

    } catch {

      message.error('Không thể tải từ vựng')

    } finally {

      setLoading(false)

    }

  }



  useEffect(() => { fetchData() }, [])



  const openCreate = () => {

    setEditing(null)

    form.resetFields()

    form.setFieldsValue({ hskLevel: 1 })

    setModalOpen(true)

  }



  const openEdit = (record) => {

    setEditing(record)

    form.setFieldsValue({

      chineseWord: record.chineseWord,

      pinyin: record.pinyin,

      meaning: record.meaning,

      example: record.example,

      hskLevel: record.hskLevel,

      topicId: record.topicId,

    })

    setModalOpen(true)

  }



  const handleSave = async () => {

    try {

      const values = await form.validateFields()

      if (editing) {

        await adminApi.updateVocabulary(editing.id, values)

        message.success('Cập nhật thành công')

      } else {

        await adminApi.createVocabulary(values)

        message.success('Thêm từ vựng thành công')

      }

      setModalOpen(false)

      fetchData(pagination.current - 1, pagination.pageSize)

    } catch (err) {

      if (err.response) message.error(err.response.data?.message || 'Lưu thất bại')

    }

  }



  const handleDelete = async (id) => {

    try {

      await adminApi.deleteVocabulary(id)

      message.success('Đã xóa')

      fetchData(pagination.current - 1, pagination.pageSize)

    } catch {

      message.error('Xóa thất bại')

    }

  }



  const handleUpload = async (file) => {

    setUploading(true)

    try {

      const { data: res } = await adminApi.uploadVocabExcel(file)

      setImportResult(res.data)

      message.success(`Import: ${res.data.imported} mới, ${res.data.updated || 0} ghi đè`)

      fetchData(pagination.current - 1, pagination.pageSize)

    } catch (err) {

      message.error(err.response?.data?.message || 'Upload thất bại')

    } finally {

      setUploading(false)

    }

    return false

  }



  const columns = [

    { title: 'Chữ Hán', dataIndex: 'chineseWord', render: t => <span className="chinese-text">{t}</span> },

    { title: 'Pinyin', dataIndex: 'pinyin' },

    { title: 'Nghĩa', dataIndex: 'meaning', ellipsis: true },

    { title: 'HSK', dataIndex: 'hskLevel', width: 70, render: l => <Tag color="red">HSK{l}</Tag> },

    { title: 'Chủ đề', dataIndex: 'topicName' },

    {

      title: 'Thao tác', width: 120,

      render: (_, record) => (

        <Space>

          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(record)} />

          <Popconfirm title="Xóa từ vựng này?" onConfirm={() => handleDelete(record.id)}>

            <Button type="link" danger icon={<DeleteOutlined />} />

          </Popconfirm>

        </Space>

      ),

    },

  ]



  return (

    <div>

      <Alert message="Trùng Chinese + Topic sẽ được ghi đè khi import Excel" type="info" showIcon style={{ marginBottom: 16 }} />

      <Space style={{ marginBottom: 16 }} wrap>

        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate} style={{ background: '#c41e3a' }}>

          Thêm từ vựng

        </Button>

        <Button icon={<DownloadOutlined />} onClick={() =>

          adminApi.downloadVocabTemplate().then(r => downloadBlob(r, 'vocabulary_template.xlsx'))

        }>Tải template Excel</Button>

        <Button icon={<ReloadOutlined />} onClick={() => fetchData(pagination.current - 1, pagination.pageSize)}>Làm mới</Button>

      </Space>



      <Card style={{ marginBottom: 16 }}>

        <Dragger accept=".xlsx,.xls" showUploadList={false} beforeUpload={handleUpload} disabled={uploading}>

          <p className="ant-upload-drag-icon"><InboxOutlined /></p>

          <p className="ant-upload-text">Import từ vựng từ Excel (ghi đè nếu trùng)</p>

        </Dragger>

        <ImportResult result={importResult} />

      </Card>



      <Table columns={columns} dataSource={data} rowKey="id" loading={loading}

        pagination={{ ...pagination, showSizeChanger: true, showTotal: t => `Tổng ${t} từ` }}

        onChange={pag => fetchData(pag.current - 1, pag.pageSize)} />



      <Modal title={editing ? 'Sửa từ vựng' : 'Thêm từ vựng'} open={modalOpen}

        onOk={handleSave} onCancel={() => setModalOpen(false)} okText="Lưu" cancelText="Hủy" width={560}>

        <Form form={form} layout="vertical">

          <Form.Item name="chineseWord" label="Chữ Hán" rules={[{ required: true }]}>

            <Input className="chinese-text" placeholder="你好" />

          </Form.Item>

          <Form.Item name="pinyin" label="Pinyin" rules={[{ required: true }]}>

            <Input placeholder="nǐ hǎo" />

          </Form.Item>

          <Form.Item name="meaning" label="Nghĩa (Tiếng Việt)" rules={[{ required: true }]}>

            <Input placeholder="Xin chào" />

          </Form.Item>

          <Form.Item name="example" label="Ví dụ">

            <Input.TextArea rows={2} placeholder="你好，我是小明。" />

          </Form.Item>

          <Form.Item name="hskLevel" label="HSK Level" rules={[{ required: true }]}>

            <Select options={[1,2,3,4,5,6].map(n => ({ value: n, label: `HSK ${n}` }))} />

          </Form.Item>

          <Form.Item name="topicId" label="Chủ đề" rules={[{ required: true }]}>

            <Select options={topics.map(t => ({ value: t.id, label: t.name }))} placeholder="Chọn chủ đề" />

          </Form.Item>

        </Form>

      </Modal>

    </div>

  )

}



// ===================== Sentence Tab =====================

function SentenceTab({ topics }) {

  const [data, setData] = useState([])

  const [loading, setLoading] = useState(false)

  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 })

  const [modalOpen, setModalOpen] = useState(false)

  const [editing, setEditing] = useState(null)

  const [importResult, setImportResult] = useState(null)

  const [uploading, setUploading] = useState(false)

  const [segments, setSegments] = useState([])

  const [form] = Form.useForm()



  const fetchData = async (page = 0, size = 10) => {

    setLoading(true)

    try {

      const { data: res } = await adminApi.listSentences({ page, size })

      const p = res.data

      setData(p.content)

      setPagination({ current: p.page + 1, pageSize: p.size, total: p.totalElements })

    } catch {

      message.error('Không thể tải câu')

    } finally {

      setLoading(false)

    }

  }



  useEffect(() => { fetchData() }, [])



  const autoSegment = async () => {

    const chinese = form.getFieldValue('chineseSentence')

    if (!chinese) return message.warning('Nhập câu tiếng Trung trước')

    try {

      const { data: res } = await adminApi.segmentSentence({ chineseSentence: chinese })

      setSegments(res.data)

      form.setFieldValue('wordSegments', res.data.join(' | '))

    } catch {

      message.error('Tách từ thất bại')

    }

  }



  const openCreate = () => {

    setEditing(null)

    setSegments([])

    form.resetFields()

    form.setFieldsValue({ hskLevel: 1 })

    setModalOpen(true)

  }



  const openEdit = (record) => {

    setEditing(record)

    setSegments(record.wordSegments || [])

    form.setFieldsValue({

      chineseSentence: record.chineseSentence,

      vietnameseSentence: record.vietnameseSentence,

      wordSegments: (record.wordSegments || []).join(' | '),

      hskLevel: record.hskLevel,

      topicId: record.topicId,

    })

    setModalOpen(true)

  }



  const handleSave = async () => {

    try {

      const values = await form.validateFields()

      const wordSegments = values.wordSegments

        ? values.wordSegments.split('|').map(s => s.trim()).filter(Boolean)

        : segments

      const payload = {

        chineseSentence: values.chineseSentence,

        vietnameseSentence: values.vietnameseSentence,

        wordSegments,

        hskLevel: values.hskLevel,

        topicId: values.topicId,

      }

      if (editing) {

        await adminApi.updateSentence(editing.id, payload)

        message.success('Cập nhật thành công')

      } else {

        await adminApi.createSentence(payload)

        message.success('Thêm câu thành công')

      }

      setModalOpen(false)

      fetchData(pagination.current - 1, pagination.pageSize)

    } catch (err) {

      if (err.response) message.error(err.response.data?.message || 'Lưu thất bại')

    }

  }



  const handleDelete = async (id) => {

    try {

      await adminApi.deleteSentence(id)

      message.success('Đã xóa')

      fetchData(pagination.current - 1, pagination.pageSize)

    } catch {

      message.error('Xóa thất bại')

    }

  }



  const handleUpload = async (file) => {

    setUploading(true)

    try {

      const { data: res } = await adminApi.uploadSentenceExcel(file)

      setImportResult(res.data)

      message.success(`Import: ${res.data.imported} mới, ${res.data.updated || 0} ghi đè`)

      fetchData(pagination.current - 1, pagination.pageSize)

    } catch (err) {

      message.error(err.response?.data?.message || 'Upload thất bại')

    } finally {

      setUploading(false)

    }

    return false

  }



  const columns = [

    { title: 'Câu tiếng Trung', dataIndex: 'chineseSentence', render: t => <span className="chinese-text">{t}</span> },

    { title: 'Câu tiếng Việt', dataIndex: 'vietnameseSentence', ellipsis: true },

    {

      title: 'Tách từ', dataIndex: 'wordSegments', width: 200,

      render: segs => segs?.map((s, i) => <Tag key={i} className="chinese-text">{s}</Tag>),

    },

    { title: 'HSK', dataIndex: 'hskLevel', width: 70, render: l => <Tag color="red">HSK{l}</Tag> },

    { title: 'Chủ đề', dataIndex: 'topicName' },

    {

      title: 'Thao tác', width: 120,

      render: (_, record) => (

        <Space>

          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(record)} />

          <Popconfirm title="Xóa câu này?" onConfirm={() => handleDelete(record.id)}>

            <Button type="link" danger icon={<DeleteOutlined />} />

          </Popconfirm>

        </Space>

      ),

    },

  ]



  return (

    <div>

      <Alert message="Trùng câu tiếng Trung sẽ được ghi đè khi import Excel. Tách từ tự động khi lưu." type="info" showIcon style={{ marginBottom: 16 }} />

      <Space style={{ marginBottom: 16 }} wrap>

        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate} style={{ background: '#c41e3a' }}>

          Thêm câu

        </Button>

        <Button icon={<DownloadOutlined />} onClick={() =>

          adminApi.downloadSentenceTemplate().then(r => downloadBlob(r, 'sentence_template.xlsx'))

        }>Tải template Excel</Button>

        <Button icon={<ReloadOutlined />} onClick={() => fetchData(pagination.current - 1, pagination.pageSize)}>Làm mới</Button>

      </Space>



      <Card style={{ marginBottom: 16 }}>

        <Dragger accept=".xlsx,.xls" showUploadList={false} beforeUpload={handleUpload} disabled={uploading}>

          <p className="ant-upload-drag-icon"><InboxOutlined /></p>

          <p className="ant-upload-text">Import câu từ Excel (ghi đè nếu trùng)</p>

        </Dragger>

        <ImportResult result={importResult} />

      </Card>



      <Table columns={columns} dataSource={data} rowKey="id" loading={loading}

        pagination={{ ...pagination, showSizeChanger: true, showTotal: t => `Tổng ${t} câu` }}

        onChange={pag => fetchData(pag.current - 1, pag.pageSize)} />



      <Modal title={editing ? 'Sửa câu' : 'Thêm câu'} open={modalOpen}

        onOk={handleSave} onCancel={() => setModalOpen(false)} okText="Lưu" cancelText="Hủy" width={600}>

        <Form form={form} layout="vertical">

          <Form.Item name="chineseSentence" label="Câu tiếng Trung" rules={[{ required: true }]}>

            <Input className="chinese-text" placeholder="我明天去北京。" onBlur={autoSegment} />

          </Form.Item>

          <Form.Item name="vietnameseSentence" label="Câu tiếng Việt" rules={[{ required: true }]}>

            <Input placeholder="Ngày mai tôi đi Bắc Kinh." />

          </Form.Item>

          <Form.Item name="wordSegments" label="Tách từ (cách nhau bởi |)"

            extra="Nhấn 'Tách từ tự động' hoặc nhập tay, ví dụ: 我 | 明天 | 去 | 北京 | 。">

            <Input.TextArea rows={2} placeholder="我 | 明天 | 去 | 北京 | 。" />

          </Form.Item>

          <Button onClick={autoSegment} style={{ marginBottom: 16 }}>Tách từ tự động</Button>

          {segments.length > 0 && (

            <div style={{ marginBottom: 16 }}>

              {segments.map((s, i) => <Tag key={i} className="chinese-text">{s}</Tag>)}

            </div>

          )}

          <Form.Item name="hskLevel" label="HSK Level" rules={[{ required: true }]}>

            <Select options={[1,2,3,4,5,6].map(n => ({ value: n, label: `HSK ${n}` }))} />

          </Form.Item>

          <Form.Item name="topicId" label="Chủ đề" rules={[{ required: true }]}>

            <Select options={topics.map(t => ({ value: t.id, label: t.name }))} placeholder="Chọn chủ đề" />

          </Form.Item>

        </Form>

      </Modal>

    </div>

  )

}



// ===================== Main Admin Page =====================

export default function AdminPage() {

  const [topics, setTopics] = useState([])



  useEffect(() => {

    topicApi.list().then(r => setTopics(r.data.data)).catch(() => {})

  }, [])



  const items = [

    { key: 'vocab', label: 'Quản lý từ vựng', children: <VocabularyTab topics={topics} /> },

    { key: 'sentence', label: 'Quản lý câu (Sắp xếp)', children: <SentenceTab topics={topics} /> },

  ]



  return (

    <div className="page-container">

      <Title level={3}>Quản trị hệ thống</Title>

      <Tabs items={items} />

    </div>

  )

}


