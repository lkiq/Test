/** 工具函数 */

/** 日期格式化 */
export function formatDate(date: string | Date, fmt = 'yyyy-MM-dd HH:mm') {
  const d = new Date(date)
  const o: Record<string, number> = {
    'M+': d.getMonth() + 1, 'd+': d.getDate(), 'H+': d.getHours(), 'm+': d.getMinutes(), 's+': d.getSeconds()
  }
  let result = fmt
  if (/(y+)/.test(result)) result = result.replace(RegExp.$1, String(d.getFullYear()).substring(4 - RegExp.$1.length))
  for (const k in o) {
    if (new RegExp(`(${k})`).test(result)) {
      result = result.replace(RegExp.$1, RegExp.$1.length === 1 ? String(o[k]) : `00${o[k]}`.substring(String(o[k]).length))
    }
  }
  return result
}

/** 防抖 */
export function debounce<T extends (...args: any[]) => any>(fn: T, delay: number) {
  let timer: ReturnType<typeof setTimeout>
  return (...args: Parameters<T>) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}
