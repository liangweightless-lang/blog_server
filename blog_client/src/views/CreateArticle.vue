<template>
  <el-card>
    <div slot="header" class="clearfix">
      <span>Publish New Article</span>
      <el-button style="float: right; padding: 3px 0" type="text" @click="$router.push('/')">Back to List</el-button>
    </div>
    
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="Title" prop="title">
        <el-input v-model="form.title" placeholder="Enter article title"></el-input>
      </el-form-item>
      
      <el-form-item label="Content" prop="content">
        <el-input type="textarea" :rows="10" v-model="form.content" placeholder="Enter article content..."></el-input>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="onSubmit" :loading="submitting">Publish</el-button>
        <el-button @click="$router.push('/')">Cancel</el-button>
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
        title: [{ required: true, message: 'Please input title', trigger: 'blur' }],
        content: [{ required: true, message: 'Please input content', trigger: 'blur' }]
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
            this.$message.success('Article published successfully!')
            this.$router.push('/')
          } catch (error) {
            this.$message.error('Failed to publish article')
          } finally {
            this.submitting = false
          }
        }
      })
    }
  }
}
</script>
