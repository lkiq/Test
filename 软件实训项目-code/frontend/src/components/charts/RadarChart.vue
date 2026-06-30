<template>
  <div ref="chartRef" style="width:100%;height:350px;"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{ data: any }>()
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

onMounted(() => { if (chartRef.value) chart = echarts.init(chartRef.value); renderChart() })
watch(() => props.data, renderChart, { deep: true })

function renderChart() {
  if (!chart || !props.data) return
  chart.setOption({
    radar: {
      indicator: (props.data.dimensions || []).map((d: string) => ({ name: d, max: 100 })),
      center: ['50%', '55%'],
      radius: '70%'
    },
    series: [{
      type: 'radar',
      data: [
        { name: '你的水平', value: props.data.userValues || [], areaStyle: { color: 'rgba(64,158,255,0.2)' }, lineStyle: { color: '#409EFF' }, itemStyle: { color: '#409EFF' } },
        { name: '岗位要求', value: props.data.requiredValues || [], areaStyle: { color: 'rgba(245,108,108,0.2)' }, lineStyle: { color: '#F56C6C' }, itemStyle: { color: '#F56C6C' } }
      ]
    }],
    legend: { bottom: 0 },
    tooltip: {}
  })
}
</script>
