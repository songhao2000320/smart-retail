<template>
  <div class="analytics-page">
    <div class="page-header">
      <h2>购买转化分析</h2>
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

    <!-- 核心指标 -->
    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">总访客</div>
          <div class="stat-value">{{ data.total_visitors || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">购买人数</div>
          <div class="stat-value success">{{ data.purchase_count || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card highlight">
          <div class="stat-label">转化率</div>
          <div class="stat-value warning">{{ data.conversion_rate || 0 }}%</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">平均停留</div>
          <div class="stat-value">{{ data.avg_duration_min || 0 }} <span style="font-size:14px">分钟</span></div>
        </div>
      </el-col>
    </el-row>

    <!-- 转化漏斗 -->
    <div class="chart-container" ref="funnelRef" style="height:300px" v-loading="loading">
      <el-empty v-if="!loading && !data.total_visitors" description="暂无转化数据" />
    </div>

    <!-- 对比 -->
    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="12">
        <div class="stat-card">
          <div class="stat-label">购买客户平均停留</div>
          <div class="stat-value success">{{ data.avg_purchase_duration_min || 0 }} <span style="font-size:14px">分钟</span></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="stat-card">
          <div class="stat-label">未购买客户平均停留</div>
          <div class="stat-value">{{ data.avg_no_purchase_duration_min || 0 }} <span style="font-size:14px">分钟</span></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { Search, Download } from '@element-plus/icons-vue'
import { getConversion } from '@/api/behavior'
import { getStoreList } from '@/api/store'
import { exportReport } from '@/api/export'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const loading = ref(false)
const funnelRef = ref(null)
let chart = null

const storeList = ref([])
const storeId = ref(null)
const dateRange = ref(null)

const data = reactive({
  total_visitors: 0,
  purchase_count: 0,
  conversion_rate: 0,
  avg_duration_min: 0,
  avg_purchase_duration_min: 0,
  avg_no_purchase_duration_min: 0
})

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
    const res = await getConversion(params)
    Object.assign(data, res.data || {})
    await nextTick()
    renderFunnel()
  } catch (e) {
    ElMessage.error('加载购买转化数据失败')
  } finally {
    loading.value = false
  }
}

function renderFunnel() {
  if (!funnelRef.value) return
  if (!chart) chart = echarts.init(funnelRef.value)

  const total = data.total_visitors || 0
  const purchase = data.purchase_count || 0
  const rate = data.conversion_rate || 0

  chart.setOption({
    title: { text: '转化漏斗', left: 'center' },
    series: [{
      type: 'funnel',
      left: '20%',
      width: '60%',
      minSize: '30%',
      gap: 2,
      label: { show: true, position: 'inside', fontSize: 14 },
      data: [
        { value: total, name: `总访客 (${total}人)` },
        { value: purchase, name: `购买 (${purchase}人 / ${rate}%)` }
      ],
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 1
      }
    }]
  })

  window.addEventListener('resize', () => chart?.resize())
}

function handleExport() {
  const params = { type: 'conversion', storeId: storeId.value }
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
.stat-card.highlight {
  background: linear-gradient(135deg, #fff7e6, #ffe8cc);
}
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: bold; color: #409EFF; }
.stat-value.success { color: #67C23A; }
.stat-value.warning { color: #E6A23C; }

.chart-container {
  width: 100%; margin-top: 24px;
}
</style>
