<template>
  <div class="hero-section">
    <div class="profile-card">
      <img :src="homeConfig.avatarUrl || '/img/avatar.png'" class="avatar" alt="Avatar" />
      <div class="profile-info">
        <h1 class="author-name">{{ homeConfig.authorName || '小柴包' }}</h1>
        <p class="author-bio">{{ homeConfig.authorBio || '记录灵感，探索生活美学。在这里分享品牌的成长脉络。' }}</p>
        <div class="social-tags">
          <span class="tag" v-for="tag in (homeConfig.tags || ['生活方式', '独立品牌']).slice(0, 3)" :key="tag">#{{ tag }}</span>
        </div>
      </div>
      <div class="actions" v-if="homeConfig.wechatQrUrl">
        <a-popover position="bottom" trigger="hover">
          <div class="wechat-icon"><icon-wechat /></div>
          <template #content>
            <div style="text-align: center; width: 140px;">
              <div style="font-size: 12px; color: #1D2129; margin-bottom: 6px; font-weight: bold;">扫码添加微信</div>
              <img :src="homeConfig.wechatQrUrl" style="width: 100%; border-radius: 6px;">
            </div>
          </template>
        </a-popover>
      </div>
    </div>
  </div>
</template>

<script>
import { getHomeConfig } from '@/api/common';

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
    }
  }
}
</script>

<style scoped>
.hero-section {
  padding: calc(10px + var(--safe-top)) 15px 15px;
  background: radial-gradient(circle at top left, rgba(255, 75, 43, 0.05) 0%, transparent 50%),
              radial-gradient(circle at top right, rgba(255, 65, 108, 0.05) 0%, transparent 50%);
}

.profile-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 20px;
  background: var(--glass-bg, rgba(255, 255, 255, 0.7));
  backdrop-filter: var(--glass-blur, blur(20px));
  -webkit-backdrop-filter: var(--glass-blur, blur(20px));
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04), 0 2px 8px rgba(0,0,0,0.02);
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid white;
  box-shadow: 0 4px 12px rgba(255, 75, 43, 0.15);
  margin-right: 14px;
}

.profile-info {
  flex: 1;
  min-width: 0; /* for text truncation */
}

.author-name {
  font-size: 17px;
  font-weight: 800;
  color: #1A1A1A;
  margin: 0 0 4px 0;
}

.author-bio {
  font-size: 12px;
  color: #86909C;
  margin: 0 0 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.social-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 10px;
  color: var(--brand-primary, #FF4B2B);
  background: rgba(255, 75, 43, 0.08);
  padding: 3px 8px;
  border-radius: 12px;
  font-weight: 600;
}

.actions {
  margin-left: 10px;
}

.wechat-icon {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: rgba(7, 193, 96, 0.1);
  color: #07C160;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
}

.wechat-icon:active {
  transform: scale(0.9);
}
</style>
