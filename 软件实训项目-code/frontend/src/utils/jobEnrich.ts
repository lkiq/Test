/**
 * 岗位数据增强工具
 * 由于后端 job_position 表字段有限，前端通过本工具补全真实求职平台常见的展示字段。
 * 后续后端扩展字段后，可直接去掉补全逻辑，优先使用接口返回字段。
 */

export interface EnrichedJob {
  id: number
  jobId: number
  title: string
  direction: string
  jd: string
  city: string
  salaryRange: string
  salaryMin?: number
  salaryMax?: number
  companyName: string
  companyLogo?: string
  companyIndustry?: string
  companyScale?: string
  experience: string
  education: string
  jobType: string
  welfare: string[]
  publishTime: string
  publishTimeLabel: string
  hrName: string
  hrTitle: string
  hrUserId?: number
  replyRate: number
  matchScore?: number
  skillTags?: any[]
  skillGaps?: any[]
}

const companies: Record<string, string[]> = {
  '后端开发': ['字节跳动', '阿里巴巴', '腾讯', '美团', '京东', '华为', '小米', '蔚来汽车', 'Shopee', '滴滴'],
  '前端开发': ['蚂蚁集团', '拼多多', '快手', '哔哩哔哩', '小红书', '网易', '携程', '微博', '知乎', '货拉拉'],
  '数据': ['百度', '京东数科', '平安科技', '招商金科', '神策数据', '贝壳找房', '欢聚集团', '深信服', '金山云', '奇安信'],
  '产品': ['腾讯产品', '字节产品', '美团产品', '百度产品', '滴滴产品', '网易产品', '携程产品', '京东产品', '阿里产品', '快手产品'],
  '测试': ['用友网络', '金蝶软件', '中兴通讯', '海康威视', '大华股份', '科大讯飞', '商汤科技', '旷视科技', '依图科技', '第四范式'],
  '移动开发': ['OPPO', 'vivo', '荣耀', '一加', '魅族', '传音控股', '大疆创新', '米哈游', '莉莉丝游戏', '叠纸游戏'],
  '运维': ['阿里云', '腾讯云', '华为云', '金山办公', '奇虎360', '盛趣游戏', '完美世界', '巨人网络', '搜狐', '新浪'],
  '安全': ['奇安信', '深信服', '绿盟科技', '天融信', '启明星辰', '安恒信息', '知道创宇', '长亭科技', '默安科技', '青藤云安全'],
  '设计': ['Figma中国', '墨刀', '即时设计', ' Canva可画', '特赞', 'ARK创新咨询', '唐硕体验咨询', 'EICO设计', ' Frog设计', 'IDEO'],
  '管理': ['华为', '中兴通讯', '联想', '海尔', '美的', '格力', 'TCL', '海信', '创维', '京东方'],
  '嵌入式': ['大疆创新', '海康威视', '大华股份', '汇川技术', '紫光展锐', '全志科技', '瑞芯微', '乐鑫科技', '移远通信', '广和通'],
  '': ['互联网科技公司', '创新科技企业', '知名互联网公司', '数字化企业', '行业头部企业']
}

const industries = ['互联网', '人工智能', '金融科技', '电子商务', '企业服务', '智能硬件', '游戏', '新能源汽车', '医疗健康', '教育科技']
const scales = ['100-499人', '500-999人', '1000-9999人', '10000人以上', '20-99人']
const experiences = ['经验不限', '1年以内', '1-3年', '3-5年', '5-10年', '10年以上']
const educations = ['学历不限', '大专', '本科', '硕士', '博士']
const jobTypes = ['全职', '实习', '兼职', '校招']
const welfarePool = ['五险一金', '带薪年假', '绩效奖金', '股票期权', '年终奖', '餐补', '交通补助', '通讯补贴', '节日福利', '定期体检', '零食下午茶', '免费班车', '远程办公', '扁平管理', '技术大牛', '弹性工作']
const hrNames = ['王女士', '李先生', '张小姐', '陈先生', '刘女士', '赵先生', '孙女士', '周先生', '吴小姐', '郑先生']
const hrTitles = ['HR', '招聘专员', 'HRBP', '招聘经理', '资深HR']

function hashIndex(seed: number, mod: number) {
  return Math.abs((seed * 9301 + 49297) % 233280) % mod
}

function parseSalary(salaryRange?: string): [number, number] {
  if (!salaryRange) return [0, 0]
  const match = salaryRange.match(/(\d+)K?\s*-\s*(\d+)K?/)
  if (!match) return [0, 0]
  return [parseInt(match[1]), parseInt(match[2])]
}

export function formatTimeAgo(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}

export function enrichJob(raw: any): EnrichedJob {
  const id = Number(raw.jobId || raw.id || 0)
  const direction = raw.direction || '后端开发'
  const companyList = companies[direction] || companies['']
  const companyName = raw.companyName || companyList[hashIndex(id, companyList.length)]
  const [salaryMin, salaryMax] = parseSalary(raw.salaryRange)
  const welfareCount = 3 + hashIndex(id + 7, 4)
  const welfare = raw.welfare
    ? (typeof raw.welfare === 'string' ? raw.welfare.split(/[,，]/).filter(Boolean) : raw.welfare)
    : Array.from({ length: welfareCount }, (_, i) => welfarePool[hashIndex(id + i * 13, welfarePool.length)])

  // 优先使用后端返回的真实数据，没有时降级随机生成
  const publishTime = raw.publishTime || new Date(Date.now() - hashIndex(id, 30) * 86400000).toISOString()

  return {
    id,
    jobId: id,
    title: raw.title || '未知岗位',
    direction,
    jd: raw.jd || '',
    city: raw.city || '全国',
    salaryRange: raw.salaryRange || '薪资面议',
    salaryMin,
    salaryMax,
    companyName,
    companyLogo: raw.companyLogo,
    companyIndustry: raw.companyIndustry || industries[hashIndex(id, industries.length)],
    companyScale: raw.companyScale || scales[hashIndex(id + 3, scales.length)],
    // 后端字段: experienceRequired / educationRequired
    experience: raw.experienceRequired || raw.experience || experiences[hashIndex(id + 5, experiences.length)],
    education: raw.educationRequired || raw.education || educations[hashIndex(id + 11, educations.length)],
    jobType: raw.jobType || jobTypes[hashIndex(id + 17, jobTypes.length)],
    welfare,
    publishTime,
    publishTimeLabel: formatTimeAgo(publishTime),
    hrName: raw.hrName || hrNames[hashIndex(id + 23, hrNames.length)],
    hrTitle: raw.hrTitle || hrTitles[hashIndex(id + 29, hrTitles.length)],
    hrUserId: raw.hrUserId ?? undefined,
    replyRate: raw.replyRate ?? (70 + hashIndex(id + 31, 25)),
    matchScore: raw.matchScore ?? undefined,
    skillTags: raw.skillTags || [],
    skillGaps: raw.skillGaps || []
  }
}

export function filterJobs(jobs: EnrichedJob[], filters: any): EnrichedJob[] {
  return jobs.filter(job => {
    if (filters.keyword && !job.title.toLowerCase().includes(filters.keyword.toLowerCase()) && !job.companyName.includes(filters.keyword)) return false
    if (filters.city && job.city !== filters.city) return false
    if (filters.direction && filters.direction.length && !filters.direction.includes(job.direction)) return false
    if (filters.experience && job.experience !== filters.experience) return false
    if (filters.education && job.education !== filters.education) return false
    if (filters.jobType && job.jobType !== filters.jobType) return false
    if (filters.salaryRange) {
      const [min, max] = filters.salaryRange
      if (job.salaryMax && (job.salaryMax < min || job.salaryMin! > max)) return false
    }
    if (filters.skill && filters.skill.length) {
      const tags = job.skillTags?.map((t: any) => (typeof t === 'string' ? t : t.skillName)) || []
      if (!filters.skill.some((s: string) => tags.includes(s))) return false
    }
    return true
  })
}

export function sortJobs(jobs: EnrichedJob[], sort: string) {
  const list = [...jobs]
  if (sort === 'salaryDesc') {
    list.sort((a, b) => (b.salaryMax || 0) - (a.salaryMax || 0))
  } else if (sort === 'salaryAsc') {
    list.sort((a, b) => (a.salaryMax || 0) - (b.salaryMax || 0))
  } else if (sort === 'newest') {
    list.sort((a, b) => new Date(b.publishTime).getTime() - new Date(a.publishTime).getTime())
  } else if (sort === 'match') {
    list.sort((a, b) => (b.matchScore || 0) - (a.matchScore || 0))
  }
  return list
}
