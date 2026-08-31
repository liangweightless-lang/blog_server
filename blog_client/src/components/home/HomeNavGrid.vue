<template>
  <div class="home-nav-grid">
    <div class="nav-item" @click="$router.push('/store')">
      <div class="icon-wrap store-icon">
        <span class="emoji">🥖</span>
      </div>
      <span class="nav-title">灵感手作</span>
      <span class="nav-sub">精选现做</span>
    </div>

    <div class="nav-item" @click="handleCampaignClick">
      <div class="icon-wrap campaign-icon">
        <span class="emoji">🔥</span>
        <span class="hot-badge">HOT</span>
      </div>
      <span class="nav-title">社区快团</span>
      <span class="nav-sub">超值成团</span>
    </div>

    <div class="nav-item" @click="handleCreatorClick">
      <div class="icon-wrap creator-icon">
        <span class="emoji">🌟</span>
      </div>
      <span class="nav-title">主理人入驻</span>
      <span class="nav-sub">开通工作台</span>
    </div>

    <div class="nav-item" @click="$router.push('/profile')">
      <div class="icon-wrap gift-icon">
        <span class="emoji">🎁</span>
      </div>
      <span class="nav-title">每日签到</span>
      <span class="nav-sub">领50积分</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeNavGrid',
  methods: {
    handleCampaignClick() {
      // 平滑滚动至快团推荐卡片或跳转
      const campaignEl = document.querySelector('.campaign-ad-card');
      if (campaignEl) {
        campaignEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else {
        this.$router.push('/store');
      }
    },
    handleCreatorClick() {
      const token = localStorage.getItem('token');
      if (!token) {
        window.dispatchEvent(new CustomEvent('open-login'));
        return;
      }
      this.$router.push('/profile');
    }
  }
};
</script>

<style scoped>
.home-nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 0 15px;
  margin-top: -8px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 6px;
  background: var(--glass-bg, rgba(255, 255, 255, 0.75));
  backdrop-filter: var(--glass-blur, blur(20px));
  -webkit-backdrop-filter: var(--glass-blur, blur(20px));
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;
}

.nav-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.12);
  border-color: rgba(255, 126, 103, 0.3);
}

.nav-item:active {
  transform: scale(0.94);
}

.icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 6px;
  position: relative;
}

.icon-wrap .emoji {
  font-size: 22px;
}

.store-icon {
  background: linear-gradient(135deg, #FFF1E6 0%, #FFE4D6 100%);
}

.campaign-icon {
  background: linear-gradient(135deg, #FFE8E8 0%, #FFD6D6 100%);
}

.creator-icon {
  background: linear-gradient(135deg, #FFF9E6 0%, #FFF0B3 100%);
}

.gift-icon {
  background: linear-gradient(135deg, #F0F5FF 0%, #E8F3FF 100%);
}

.hot-badge {
  position: absolute;
  top: -4px;
  right: -6px;
  background: linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%);
  color: #FFFFFF;
  font-size: 9px;
  font-weight: 800;
  padding: 1px 4px;
  border-radius: 6px;
  transform: scale(0.85);
  box-shadow: 0 2px 6px rgba(255, 65, 108, 0.4);
}

.nav-title {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.2;
}

.nav-sub {
  font-size: 10px;
  color: #86909C;
  margin-top: 2px;
}

@media (max-width: 768px) {
  .nav-item {
    padding: 10px 4px;
    border-radius: 16px;
  }
  .icon-wrap {
    width: 40px;
    height: 40px;
    border-radius: 12px;
  }
  .icon-wrap .emoji {
    font-size: 20px;
  }
  .nav-title {
    font-size: 12px;
  }
  .nav-sub {
    font-size: 9px;
  }
}
</style>
