<template>
  <div class="home-aesthetic-root">
    <!-- 主理人轻氧杂志风卡片 -->
    <ProfileHero />
    
    <!-- 金刚区轻奢导航 -->
    <HomeNavGrid />

    <!-- 浮空微胶囊搜索区 -->
    <div class="search-floating-wrapper">
      <SearchBar @search="handleSearch" />
    </div>

    <!-- 极简吸顶分类导航栏 (Airy Sticky Navigation) -->
    <div class="category-sticky-bar">
      <div class="category-scroll-track">
        <button 
          v-for="cat in categories" 
          :key="cat.key" 
          class="cat-pill-btn"
          :class="{ active: activeCategory === cat.key }"
          @click="selectCategory(cat.key)"
        >
          <span v-if="cat.icon" class="cat-pill-icon">{{ cat.icon }}</span>
          <span class="cat-pill-text">{{ cat.title }}</span>
        </button>
      </div>
    </div>

    <!-- 小红书双列现代流光瀑布流 -->
    <ArticleGrid :articles="filteredArticles" :campaigns="showStore ? campaigns : []" :loading="loading" />
    
    <!-- 创作者专属悬浮快捷按钮 -->
    <div v-if="canCreate" class="creative-fab-btn" @click="$router.push('/create')" title="书写新灵感">
      <icon-plus />
    </div>
  </div>
</template>

<script>
import axios from '@/utils/request'
import ProfileHero from '@/components/home/ProfileHero.vue'
import HomeNavGrid from '@/components/home/HomeNavGrid.vue'
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
    HomeNavGrid,
    ArticleGrid,
    SearchBar
  },
  data() {
    return {
      articles: [],
      campaigns: [],
      loading: false,
      searchQuery: '',
      activeCategory: 'all',
      categories: [
        { key: 'all', title: '探索推荐', icon: '✨' },
        { key: 'lifestyle', title: '生活美学', icon: '🌿' },
        { key: 'brand', title: '独立品牌', icon: '☕' },
        { key: 'baking', title: '手作烘焙', icon: '🥖' }
      ]
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    user() {
      return this.userInfo
    },
    canCreate() {
      return this.user && (this.user.role === 'ADMIN' || this.user.role === 'CREATOR')
    },
    showStore() {
      return import.meta.env.VITE_SHOW_STORE !== 'false';
    },
    filteredArticles() {
      if (this.activeCategory === 'all') return this.articles;
      const tagMap = {
        'lifestyle': '生活方式',
        'brand': '独立品牌',
        'baking': '手作记录'
      };
      return this.articles.filter(a => a.tags && a.tags.includes(tagMap[this.activeCategory]));
    }
  },
  created() {
    this.fetchArticles()
  },
  methods: {
    selectCategory(key) {
      this.activeCategory = key;
    },
    handleSearch(query) {
      this.searchQuery = query
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.fetchArticles()
      }, 350)
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
          getCampaigns().catch(() => ({ data: { data: [] } }))
        ]);
        
        this.articles = artRes.data.data || [];
        this.campaigns = (camRes.data.data || []).filter(c => c.status === 1);
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
.home-aesthetic-root {
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: relative;
  min-height: 100vh;
  padding-bottom: 96px;
  background: transparent;
}

.search-floating-wrapper {
  padding: 0 16px;
  margin-top: 2px;
}

/* 现代极简吸顶分类导航栏 */
.category-sticky-bar {
  position: sticky;
  top: 0;
  z-index: 80;
  padding: 8px 16px;
  background: rgba(250, 250, 250, 0.88);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

.category-scroll-track {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.category-scroll-track::-webkit-scrollbar {
  display: none;
}

.cat-pill-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 16px;
  border-radius: 20px;
  border: 1px solid transparent;
  background: rgba(0, 0, 0, 0.04);
  color: #4E5969;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;
  letter-spacing: -0.2px;
}

.cat-pill-btn.active {
  background: #1A1D20;
  color: #FFFFFF;
  box-shadow: 0 4px 14px rgba(26, 29, 32, 0.2);
  transform: scale(1.02);
}

.cat-pill-btn:active {
  transform: scale(0.96);
}

.cat-pill-icon {
  font-size: 13px;
}

/* 浮动发帖按钮 */
.creative-fab-btn {
  position: fixed;
  right: 20px;
  bottom: 100px;
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  color: white;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(255, 42, 84, 0.35);
  cursor: pointer;
  z-index: 100;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  font-size: 24px;
}

.creative-fab-btn:hover {
  transform: scale(1.08) rotate(90deg);
  box-shadow: 0 12px 30px rgba(255, 42, 84, 0.45);
}

.creative-fab-btn:active {
  transform: scale(0.92);
}

@media (max-width: 768px) {
  .home-aesthetic-root {
    gap: 10px;
  }
  .search-floating-wrapper {
    padding: 0 12px;
  }
  .category-sticky-bar {
    padding: 6px 12px;
  }
}
</style>
