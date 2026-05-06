<template>
  <el-card>
    <div slot="header" class="clearfix">
      <span>发布新文章</span>
      <el-button style="float: right; padding: 3px 0" type="text" @click="$router.push('/')">返回列表</el-button>
    </div>
    
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题"></el-input>
      </el-form-item>
      
      <el-form-item label="正文" prop="content">
        <el-input type="textarea" :rows="10" v-model="form.content" placeholder="请输入文章正文..."></el-input>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="onSubmit" :loading="submitting">发布</el-button>
        <el-button @click="$router.push('/')">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import axios from 'axios'

export default {
  name: 'CreateArticle',
  data() {
    return {
      form: {
        title: '',
        content: ''
      },
      rules: {
        title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入文章正文', trigger: 'blur' }]
      },
      submitting: false
    }
  },
  methods: {
    onSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          this.submitting = true
          try {
            await axios.post('/api/articles', this.form)
            this.$message.success('文章发布成功！')
            this.$router.push('/')
          } catch (error) {
            this.$message.error('文章发布失败，请稍后重试')
          } finally {
            this.submitting = false
          }
        }
      })
    }
  }
}
</script>
