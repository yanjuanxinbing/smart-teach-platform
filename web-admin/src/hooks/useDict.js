import { ref, onMounted } from 'vue'
import { dictDataList } from '@/api/system'

/**
 * 加载并缓存数据字典
 * 用法: const dict = useDict('course_type', 'exp_type')
 * 模板中: dict.course_type 即为 [{ value, label }]
 */
const cache = new Map()

export const useDict = (...types) => {
  const result = {}
  types.forEach(t => { result[t] = ref([]); cache.set(t, result[t]) })

  const load = async () => {
    for (const t of types) {
      try {
        const list = await dictDataList(t)
        result[t].value = list.map(d => ({ value: d.dictValue, label: d.dictLabel, raw: d }))
      } catch (e) {
        result[t].value = []
      }
    }
  }
  load()
  return result
}
