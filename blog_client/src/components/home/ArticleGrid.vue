<template>
  <div class="article-grid-container">

    <a-spin :loading="loading" style="width: 100%; display: block;">
      <div class="waterfall-grid" v-if="mixedItems.length > 0">
        <div class="waterfall-item" v-for="(item, index) in mixedItems" :key="item.type + '-' + item.data.id">
          
          <!-- 常规日记卡片 (小红书/得物风格) -->
          <a-card 
            v-if="item.type === 'article'" 
            class="xhs-card" 
            hoverable 
            :bordered="false" 
            :body-style="{ padding: '0px' }" 
            @click="viewArticle(item.data)"
          >
            <div v-if="isValidUrl(item.data.coverUrl)" class="xhs-cover-img">
              <img :src="item.data.coverUrl" alt="cover" loading="lazy" />
              <div v-if="item.data.tags && item.data.tags.length" class="cover-tag-float">
                #{{ item.data.tags[0] }}
              </div>
            </div>
            <div v-else class="xhs-cover-placeholder" :style="{ background: getGradient(item.data.id), height: getRandomHeight(item.data.id) + 'px' }">
              <span class="cover-icon">✨</span>
              <div v-if="item.data.tags && item.data.tags.length" class="cover-tag-float">
                #{{ item.data.tags[0] }}
              </div>
            </div>
            
            <div class="xhs-info">
              <div class="xhs-title">{{ item.data.title }}</div>
              
              <div class="xhs-bottom">
                <div class="xhs-author">
                  <a-avatar :size="22" class="author-avatar-shadow">
                    <img :src="homeConfig.avatarUrl || '/img/avatar.png'" />
                  </a-avatar>
                  <span class="xhs-author-name">{{ homeConfig.authorName || '小柴包' }}</span>
                </div>
                <div class="xhs-likes" @click.stop="handleLike(item.data)">
                  <icon-heart-fill v-if="isLiked(item.data.id)" class="heart-icon liked" />
                  <icon-heart v-else class="heart-icon" />
                  <span class="likes-count">{{ (item.data.likesCount || 0) + (isLiked(item.data.id) ? 1 : 0) }}</span>
                </div>
              </div>
            </div>
          </a-card>

          <!-- 快团引流卡片 (超高转化率商品推荐) -->
          <a-card 
            v-else-if="item.type === 'campaign'" 
            class="xhs-card campaign-ad-card" 
            hoverable 
            :bordered="false" 
            :body-style="{ padding: '0px' }" 
            @click="$router.push(`/campaign/${item.data.id}`)"
          >
            <div class="campaign-ad-cover">
              <div class="campaign-ad-badge">
                <span class="fire-emoji">🔥</span>
                <span>正在热团</span>
              </div>
              <img v-if="item.data.products && item.data.products[0]" :src="item.data.products[0].product?.image" loading="lazy" />
              <div v-else class="campaign-ad-placeholder">🎁 社区快团特惠</div>
            </div>
            <div class="xhs-info campaign-info">
              <div class="xhs-title campaign-ad-title">
                <span class="campaign-tag-inline">快团</span>
                {{ item.data.title }}
              </div>
              <div class="xhs-bottom">
                <div class="xhs-author">
                  <span class="campaign-price">¥{{ getMinPrice(item.data) }}<span class="price-suffix">起</span></span>
                </div>
                <div class="xhs-likes">
                  <button class="campaign-go-btn">去抢购</button>
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
import { Message } from '@arco-design/web-vue';

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
  data() {
    return {
      homeConfig: {
        avatarUrl: '',
        authorName: ''
      },
      likedIds: []
    };
  },
  computed: {
    mixedItems() {
      const items = [];
      let cIdx = 0;
      const articleList = this.articles || [];
      
      for (let i = 0; i < articleList.length; i++) {
        items.push({ type: 'article', data: articleList[i] });
        if ((i === 1 || (i > 1 && (i - 1) % 4 === 0)) && cIdx < this.campaigns.length) {
          items.push({ type: 'campaign', data: this.campaigns[cIdx] });
          cIdx++;
        }
      }
      
      if (articleList.length < 2 && this.campaigns.length > 0) {
        items.push({ type: 'campaign', data: this.campaigns[0] });
      }
      return items;
    }
  },
  created() {
    this.fetchHomeConfig();
    try {
      this.likedIds = JSON.parse(localStorage.getItem('user_liked_articles') || '[]');
    } catch (e) {
      this.likedIds = [];
    }
  },
  methods: {
    async fetchHomeConfig() {
      try {
        const res = await getHomeConfig();
        if (res.data && res.data.data) {
          this.homeConfig = res.data.data;
        }
      } catch (e) {
        // ignore
      }
    },
    isLiked(id) {
      return this.likedIds.includes(id);
    },
    handleLike(article) {
      if (this.isLiked(article.id)) {
        this.likedIds = this.likedIds.filter(i => i !== article.id);
      } else {
        this.likedIds.push(article.id);
        Message.success({ content: '已点赞 ❤️', duration: 1500 });
      }
      localStorage.setItem('user_liked_articles', JSON.stringify(this.likedIds));
    },
    isValidUrl(url) {
      if (!url) return false;
      return url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/uploads/') || url.startsWith('/img/');
    },
    getGradient(id) {
      const gradients = [
        'linear-gradient(135deg, #FF9A8B 0%, #FF6A88 55%, #FF99AC 100%)',
        'linear-gradient(135deg, #FEE140 0%, #FA709A 100%)',
        'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
        'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
      ];
      return gradients[(id || 0) % gradients.length];
    },
    getRandomHeight(id) {
      const heights = [200, 240, 220, 260];
      return heights[(id || 0) % heights.length];
    },
    viewArticle(article) {
      this.$router.push(`/article/${article.id}`);
    },
    getMinPrice(campaign) {
      if (!campaign.products || campaign.products.length === 0) return '0.00';
      const prices = campaign.products.map(p => p.groupPrice || p.price || 0);
      return Math.min(...prices).toFixed(2);
    }
  }
};
</script>

<style scoped>
.waterfall-grid {
  column-count: 3;
  column-gap: 14px;
  width: 100%;
  padding: 0 15px;
}

.waterfall-item {
  break-inside: avoid;
  margin-bottom: 14px;
}

.xhs-card {
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04) !important;
  cursor: pointer;
  background: var(--glass-bg, rgba(255, 255, 255, 0.85)) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;
}

:deep(.xhs-card > .arco-card-body) {
  padding: 0px;
  background: transparent;
}

:deep(.xhs-card:hover) {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(255, 126, 103, 0.12) !important;
}

:deep(.xhs-card:active) {
  transform: scale(0.98);
}

.xhs-cover-img {
  width: 100%;
  position: relative;
  overflow: hidden;
}

.xhs-cover-img img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.xhs-card:hover .xhs-cover-img img {
  transform: scale(1.03);
}

.cover-tag-float {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
}

.xhs-cover-placeholder {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.cover-icon {
  font-size: 36px;
  opacity: 0.9;
}

.xhs-info {
  padding: 12px 14px;
}

.xhs-title {
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.45;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  letter-spacing: -0.2px;
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

.author-avatar-shadow {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.xhs-author-name {
  font-size: 12px;
  color: #86909C;
  font-weight: 500;
}

.xhs-likes {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86909C;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.heart-icon {
  font-size: 14px;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.heart-icon.liked {
  color: #FF4D4F;
  animation: heartPop 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes heartPop {
  0% { transform: scale(0.8); }
  50% { transform: scale(1.4); }
  100% { transform: scale(1); }
}

.likes-count {
  font-size: 12px;
  font-weight: 600;
}

/* 快团卡片专属样式 */
.campaign-ad-card {
  background: linear-gradient(180deg, #FFF8F6 0%, #FFFFFF 100%) !important;
  border: 1px solid #FFE4D6 !important;
}

.campaign-ad-cover {
  position: relative;
  width: 100%;
}

.campaign-ad-cover img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.campaign-ad-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%);
  color: #FFFFFF;
  padding: 3px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 3px;
  box-shadow: 0 4px 10px rgba(255, 65, 108, 0.35);
  animation: badgePulse 2s infinite ease-in-out;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.campaign-tag-inline {
  background: linear-gradient(135deg, #FF7E67, #FF9A8B);
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 4px;
  margin-right: 4px;
}

.campaign-price {
  font-size: 16px;
  font-weight: 800;
  color: #FF4D4F;
}

.price-suffix {
  font-size: 10px;
  font-weight: 500;
  color: #86909C;
  margin-left: 2px;
}

.campaign-go-btn {
  background: linear-gradient(135deg, #FF7E67 0%, #FF416C 100%);
  color: #FFFFFF;
  border: none;
  font-size: 11px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 14px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(255, 126, 103, 0.3);
  transition: transform 0.2s ease;
}

.campaign-go-btn:active {
  transform: scale(0.92);
}

@media (max-width: 768px) {
  .waterfall-grid {
    column-count: 2;
    column-gap: 10px;
    padding: 0 12px;
  }
  .waterfall-item {
    margin-bottom: 10px;
  }
  .xhs-title {
    font-size: 13px;
    margin-bottom: 8px;
  }
  .xhs-info {
    padding: 10px 10px;
  }
}
</style>
