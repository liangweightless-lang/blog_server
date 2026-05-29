<template>
  <div class="home-container">
    <ProfileHero />
    
    <!-- 搜索功能区 -->
    <div class="home-search-wrapper">
      <SearchBar @search="handleSearch" />
    </div>

    <div class="category-tabs-wrapper">
      <a-tabs v-model:active-key="activeCategory" :hide-content="true" @change="handleCategoryChange" size="large">
        <a-tab-pane key="all" title="推荐" />
        <a-tab-pane key="lifestyle" title="生活方式" />
        <a-tab-pane key="brand" title="独立品牌" />
        <a-tab-pane key="baking" title="烘焙日常" />
      </a-tabs>
    </div>

    <ArticleGrid :articles="filteredArticles" :campaigns="campaigns" :loading="loading" />
    
    <!-- 管理员专属悬浮按钮 -->
    <div v-if="isAdmin" class="admin-fab-mini" @click="$router.push('/create')">
      <icon-plus />
    </div>
  </div>
</template>

<script>
import axios from '@/utils/request'
import ProfileHero from '@/components/home/ProfileHero.vue'
import ArticleGrid from '@/components/home/ArticleGrid.vue'
import SearchBar from '@/components/common/SearchBar.vue'
import { getCampaigns } from '@/api/campaign'
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
      campaigns: [],
      loading: false,
      searchQuery: '',
      activeCategory: 'all'
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    user() {
      return this.userInfo
    },
    isAdmin() {
      return this.user && this.user.role === 'ADMIN'
    },
    filteredArticles() {
      if (this.activeCategory === 'all') return this.articles;
      const tagMap = {
        'lifestyle': '生活方式',
        'brand': '独立品牌',
        'baking': '烘焙日常'
      };
      return this.articles.filter(a => a.tags && a.tags.includes(tagMap[this.activeCategory]));
    }
  },
  created() {
    this.fetchArticles()
  },
  methods: {
    handleCategoryChange() {
      // 本地筛选或重新拉取数据
    },
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
        
        const [artRes, camRes] = await Promise.all([
          axios.get(url),
          getCampaigns()
        ]);
        
        this.articles = artRes.data.data || [];
        this.campaigns = (camRes.data.data || []).filter(c => c.status === 1); // 仅进行中
      } catch (error) {
        Message.error('获取内容失败')
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
  margin-bottom: 0px;
  z-index: 10;
  background: var(--glass-bg, rgba(255, 255, 255, 0.7));
  backdrop-filter: var(--glass-blur, blur(20px));
  -webkit-backdrop-filter: var(--glass-blur, blur(20px));
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
}

.category-tabs-wrapper {
  margin-top: 0px;
  padding: 0 15px;
}
:deep(.category-tabs-wrapper .arco-tabs-nav-tab) {
  font-weight: 600;
  font-size: 15px;
}
:deep(.category-tabs-wrapper .arco-tabs-nav-tab-active) {
  font-weight: 800;
  color: var(--brand-primary);
  font-size: 16px;
}
:deep(.category-tabs-wrapper .arco-tabs-nav-ink) {
  background-color: var(--brand-primary);
}

.admin-fab-mini {
  position: fixed;
  right: 24px;
  bottom: 100px;
  background: var(--brand-gradient);
  color: white;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 24px rgba(255, 75, 43, 0.4);
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  font-size: 28px;
}

.admin-fab-mini:hover {
  transform: scale(1.1) rotate(90deg);
  box-shadow: 0 16px 32px rgba(255, 75, 43, 0.5);
}

.admin-fab-mini:active {
  transform: scale(0.9);
}

@media (max-width: 768px) {
  .home-container {
    gap: 12px;
  }
  .admin-fab-mini {
    right: 20px;
    bottom: 110px; /* 调高位置，避开全新的悬浮底部菜单栏 */
    width: 50px;
    height: 50px;
    font-size: 24px;
  }
}
</style>
