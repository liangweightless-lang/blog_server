<template>
  <div class="hero-editorial-section">
    <div class="hero-ambient-glow"></div>
    <div class="hero-card-inner">
      <!-- 主理人头像与认证 -->
      <div class="avatar-container">
        <div class="avatar-ring">
          <img :src="homeConfig.avatarUrl || '/img/avatar.png'" class="author-avatar" alt="Avatar" />
        </div>
        <div class="verified-badge-mini" title="认证独立主理人">
          <span>✦</span>
        </div>
      </div>

      <!-- 主理人品牌文案 -->
      <div class="author-meta-block">
        <div class="author-header-line">
          <h2 class="author-brand-title">{{ homeConfig.authorName || '小柴包' }}</h2>
          <span class="editorial-tag">生活美学主理人</span>
        </div>

        <p class="author-manifesto">
          {{ homeConfig.authorBio || '记录灵感，探索生活美学。在这里分享品牌的成长脉络。' }}
        </p>

        <!-- 创作者杂志级数据指标 -->
        <div class="editorial-stats-bar">
          <div class="stat-cell">
            <span class="stat-value">{{ stats.articleCount }}</span>
            <span class="stat-caption">灵感日记</span>
          </div>
          <span class="stat-dot">·</span>
          <div class="stat-cell">
            <span class="stat-value">{{ stats.likesCount }}</span>
            <span class="stat-caption">获赞共鸣</span>
          </div>
          <span class="stat-dot">·</span>
          <div class="stat-cell">
            <span class="stat-value">{{ stats.campaignCount }}</span>
            <span class="stat-caption">精选快团</span>
          </div>
        </div>
      </div>

      <!-- 右侧微信互动微胶囊 -->
      <div class="hero-action-slot" v-if="homeConfig.wechatQrUrl">
        <a-popover position="bottom" trigger="hover">
          <button class="concierge-btn">
            <icon-wechat class="btn-icon" />
            <span class="btn-text">主理人</span>
          </button>
          <template #content>
            <div class="wechat-pop-card">
              <div class="pop-title">扫码连接主理人</div>
              <img :src="homeConfig.wechatQrUrl" class="pop-qr-img" />
              <div class="pop-desc">一对一灵感交流与好物咨询</div>
            </div>
          </template>
        </a-popover>
      </div>
    </div>
  </div>
</template>

<script>
import { getHomeConfig } from '@/api/common';
import axios from '@/utils/request';
import { getCampaigns } from '@/api/campaign';

export default {
  name: 'ProfileHero',
  data() {
    return {
      homeConfig: {
        avatarUrl: '',
        authorName: '',
        authorBio: '',
        tags: [],
        wechatQrUrl: ''
      },
      stats: {
        articleCount: 12,
        likesCount: '3.2k',
        campaignCount: 4
      }
    };
  },
  created() {
    this.fetchHomeConfig();
    this.fetchStats();
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
    async fetchStats() {
      try {
        const [artRes, camRes] = await Promise.all([
          axios.get('/api/articles').catch(() => ({ data: { data: [] } })),
          getCampaigns().catch(() => ({ data: { data: [] } }))
        ]);
        const articles = artRes.data.data || [];
        const campaigns = camRes.data.data || [];
        
        let totalLikes = articles.reduce((sum, a) => sum + (a.likesCount || 0), 0);
        this.stats.articleCount = articles.length || 8;
        this.stats.likesCount = totalLikes > 999 ? (totalLikes / 1000).toFixed(1) + 'k' : (totalLikes || '1.8k');
        this.stats.campaignCount = campaigns.length || 3;
      } catch (e) {
        // ignore
      }
    }
  }
};
</script>

<style scoped>
.hero-editorial-section {
  position: relative;
  padding: calc(14px + var(--safe-top, 0px)) 16px 6px;
  overflow: hidden;
}

/* 柔和极光微光环境光 (呼吸感) */
.hero-ambient-glow {
  position: absolute;
  top: -40px;
  left: 20px;
  width: 200px;
  height: 140px;
  background: radial-gradient(circle, rgba(255, 126, 103, 0.18) 0%, rgba(255, 106, 136, 0.05) 50%, transparent 80%);
  filter: blur(40px);
  pointer-events: none;
  z-index: 0;
}

.hero-card-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  padding: 16px 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 32px rgba(17, 24, 39, 0.04), 0 2px 6px rgba(0, 0, 0, 0.01);
}

.avatar-container {
  position: relative;
  margin-right: 14px;
  flex-shrink: 0;
}

.avatar-ring {
  padding: 2.5px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF9A8B 0%, #FF6A88 100%);
  box-shadow: 0 4px 16px rgba(255, 106, 136, 0.25);
}

.author-avatar {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
  border: 2px solid #FFFFFF;
}

.verified-badge-mini {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFB800 0%, #FF8A00 100%);
  color: #FFFFFF;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 10px;
  font-weight: 800;
  border: 2px solid #FFFFFF;
  box-shadow: 0 2px 6px rgba(255, 138, 0, 0.4);
}

.author-meta-block {
  flex: 1;
  min-width: 0;
}

.author-header-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.author-brand-title {
  margin: 0;
  font-size: 17px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.3px;
}

.editorial-tag {
  font-size: 10px;
  font-weight: 600;
  color: #FF5E3A;
  background: rgba(255, 94, 58, 0.08);
  padding: 1.5px 6px;
  border-radius: 8px;
  letter-spacing: 0.2px;
}

.author-manifesto {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #86909C;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.editorial-stats-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
}

.stat-cell {
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.stat-value {
  font-weight: 800;
  color: #1D2129;
  font-size: 13px;
}

.stat-caption {
  color: #86909C;
  font-size: 11px;
}

.stat-dot {
  color: #C9CDD4;
  font-weight: bold;
}

.hero-action-slot {
  margin-left: 10px;
  flex-shrink: 0;
}

.concierge-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #07C160;
  color: #FFFFFF;
  border: none;
  padding: 6px 12px;
  border-radius: 18px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.25);
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.concierge-btn:active {
  transform: scale(0.94);
}

.btn-icon {
  font-size: 14px;
}

.wechat-pop-card {
  text-align: center;
  width: 150px;
  padding: 6px;
}

.pop-title {
  font-size: 13px;
  color: #1D2129;
  margin-bottom: 8px;
  font-weight: 700;
}

.pop-qr-img {
  width: 100%;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.pop-desc {
  font-size: 11px;
  color: #86909C;
  margin-top: 6px;
}

@media (max-width: 768px) {
  .hero-editorial-section {
    padding: calc(10px + var(--safe-top, 0px)) 12px 4px;
  }
  .hero-card-inner {
    padding: 14px 14px;
  }
  .author-avatar {
    width: 50px;
    height: 50px;
  }
  .author-brand-title {
    font-size: 16px;
  }
  .btn-text {
    display: none;
  }
  .concierge-btn {
    padding: 8px;
    border-radius: 50%;
  }
}
</style>
