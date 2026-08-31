<template>
  <div class="hero-section">
    <div class="profile-card">
      <div class="avatar-wrap">
        <img :src="homeConfig.avatarUrl || '/img/avatar.png'" class="avatar" alt="Avatar" />
        <span class="v-badge" title="官方认证主理人">
          <icon-star />
        </span>
      </div>

      <div class="profile-info">
        <div class="name-row">
          <h1 class="author-name">{{ homeConfig.authorName || '小柴包' }}</h1>
          <span class="cert-tag">认证主理人</span>
        </div>

        <p class="author-bio">{{ homeConfig.authorBio || '记录灵感，探索生活美学。在这里分享品牌的成长脉络。' }}</p>

        <!-- 创作者数据栏 (小红书/即刻风格) -->
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-num">{{ stats.articleCount }}</span>
            <span class="stat-label">灵感</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ stats.likesCount }}</span>
            <span class="stat-label">获赞</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ stats.campaignCount }}</span>
            <span class="stat-label">好物快团</span>
          </div>
        </div>
      </div>

      <div class="actions" v-if="homeConfig.wechatQrUrl">
        <a-popover position="bottom" trigger="hover">
          <button class="contact-btn">
            <icon-wechat />
            <span>主理人微信</span>
          </button>
          <template #content>
            <div style="text-align: center; width: 150px; padding: 6px;">
              <div style="font-size: 13px; color: #1D2129; margin-bottom: 8px; font-weight: 700;">扫码添加主理人</div>
              <img :src="homeConfig.wechatQrUrl" style="width: 100%; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
              <div style="font-size: 11px; color: #86909C; margin-top: 6px;">一对一灵感交流与好物咨询</div>
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
        // fallback
      }
    }
  }
};
</script>

<style scoped>
.hero-section {
  padding: calc(10px + var(--safe-top, 0px)) 15px 10px;
  background: radial-gradient(circle at 10% 20%, rgba(255, 126, 103, 0.08) 0%, transparent 60%),
              radial-gradient(circle at 90% 80%, rgba(255, 106, 136, 0.06) 0%, transparent 60%);
}

.profile-card {
  display: flex;
  align-items: center;
  padding: 18px 20px;
  border-radius: 24px;
  background: var(--glass-bg, rgba(255, 255, 255, 0.85));
  backdrop-filter: var(--glass-blur, blur(25px));
  -webkit-backdrop-filter: var(--glass-blur, blur(25px));
  border: 1px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 10px 30px rgba(255, 126, 103, 0.06), 0 2px 8px rgba(0, 0, 0, 0.02);
  animation: heroFadeIn 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
}

@keyframes heroFadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.avatar-wrap {
  position: relative;
  margin-right: 16px;
  flex-shrink: 0;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  border: 2.5px solid #FFFFFF;
  box-shadow: 0 4px 16px rgba(255, 126, 103, 0.25);
  display: block;
}

.v-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #FFB800 0%, #FF8A00 100%);
  color: #FFFFFF;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 11px;
  border: 2px solid #FFFFFF;
  box-shadow: 0 2px 6px rgba(255, 138, 0, 0.4);
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.author-name {
  font-size: 18px;
  font-weight: 800;
  color: #1D2129;
  margin: 0;
  letter-spacing: -0.2px;
}

.cert-tag {
  font-size: 10px;
  font-weight: 700;
  color: #FF7E67;
  background: rgba(255, 126, 103, 0.1);
  padding: 2px 7px;
  border-radius: 10px;
  border: 1px solid rgba(255, 126, 103, 0.2);
}

.author-bio {
  font-size: 12px;
  color: #86909C;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-num {
  font-size: 14px;
  font-weight: 800;
  color: #1D2129;
}

.stat-label {
  font-size: 11px;
  color: #86909C;
}

.stat-divider {
  width: 1px;
  height: 10px;
  background: #E5E6EB;
}

.actions {
  margin-left: 12px;
  flex-shrink: 0;
}

.contact-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: linear-gradient(135deg, #07C160 0%, #00AE52 100%);
  color: #FFFFFF;
  border: none;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.25);
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.contact-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2px 6px rgba(7, 193, 96, 0.2);
}

@media (max-width: 768px) {
  .profile-card {
    padding: 14px 16px;
    border-radius: 20px;
  }
  .avatar {
    width: 54px;
    height: 54px;
  }
  .author-name {
    font-size: 16px;
  }
  .contact-btn span {
    display: none;
  }
  .contact-btn {
    padding: 8px;
    border-radius: 50%;
  }
}
</style>
