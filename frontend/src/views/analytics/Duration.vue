<template>
  <div class="analytics-page">
    <div class="page-header">
      <h2>停留时长分析</h2>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <el-select v-model="storeId" placeholder="选择门店" clearable style="width: 180px">
        <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
        start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
        style="width: 260px" />
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon> 查询
      </el-button>
      <el-button type="success" @click="handleExport">
        <el-icon><Download /></el-icon> 导出Excel
      </el-button>
    </div>

    <!-- 汇总卡片 -->
    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="12">
        <div class="stat-card">
          <div class="stat-label">整体平均停留时长</div>
          <div class="stat-value">{{ overallAvg }} <span style="font-size:16px">分钟</span></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="stat-card">
          <div class="stat-label">数据天数</div>
          <div class="stat-value">{{ dailyData.length }} <span style="font-size:16px">天</span></div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <div class="chart-container" ref="chartRef" v-loading="loading">
      <el-empty v-if="!loading && dailyData.length === 0" description="暂无停留时长数据" />
    </div>

    <!-- 数据表 -->
    <el-table :data="dailyData" border stripe style="margin-top: 20px">
      <el-table-column prop="date_key" label="日期" align="center" />
      <el-table-column prop="total_count" label="客流" align="center" />
      <el-table-column label="平均停留" align="center">
        <template #default="{ row }">{{ row.avg_duration_min || 0 }} 分钟</template>
      </el-table-column>
      <el-table-column prop="purchase_count" label="购买人数" align="center" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { Search, Download } from '@element-plus/icons-vue'
import { getDuration } from '@/api/behavior'
import { getStoreList } from '@/api/store'
import { exportReport } from '@/api/export'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const loading = ref(false)
const chartRef = ref(null)
let chart = null

const storeList = ref([])
const storeId = ref(null)
const dateRange = ref(null)
const dailyData = ref([])
const overallAvg = ref(0)

async function loadStores() {
  try {
    const res = await getStoreList({ page: 1, size: 100 })
    storeList.value = res.data?.list || res.data?.records || []
  } catch (e) { /* ignore */ }
}

async function loadData() {
  loading.value = true
  try {
    const params = { storeId: storeId.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getDuration(params)
    const result = res.data || {}
    dailyData.value = result.dailyData || []
    overallAvg.value = result.overallAvgDuration || 0
    await nextTick()
    renderChart()
  } catch (e) {
    ElMessage.error('加载停留时长数据失败')
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || dailyData.value.length === 0) return
  if (!chart) chart = echarts.init(chartRef.value)

  const keys = dailyData.value.map(d => d.date_key)
  const durations = dailyData.value.map(d => d.avg_duration_min || 0)
  const visitors = dailyData.value.map(d => d.total_count || 0)

  chart.setOption({
    title: { text: '停留时长趋势', left: 'center' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均停留(分钟)', '客流量'], bottom: 0 },
    grid: { left: 50, right: 50, top: 50, bottom: 40 },
    xAxis: { type: 'category', data: keys },
    yAxis: [
      { type: 'value', name: '分钟' },
      { type: 'value', name: '人' }
    ],
    series: [
      { name: '平均停留(分钟)', type: 'line', data: durations, itemStyle: { color: '#E6A23C' }, smooth: true, yAxisIndex: 0 },
      { name: '客流量', type: 'bar', data: visitors, itemStyle: { color: '#409EFF' }, barMaxWidth: 30, yAxisIndex: 1 }
    ]
  })

  window.addEventListener('resize', () => chart?.resize())
}

function handleExport() {
  const params = { type: 'duration', storeId: storeId.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  exportReport(params)
}

onMounted(() => loadStores())

onUnmounted(() => {
  chart?.dispose()
})
</script>

<style scoped>
.analytics-page { padding: 0; }
.page-header h2 { margin: 0; font-size: 20px; }

.filter-bar {
  display: flex; gap: 12px; flex-wrap: wrap; align-items: center;
  margin-top: 16px;
}

.stat-card {
  background: #f5f7fa; border-radius: 8px; padding: 20px; text-align: center;
}
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 32px; font-weight: bold; color: #409EFF; }

.chart-container {
  width: 100%; height: 380px; margin-top: 24px;
}
</style>
