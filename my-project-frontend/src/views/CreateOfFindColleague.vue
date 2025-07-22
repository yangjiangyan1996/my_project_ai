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
      <el-form-item label="一级分类" prop="firstCategory">
        <el-select v-model="form.firstCategory" placeholder="请选择一级分类">
          <el-option
            v-for="item in firstCategoryOptions"
            :key="item.code"
            :label="item.desc"
            :value="item.code"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="二级分类" prop="secondCategory">
        <el-select v-model="form.secondCategory" :disabled="!form.firstCategory"  @click="checkFirstCategory" placeholder="请选择二级分类">
          <el-option
            v-for="item in secondCategoryOptions"
            :key="item.code"
            :label="item.desc"
            :value="item.code"
          />
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
        <el-form-item label="副业属性" prop="labels">
          <el-checkbox-group v-model="form.tags">
            <el-checkbox 
              v-for="tag in tagsOptions" 
              :key="tag.code"
              :label="tag.code"
            >
              {{ tag.desc }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-row :gutter="20">
          <!-- 替换每日投入时间部分 -->
      <el-col :span="8">
        

        <el-form-item label="每日投入时间" prop="timePerDay">
  <el-input-number
    v-model="form.timePerDay"
    :min="1"
    :step="1"
    controls-position="right"
    placeholder="请输入时间"
  />
  <span class="unit-label">小时</span>
</el-form-item>
      </el-col>

<!-- 替换月收益范围部分 -->
          <el-col :span="8">
            <el-form-item label="月收益范围" prop="incomeEstimate">
              <div style="display: flex; align-items: center;">
                <el-input-number
                  v-model="form.incomeEstimateMin"
                  :min="0"
                  placeholder="最低"
                />
                <span style="margin: 0 10px;">~</span>
                <el-input-number
                  v-model="form.incomeEstimateMax"
                  :min="0"
                  placeholder="最高"
                />
                <span style="margin-left: 6px;">元</span>
              </div>
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
              <el-input-number
                v-model="form.memberNum"
                :min="1"
                :max="1000"
                :step="1"
                controls-position="right"
                placeholder="请输入成员数量"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="适合人群" prop="targetAudience">
              <el-select 
                v-model="form.targetAudience" 
                placeholder="请选择适合人群"
                clearable
                filterable
              >
                <el-option
                  v-for="item in targetAudienceOptions"
                  :key="item.code"
                  :label="item.desc"
                  :value="item.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- <el-form-item label="标签" prop="tags">
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
        </el-form-item> -->
        
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
import { ref, reactive, onMounted, nextTick, shallowRef, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { post, get } from '@/net'
import { ElMessage } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

const router = useRouter()
//const itemId = router.query.id
const projectId = ref(null)
// 适合人群选项
const targetAudienceOptions = ref([])
const allCategoryData = ref([])           // 后端完整数据
const firstCategoryOptions = ref([])      // 一级分类
const secondCategoryOptions = ref([])     // 当前联动的二级分类
const tagsOptions = ref([])  

// 初始化加载数据
onMounted(async() => {
  const itemId = router.currentRoute.value.query.id;
  console.log('接收到的项目ID:', itemId);
  
   // 加载适合人群选项
  await loadTargetAudienceOptions()
  await loadCategoryOptions();
  await loadTagsOptions(); 

  if (itemId && /^\d+$/.test(itemId)) {
    projectId.value = itemId
    loadProjectDetail(itemId);
  } else {
    // ElMessage.error('无效的项目ID参数');
    console.warn('非法项目ID:', itemId);
  }
})


// 表单数据
const form = reactive({
  // 项目字段
  name: '',
  firstCategory: null,     // 一级分类 code
  secondCategory: null,    // 二级分类 code
  description: '',
  difficulty: 1,
  imageUrl: '',

  // 详情字段
  steps: '',
  tools: '',
  timePerDay: 1,       // 数字（小时）
  incomeEstimateMin: null,     // 最小收益
  incomeEstimateMax: null,     // 最大收益
  targetAudience: '',
  riskWarning: '',
  // isRemote: true,
  // isFreeEntry: true,
  tags: [],

  // 成员招募
  needMember: 1,
  memberNum: null
})

// 获取副业属性选项
const loadTagsOptions = async () => {
  try {
    const res = await get('/api/unauth/common/getLabels')
    if (res && Array.isArray(res)) {
      tagsOptions.value = res.map(item => ({
        code: item.code,
        desc: item.desc
      }))
    }
  } catch (error) {
    console.log(error)
    ElMessage.error('获取副业属性选项失败')
  }
}

const checkFirstCategory = () => {
  if (!form.firstCategory) {
    ElMessage.warning('请先选择一级分类')
  }
}

// 获取适合人群选项
const loadTargetAudienceOptions = async () => {
  try {
    const res = await get('/api/unauth/common/getUserType')
    if (res && Array.isArray(res)) {
      targetAudienceOptions.value = res.map(item => ({
        code: item.code,
        desc: item.desc
      }))
    }
  } catch (error) {
    ElMessage.error('获取适合人群选项失败')
  }
}

// 获取分类选项
const loadCategoryOptions = async () => {
  try {
    const res = await get('/api/unauth/common/category')
    if (res && Array.isArray(res)) {
      allCategoryData.value = res
      firstCategoryOptions.value = res.map(item => ({
        code: item.code,
        desc: item.desc
      }))
    }
  } catch (error) {
    ElMessage.error('获取分类选项失败')
  }
}

watch(() => form.firstCategory, (newVal) => {
  const selected = allCategoryData.value.find(c => c.code === newVal)
  secondCategoryOptions.value = selected?.subs || []
  form.secondCategory = null
})



// 校验规则
const rules = {
  name: [{ required: true, message: '请输入副业名称', trigger: 'blur' }],
  firstCategory: [{ required: true, message: '请选择一级分类', trigger: 'change' }],
  secondCategory: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入简短描述', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度等级', trigger: 'change' }],
  steps: [{ required: true, message: '请输入操作步骤', trigger: 'blur' }],
  tools: [{ required: true, message: '请输入推荐工具', trigger: 'blur' }],
  timePerDay: [
    {
      validator: (rule, value, callback) => {
        if (value <= 0) callback(new Error('每日时间必须大于0'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  incomeEstimate: [
    {
      validator(rule, value, callback) {
        if (form.incomeEstimateMin === null || form.incomeEstimateMax === null) {
          callback(new Error('请输入完整的收益范围'));
        } else if (form.incomeEstimateMin > form.incomeEstimateMax) {
          callback(new Error('最小值不能大于最大值'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
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

      const requestData = {
        id: projectId.value,
        name: form.name,
        firstCategory: form.firstCategory,
        secondCategory: form.secondCategory,
        description: form.description,
        difficulty: form.difficulty,
        imageUrl: form.imageUrl,

        steps: form.steps,
        tools: form.tools,
        timePerDay: form.timePerDay,
        incomeEstimateMin: form.incomeEstimateMin,
        incomeEstimateMax: form.incomeEstimateMax,
        targetAudience: form.targetAudience,
        riskWarning: form.riskWarning,
        isRemote: form.isRemote ? 1 : 0,
        isFreeEntry: form.isFreeEntry ? 1 : 0,
        tags: form.tags.join(','),
        status: 0,
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
const loadProjectDetail = async (id) => {
  try {
    console.log("id", id)
    const res = await get(`/api/unauth/project/detailForUpdate?projectId=${id}`);
    const data = res;
    console.log("form after before", data);

    // 逐个赋值
    form.imageUrl = data.imageUrl || '';
    form.steps = data.steps || '';
    form.tools = data.tools || '';
    form.timePerDay = data.timePerDay || '';
    form.incomeEstimateMin = data.incomeEstimateMin || '';
    form.incomeEstimateMax = data.incomeEstimateMax || '';
    form.targetAudience = data.targetAudience || '';
    form.riskWarning = data.riskWarning || '';
    form.isRemote = data.isRemote === 1;
    form.isFreeEntry = data.isFreeEntry === 1;
    // form.tags = data.tags?.split(',') || [];
    form.memberNum = data.memberNum || null;
    form.tags = data.tags ? data.tags.split(',').map(Number) : []; 

    // 补充一些必须字段，防止报错（后端未返回）
    form.name = data.name || '从接口补充名称';
    form.description = data.description || '';
    form.difficulty = data.difficulty || 1;
    form.needMember = data.needMember || 1;
    form.memberNum = data.memberNum || 1;

    // 分类回显处理 - 先设置一级分类
    form.firstCategory = data.firstCategory || null;
    form.secondCategory = data.secondCategory || null;

    // 如果有二级分类，需要先找到对应的一级分类，再设置二级分类选项
    if (data.secondCategory) {
      // 等待分类数据加载完成
      await nextTick();
      
      // 查找当前二级分类属于哪个一级分类
      const firstCategory = allCategoryData.value.find(item => 
        item.subs && item.subs.some(sub => sub.code === data.secondCategory)
      );
      
      if (firstCategory) {
        // 设置一级分类
        form.firstCategory = firstCategory.code;
        // 设置二级分类选项
        secondCategoryOptions.value = firstCategory.subs || [];
        // 设置二级分类值
        form.secondCategory = data.secondCategory;
      }
    } else if (data.firstCategory) {
      // 只有一级分类的情况
      const firstCategory = allCategoryData.value.find(item => item.code === data.firstCategory);
      if (firstCategory) {
        secondCategoryOptions.value = firstCategory.subs || [];
      }
    }

    console.log("form after setting", form);

    // 等编辑器初始化完成后再设置内容
    nextTick(() => {
      if (editorRef.value) {
        editorRef.value.setHtml(data.steps || '');
      }
    });

  } catch (error) {
    ElMessage.error('项目加载失败');
  }
};

</script>

<style scoped>


/* 高级感标签样式：更深的灰色 + 更粗字体 + 合适字号 */
::v-deep(.el-form-item__label) {
  color: #333;              /* 更深的灰色 */
  font-weight: 500;         /* 半粗 */
  font-size: 15px;          /* 略大一点 */
}
.unit-label {
  color: #333;          /* 与 label 保持一致 */
  font-weight: 500;     /* 半粗字体 */
  font-size: 15px;      /* 字号一致 */
  margin-left: 6px;
}
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

.el-input-number__decrease,
.el-input-number__increase {
  background-color: white !important;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  width: 28px;
  height: 28px;
  color: #666;
}

.el-input-number__decrease:hover,
.el-input-number__increase:hover {
  background-color: #f0f0f0 !important;
  color: #333;
}

</style>
