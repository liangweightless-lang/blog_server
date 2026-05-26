<template>
  <div class="article-manager">
    <div class="tab-header">
      <span style="margin-right: 15px; color: #8C6A5D; font-weight: bold;">[测试日志] 共 {{ articles.length }} 篇数据已到达</span>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="goToCreateArticle">发布新日常</el-button>
    </div>

    <el-table :data="articles" v-loading="loadingArticles" stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="封面" width="100">
        <template slot-scope="scope">
          <el-image :src="scope.row.coverUrl" style="width: 50px; height: 50px; border-radius: 8px;" fit="cover">
            <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="文章信息">
        <template slot-scope="scope">
          <div style="font-weight: bold; font-size: 15px; color: #333;">{{ scope.row.title }}</div>
          <div style="font-size: 12px; color: #999;">{{ formatTime(scope.row.createTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="likesCount" label="数据" width="100">
        <template slot-scope="scope">
          <span class="stat-badge"><i class="el-icon-sugar"></i> {{ scope.row.likesCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="danger" plain @click="handleDeleteArticle(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ArticleManager',
  data() {
    return {
      loadingArticles: false,
      articles: []
    }
  },
  created() {
    this.fetchArticles();
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    formatTime(timeStr) {
      return new Date(timeStr).toLocaleString();
    },
    async fetchArticles() {
      this.loadingArticles = true;
      try {
        const res = await axios.get('/api/articles');
        this.articles = res.data.data || [];
      } catch (error) {
        this.$message.error('加载文章失败');
      } finally {
        this.loadingArticles = false;
      }
    },
    goToCreateArticle() {
      this.$router.push('/create');
    },
    async handleDeleteArticle(article) {
      try {
        await this.$confirm('确定要删除这篇日常吗？', '提示', { type: 'warning' });
        await axios.delete(`/api/articles/${article.id}`, { headers: this.getAuthHeader() });
        this.$message.success('已删除');
        this.fetchArticles();
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败');
      }
    }
  }
}
</script>

<style scoped>
.tab-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}
.stat-badge {
  background: #fff0ed;
  color: #FF7E67;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #ccc;
  font-size: 20px;
}
</style>
