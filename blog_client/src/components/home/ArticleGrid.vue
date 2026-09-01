<template>
  <div class="editorial-grid-wrapper">
    <a-spin :loading="loading" style="width: 100%; display: block;">
      <div class="masonry-waterfall" v-if="mixedItems.length > 0">
        <div class="masonry-brick" v-for="(item, index) in mixedItems" :key="item.type + '-' + item.data.id">
          
          <!-- 灵感手作日记卡片 (小红书/即刻高级风) -->
          <div 
            v-if="item.type === 'article'" 
            class="clean-article-card" 
            @click="viewArticle(item.data)"
          >
            <div class="card-cover-box">
              <img 
                v-if="isValidUrl(item.data.coverUrl)" 
                :src="item.data.coverUrl" 
                class="card-img"
                alt="cover" 
                loading="lazy" 
              />
              <div v-else class="card-img-placeholder" :style="{ background: getGradient(item.data.id), height: getRandomHeight(item.data.id) + 'px' }">
                <span class="placeholder-sparkle">✦</span>
              </div>
              <div v-if="item.data.tags && item.data.tags.length" class="floating-tag-pill">
                #{{ item.data.tags[0] }}
              </div>
            </div>
            
            <div class="card-meta-box">
              <h3 class="card-story-title">{{ item.data.title }}</h3>
              
              <div class="card-footer-row">
                <div class="author-micro-info">
                  <img :src="homeConfig.avatarUrl || '/img/avatar.png'" class="author-micro-avatar" />
                  <span class="author-micro-name">{{ homeConfig.authorName || '小柴包' }}</span>
                </div>
                <div class="like-interaction" @click.stop="handleLike(item.data)">
                  <icon-heart-fill v-if="isLiked(item.data.id)" class="heart-svg-icon is-liked" />
                  <icon-heart v-else class="heart-svg-icon" />
                  <span class="like-counter">{{ (item.data.likesCount || 0) + (isLiked(item.data.id) ? 1 : 0) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 社区快团高质感推荐卡片 -->
          <div 
            v-else-if="item.type === 'campaign'" 
            class="clean-article-card campaign-spotlight-card" 
            @click="$router.push(`/campaign/${item.data.id}`)"
          >
            <div class="card-cover-box">
              <div class="campaign-fire-badge">
                <span class="fire-flame">🔥</span>
                <span>正在热团</span>
              </div>
              <img v-if="item.data.products && item.data.products[0]" :src="item.data.products[0].product?.image" class="card-img" loading="lazy" />
              <div v-else class="card-img-placeholder campaign-placeholder">🎁 快团特惠</div>
            </div>
            <div class="card-meta-box campaign-meta">
              <h3 class="card-story-title campaign-title">
                <span class="inline-campaign-badge">快团</span>
                {{ item.data.title }}
              </h3>
              <div class="card-footer-row">
                <div class="campaign-price-block">
                  <span class="campaign-price-val">¥{{ getMinPrice(item.data) }}</span>
                  <span class="campaign-price-from">起</span>
                </div>
                <button class="campaign-join-btn">去拼团</button>
              </div>
            </div>
          </div>

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
        'linear-gradient(135deg, #FF9A8B 0%, #FF6A88 100%)',
        'linear-gradient(135deg, #FEE140 0%, #FA709A 100%)',
        'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
        'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
      ];
      return gradients[(id || 0) % gradients.length];
    },
    getRandomHeight(id) {
      const heights = [190, 230, 210, 250];
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
.editorial-grid-wrapper {
  width: 100%;
}

.masonry-waterfall {
  column-count: 3;
  column-gap: 14px;
  width: 100%;
  padding: 0 16px;
}

.masonry-brick {
  break-inside: avoid;
  margin-bottom: 14px;
}

/* 纯净轻氧无界卡片 */
.clean-article-card {
  border-radius: 18px;
  overflow: hidden;
  background: #FFFFFF;
  box-shadow: 0 4px 20px rgba(17, 24, 39, 0.04), 0 1px 3px rgba(0, 0, 0, 0.02);
  cursor: pointer;
  transition: all 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.clean-article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(17, 24, 39, 0.08);
}

.clean-article-card:active {
  transform: scale(0.98);
}

.card-cover-box {
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #F2F3F5;
}

.card-img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.clean-article-card:hover .card-img {
  transform: scale(1.03);
}

.floating-tag-pill {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  letter-spacing: 0.2px;
}

.card-img-placeholder {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.placeholder-sparkle {
  font-size: 32px;
  color: #FFFFFF;
  opacity: 0.9;
}

.card-meta-box {
  padding: 12px 14px;
}

.card-story-title {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 700;
  color: #1A1D20;
  line-height: 1.45;
  letter-spacing: -0.2px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-micro-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.author-micro-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.author-micro-name {
  font-size: 12px;
  color: #86909C;
  font-weight: 500;
}

.like-interaction {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #86909C;
  cursor: pointer;
  padding: 2px 4px;
}

.heart-svg-icon {
  font-size: 14px;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.heart-svg-icon.is-liked {
  color: #FF3B30;
  animation: heartPop 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes heartPop {
  0% { transform: scale(0.8); }
  50% { transform: scale(1.35); }
  100% { transform: scale(1); }
}

.like-counter {
  font-size: 12px;
  font-weight: 600;
}

/* 快团专属高质感设计 */
.campaign-spotlight-card {
  background: linear-gradient(180deg, #FFFDFB 0%, #FFFFFF 100%);
  border: 1px solid rgba(255, 126, 103, 0.15);
}

.campaign-fire-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%);
  color: #FFFFFF;
  padding: 3px 8px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 3px;
  box-shadow: 0 4px 10px rgba(255, 65, 108, 0.35);
}

.inline-campaign-badge {
  background: #FF5E3A;
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 4px;
  margin-right: 4px;
}

.campaign-price-val {
  font-size: 16px;
  font-weight: 800;
  color: #FF3B30;
}

.campaign-price-from {
  font-size: 10px;
  color: #86909C;
  margin-left: 2px;
}

.campaign-join-btn {
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  color: #FFFFFF;
  border: none;
  font-size: 11px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 14px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(255, 42, 84, 0.3);
}

@media (max-width: 768px) {
  .masonry-waterfall {
    column-count: 2;
    column-gap: 10px;
    padding: 0 12px;
  }
  .masonry-brick {
    margin-bottom: 10px;
  }
  .card-meta-box {
    padding: 10px 10px;
  }
  .card-story-title {
    font-size: 13px;
    margin-bottom: 8px;
  }
}
</style>
