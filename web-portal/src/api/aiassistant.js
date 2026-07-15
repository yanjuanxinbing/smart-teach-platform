import request from '@/utils/request'

/**
 * AI 解题 - 学生发起提问（异步，一次返回完整结果）
 *   POST /portal/aiassistant/solve
 *   入参: { prompt: string, sessionId?: number }
 *   出参: { sessionId, answer, model, durationMs, prompt, createdAt }
 */
export const solveQuestion = (data) => request.post('/portal/aiassistant/solve', data)

/**
 * AI 解题 - 流式提问（fetch + ReadableStream 解析 SSE）
 *  onChunk({delta, sessionId}) —— 每收到一个片段回调一次
 *  返回 AbortController，可在外层 abort() 中断
 *  异常时 onError 被调，最后返回 {sessionId, aborted}
 */
export async function streamQuestion({ prompt, sessionId, onChunk, signal }) {
  const token = localStorage.getItem('portal_token')
  const params = new URLSearchParams({ prompt })
  if (sessionId) params.set('sessionId', sessionId)
  const url = `/api/portal/aiassistant/stream?${params.toString()}`

  const resp = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': 'Bearer ' + token,
      'Accept': 'text/event-stream'
    },
    signal
  })

  if (!resp.ok || !resp.body) {
    let msg = `HTTP ${resp.status}`
    try {
      const t = await resp.text()
      try { msg = JSON.parse(t).message || msg } catch { msg = t || msg }
    } catch {}
    throw new Error(msg)
  }

  const reader = resp.body.getReader()
  const decoder = new TextDecoder()
  let buf = ''
  let lastSessionId = sessionId || null

  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buf += decoder.decode(value, { stream: true })

    // SSE 帧: "data: <...>\n\n"
    let idx
    while ((idx = buf.indexOf('\n\n')) >= 0) {
      const frame = buf.slice(0, idx)
      buf = buf.slice(idx + 2)
      const m = /^data:\s?(.*)$/ms.exec(frame)
      if (m && m[1]) {
        try {
          const payload = JSON.parse(m[1])
          if (payload.sessionId) lastSessionId = payload.sessionId
          if (onChunk) onChunk(payload)
        } catch (_) {
          // 非 JSON，按纯文本片段处理
          if (onChunk) onChunk({ delta: m[1], sessionId: lastSessionId })
        }
      }
    }
  }

  return { sessionId: lastSessionId, aborted: signal?.aborted === true }
}

/**
 * AI 解题 - 我的会话列表
 *   GET /portal/aiassistant/history?pageNum=1&pageSize=20
 */
export const listHistory = (params) =>
  request.get('/portal/aiassistant/history', { params })

/**
 * AI 解题 - 某会话的完整消息流水
 *   GET /portal/aiassistant/history/{sessionId}/messages
 */
export const sessionMessages = (sessionId) =>
  request.get(`/portal/aiassistant/history/${sessionId}/messages`)

/**
 * AI 解题 - 删除某个会话
 *   DELETE /portal/aiassistant/history/{sessionId}
 */
export const removeSession = (sessionId) =>
  request.delete(`/portal/aiassistant/history/${sessionId}`)
