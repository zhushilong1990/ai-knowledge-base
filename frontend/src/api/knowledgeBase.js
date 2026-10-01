import api from './http.js'

export function getKnowledgeBases() {
  return api.get('/knowledge-bases')
}

export function createKnowledgeBase(data) {
  return api.post('/knowledge-bases', data)
}

export function getDocuments(kbId) {
  return api.get(`/knowledge-bases/${kbId}/documents`)
}

export function deleteDocument(docId) {
  return api.delete(`/documents/${docId}`)
}
