<template>
  <div class="home-container">
    <ProfileHero />
    <ArticleGrid :articles="articles" :loading="loading" />
  </div>
</template>

<script>
import axios from 'axios'
import ProfileHero from '@/components/home/ProfileHero.vue'
import ArticleGrid from '@/components/home/ArticleGrid.vue'

export default {
  name: 'Home',
  components: {
    ProfileHero,
    ArticleGrid
  },
  data() {
    return {
      articles: [],
      loading: false
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
        this.articles = res.data.data
      } catch (error) {
        this.$message.error('获取故事列表失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
@media (max-width: 768px) {
  .home-container {
    gap: 16px;
  }
}
</style>
