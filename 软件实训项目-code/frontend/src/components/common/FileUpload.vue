<template>
  <el-upload
    class="upload-area"
    drag
    :auto-upload="false"
    :limit="1"
    :on-change="handleChange"
    :before-upload="beforeUpload"
    accept=".pdf,.docx"
    :file-list="fileList"
  >
    <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
    <div class="el-upload__text">将简历文件拖到此处，或<em>点击上传</em></div>
    <template #tip>
      <div class="el-upload__tip">仅支持 PDF/docx 格式，大小不超过 5MB</div>
    </template>
  </el-upload>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const emit = defineEmits<{ fileChange: [file: File] }>()
const fileList = ref<any[]>([])

function beforeUpload(file: File) {
  const isPDF = file.type === 'application/pdf'
  const isDOCX = file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isPDF && !isDOCX) { ElMessage.error('仅支持 PDF 和 DOCX 格式'); return false }
  if (!isLt5M) { ElMessage.error('文件大小不能超过 5MB'); return false }
  return true
}

function handleChange(file: any) {
  if (file.raw) {
    emit('fileChange', file.raw)
  }
}
</script>
