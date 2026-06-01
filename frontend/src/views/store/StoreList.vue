<template>
  <div class="store-list-page">
    <div class="page-header">
      <h2>门店管理</h2>
      <el-button v-if="isAdmin" type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 新增门店
      </el-button>
    </div>

    <!-- 门店列表 -->
    <el-table :data="storeList" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="门店名称" min-width="180" />
      <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right" v-if="isAdmin">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="goZones(row)">
            <el-icon><Grid /></el-icon> 区域管理
          </el-button>
          <el-button link type="warning" size="small" @click="showEditDialog(row)">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-popconfirm title="确定删除该门店？关联区域也会被删除。" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button link type="danger" size="small">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" v-else>
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="goZones(row)">
            <el-icon><Grid /></el-icon> 区域管理
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑门店弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingStore ? '编辑门店' : '新增门店'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入门店名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="门店地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入门店地址" maxlength="200" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" />
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getStoreList, createStore, updateStore, deleteStore } from '@/api/store'
import { ElMessage } from 'element-plus'
import { Plus, Grid, Edit, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const isAdmin = computed(() => authStore.isAdmin())

const loading = ref(false)
const submitting = ref(false)
const storeList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const editingStore = ref(null)
const formRef = ref(null)
const form = ref({ name: '', address: '', phone: '' })
const rules = {
  name: [{ required: true, message: '请输入门店名称', trigger: 'blur' }]
}

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res = await getStoreList({ page: page.value, pageSize: pageSize.value })
    storeList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function showCreateDialog() {
  editingStore.value = null
  form.value = { name: '', address: '', phone: '' }
  dialogVisible.value = true
}

function showEditDialog(row) {
  editingStore.value = row
  form.value = { name: row.name, address: row.address || '', phone: row.phone || '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form.value }
    if (editingStore.value) {
      await updateStore(editingStore.value.id, data)
      ElMessage.success('门店更新成功')
    } else {
      await createStore(data)
      ElMessage.success('门店创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // 错误已在拦截器处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteStore(id)
    ElMessage.success('门店已删除')
    loadData()
  } catch {
    // 错误已在拦截器处理
  }
}

function goZones(row) {
  router.push(`/stores/${row.id}/zones`)
}
</script>

<style scoped>
.store-list-page {
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

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
