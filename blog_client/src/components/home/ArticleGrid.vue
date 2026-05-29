<template>
  <div class="article-grid-container">

    <a-spin :loading="loading" style="width: 100%; display: block;">
      <div class="waterfall-grid" v-if="articles.length > 0">
        <div class="waterfall-item" v-for="article in articles" :key="article.id">
          <a-card class="xhs-card" hoverable :bordered="false" :body-style="{ padding: '0px' }" @click="viewArticle(article)">
            <!-- 封面图：有图则使用img标签自适应高度，无图则使用随机高度占位 -->
            <div v-if="isValidUrl(article.coverUrl)" class="xhs-cover-img">
              <img :src="article.coverUrl" alt="cover" />
            </div>
            <div v-else class="xhs-cover-placeholder" :style="{ background: getGradient(article.id), height: getRandomHeight(article.id) + 'px' }">
              <span class="cover-icon">✨</span>
            </div>
            
            <div class="xhs-info">
              <div class="xhs-title">{{ article.title }}</div>
              <div class="xhs-bottom">
                <div class="xhs-author">
                  <a-avatar :size="20">
                    <img :src="homeConfig.avatarUrl || '/img/avatar.png'" />
                  </a-avatar>
                  <span class="xhs-author-name">{{ homeConfig.authorName || '小柴包' }}</span>
                </div>
                <div class="xhs-likes">
                  <icon-heart /> {{ article.likesCount || 0 }}
                </div>
              </div>
            </div>
          </a-card>
        </div>
      </div>
      <a-empty v-else-if="!loading" description="暂无内容，快来书写第一篇日记吧" style="margin-top: 40px;"></a-empty>
    </a-spin>

  </div>
</template>

<script>
import { getHomeConfig } from '@/api/common';

export default {
  name: 'ArticleGrid',
  props: {
    articles: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      homeConfig: {
        avatarUrl: '',
        authorName: ''
      }
    }
  },
  created() {
    this.fetchHomeConfig();
  },
  methods: {
    async fetchHomeConfig() {
      try {
        const res = await getHomeConfig();
        if (res.data && res.data.data) {
          this.homeConfig = res.data.data;
        }
      } catch (error) {
        console.error('获取首页配置失败', error);
      }
    },
    viewArticle(article) {
      this.$router.push(`/article/${article.id}`)
    },
    isValidUrl(url) {
      if (!url) return false;
      const s = url.trim();
      return !s.startsWith('<') && !s.includes('html') && !s.includes('DOCTYPE');
    },
    formatContent(content) {
      if (!content) return ''
      return content.replace(/\n/g, '<br>')
    },
    getGradient(id) {
      const gradients = [
        'linear-gradient(135deg, #FFE1D9 0%, #FFC1B6 100%)',
        'linear-gradient(135deg, #FFF0E6 0%, #FFD1A9 100%)',
        'linear-gradient(135deg, #E6F0FF 0%, #BFE0FF 100%)',
        'linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%)',
        'linear-gradient(135deg, #F3E5F5 0%, #E1BEE7 100%)',
      ];
      return gradients[id % gradients.length];
    },
    getRandomHeight(id) {
      // Generate pseudo-random heights between 160px and 260px for placeholders to ensure masonry effect
      const heights = [160, 200, 240, 180, 260, 190];
      return heights[id % heights.length];
    }
  }
}
</script>

<style scoped>
.waterfall-grid {
  column-count: 3;
  column-gap: 16px;
  width: 100%;
  margin-top: 10px;
}

.waterfall-item {
  break-inside: avoid;
  margin-bottom: 16px;
}

.xhs-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04) !important;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

:deep(.xhs-card > .arco-card-body) {
  padding: 0px;
  background: transparent;
}

:deep(.xhs-card:hover) {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08) !important;
}

.xhs-cover-img {
  width: 100%;
  display: block;
}

.xhs-cover-img img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
}

.xhs-cover-placeholder {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  font-size: 40px;
  opacity: 0.8;
}

.xhs-info {
  padding: 12px;
}

.xhs-title {
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.4;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.xhs-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.xhs-author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.xhs-author-name {
  font-size: 12px;
  color: #86909C;
}

.xhs-likes {
  font-size: 12px;
  color: #86909C;
  display: flex;
  align-items: center;
  gap: 4px;
}

.xhs-likes i {
  font-size: 14px;
  color: #FF7E67;
}

@media (max-width: 1024px) {
  .waterfall-grid {
    column-count: 3;
  }
}

@media (max-width: 768px) {
  .waterfall-grid {
    column-count: 2;
    column-gap: 12px;
  }
  .waterfall-item {
    margin-bottom: 12px;
  }
  .xhs-title {
    font-size: 14px;
  }
  .xhs-info {
    padding: 10px;
  }
}
</style>
