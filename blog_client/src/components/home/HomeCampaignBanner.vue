<template>
  <div class="campaign-spotlight-wrapper" v-if="activeCampaigns.length > 0">
    <div class="spotlight-header">
      <div class="header-left">
        <span class="live-dot-pulse"></span>
        <span class="spotlight-title">社区快团 · 火热拼单中</span>
      </div>
      <div class="header-right" @click="$router.push('/store')">
        <span class="more-text">查看更多</span>
        <icon-right class="arrow-icon" />
      </div>
    </div>

    <!-- 紧凑单卡片或横向滑块 -->
    <div class="campaign-cards-track">
      <div 
        v-for="cam in activeCampaigns" 
        :key="cam.id" 
        class="spotlight-card"
        @click="$router.push(`/campaign/${cam.id}`)"
      >
        <div class="card-cover-side">
          <img 
            v-if="cam.products && cam.products[0] && cam.products[0].product" 
            :src="$formatImageUrl(cam.products[0].product.image)" 
            class="cover-img" 
            alt="快团商品"
          />
          <div v-else class="empty-cover">
            <icon-fire style="font-size: 24px; color: #FF5A34;" />
          </div>
          <div class="hot-tag-badge">拼团中</div>
        </div>

        <div class="card-info-side">
          <h4 class="campaign-title">{{ cam.title }}</h4>
          
          <!-- 成团进度指示条 -->
          <div class="progress-bar-wrap">
            <div class="progress-track">
              <div 
                class="progress-fill" 
                :style="{ width: getProgressPercent(cam) + '%' }"
              ></div>
            </div>
            <span class="progress-label" v-if="cam.targetNum > 0">
              {{ cam.groupStatus === 1 ? '已成团' : `差 ${Math.max(1, cam.targetNum - (cam.currentNum || 0))} 人` }}
            </span>
          </div>

          <div class="price-action-row">
            <div class="price-box">
              <span class="currency">¥</span>
              <span class="main-price">{{ getMinPrice(cam) }}</span>
              <span class="price-tip">成团特惠</span>
            </div>
            <button class="join-action-btn">
              <span>去参团</span>
              <icon-arrow-right />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeCampaignBanner',
  props: {
    campaigns: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    activeCampaigns() {
      // 过滤只展示进行中的快团活动（status === 1）
      return (this.campaigns || []).filter(c => c.status === 1).slice(0, 3);
    }
  },
  methods: {
    getMinPrice(cam) {
      if (!cam.products || cam.products.length === 0) return '0.00';
      const prices = cam.products
        .map(p => p.groupPrice || p.product?.price || 0)
        .filter(p => Number(p) > 0);
      return prices.length > 0 ? Math.min(...prices) : '0.00';
    },
    getProgressPercent(cam) {
      if (!cam.targetNum || cam.targetNum <= 0) return 100;
      const current = cam.currentNum || 0;
      return Math.min(100, Math.round((current / cam.targetNum) * 100));
    }
  }
};
</script>

<style scoped>
.campaign-spotlight-wrapper {
  margin: 12px 16px 6px;
  background: linear-gradient(145deg, #FFF9F6 0%, #FFFFFF 100%);
  border-radius: 20px;
  padding: 14px;
  border: 1px solid rgba(255, 90, 52, 0.12);
  box-shadow: 0 6px 20px rgba(255, 90, 52, 0.05);
}

.spotlight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.live-dot-pulse {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #FF5A34;
  box-shadow: 0 0 0 0 rgba(255, 90, 52, 0.7);
  animation: pulse 1.6s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(255, 90, 52, 0.7);
  }
  70% {
    transform: scale(1);
    box-shadow: 0 0 0 6px rgba(255, 90, 52, 0);
  }
  100% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(255, 90, 52, 0);
  }
}

.spotlight-title {
  font-size: 13px;
  font-weight: 800;
  color: #1D2129;
  letter-spacing: -0.2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: #86909C;
  cursor: pointer;
}

.header-right:hover {
  color: #FF5A34;
}

.arrow-icon {
  font-size: 11px;
}

.campaign-cards-track {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.spotlight-card {
  display: flex;
  gap: 12px;
  background: #FFFFFF;
  border-radius: 14px;
  padding: 10px;
  border: 1px solid rgba(0, 0, 0, 0.03);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  cursor: pointer;
  transition: all 0.2s ease;
}

.spotlight-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 90, 52, 0.1);
}

.spotlight-card:active {
  transform: scale(0.985);
}

.card-cover-side {
  position: relative;
  width: 76px;
  height: 76px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  background: #F7F8FA;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.empty-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hot-tag-badge {
  position: absolute;
  top: 4px;
  left: 4px;
  font-size: 9px;
  font-weight: 700;
  color: #FFFFFF;
  background: rgba(255, 90, 52, 0.9);
  padding: 1px 5px;
  border-radius: 4px;
  backdrop-filter: blur(4px);
}

.card-info-side {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.campaign-title {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-bar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 4px 0;
}

.progress-track {
  flex: 1;
  height: 5px;
  background: #F2F3F5;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FFA940 0%, #FF5A34 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-label {
  font-size: 10.5px;
  color: #FF5A34;
  font-weight: 600;
  flex-shrink: 0;
}

.price-action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-box {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.currency {
  font-size: 11px;
  font-weight: 800;
  color: #FF5A34;
}

.main-price {
  font-size: 16px;
  font-weight: 800;
  color: #FF5A34;
}

.price-tip {
  font-size: 9.5px;
  color: #86909C;
  margin-left: 4px;
}

.join-action-btn {
  display: flex;
  align-items: center;
  gap: 3px;
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  color: #FFFFFF;
  border: none;
  border-radius: 14px;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(255, 94, 58, 0.25);
  transition: all 0.2s ease;
}

.join-action-btn:active {
  transform: scale(0.95);
}

@media (max-width: 768px) {
  .campaign-spotlight-wrapper {
    margin: 10px 12px 4px;
    padding: 12px;
    border-radius: 18px;
  }
}
</style>
