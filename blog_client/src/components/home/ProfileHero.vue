<template>
  <div class="hero-section">
    <div class="hero-overlay"></div>
    <div class="profile-content">
      <img :src="homeConfig.avatarUrl || '/img/avatar.png'" class="avatar" alt="Avatar" />
      <h1 class="author-name">{{ homeConfig.authorName || '小柴包' }}</h1>
      <p class="author-bio">{{ homeConfig.authorBio || '记录灵感，探索生活美学。在这里分享品牌的成长脉络，以及创作者的生活方式碎片。' }}</p>
      <div class="social-links">
        <a-tag size="small" color="orangered" v-for="tag in (homeConfig.tags || ['生活方式', '独立品牌', '创作手记'])" :key="tag">#{{ tag }}</a-tag>
        <a-popover
          v-if="homeConfig.wechatQrUrl"
          position="bottom"
          trigger="hover">
          <a-button size="small" shape="round" class="wechat-btn">
            <template #icon><icon-message /></template>
            微信
          </a-button>
          <template #content>
            <div style="text-align: center; width: 160px;">
              <div style="font-size: 13px; color: #5C433B; margin-bottom: 8px; font-weight: bold;">扫码添加微信</div>
              <img :src="homeConfig.wechatQrUrl" style="width: 100%; border-radius: 8px;">
            </div>
          </template>
        </a-popover>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

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
        const res = await axios.get('/api/home/config');
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
  position: relative;
  height: 340px;
  border-radius: 24px;
  background: linear-gradient(135deg, #FFE1D9, #FFC1B6, #FFD1C9);
  background-size: 200% 200%;
  animation: gradientFlow 8s ease infinite;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(255, 126, 103, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}
@keyframes gradientFlow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.hero-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image: radial-gradient(circle at center, rgba(255,255,255,0.4) 0%, transparent 70%);
}
.profile-content {
  position: relative;
  z-index: 1;
  color: #5C433B;
  animation: fadeIn 1s ease-out;
  padding: 0 20px;
}
.avatar {
  width: 105px;
  height: 105px;
  border-radius: 50%;
  border: 4px solid #FFFFFF;
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.25), 0 0 0 0 rgba(255, 255, 255, 0.7);
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  object-fit: cover;
  background-color: white;
  animation: pulseAvatar 2s infinite;
}
@keyframes pulseAvatar {
  0% { box-shadow: 0 8px 24px rgba(255, 126, 103, 0.25), 0 0 0 0 rgba(255, 255, 255, 0.7); }
  70% { box-shadow: 0 8px 24px rgba(255, 126, 103, 0.25), 0 0 0 15px rgba(255, 255, 255, 0); }
  100% { box-shadow: 0 8px 24px rgba(255, 126, 103, 0.25), 0 0 0 0 rgba(255, 255, 255, 0); }
}
.avatar:hover {
  transform: scale(1.08) rotate(5deg);
}
.author-name {
  margin: 20px 0 10px 0;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(255,255,255,0.5);
}
.author-bio {
  font-size: 15px;
  color: #8C6A5D;
  max-width: 600px;
  margin: 0 auto 20px auto;
  line-height: 1.6;
}
.social-links {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}
.social-links .arco-tag {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  color: #FF7E67;
  border-radius: 20px;
  padding: 0 16px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.wechat-btn {
  background: rgba(7, 193, 96, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: #FFF;
  border: 1px solid rgba(255, 255, 255, 0.3);
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.3);
  transition: all 0.3s ease;
}
.wechat-btn:hover {
  background: rgba(6, 173, 86, 0.95);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(7, 193, 96, 0.4);
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
@media (max-width: 768px) {
  .hero-section {
    height: auto;
    padding: 24px 15px;
    border-radius: 16px;
  }
  .avatar {
    width: 56px;
    height: 56px;
    border-width: 3px;
  }
  .author-name {
    font-size: 18px;
    margin: 10px 0 6px 0;
  }
  .author-bio {
    font-size: 12px;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .social-links {
    gap: 6px;
  }
  .social-links .arco-tag {
    padding: 0 10px;
    font-size: 11px;
  }
}
</style>
