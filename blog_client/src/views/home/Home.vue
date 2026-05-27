<template>
  <div class="home-container">
    <ProfileHero />
    
    <!-- 搜索功能区 -->
    <div class="home-search-wrapper">
      <SearchBar @search="handleSearch" />
    </div>

    <ArticleGrid :articles="articles" :loading="loading" />
    
    <!-- 管理员专属悬浮按钮 -->
    <div v-if="isAdmin" class="admin-fab" @click="$router.push('/create')">
      <icon-plus />
      <span class="fab-text">发布日常</span>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import ProfileHero from '@/components/home/ProfileHero.vue'
import ArticleGrid from '@/components/home/ArticleGrid.vue'
import SearchBar from '@/components/common/SearchBar.vue'
import { Message } from '@arco-design/web-vue'
import { mapState } from 'pinia'
import { useUserStore } from '@/stores/user'

export default {
  name: 'Home',
  components: {
    ProfileHero,
    ArticleGrid,
    SearchBar
  },
  data() {
    return {
      articles: [],
      loading: false,
      searchQuery: ''
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    user() {
      return this.userInfo
    },
    isAdmin() {
      return this.user && this.user.role === 'ADMIN'
    }
  },
  created() {
    this.fetchArticles()
  },
  methods: {
    handleSearch(query) {
      this.searchQuery = query
      // 防抖处理：500ms 内没有新输入则发起请求
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.fetchArticles()
      }, 500)
    },
    async fetchArticles() {
      this.loading = true
      try {
        let url = '/api/articles'
        if (this.searchQuery) {
          url = `/api/articles/search?keyword=${encodeURIComponent(this.searchQuery)}`
        }
        const res = await axios.get(url)
        this.articles = res.data.data
      } catch (error) {
        Message.error('搜索日记失败')
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
  position: relative;
  min-height: 100vh;
}

.home-search-wrapper {
  padding: 0 10px;
  margin-top: -10px;
  margin-bottom: 10px;
  z-index: 10;
}

.admin-fab {
  position: fixed;
  right: 24px;
  bottom: 40px;
  background: linear-gradient(135deg, #FF7E67 0%, #FF5A44 100%);
  color: white;
  width: 120px;
  height: 48px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 8px 20px rgba(255, 126, 103, 0.4);
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  font-weight: 700;
  font-size: 14px;
}

.admin-fab:hover {
  transform: scale(1.05) translateY(-2px);
  box-shadow: 0 12px 24px rgba(255, 126, 103, 0.5);
}

.admin-fab:active {
  transform: scale(0.95);
}

@media (max-width: 768px) {
  .home-container {
    gap: 16px;
  }
  .admin-fab {
    right: 20px;
    bottom: 90px; /* 调高位置，避开底部菜单栏 */
    width: 110px;
    height: 44px;
    font-size: 13px;
  }
}
</style>
