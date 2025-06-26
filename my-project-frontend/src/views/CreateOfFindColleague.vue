<template>
  <div class="create-container">
    <el-button @click="goBack" class="back-button">返回</el-button>
    
    <el-card class="create-card">
      <h2>创建新副业</h2>
      <el-divider />
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <!-- 基本信息 -->
        <el-form-item label="副业名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入副业名称" />
        </el-form-item>
        
        <el-form-item label="封面图片" prop="imageUrl">
          <el-upload
            class="cover-uploader"
            action="http://localhost:8080/api/unauth/common/upload"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload">
            <img v-if="form.imageUrl" :src="form.imageUrl" class="cover-image">
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="副业分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id">
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="简短描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="用简短的一句话描述你的副业" />
        </el-form-item>
        
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        
        <!-- 副业属性 -->
        <el-form-item label="副业属性">
          <el-checkbox v-model="form.isRemote" label="可远程" />
          <el-checkbox v-model="form.isFreeEntry" label="零门槛" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="每日投入时间" prop="timePerDay">
              <el-input v-model="form.timePerDay" placeholder="如：2小时" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="月收益范围" prop="incomeEstimate">
              <el-input v-model="form.incomeEstimate" placeholder="如：3000-8000元" />
            </el-form-item>
          </el-col>
          <el-col :span="24" style="margin-bottom: 15px">
            <el-form-item label="是否招纳成员" prop="needMember">
              <el-radio-group v-model="form.needMember">
                <el-radio :label="1">需要</el-radio>
                <el-radio :label="0">不需要</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.needMember === 1">
            <el-form-item label="成员数量" prop="memberNum">
              <el-input 
                v-model="form.memberNum"
                type="number"
                :min="1"
                :max="1000"
                placeholder="请输入1-1000之间的整数"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="适合人群" prop="targetAudience">
              <el-input v-model="form.targetAudience" placeholder="如：上班族、学生" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="标签" prop="tags">
          <el-tag
            v-for="tag in form.tags"
            :key="tag"
            closable
            @close="removeTag(tag)">
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            size="small"
            @keyup.enter="addTag"
            @blur="addTag"
          />
          <el-button v-else size="small" @click="showTagInput">+ 添加标签</el-button>
        </el-form-item>
        
        <!-- 详细内容 -->
        <el-divider>详细内容</el-divider>
        
        <el-form-item label="操作步骤" prop="steps">
          <div style="border: 1px solid #ccc; margin-bottom: 10px;">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
            />
            <Editor
              style="height: 400px; overflow-y: hidden;"
              v-model="form.steps"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="handleEditorCreated"
            />
          </div>
        </el-form-item>
        
        <el-form-item label="推荐工具" prop="tools">
          <el-input 
            v-model="form.tools" 
            type="textarea" 
            :rows="3" 
            placeholder="列出推荐的平台、工具或资源..." />
        </el-form-item>
        
        <el-form-item label="风险提示" prop="riskWarning">
          <el-input
            v-model="form.riskWarning"
            type="textarea"
            :rows="3"
            placeholder="提醒用户需要注意的风险点"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, shallowRef, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { post } from '@/net'
import { ElMessage } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

const router = useRouter()

// 表单数据
const form = reactive({
  // projects表字段
  name: '',
  category: null,
  description: '',
  difficulty: 1,
  imageUrl: '',
  
  // projects_detail表字段
  steps: '',
  tools: '',
  timePerDay: '',
  incomeEstimate: '',
  targetAudience: '',
  riskWarning: '',
  isRemote: true,
  isFreeEntry: true,
  tags: [],
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入副业名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入简短描述', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度等级', trigger: 'change' }],
  steps: [{ required: true, message: '请输入操作步骤', trigger: 'blur' }],
  tools: [{ required: true, message: '请输入推荐工具', trigger: 'blur' }],
  timePerDay: [{ required: true, message: '请输入每日投入时间', trigger: 'blur' }]
}

// 分类数据
const categories = ref([
  { id: 1, name: '电商' },
  { id: 2, name: 'AI' },
  { id: 3, name: '新媒体' },
  { id: 4, name: '内容创作' },
  { id: 5, name: '投资理财' }
])

// 富文本编辑器配置
const editorRef = shallowRef()
const toolbarConfig = {}
const editorConfig = {
  placeholder: '请输入操作步骤...',
  MENU_CONF: {
    uploadImage: {
      server: 'http://localhost:8080/api/unauth/common/upload',
      fieldName: 'file',
      maxFileSize: 2 * 1024 * 1024, // 2M
      allowedFileTypes: ['image/*'],
      customInsert(res, insertFn) {
      console.log("图片",res)
        if (res && res.data) {
          insertFn(res.data)
        }
      }
    }
  }
}

const handleEditorCreated = (editor) => {
  editorRef.value = editor
}

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

// 标签相关
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref()

// 表单提交状态
const submitting = ref(false)
const formRef = ref()

// 显示标签输入框
const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value.focus()
  })
}

// 添加标签
const addTag = () => {
  if (tagInputValue.value && !form.tags.includes(tagInputValue.value)) {
    form.tags.push(tagInputValue.value)
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

// 移除标签
const removeTag = (tag) => {
  form.tags = form.tags.filter(t => t !== tag)
}

// 封面图片上传
const handleCoverSuccess = (response) => {
  form.imageUrl = response.data
  ElMessage.success('上传成功')
}

const beforeCoverUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('封面图片只能是 JPG/PNG 格式!')
    return false
  }
  // if (!isLt2M) {
  //   ElMessage.error('封面图片大小不能超过 2MB!')
  //   return false
  // }
  return isJPG && isLt2M
}

// 提交表单
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      
      // 组装请求数据
      const requestData = {
        // projects表字段
        name: form.name,
        category: form.category,
        description: form.description,
        difficulty: form.difficulty,
        imageUrl: form.imageUrl,
        
        // projects_detail表字段
        steps: form.steps,
        tools: form.tools,
        timePerDay: form.timePerDay,
        incomeEstimate: form.incomeEstimate,
        targetAudience: form.targetAudience,
        riskWarning: form.riskWarning,
        isRemote: form.isRemote ? 1 : 0,
        isFreeEntry: form.isFreeEntry ? 1 : 0,
        tags: form.tags.join(','),
        status: 0, // 0=待审核
        needMember: form.needMember,
        memberNum: form.memberNum
      }
      
      post('/api/auth/project/createFindCollage', requestData)
        .then(() => {
          ElMessage.success('创建成功')
          goBack()
        })
        .catch(err => {
          ElMessage.error(err.message || '提交失败')
        })
        .finally(() => {
          submitting.value = false
        })
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value.resetFields()
  form.tags = []
  form.steps = ''
  if (editorRef.value) {
    editorRef.value.clear()
  }
}

// 返回
const goBack = () => {
  router.go(-1)
}

onMounted(() => {
  // 可以在这里加载分类数据
})
</script>

<style scoped>
.create-container {
  padding: 30px;
  max-width: 1100px;
  margin: 0 auto;
  background: #f7f9fc;
}

.back-button {
  position: absolute;
  left: 30px;
  top: 30px;
  z-index: 1000;
}

.create-card {
  background-color: #fff;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border-radius: 16px;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 320px;
  height: 180px;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 320px;
  height: 180px;
  line-height: 180px;
  text-align: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.el-tag {
  margin-right: 10px;
  margin-bottom: 5px;
}

@media screen and (max-width: 768px) {
  .create-container {
    padding: 15px;
  }
  
  .create-card {
    padding: 15px;
  }
  
  .cover-uploader,
  .cover-uploader-icon {
    width: 100%;
    height: 150px;
    line-height: 150px;
  }
}
</style>

<style>
/* 覆盖wangEditor的默认样式 */
.w-e-toolbar {
  background-color: #f5f7fa !important;
  border-bottom: 1px solid #e4e7ed !important;
}
.w-e-text-container {
  background-color: #fff !important;
  border: none !important;
}
</style>