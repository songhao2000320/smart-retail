<template>
  <div class="zone-manage-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="$router.push('/stores')">
          <el-icon><ArrowLeft /></el-icon> 返回门店列表
        </el-button>
        <h2>{{ storeName }} — 区域管理</h2>
      </div>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 新增区域
      </el-button>
    </div>

    <!-- 门店布局预览 -->
    <div class="layout-preview" v-if="zones.length > 0">
      <h3>门店平面图预览</h3>
      <div class="canvas">
        <div
          v-for="zone in zones"
          :key="zone.id"
          class="zone-block"
          :style="{
            left: zone.posX + '%',
            top: zone.posY + '%',
            width: zone.width + '%',
            height: zone.height + '%'
          }"
          :title="zone.name"
        >
          {{ zone.name }}
        </div>
      </div>
    </div>

    <!-- 区域列表 -->
    <el-table :data="zones" border stripe v-loading="loading" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="区域名称" min-width="150" />
      <el-table-column label="位置 (X, Y)" width="140">
        <template #default="{ row }">
          {{ row.posX }}%, {{ row.posY }}%
        </template>
      </el-table-column>
      <el-table-column label="尺寸 (宽×高)" width="140">
        <template #default="{ row }">
          {{ row.width }}% × {{ row.height }}%
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="warning" size="small" @click="showEditDialog(row)">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-popconfirm title="确定删除该区域？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger" size="small">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && zones.length === 0" description="暂无区域数据，请添加区域" />

    <!-- 新增/编辑区域弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingZone ? '编辑区域' : '新增区域'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="区域名称" prop="name">
          <el-input v-model="form.name" placeholder="如：生鲜区、零食区" maxlength="30" />
        </el-form-item>
        <el-form-item label="X 坐标(%)" prop="posX">
          <el-input-number v-model="form.posX" :min="0" :max="100" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Y 坐标(%)" prop="posY">
          <el-input-number v-model="form.posY" :min="0" :max="100" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="宽度(%)" prop="width">
          <el-input-number v-model="form.width" :min="1" :max="100" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="高度(%)" prop="height">
          <el-input-number v-model="form.height" :min="1" :max="100" :precision="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStoreById, getZones, createZone, updateZone, deleteZone } from '@/api/store'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const storeId = ref(Number(route.params.id))
const storeName = ref('')

const loading = ref(false)
const submitting = ref(false)
const zones = ref([])

const dialogVisible = ref(false)
const editingZone = ref(null)
const formRef = ref(null)
const form = ref({ name: '', posX: 10, posY: 10, width: 20, height: 20 })
const rules = {
  name: [{ required: true, message: '请输入区域名称', trigger: 'blur' }],
  posX: [{ required: true, message: '请输入X坐标', trigger: 'blur' }],
  posY: [{ required: true, message: '请输入Y坐标', trigger: 'blur' }],
  width: [{ required: true, message: '请输入宽度', trigger: 'blur' }],
  height: [{ required: true, message: '请输入高度', trigger: 'blur' }]
}

onMounted(async () => {
  await loadStoreInfo()
  loadZones()
})

async function loadStoreInfo() {
  try {
    const res = await getStoreById(storeId.value)
    storeName.value = res.data.name || '未知门店'
  } catch {
    router.push('/stores')
  }
}

async function loadZones() {
  loading.value = true
  try {
    const res = await getZones(storeId.value)
    zones.value = res.data || []
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function showCreateDialog() {
  editingZone.value = null
  form.value = { name: '', posX: 10, posY: 10, width: 20, height: 20 }
  dialogVisible.value = true
}

function showEditDialog(row) {
  editingZone.value = row
  form.value = {
    name: row.name,
    posX: row.posX,
    posY: row.posY,
    width: row.width,
    height: row.height
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form.value }
    if (editingZone.value) {
      await updateZone(storeId.value, editingZone.value.id, data)
      ElMessage.success('区域更新成功')
    } else {
      await createZone(storeId.value, data)
      ElMessage.success('区域创建成功')
    }
    dialogVisible.value = false
    loadZones()
  } catch {
    // 错误已在拦截器处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteZone(storeId.value, id)
    ElMessage.success('区域已删除')
    loadZones()
  } catch {
    // 错误已在拦截器处理
  }
}
</script>

<style scoped>
.zone-manage-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.layout-preview {
  margin-bottom: 10px;
}

.layout-preview h3 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #606266;
}

.canvas {
  position: relative;
  width: 100%;
  padding-bottom: 60%;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  background: #fafbfc;
  overflow: hidden;
}

.zone-block {
  position: absolute;
  border: 2px solid #409EFF;
  background: rgba(64, 158, 255, 0.12);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  color: #409EFF;
  cursor: default;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
