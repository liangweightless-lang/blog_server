<template>
  <div class="home-container">
    <!-- 主理人认证卡片 -->
    <ProfileHero />
    
    <!-- 金刚区快捷导航 (市面成熟App标配) -->
    <HomeNavGrid />

    <!-- 搜索功能区 (毛玻璃质感) -->
    <div class="home-search-wrapper">
      <SearchBar @search="handleSearch" />
    </div>

    <!-- 分类微胶囊 Tab 栏 (吸顶粘性导航) -->
    <div class="category-tabs-sticky">
      <div class="category-capsule-list">
        <button 
          v-for="cat in categories" 
          :key="cat.key" 
          class="category-capsule-btn"
          :class="{ active: activeCategory === cat.key }"
          @click="selectCategory(cat.key)"
        >
          <span v-if="cat.icon" class="cat-icon">{{ cat.icon }}</span>
          <span>{{ cat.title }}</span>
        </button>
      </div>
    </div>

    <!-- 小红书双列双端瀑布流 -->
    <ArticleGrid :articles="filteredArticles" :campaigns="showStore ? campaigns : []" :loading="loading" />
    
    <!-- 创作者/管理员专属快捷发帖悬浮按钮 -->
    <div v-if="canCreate" class="admin-fab-mini" @click="$router.push('/create')" title="书写新灵感">
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
        { key: 'all', title: '推荐', icon: '✨' },
        { key: 'lifestyle', title: '生活方式', icon: '🌿' },
        { key: 'brand', title: '独立品牌', icon: '☕' },
        { key: 'baking', title: '手作记录', icon: '🥖' }
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
      }, 400)
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
.home-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  min-height: 100vh;
  padding-bottom: 90px;
}

.home-search-wrapper {
  padding: 0 15px;
}

/* 分类微胶囊吸顶栏 */
.category-tabs-sticky {
  position: sticky;
  top: 0;
  z-index: 90;
  padding: 8px 15px;
  background: var(--glass-bg, rgba(255, 255, 255, 0.85));
  backdrop-filter: var(--glass-blur, blur(20px));
  -webkit-backdrop-filter: var(--glass-blur, blur(20px));
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

.category-capsule-list {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.category-capsule-list::-webkit-scrollbar {
  display: none;
}

.category-capsule-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  background: #F7F8FA;
  color: #4E5969;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;
}

.category-capsule-btn.active {
  background: linear-gradient(135deg, #FF7E67 0%, #FF6A88 100%);
  color: #FFFFFF;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.3);
  transform: scale(1.02);
}

.category-capsule-btn:active {
  transform: scale(0.95);
}

.cat-icon {
  font-size: 13px;
}

/* 浮动发帖按钮 */
.admin-fab-mini {
  position: fixed;
  right: 20px;
  bottom: 105px;
  background: linear-gradient(135deg, #FF9A8B 0%, #FF6A88 55%, #FF99AC 100%);
  color: white;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(255, 106, 136, 0.4);
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  font-size: 24px;
}

.admin-fab-mini:hover {
  transform: scale(1.1) rotate(90deg);
  box-shadow: 0 12px 30px rgba(255, 106, 136, 0.5);
}

.admin-fab-mini:active {
  transform: scale(0.92);
}

@media (max-width: 768px) {
  .home-container {
    gap: 12px;
  }
}
</style>
