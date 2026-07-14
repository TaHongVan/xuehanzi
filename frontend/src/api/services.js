import api from './axios'

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  verifyRegistration: (data) => api.post('/auth/register/verify', data),
  verifyRegistrationLink: (data) => api.post('/auth/register/verify-link', data),
  resendRegistration: (data) => api.post('/auth/register/resend', data),
  forgotPassword: (data) => api.post('/auth/forgot-password', data),
  resetPassword: (data) => api.post('/auth/reset-password', data),
  googleLogin: (data) => api.post('/auth/google', data),
}

export const vocabularyApi = {
  list: (params) => api.get('/vocabularies', { params }),
  stats: () => api.get('/vocabularies/stats'),
  getById: (id) => api.get(`/vocabularies/${id}`),
  updateStatus: (id, status) => api.patch(`/vocabularies/${id}/status`, null, { params: { status } }),
}

export const topicApi = {
  list: () => api.get('/topics'),
}

export const testApi = {
  getQuestions: (params) => api.get('/test/questions', { params }),
  submit: (data) => api.post('/test/submit', data),
}

export const sentenceApi = {
  getExercises: (params) => api.get('/sentence/exercises', { params }),
  check: (data) => api.post('/sentence/check', data),
}

export const chatApi = {
  send: (data) => api.post('/chat', data),
  getConversations: () => api.get('/chat/conversations'),
  getConversation: (id) => api.get(`/chat/conversations/${id}`),
  deleteConversation: (id) => api.delete(`/chat/conversations/${id}`),
}

export const handwritingApi = {
  check: (data) => api.post('/handwriting/check', data),
}

export const adminApi = {
  // Vocabulary
  listVocabularies: (params) => api.get('/admin/vocabularies', { params }),
  getVocabulary: (id) => api.get(`/admin/vocabularies/${id}`),
  createVocabulary: (data) => api.post('/admin/vocabularies', data),
  updateVocabulary: (id, data) => api.put(`/admin/vocabularies/${id}`, data),
  deleteVocabulary: (id) => api.delete(`/admin/vocabularies/${id}`),
  downloadVocabTemplate: () => api.get('/admin/template', { responseType: 'blob' }),
  uploadVocabExcel: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/admin/upload-excel', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  // Sentences
  listSentences: (params) => api.get('/admin/sentences', { params }),
  getSentence: (id) => api.get(`/admin/sentences/${id}`),
  createSentence: (data) => api.post('/admin/sentences', data),
  updateSentence: (id, data) => api.put(`/admin/sentences/${id}`, data),
  deleteSentence: (id) => api.delete(`/admin/sentences/${id}`),
  segmentSentence: (data) => api.post('/admin/sentences/segment', data),
  downloadSentenceTemplate: () => api.get('/admin/sentence-template', { responseType: 'blob' }),
  uploadSentenceExcel: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/admin/upload-sentence-excel', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
