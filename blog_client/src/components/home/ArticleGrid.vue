<template>
  <div class="article-grid-container">

    <a-spin :loading="loading" style="width: 100%; display: block;">
      <div class="waterfall-grid" v-if="mixedItems.length > 0">
        <div class="waterfall-item" v-for="(item, index) in mixedItems" :key="item.type + '-' + item.data.id">
          
          <!-- 常规日记卡片 -->
          <a-card v-if="item.type === 'article'" class="xhs-card" hoverable :bordered="false" :body-style="{ padding: '0px' }" @click="viewArticle(item.data)">
            <div v-if="isValidUrl(item.data.coverUrl)" class="xhs-cover-img">
              <img :src="item.data.coverUrl" alt="cover" />
            </div>
            <div v-else class="xhs-cover-placeholder" :style="{ background: getGradient(item.data.id), height: getRandomHeight(item.data.id) + 'px' }">
              <span class="cover-icon">✨</span>
            </div>
            
            <div class="xhs-info">
              <div class="xhs-title">{{ item.data.title }}</div>
              <div class="xhs-bottom">
                <div class="xhs-author">
                  <a-avatar :size="20">
                    <img :src="homeConfig.avatarUrl || '/img/avatar.png'" />
                  </a-avatar>
                  <span class="xhs-author-name">{{ homeConfig.authorName || '小柴包' }}</span>
                </div>
                <div class="xhs-likes">
                  <icon-heart /> {{ item.data.likesCount || 0 }}
                </div>
              </div>
            </div>
          </a-card>

          <!-- 快团引流卡片 -->
          <a-card v-else-if="item.type === 'campaign'" class="xhs-card campaign-ad-card" hoverable :bordered="false" :body-style="{ padding: '0px' }" @click="$router.push(`/campaign/${item.data.id}`)">
            <div class="campaign-ad-cover">
              <div class="campaign-ad-badge"><icon-fire /> 正在热团</div>
              <img v-if="item.data.products && item.data.products[0]" :src="item.data.products[0].product?.image" />
              <div v-else class="campaign-ad-placeholder">🎁 社区快团特惠</div>
            </div>
            <div class="xhs-info campaign-info">
              <div class="xhs-title campaign-ad-title">【快团】{{ item.data.title }}</div>
              <div class="xhs-bottom">
                <div class="xhs-author">
                  <span class="campaign-price">¥{{ getMinPrice(item.data) }}<span style="font-size: 10px; margin-left: 2px;">起</span></span>
                </div>
                <div class="xhs-likes">
                  <a-button size="mini" type="primary" shape="round" class="campaign-go-btn">去抢购</a-button>
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
    campaigns: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    mixedItems() {
      const items = [];
      let cIdx = 0;
      const articleList = this.articles || [];
      
      for (let i = 0; i < articleList.length; i++) {
        items.push({ type: 'article', data: articleList[i] });
        // 在第 2 篇和之后每隔 4 篇插入一个快团卡片
        if ((i === 1 || (i > 1 && (i - 1) % 4 === 0)) && cIdx < this.campaigns.length) {
          items.push({ type: 'campaign', data: this.campaigns[cIdx] });
          cIdx++;
        }
      }
      
      // 如果文章极少，但有快团，保证至少展示一个
      if (articleList.length < 2 && this.campaigns.length > 0) {
        items.push({ type: 'campaign', data: this.campaigns[0] });
      }
      return items;
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
    },
    getMinPrice(campaign) {
      if (!campaign.products || campaign.products.length === 0) return 0;
      return Math.min(...campaign.products.map(p => p.groupPrice)).toFixed(2);
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
  transition: color 0.3s;
}

.xhs-likes i {
  font-size: 14px;
  color: #C9CDD4; /* default gray */
  transition: transform 0.3s, color 0.3s;
}

.xhs-card:hover .xhs-likes i {
  color: var(--brand-primary, #FF4B2B);
  transform: scale(1.1);
}

/* 快团专属卡片样式 */
.campaign-ad-card {
  background: linear-gradient(180deg, #FFFFFF 0%, #FFF5F4 100%) !important;
  border: 1px solid rgba(255, 75, 43, 0.15) !important;
}

.campaign-ad-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 1; /* 1:1 Square */
  background: #F2F3F5;
  overflow: hidden;
}

.campaign-ad-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.campaign-ad-card:hover .campaign-ad-cover img {
  transform: scale(1.05);
}

.campaign-ad-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #FF4B2B;
  font-weight: bold;
  background: linear-gradient(135deg, #FFE1D9 0%, #FFC1B6 100%);
}

.campaign-ad-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%));
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: bold;
  z-index: 2;
  box-shadow: 0 4px 8px rgba(255, 75, 43, 0.3);
}

.campaign-info {
  background: transparent;
}

.campaign-ad-title {
  color: var(--brand-primary, #FF4B2B) !important;
}

.campaign-price {
  color: #F53F3F;
  font-size: 18px;
  font-weight: 800;
}

.campaign-go-btn {
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%));
  border: none;
  font-weight: bold;
  box-shadow: 0 4px 8px rgba(255, 75, 43, 0.2);
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
