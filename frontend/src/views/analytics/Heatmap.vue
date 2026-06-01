<template>
  <div class="analytics-page">
    <div class="page-header">
      <h2>热力图分析</h2>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <el-select v-model="storeId" placeholder="选择门店" clearable style="width: 180px" @change="loadData">
        <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
        start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
        style="width: 260px" />
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon> 查询
      </el-button>
    </div>

    <!-- 平面图热力图 + 数据表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="14">
        <div class="heatmap-container" v-loading="loading">
          <div v-if="!loading && heatmapData.length === 0" class="empty-hint">暂无数据，请选择门店后查询</div>
          <ZoneCanvas
            v-else
            :zones="canvasZones"
            :width="620"
            :height="480"
            :show-heat="true"
            heat-key="visitorCount"
            :stat-labels="statLabels"
            @zone-click="handleZoneClick"
          />
        </div>
      </el-col>
      <el-col :span="10">
        <el-table :data="heatmapData" border size="small" max-height="480">
          <el-table-column prop="zone_name" label="区域名称" />
          <el-table-column prop="visitor_count" label="到访人数" sortable align="center" />
          <el-table-column label="平均停留" align="center">
            <template #default="{ row }">{{ row.avg_stay_min || 0 }} 分钟</template>
          </el-table-column>
          <el-table-column label="总停留" align="center">
            <template #default="{ row }">{{ row.total_stay_min || 0 }} 分钟</template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getHeatmap } from '@/api/behavior'
import { getStoreList } from '@/api/store'
import { ElMessage } from 'element-plus'
import ZoneCanvas from '@/components/ZoneCanvas.vue'

const loading = ref(false)

const storeList = ref([])
const storeId = ref(null)
const dateRange = ref(null)
const heatmapData = ref([])

const statLabels = {
  visitorCount: '到访人数',
  avgStayMin: '平均停留(分)',
  totalStayMin: '总停留(分)'
}

// 转换为 ZoneCanvas 需要的格式
const canvasZones = computed(() => {
  return heatmapData.value.map(d => ({
    id: d.zone_id || d.id,
    name: d.zone_name,
    posX: d.pos_x || 0,
    posY: d.pos_y || 0,
    width: d.width || 10,
    height: d.height || 10,
    visitorCount: d.visitor_count || 0,
    avgStayMin: d.avg_stay_min || 0,
    totalStayMin: d.total_stay_min || 0,
    stats: {
      visitorCount: (d.visitor_count || 0) + ' 人',
      avgStayMin: (d.avg_stay_min || 0) + ' 分钟',
      totalStayMin: (d.total_stay_min || 0) + ' 分钟'
    }
  }))
})

function handleZoneClick(zone) {
  // 可以扩展：点击区域后跳转或高亮
  console.log('Clicked zone:', zone.name)
}

async function loadStores() {
  try {
    const res = await getStoreList({ page: 1, size: 100 })
    storeList.value = res.data?.list || res.data?.records || []
  } catch (e) { /* ignore */ }
}

async function loadData() {
  if (!storeId.value) return
  loading.value = true
  try {
    const params = { storeId: storeId.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getHeatmap(params)
    heatmapData.value = res.data || []
  } catch (e) {
    ElMessage.error('加载热力图数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadStores())
</script>

<style scoped>
.analytics-page { padding: 0; }
.page-header h2 { margin: 0; font-size: 20px; }

.filter-bar {
  display: flex; gap: 12px; flex-wrap: wrap; align-items: center;
  margin-top: 16px;
}

.heatmap-container {
  width: 100%; height: 480px;
  background: #fafafa; border-radius: 8px; border: 1px solid #ebeef5;
  position: relative;
}

.empty-hint {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  color: #909399; font-size: 14px;
}
</style>
