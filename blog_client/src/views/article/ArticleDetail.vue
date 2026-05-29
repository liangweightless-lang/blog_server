<template>
  <a-spin :loading="loading" style="width: 100%; display: block;">
    <div class="xhs-detail-container">
      <div class="xhs-detail-header">
        <div class="header-left" @click="$router.push('/')">
          <icon-left style="font-size: 24px;" />
        </div>
        <div class="header-author">
          <a-avatar class="author-avatar" :size="36">
            <img :src="homeConfig.avatarUrl || '/img/avatar.png'" />
          </a-avatar>
          <span class="author-name">{{ homeConfig.authorName || '小柴包' }}</span>
        </div>
        <div class="header-right">
          <a-button type="text" style="color: #FF7E67; font-size: 20px;" @click="shareArticle">
            <template #icon><icon-share-alt /></template>
          </a-button>
        </div>
      </div>

      <div class="xhs-media-carousel" v-if="mediaUrls.length > 0">
        <div
          class="swipe-carousel"
          ref="carouselRef"
          @touchstart="onTouchStart"
          @touchmove="onTouchMove"
          @touchend="onTouchEnd"
        >
          <div
            class="swipe-track"
            :style="{
              transform: `translateX(calc(-${currentSlide * 100}% + ${swipeOffset}px))`,
              transition: isSwiping ? 'none' : 'transform 0.35s cubic-bezier(0.25, 1, 0.5, 1)'
            }"
          >
            <div class="swipe-slide" v-for="(url, index) in mediaUrls" :key="index">
              <div class="carousel-image-wrapper">
                <a-image :src="url" class="carousel-image" width="100%" height="100%" fit="contain" />
              </div>
            </div>
          </div>
        </div>
        <div class="carousel-indicators" v-if="mediaUrls.length > 1">
          <span
            v-for="(_, i) in mediaUrls"
            :key="i"
            class="indicator-dot"
            :class="{ active: i === currentSlide }"
          ></span>
        </div>
        <div class="carousel-counter" v-if="mediaUrls.length > 1">
          {{ currentSlide + 1 }} / {{ mediaUrls.length }}
        </div>
      </div>
      <div class="xhs-media-placeholder" v-else :style="{ background: getGradient(article.id) }">
        <span class="cover-icon">✨</span>
      </div>

      <div class="xhs-detail-content">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-text" v-html="formatContent(article.content)"></div>
        <div class="article-meta">
          <span class="post-time">{{ $formatDate(article.createTime) }}</span>
        </div>

        <!-- 创作者推荐商品卡片 (已封装组件) -->
        <ArticleProductCard :product="product" @click="goToProduct" />

        <CommentSection v-if="article.id" :articleId="article.id" ref="commentSection" @update-count="commentCount = $event" />
      </div>

      <div class="xhs-detail-footer">
        <div class="footer-input-box">
          <a-input v-model="newComment" placeholder="说点什么..." size="large" class="custom-input" @press-enter="submitComment">
            <template #append>
              <a-button type="primary" @click="submitComment" style="background: #FF7E67; border: none;">发送</a-button>
            </template>
          </a-input>
        </div>
        <div class="footer-actions">
          <div class="action-btn" @click="likeArticle">
            <icon-heart />
            <span>{{ article.likesCount || '赞' }}</span>
          </div>
          <div class="action-btn" @click="toggleFavorite">
            <icon-star-fill v-if="isFavorited" style="color: #FF7E67;" />
            <icon-star v-else />
            <span :style="{ color: isFavorited ? '#FF7E67' : '' }">{{ isFavorited ? '已收藏' : '收藏' }}</span>
          </div>
          <div class="action-btn">
            <icon-message />
            <span>{{ commentCount || '评论' }}</span>
          </div>
          <div class="action-btn" @click="shareArticle">
            <icon-share-alt />
            <span>分享</span>
          </div>
        </div>
      </div>
      
      <!-- 自定义高级分享弹窗 -->
      <a-modal v-model:visible="shareModalVisible" :footer="false" :header="false" modal-class="custom-share-modal" unmount-on-close>
        <div class="share-modal-content">
          <div class="share-modal-header">
            <div class="success-icon-wrapper">
              <icon-check-circle-fill class="success-icon" />
            </div>
            <h2>链接已复制！</h2>
            <p>快去粘贴分享给你的好友吧</p>
          </div>
          
          <div class="share-article-preview">
            <div class="preview-img-box" :style="{ background: mediaUrls.length > 0 ? 'transparent' : getGradient(article.id) }">
              <img v-if="mediaUrls.length > 0" :src="mediaUrls[0]" />
              <span v-else class="preview-icon">✨</span>
            </div>
            <div class="preview-info">
              <div class="preview-title">{{ article.title }}</div>
              <div class="preview-author">
                <a-avatar :size="16"><img :src="homeConfig.avatarUrl || '/img/avatar.png'" /></a-avatar>
                <span>{{ homeConfig.authorName || '小柴包' }}</span>
              </div>
            </div>
          </div>
          
          <div class="share-actions">
            <a-button class="share-action-btn wechat-btn" shape="round" size="large" @click="shareModalVisible = false">
              <template #icon><icon-wechat /></template> 去微信粘贴
            </a-button>
          </div>
        </div>
      </a-modal>
    </div>
  </a-spin>
</template>

<script>
import request from '@/utils/request'
import { getArticleDetail, likeArticle, checkFavoriteStatus as getFavoriteStatus, toggleFavorite as apiToggleFavorite } from '@/api/article'
import { getProductDetail } from '@/api/product'
import { getHomeConfig } from '@/api/common'
import { Message, Modal } from '@arco-design/web-vue'
import ArticleProductCard from '@/components/article/ArticleProductCard.vue'
import CommentSection from '@/components/article/CommentSection.vue'
import DOMPurify from 'dompurify'

export default {
  name: 'ArticleDetail',
  components: {
    ArticleProductCard,
    CommentSection
  },
  data() {
    return {
      loading: true,
      article: {},
      mediaUrls: [],
      commentCount: 0,
      newComment: '',
      submitting: false,
      product: null,
      isFavorited: false,
      shareModalVisible: false,
      currentSlide: 0,
      swipeOffset: 0,
      isSwiping: false,
      touchStartX: 0,
      touchStartY: 0,
      touchStartTime: 0,
      homeConfig: {
        avatarUrl: '',
        authorName: ''
      }
    }
  },
  created() {
    this.fetchArticle()
    this.checkFavoriteStatus()
    this.fetchHomeConfig()
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
    async checkFavoriteStatus() {
      const token = localStorage.getItem('token');
      if (!token) return;
      const id = this.$route.params.id;
      try {
        const res = await getFavoriteStatus(id);
        this.isFavorited = res.data.data;
      } catch (e) {
        console.error('获取收藏状态失败');
      }
    },
    async toggleFavorite() {
      const token = localStorage.getItem('token');
      if (!token) {
        Message.warning('请先登录后收藏');
        return;
      }
      const id = this.$route.params.id;
      try {
        await apiToggleFavorite(id);
        this.isFavorited = !this.isFavorited;
        Message.success(this.isFavorited ? '收藏成功' : '已取消收藏');
        window.dispatchEvent(new CustomEvent('favorites-updated'));
      } catch (e) {
        Message.error('操作失败');
      }
    },
    async fetchArticle() {
      const id = this.$route.params.id
      try {
        const res = await getArticleDetail(id)
        this.article = (res.data && res.data.data) ? res.data.data : {}
        if (this.article.mediaUrls) {
          try {
            let urls = JSON.parse(this.article.mediaUrls)
            if (Array.isArray(urls)) {
              urls = urls.filter(u => typeof u === 'string' && !u.trim().startsWith('<') && !u.includes('html'));
              // In Capacitor, relative /uploads/ paths won't resolve — prepend backend base URL
              if (request.defaults.baseURL) {
                const base = request.defaults.baseURL.replace(/\/$/, '')
                urls = urls.map(u => (typeof u === 'string' && u.startsWith('/uploads/')) ? base + u : u)
              }
              this.mediaUrls = urls
            } else {
              this.mediaUrls = []
            }
          } catch (e) {
            this.mediaUrls = []
          }
        }
        if (this.article.productId) {
          this.fetchProduct(this.article.productId)
        }
      } catch (error) {
        Message.error('无法加载日记详情')
      } finally {
        this.loading = false
      }
    },
    async fetchProduct(productId) {
      try {
        const res = await getProductDetail(productId)
        this.product = (res.data && res.data.data) ? res.data.data : null;
      } catch (error) {
        console.error('获取商品详情失败', error)
      }
    },
    formatContent(content) {
      if (!content) return ''
      const safeContent = DOMPurify.sanitize(content)
      return safeContent.replace(/\n/g, '<br>')
    },
    async submitComment() {
      if (!this.newComment.trim() || this.submitting) return;
      this.submitting = true;
      try {
        const success = await this.$refs.commentSection.submitComment(this.newComment);
        if (success) {
          this.newComment = '';
        }
      } finally {
        this.submitting = false;
      }
    },
    async likeArticle() {
      const id = this.$route.params.id;
      try {
        await likeArticle(id);
        if (this.article.likesCount === undefined || this.article.likesCount === null) {
          this.article.likesCount = 0;
        }
        this.article.likesCount++;
        Message.success('点赞成功');
      } catch (error) {
        Message.error('点赞失败');
      }
    },
    shareArticle() {
      const url = window.location.href;
      const copyToClipboard = (text) => {
        if (navigator.clipboard && window.isSecureContext) {
          return navigator.clipboard.writeText(text);
        } else {
          const textArea = document.createElement("textarea");
          textArea.value = text;
          textArea.style.position = "fixed";
          textArea.style.left = "-9999px";
          textArea.style.top = "-9999px";
          document.body.appendChild(textArea);
          textArea.focus();
          textArea.select();
          try {
            const successful = document.execCommand('copy');
            document.body.removeChild(textArea);
            return successful ? Promise.resolve() : Promise.reject();
          } catch (err) {
            document.body.removeChild(textArea);
            return Promise.reject(err);
          }
        }
      };

      copyToClipboard(url).then(() => {
        this.shareModalVisible = true;
      }).catch(() => {
        Message.error('复制失败，请手动复制浏览器地址栏链接分享');
      });
    },
    goToProduct() {
      if (this.product) {
        this.$router.push({
          path: '/store',
          query: { buyProductId: this.product.id }
        })
      }
    },
    onTouchStart(e) {
      this.touchStartX = e.touches[0].clientX;
      this.touchStartY = e.touches[0].clientY;
      this.touchStartTime = Date.now();
      this.isSwiping = false;
      this.swipeOffset = 0;
    },
    onTouchMove(e) {
      const dx = e.touches[0].clientX - this.touchStartX;
      const dy = e.touches[0].clientY - this.touchStartY;
      // Only treat as horizontal swipe if horizontal movement > vertical
      if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 10) {
        e.preventDefault();
        this.isSwiping = true;
        // Add resistance at edges
        if ((this.currentSlide === 0 && dx > 0) ||
            (this.currentSlide === this.mediaUrls.length - 1 && dx < 0)) {
          this.swipeOffset = dx * 0.3; // rubber band effect
        } else {
          this.swipeOffset = dx;
        }
      }
    },
    onTouchEnd() {
      if (!this.isSwiping) return;
      const threshold = 50;
      const velocity = Math.abs(this.swipeOffset) / (Date.now() - this.touchStartTime);
      if (this.swipeOffset < -threshold || (this.swipeOffset < -20 && velocity > 0.3)) {
        // Swipe left → next
        if (this.currentSlide < this.mediaUrls.length - 1) {
          this.currentSlide++;
        }
      } else if (this.swipeOffset > threshold || (this.swipeOffset > 20 && velocity > 0.3)) {
        // Swipe right → prev
        if (this.currentSlide > 0) {
          this.currentSlide--;
        }
      }
      this.swipeOffset = 0;
      this.isSwiping = false;
    },
    getGradient(id) {
      if (!id) return '#FFF0E6';
      const gradients = [
        'linear-gradient(135deg, #FFE1D9 0%, #FFC1B6 100%)',
        'linear-gradient(135deg, #FFF0E6 0%, #FFD1A9 100%)',
        'linear-gradient(135deg, #E6F0FF 0%, #BFE0FF 100%)',
        'linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%)',
        'linear-gradient(135deg, #F3E5F5 0%, #E1BEE7 100%)',
      ];
      return gradients[id % gradients.length];
    }
  }
}
</script>

<style scoped>
.xhs-detail-container {
  background: transparent;
  border-radius: 24px;
  overflow: hidden;
  position: relative;
  min-height: 100vh;
  padding-bottom: 70px;
}

.xhs-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-left {
  font-size: 24px;
  color: #1D2129;
  cursor: pointer;
  width: 60px;
  transition: transform 0.2s ease;
}
.header-left:active {
  transform: scale(0.9);
}

.header-author {
  display: flex;
  align-items: center;
  gap: 10px;
}



.author-name {
  font-weight: 700;
  color: #1D2129;
  font-size: 15px;
}

.header-right {
  width: 60px;
  text-align: right;
}

.xhs-media-carousel {
  width: 100%;
  background: transparent;
  padding: 15px;
  position: relative;
}

.swipe-carousel {
  width: 100%;
  height: 400px;
  overflow: hidden;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  touch-action: pan-y; /* allow vertical scroll, we handle horizontal */
}

.swipe-track {
  display: flex;
  height: 100%;
  will-change: transform;
}

.swipe-slide {
  min-width: 100%;
  height: 100%;
  flex-shrink: 0;
}

.carousel-image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #f5f5f5;
}

.carousel-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 12px;
}

.indicator-dot {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  background: #C9CDD4;
  transition: all 0.3s ease;
}

.indicator-dot.active {
  width: 18px;
  background: #FF7E67;
}

.carousel-counter {
  position: absolute;
  top: 26px;
  right: 26px;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 12px;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.xhs-media-placeholder {
  width: 100%;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  font-size: 60px;
  opacity: 0.8;
}

.xhs-detail-content {
  padding: 24px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px 24px 0 0;
  margin-top: -20px;
  position: relative;
  z-index: 10;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.03);
}

.article-title {
  font-size: 24px;
  font-weight: 800;
  color: #1D2129;
  margin-bottom: 16px;
  margin-top: 0;
  line-height: 1.4;
  letter-spacing: 0.5px;
}

.article-text {
  font-size: 16px;
  color: #4E5969;
  line-height: 1.8;
  margin-bottom: 24px;
  letter-spacing: 0.3px;
  white-space: pre-wrap; /* Ensure newlines and spaces are preserved */
  word-wrap: break-word;
}

.article-meta {
  font-size: 13px;
  color: #86909C;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  padding-bottom: 20px;
}

.xhs-detail-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 -4px 20px rgba(0,0,0,0.03);
  max-width: 1000px; /* match main-content width */
  margin: 0 auto;
}

.footer-input-box {
  flex: 1;
  margin-right: 20px;
}

:deep(.custom-input .arco-input-wrapper) {
  border-radius: 24px 0 0 24px;
  background: rgba(242, 243, 245, 0.8);
  border: 1px solid transparent;
  transition: all 0.3s;
}
:deep(.custom-input.arco-input-focus .arco-input-wrapper) {
  background: #FFF;
  border-color: #FF7E67;
}
:deep(.custom-input .arco-input-append) {
  border-radius: 0 24px 24px 0;
  padding: 0;
  background: transparent;
  border: none;
}

.footer-actions {
  display: flex;
  gap: 20px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #4E5969;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-btn:hover {
  transform: translateY(-2px);
  color: #FF7E67;
}

.action-btn:active {
  transform: scale(0.9);
}

.action-btn svg {
  font-size: 24px;
  color: #86909C;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-btn:hover svg,
.action-btn svg[style*="color: #FF7E67"],
.action-btn svg[style*="color: rgb(255, 126, 103)"] {
  color: #FF7E67 !important;
  transform: scale(1.15);
}

/* 自定义分享弹窗样式 */
:deep(.custom-share-modal) {
  border-radius: 24px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}

.share-modal-content {
  padding: 10px;
}

.share-modal-header {
  text-align: center;
  margin-bottom: 24px;
}

.success-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(7, 193, 96, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px auto;
  animation: popIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.success-icon {
  font-size: 32px;
  color: #07C160;
}

.share-modal-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #1D2129;
  font-weight: 800;
}

.share-modal-header p {
  margin: 0;
  font-size: 14px;
  color: #86909C;
}

.share-article-preview {
  display: flex;
  background: rgba(0, 0, 0, 0.03);
  padding: 12px;
  border-radius: 16px;
  margin-bottom: 30px;
}

.preview-img-box {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 12px;
}

.preview-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-icon {
  font-size: 24px;
}

.preview-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

.preview-title {
  font-size: 14px;
  color: #1D2129;
  font-weight: 600;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.preview-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #86909C;
}

.share-actions {
  display: flex;
  justify-content: center;
}

.wechat-btn {
  width: 100%;
  background: #07C160;
  color: white;
  font-weight: bold;
  height: 48px;
  font-size: 16px;
  transition: all 0.3s ease;
  border: none;
}
.wechat-btn:hover {
  background: #06AD56;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(7, 193, 96, 0.3);
}

@keyframes popIn {
  0% { transform: scale(0.5); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

@media (max-width: 768px) {
  .xhs-detail-container {
    border-radius: 16px;
  }
  .xhs-detail-content {
    padding: 16px;
  }
  .article-title {
    font-size: 18px;
  }
  .xhs-media-carousel {
    height: auto;
    padding: 10px;
  }
  .swipe-carousel {
    height: 280px !important;
  }
  .xhs-media-placeholder {
    height: 200px;
  }
  .xhs-detail-footer {
    padding: 12px 16px;
  }
  .footer-actions {
    gap: 12px;
  }
  .footer-input-box {
    margin-right: 12px;
  }
  
  :deep(.custom-share-modal) {
    position: absolute !important;
    bottom: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
    border-radius: 24px 24px 0 0;
    padding-bottom: env(safe-area-inset-bottom);
    animation: slideUpModal 0.4s cubic-bezier(0.25, 1, 0.5, 1);
  }
}

@keyframes slideUpModal {
  from { transform: translateY(100%); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>
