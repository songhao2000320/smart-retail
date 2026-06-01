<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2>数据仪表盘</h2>
      <div style="display:flex;gap:10px;align-items:center">
        <el-select v-model="storeId" placeholder="全部门店" clearable style="width: 180px" @change="loadData">
          <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-button type="danger" @click="handleExportPdf">
          <el-icon><Download /></el-icon> 导出PDF
        </el-button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="6">
        <div class="kpi-card blue">
          <div class="kpi-icon">
            <el-icon size="28"><UserFilled /></el-icon>
          </div>
          <div class="kpi-info">
            <div class="kpi-label">今日客流</div>
            <div class="kpi-value">{{ summary.todayVisitors || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="kpi-card green">
          <div class="kpi-icon">
            <el-icon size="28"><ShoppingCartFull /></el-icon>
          </div>
          <div class="kpi-info">
            <div class="kpi-label">今日成交</div>
            <div class="kpi-value">{{ summary.todayPurchases || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="kpi-card orange">
          <div class="kpi-icon">
            <el-icon size="28"><TrendCharts /></el-icon>
          </div>
          <div class="kpi-info">
            <div class="kpi-label">累计转化率</div>
            <div class="kpi-value">{{ summary.totalConversionRate || 0 }}%</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="kpi-card purple">
          <div class="kpi-icon">
            <el-icon size="28"><Timer /></el-icon>
          </div>
          <div class="kpi-info">
            <div class="kpi-label">平均停留</div>
            <div class="kpi-value">{{ summary.avgStayDuration || 0 }} <span style="font-size:14px">分钟</span></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近7天趋势 -->
    <div class="chart-container" ref="trendRef" v-loading="loading">
      <el-empty v-if="!loading && recentTrend.length === 0" description="暂无趋势数据" />
    </div>

    <!-- 多门店对比 -->
    <div style="margin-top: 24px" v-if="userRole === 'admin' || userRole === 'manager'">
      <h3 style="margin-bottom: 12px">多门店对比</h3>
      <el-table :data="compareData" border stripe v-loading="compareLoading">
        <el-table-column prop="store_name" label="门店" />
        <el-table-column prop="visitor_count" label="客流量" sortable align="center" />
        <el-table-column prop="purchase_count" label="购买人数" sortable align="center" />
        <el-table-column label="转化率" align="center">
          <template #default="{ row }">
            {{ row.visitor_count > 0 ? (row.purchase_count / row.visitor_count * 100).toFixed(1) : 0 }}%
          </template>
        </el-table-column>
        <el-table-column label="平均停留" align="center">
          <template #default="{ row }">{{ row.avg_duration_min || 0 }} 分钟</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { UserFilled, ShoppingCartFull, TrendCharts, Timer, Download } from '@element-plus/icons-vue'
import { getDashboard, getCompare } from '@/api/behavior'
import { getStoreList } from '@/api/store'
import { exportDashboardPdf } from '@/api/export'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import * as echarts from 'echarts'

const authStore = useAuthStore()
const loading = ref(false)
const compareLoading = ref(false)
const trendRef = ref(null)
let chart = null

const storeList = ref([])
const storeId = ref(null)
const compareData = ref([])

const userRole = computed(() => authStore.user?.role || '')

const summary = reactive({
  todayVisitors: 0,
  todayPurchases: 0,
  totalVisitors: 0,
  totalConversionRate: 0,
  avgStayDuration: 0
})

const recentTrend = ref([])

async function loadStores() {
  try {
    const res = await getStoreList({ page: 1, size: 100 })
    storeList.value = res.data?.list || res.data?.records || []
  } catch (e) { /* ignore */ }
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (storeId.value) params.storeId = storeId.value
    const res = await getDashboard(params)
    const data = res.data || {}
    Object.assign(summary, data.summary || {})
    recentTrend.value = data.recentTrend || []
    await nextTick()
    renderTrend()
  } catch (e) {
    ElMessage.error('加载仪表盘数据失败')
  } finally {
    loading.value = false
  }

  // 多门店对比
  if (userRole.value === 'admin' || userRole.value === 'manager') {
    loadCompare()
  }
}

async function loadCompare() {
  compareLoading.value = true
  try {
    const res = await getCompare({})
    compareData.value = res.data || []
  } catch (e) {
    ElMessage.error('加载多门店对比数据失败')
  } finally {
    compareLoading.value = false
  }
}

function renderTrend() {
  if (!trendRef.value || recentTrend.value.length === 0) return
  if (!chart) chart = echarts.init(trendRef.value)

  const keys = recentTrend.value.map(d => d.date_key)
  const totals = recentTrend.value.map(d => d.total_count || 0)
  const purchases = recentTrend.value.map(d => d.purchase_count || 0)

  chart.setOption({
    title: { text: '近7天客流趋势', left: 'center' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['客流', '购买'], bottom: 0 },
    grid: { left: 50, right: 30, top: 50, bottom: 40 },
    xAxis: { type: 'category', data: keys },
    yAxis: { type: 'value' },
    series: [
      { name: '客流', type: 'bar', data: totals, itemStyle: { color: '#409EFF' }, barMaxWidth: 35 },
      { name: '购买', type: 'line', data: purchases, itemStyle: { color: '#67C23A' }, smooth: true, symbolSize: 8 }
    ]
  })

  window.addEventListener('resize', () => chart?.resize())
}

async function handleExportPdf() {
  const params = {}
  if (storeId.value) params.storeId = storeId.value
  exportDashboardPdf(params)
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
.dashboard-page { padding: 0; }

.page-header {
  display: flex; justify-content: space-between; align-items: center;
}
.page-header h2 { margin: 0; font-size: 20px; }

.kpi-card {
  display: flex; align-items: center; gap: 16px;
  padding: 20px; border-radius: 10px; color: #fff;
}
.kpi-card.blue { background: linear-gradient(135deg, #409EFF, #337ECC); }
.kpi-card.green { background: linear-gradient(135deg, #67C23A, #529B2E); }
.kpi-card.orange { background: linear-gradient(135deg, #E6A23C, #C78E2A); }
.kpi-card.purple { background: linear-gradient(135deg, #9B59B6, #7D3C98); }

.kpi-icon { opacity: 0.8; }
.kpi-label { font-size: 13px; opacity: 0.85; margin-bottom: 4px; }
.kpi-value { font-size: 26px; font-weight: bold; }

.chart-container {
  width: 100%; height: 380px; margin-top: 24px;
}
</style>
