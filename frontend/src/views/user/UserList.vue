<template>
  <div class="user-list-page">
    <div class="page-header">
      <h3>用户管理</h3>
      <el-select v-model="filterRole" placeholder="按角色筛选" clearable @change="handleSearch">
        <el-option label="管理员" value="admin" />
        <el-option label="门店经理" value="manager" />
        <el-option label="数据分析师" value="analyst" />
      </el-select>
    </div>

    <el-table :data="userList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.role)" size="small">
            {{ roleLabel(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '锁定' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="openRoleDialog(row)">
            修改角色
          </el-button>
          <el-button
            v-if="row.role === 'analyst'"
            type="warning" size="small" link @click="openAuthDialog(row)">
            门店授权
          </el-button>
          <el-button type="danger" size="small" link @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '锁定' : '解锁' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchUsers" />
    </div>

    <!-- 修改角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <span>{{ selectedUser?.username }}</span>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="selectedRole" placeholder="请选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="门店经理" value="manager" />
            <el-option label="数据分析师" value="analyst" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleLoading" @click="handleRoleUpdate">确认</el-button>
      </template>
    </el-dialog>

    <!-- 门店授权对话框 -->
    <el-dialog v-model="authDialogVisible" title="门店授权" width="500px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <span>{{ authUser?.username }}</span>
        </el-form-item>
        <el-form-item label="授权门店">
          <el-checkbox-group v-model="selectedStoreIds">
            <el-checkbox v-for="s in allStores" :key="s.id" :label="s.id" :value="s.id">
              {{ s.name }}
            </el-checkbox>
          </el-checkbox-group>
          <div v-if="allStores.length === 0" style="color:#909399">暂无可用门店</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="authDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="authLoading" @click="handleAuthSave">保存授权</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, updateUserRole, updateUserStatus, getUserAuths, saveUserAuths } from '@/api/user'
import { getAllStores } from '@/api/store'
import { ElMessage, ElMessageBox } from 'element-plus'

const userList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterRole = ref('')

const roleDialogVisible = ref(false)
const roleLoading = ref(false)
const selectedUser = ref(null)
const selectedRole = ref('')

// 授权相关
const authDialogVisible = ref(false)
const authLoading = ref(false)
const authUser = ref(null)
const selectedStoreIds = ref([])
const allStores = ref([])

const roleLabel = (role) => {
  const map = { admin: '管理员', manager: '门店经理', analyst: '数据分析师' }
  return map[role] || role
}

const roleTagType = (role) => {
  const map = { admin: 'danger', manager: 'warning', analyst: 'info' }
  return map[role] || 'info'
}

const handleSearch = () => {
  page.value = 1
  fetchUsers()
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      pageSize: pageSize.value,
      role: filterRole.value || undefined
    })
    userList.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const openRoleDialog = (row) => {
  selectedUser.value = row
  selectedRole.value = row.role
  roleDialogVisible.value = true
}

const handleRoleUpdate = async () => {
  if (!selectedRole.value) {
    ElMessage.warning('请选择角色')
    return
  }
  roleLoading.value = true
  try {
    await updateUserRole(selectedUser.value.id, selectedRole.value)
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error(error.message || '角色修改失败')
  } finally {
    roleLoading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '锁定' : '解锁'
  try {
    await ElMessageBox.confirm(`确认${action}用户 "${row.username}" 吗？`, '提示', {
      type: 'warning'
    })
    await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || `${action}失败`)
    }
  }
}

const openAuthDialog = async (row) => {
  authUser.value = row
  selectedStoreIds.value = []
  authDialogVisible.value = true

  // 加载所有门店
  try {
    const res = await getAllStores()
    allStores.value = res.data || []
  } catch (e) {
    allStores.value = []
  }

  // 加载已授权门店
  try {
    const res2 = await getUserAuths(row.id)
    const auths = res2.data || []
    selectedStoreIds.value = auths.map(a => a.storeId)
  } catch (e) {
    selectedStoreIds.value = []
  }
}

const handleAuthSave = async () => {
  authLoading.value = true
  try {
    await saveUserAuths(authUser.value.id, selectedStoreIds.value)
    ElMessage.success('授权保存成功')
    authDialogVisible.value = false
  } catch (e) {
    ElMessage.error('授权保存失败')
  } finally {
    authLoading.value = false
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-list-page {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
