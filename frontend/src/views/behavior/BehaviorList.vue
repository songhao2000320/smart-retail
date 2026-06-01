<template>
  <div class="behavior-page">
    <!-- 顶部筛选 -->
    <div class="page-header">
      <h2>行为数据管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="showImportDialog" v-if="canEdit">
          <el-icon><Upload /></el-icon> 批量导入
        </el-button>
        <el-button type="primary" @click="showAddDialog" v-if="canEdit">
          <el-icon><Plus /></el-icon> 添加记录
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filters.storeId" placeholder="选择门店" clearable style="width: 180px">
        <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-select v-model="filters.isPurchased" placeholder="购买状态" clearable style="width: 130px">
        <el-option label="已购买" :value="1" />
        <el-option label="未购买" :value="0" />
      </el-select>
      <el-date-picker v-model="filters.dateRange" type="daterange" range-separator="至"
        start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
        style="width: 260px" />
      <el-input v-model="filters.keyword" placeholder="搜索门店名称" clearable style="width: 200px" />
      <el-button type="primary" @click="loadData">
        <el-icon><Search /></el-icon> 查询
      </el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="storeName" label="门店" min-width="130" />
      <el-table-column prop="entryTime" label="进店时间" width="170" align="center" />
      <el-table-column prop="leaveTime" label="离开时间" width="170" align="center" />
      <el-table-column label="停留时长" width="110" align="center">
        <template #default="{ row }">
          <el-tag type="warning" v-if="row.stayDurationMinutes">{{ row.stayDurationMinutes }} 分钟</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="停留区域" min-width="160">
        <template #default="{ row }">
          <template v-if="row.zoneNames">
            <el-tag v-for="(name, i) in row.zoneNames.split(', ')" :key="i" size="small" style="margin: 1px 2px">
              {{ name }}
            </el-tag>
          </template>
          <span v-else style="color: #c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="是否购买" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.isPurchased === 1 ? 'success' : 'info'">
            {{ row.isPurchased === 1 ? '已购买' : '未购买' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creatorName" label="录入人" width="100" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
          <el-button size="small" type="warning" link @click="showEditDialog(row)" v-if="canEdit">编辑</el-button>
          <el-popconfirm title="确定删除此记录?" @confirm="handleDelete(row.id)" v-if="canEdit">
            <template #reference>
              <el-button size="small" type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]" :total="pagination.total" layout="total, sizes, prev, pager, next"
        @size-change="loadData" @current-change="loadData" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="门店" prop="storeId">
          <el-select v-model="form.storeId" placeholder="请选择门店" style="width: 100%">
            <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="进店时间" prop="entryTime">
          <el-date-picker v-model="form.entryTime" type="datetime" placeholder="选择进店时间"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="离开时间" prop="leaveTime">
          <el-date-picker v-model="form.leaveTime" type="datetime" placeholder="选择离开时间"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否购买">
          <el-switch v-model="form.isPurchased" :active-value="1" :inactive-value="0"
            active-text="是" inactive-text="否" />
        </el-form-item>
        <el-divider content-position="left">区域停留记录</el-divider>
        <div v-for="(zs, index) in form.zoneStays" :key="index" class="zone-stay-row">
          <el-select v-model="zs.zoneId" placeholder="区域" style="width: 180px">
            <el-option v-for="z in zoneList" :key="z.id" :label="z.name" :value="z.id" />
          </el-select>
          <el-date-picker v-model="zs.entryTime" type="datetime" placeholder="进入时间"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 190px" />
          <el-date-picker v-model="zs.leaveTime" type="datetime" placeholder="离开时间"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 190px" />
          <el-button type="danger" :icon="Delete" circle size="small" @click="form.zoneStays.splice(index, 1)" />
        </div>
        <el-button type="primary" link @click="addZoneStay" :disabled="!form.storeId">
          <el-icon><Plus /></el-icon> 添加区域停留
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="行为记录详情" width="600px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="门店">{{ detail.storeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="进店时间">{{ detail.entryTime }}</el-descriptions-item>
        <el-descriptions-item label="离开时间">{{ detail.leaveTime }}</el-descriptions-item>
        <el-descriptions-item label="是否购买">
          <el-tag :type="detail.isPurchased === 1 ? 'success' : 'info'">
            {{ detail.isPurchased === 1 ? '已购买' : '未购买' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="录入人">{{ detail.creatorName || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider v-if="detailZoneStays.length > 0">区域停留记录</el-divider>
      <el-table :data="detailZoneStays" border size="small" v-if="detailZoneStays.length > 0">
        <el-table-column prop="zoneName" label="区域" />
        <el-table-column prop="entryTime" label="进入时间" />
        <el-table-column prop="leaveTime" label="离开时间" />
      </el-table>
    </el-dialog>

    <!-- 导入弹窗 -->
    <ImportDialog v-model="importVisible" @success="onImportSuccess" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Delete, Upload } from '@element-plus/icons-vue'
import { getBehaviorList, getBehaviorDetail, createBehavior, updateBehavior, deleteBehavior } from '@/api/behavior'
import { getStoreList } from '@/api/store'
import { useAuthStore } from '@/stores/auth'
import ImportDialog from './ImportDialog.vue'

const authStore = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const storeList = ref([])
const zoneList = ref([])
const detail = ref(null)
const detailZoneStays = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const importVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const canEdit = computed(() => authStore.isAdmin() || authStore.isManager())

const pagination = reactive({ page: 1, size: 10, total: 0 })

const filters = reactive({
  storeId: null,
  isPurchased: null,
  dateRange: null,
  keyword: ''
})

const form = reactive({
  id: null,
  storeId: null,
  entryTime: '',
  leaveTime: '',
  isPurchased: 0,
  zoneStays: []
})

const rules = {
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  entryTime: [{ required: true, message: '请选择进店时间', trigger: 'change' }],
  leaveTime: [{ required: true, message: '请选择离开时间', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑行为记录' : '添加行为记录')

async function loadStores() {
  try {
    const res = await getStoreList({ page: 1, size: 100 })
    storeList.value = res.data?.list || res.data?.records || []
  } catch (e) { /* ignore */ }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      storeId: filters.storeId,
      isPurchased: filters.isPurchased,
      keyword: filters.keyword
    }
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }
    const res = await getBehaviorList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.storeId = null
  filters.isPurchased = null
  filters.dateRange = null
  filters.keyword = ''
  pagination.page = 1
  loadData()
}

function showAddDialog() {
  isEdit.value = false
  form.id = null
  form.storeId = null
  form.entryTime = ''
  form.leaveTime = ''
  form.isPurchased = 0
  form.zoneStays = []
  zoneList.value = []
  dialogVisible.value = true
}

async function showEditDialog(row) {
  isEdit.value = true
  form.id = row.id
  form.storeId = row.storeId
  form.entryTime = row.entryTime
  form.leaveTime = row.leaveTime
  form.isPurchased = row.isPurchased
  form.zoneStays = []
  // 加载区域列表和已有停留记录
  await loadZones(row.storeId)
  try {
    const res = await getBehaviorDetail(row.id)
    const stays = res.data?.zoneStays || []
    form.zoneStays = stays.map(s => ({
      zoneId: s.zoneId,
      entryTime: s.entryTime,
      leaveTime: s.leaveTime
    }))
  } catch (e) { /* ignore */ }
  dialogVisible.value = true
}

async function showDetail(row) {
  detailVisible.value = true
  try {
    const res = await getBehaviorDetail(row.id)
    detail.value = res.data?.behavior
    detailZoneStays.value = res.data?.zoneStays || []
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      storeId: form.storeId,
      entryTime: form.entryTime,
      leaveTime: form.leaveTime,
      isPurchased: form.isPurchased,
      zoneStays: form.zoneStays
    }
    if (isEdit.value) {
      await updateBehavior(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await createBehavior(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteBehavior(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function addZoneStay() {
  form.zoneStays.push({ zoneId: null, entryTime: '', leaveTime: '' })
}

// 根据门店加载区域
async function loadZones(storeId) {
  if (!storeId) return
  try {
    const { getZones } = await import('@/api/store')
    const res = await getZones(storeId)
    zoneList.value = res.data || []
  } catch (e) { /* ignore */ }
}

// 监听门店选择变化，自动加载区域
import { watch } from 'vue'
watch(() => form.storeId, (val) => {
  if (val) loadZones(val)
})

function showImportDialog() {
  importVisible.value = true
}

function onImportSuccess() {
  loadData()
}

onMounted(() => {
  loadStores()
  loadData()
})
</script>

<style scoped>
.behavior-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 16px;
  align-items: center;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.zone-stay-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
}
</style>
