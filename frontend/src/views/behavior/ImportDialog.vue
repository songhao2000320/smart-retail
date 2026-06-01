<template>
  <el-dialog v-model="visible" title="批量导入行为数据" width="560px" destroy-on-close
    @update:model-value="$emit('update:modelValue', $event)">
    <!-- 步骤说明 -->
    <el-alert type="info" :closable="false" show-icon class="mb-4">
      <template #title>
        支持导入 .xlsx / .xls 格式的 Excel 文件，系统会自动解析并批量创建行为记录
      </template>
    </el-alert>

    <!-- 上传区域 -->
    <div class="upload-section">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将 Excel 文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请按照模板格式填写数据，门店ID、进店时间、离开时间为必填项
          </div>
        </template>
      </el-upload>
    </div>

    <!-- 模板下载 -->
    <div class="template-download">
      <el-button type="primary" link @click="handleDownloadTemplate">
        <el-icon><Download /></el-icon> 下载导入模板
      </el-button>
    </div>

    <!-- 导入结果 -->
    <div v-if="importResult" class="import-result">
      <el-alert :type="importResult.skipCount > 0 ? 'warning' : 'success'" :closable="false" show-icon>
        <template #title>
          {{ importResult.message || `导入完成：成功 ${importResult.successCount} 条` }}
        </template>
      </el-alert>
      <div v-if="importResult.errors" class="error-detail">
        <p style="color: #e6a23c; margin: 8px 0 4px">跳过详情：</p>
        <p style="color: #909399; font-size: 12px; white-space: pre-wrap">{{ importResult.errors }}</p>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="handleImport" :loading="importing" :disabled="!file">
        开始导入
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Download } from '@element-plus/icons-vue'
import { importBehaviors, downloadTemplate } from '@/api/behavior'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const file = ref(null)
const importing = ref(false)
const importResult = ref(null)
const uploadRef = ref(null)

function handleFileChange(uploadFile) {
  file.value = uploadFile.raw
  importResult.value = null
}

function handleFileRemove() {
  file.value = null
  importResult.value = null
}

async function handleDownloadTemplate() {
  try {
    const res = await downloadTemplate()
    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '行为数据导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (e) {
    ElMessage.error('模板下载失败')
  }
}

async function handleImport() {
  if (!file.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  importing.value = true
  importResult.value = null
  try {
    const res = await importBehaviors(file.value)
    importResult.value = {
      successCount: res.data?.successCount ?? 0,
      skipCount: res.data?.skipCount ?? 0,
      errors: res.data?.errors,
      message: res.message
    }
    if (importResult.value.successCount > 0) {
      ElMessage.success(res.message || '导入成功')
      emit('success')
    } else {
      ElMessage.warning('没有成功导入任何数据')
    }
    // 清除文件选择
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
    file.value = null
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '导入失败')
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.mb-4 {
  margin-bottom: 16px;
}

.upload-section {
  margin-bottom: 12px;
}

.template-download {
  margin-bottom: 16px;
}

.import-result {
  margin-top: 8px;
}

.error-detail {
  margin-top: 4px;
}
</style>
