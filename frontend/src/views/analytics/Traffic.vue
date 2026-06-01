<template>
  <div class="analytics-page">
    <div class="page-header">
      <h2>客流统计分析</h2>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <el-select v-model="storeId" placeholder="选择门店" clearable style="width: 180px">
        <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
        start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
        style="width: 260px" />
      <el-radio-group v-model="granularity">
        <el-radio-button value="day">按天</el-radio-button>
        <el-radio-button value="hour">按小时</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon> 查询
      </el-button>
      <el-button type="success" @click="handleExport">
        <el-icon><Download /></el-icon> 导出Excel
      </el-button>
    </div>

    <!-- 汇总卡片 -->
    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">总客流量</div>
          <div class="stat-value">{{ summary.totalVisitors }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">购买人数</div>
          <div class="stat-value success">{{ summary.totalPurchases }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">整体转化率</div>
          <div class="stat-value warning">
            {{ summary.totalVisitors > 0 ? (summary.totalPurchases / summary.totalVisitors * 100).toFixed(1) : 0 }}%
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <div class="chart-container" ref="chartRef" v-loading="loading">
      <el-empty v-if="!loading && summary.totalVisitors === 0 && summary.totalPurchases === 0" description="暂无客流数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { Search, Download } from '@element-plus/icons-vue'
import { getTraffic } from '@/api/behavior'
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
const granularity = ref('day')

const summary = reactive({ totalVisitors: 0, totalPurchases: 0 })

async function loadStores() {
  try {
    const res = await getStoreList({ page: 1, size: 100 })
    storeList.value = res.data?.list || res.data?.records || []
  } catch (e) { /* ignore */ }
}

async function loadData() {
  loading.value = true
  try {
    const params = { storeId: storeId.value, granularity: granularity.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getTraffic(params)
    const result = res.data || {}
    summary.totalVisitors = result.totalVisitors || 0
    summary.totalPurchases = result.totalPurchases || 0

    const list = result.data || []
    await nextTick()
    renderChart(list, result.granularity || 'day')
  } catch (e) {
    ElMessage.error('加载客流数据失败')
  } finally {
    loading.value = false
  }
}

function renderChart(data, gran) {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const keys = data.map(d => gran === 'hour' ? (d.time_key || d.hour + ':00') : d.date_key)
  const totals = data.map(d => d.total_count || 0)
  const purchases = data.map(d => d.purchase_count || 0)

  chart.setOption({
    title: { text: '客流趋势', left: 'center' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['总客流', '购买人数'], bottom: 0 },
    grid: { left: 50, right: 30, top: 50, bottom: 40 },
    xAxis: { type: 'category', data: keys, axisLabel: { rotate: gran === 'hour' ? 45 : 0 } },
    yAxis: { type: 'value' },
    series: [
      { name: '总客流', type: 'bar', data: totals, itemStyle: { color: '#409EFF' }, barMaxWidth: 30 },
      { name: '购买人数', type: 'line', data: purchases, itemStyle: { color: '#67C23A' }, smooth: true }
    ]
  })

  window.addEventListener('resize', () => chart?.resize())
}

function handleExport() {
  const params = { type: 'traffic', storeId: storeId.value, granularity: granularity.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  exportReport(params)
}

onMounted(() => {
  loadStores()
  loadData()
})

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
.stat-value.success { color: #67C23A; }
.stat-value.warning { color: #E6A23C; }

.chart-container {
  width: 100%; height: 420px; margin-top: 24px;
}
</style>
