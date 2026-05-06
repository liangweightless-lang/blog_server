<template>
  <div>
    <el-card>
      <div slot="header" class="clearfix">
        <span>Article List</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="$router.push('/create')">Publish New Article</el-button>
      </div>
      
      <el-table :data="articles" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="Title"></el-table-column>
        <el-table-column prop="createTime" label="Publish Date" width="180">
          <template slot-scope="scope">
            {{ new Date(scope.row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="120">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewArticle(scope.row)">Read</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="currentArticle.title" :visible.sync="dialogVisible" width="60%">
      <div v-html="formatContent(currentArticle.content)"></div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">Close</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Home',
  data() {
    return {
      articles: [],
      loading: false,
      dialogVisible: false,
      currentArticle: {}
    }
  },
  created() {
    this.fetchArticles()
  },
  methods: {
    async fetchArticles() {
      this.loading = true
      try {
        const res = await axios.get('/api/articles')
        this.articles = res.data
      } catch (error) {
        this.$message.error('Failed to load articles')
      } finally {
        this.loading = false
      }
    },
    viewArticle(article) {
      this.currentArticle = article
      this.dialogVisible = true
    },
    formatContent(content) {
      if (!content) return ''
      return content.replace(/\n/g, '<br>')
    }
  }
}
</script>
