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
import axios from '@/utils/request'
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
  padding: 12px 15px;
  margin-top: -10px;
  margin-bottom: 10px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
}

.admin-fab {
  position: fixed;
  right: 24px;
  bottom: 40px;
  background: linear-gradient(135deg, rgba(255, 126, 103, 0.95) 0%, rgba(255, 90, 68, 0.95) 100%);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: white;
  width: 120px;
  height: 48px;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.4);
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  font-weight: 700;
  font-size: 14px;
}

.admin-fab:hover {
  transform: scale(1.05) translateY(-4px);
  box-shadow: 0 14px 30px rgba(255, 126, 103, 0.5);
}

.admin-fab:active {
  transform: scale(0.95) translateY(0);
}

@media (max-width: 768px) {
  .home-container {
    gap: 16px;
  }
  .admin-fab {
    right: 20px;
    bottom: 110px; /* 调高位置，避开全新的悬浮底部菜单栏 */
    width: 110px;
    height: 44px;
    font-size: 13px;
  }
}
</style>
