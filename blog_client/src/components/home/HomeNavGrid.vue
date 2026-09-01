<template>
  <div class="editorial-nav-container">
    <div class="nav-tile" @click="$router.push('/store')">
      <div class="tile-icon-box box-bread">
        <span class="tile-emoji">🥖</span>
      </div>
      <div class="tile-text-wrap">
        <span class="tile-main-text">灵感手作</span>
        <span class="tile-sub-text">现烤甄选</span>
      </div>
    </div>

    <div class="nav-tile" @click="handleCampaignClick">
      <div class="tile-icon-box box-fire">
        <span class="tile-emoji">🔥</span>
        <span class="tile-corner-pill">HOT</span>
      </div>
      <div class="tile-text-wrap">
        <span class="tile-main-text">社区快团</span>
        <span class="tile-sub-text">超值拼购</span>
      </div>
    </div>

    <div class="nav-tile" @click="handleCreatorClick">
      <div class="tile-icon-box box-star">
        <span class="tile-emoji">🌟</span>
      </div>
      <div class="tile-text-wrap">
        <span class="tile-main-text">主理空间</span>
        <span class="tile-sub-text">入驻开店</span>
      </div>
    </div>

    <div class="nav-tile" @click="$router.push('/profile')">
      <div class="tile-icon-box box-gift">
        <span class="tile-emoji">🎁</span>
      </div>
      <div class="tile-text-wrap">
        <span class="tile-main-text">每日签到</span>
        <span class="tile-sub-text">领50积分</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeNavGrid',
  methods: {
    handleCampaignClick() {
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
.editorial-nav-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 4px 16px 0;
}

.nav-tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 4px 10px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 20px rgba(17, 24, 39, 0.03);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;
}

.nav-tile:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.12);
  border-color: rgba(255, 126, 103, 0.2);
}

.nav-tile:active {
  transform: scale(0.94);
}

.tile-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 8px;
  position: relative;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.04);
}

.tile-emoji {
  font-size: 22px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.box-bread {
  background: linear-gradient(135deg, #FFF6EC 0%, #FFEAD4 100%);
  border: 1px solid rgba(255, 200, 150, 0.3);
}

.box-fire {
  background: linear-gradient(135deg, #FFF0F0 0%, #FFDEDE 100%);
  border: 1px solid rgba(255, 150, 150, 0.3);
}

.box-star {
  background: linear-gradient(135deg, #FFFDF0 0%, #FFF4C2 100%);
  border: 1px solid rgba(255, 220, 100, 0.3);
}

.box-gift {
  background: linear-gradient(135deg, #F0F6FF 0%, #DEEDFF 100%);
  border: 1px solid rgba(150, 200, 255, 0.3);
}

.tile-corner-pill {
  position: absolute;
  top: -5px;
  right: -7px;
  background: linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%);
  color: #FFFFFF;
  font-size: 9px;
  font-weight: 800;
  padding: 1px 4px;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(255, 65, 108, 0.4);
  letter-spacing: 0.2px;
}

.tile-text-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tile-main-text {
  font-size: 13px;
  font-weight: 700;
  color: #1A1D20;
  line-height: 1.2;
  letter-spacing: -0.2px;
}

.tile-sub-text {
  font-size: 10px;
  color: #86909C;
  margin-top: 2px;
}

@media (max-width: 768px) {
  .editorial-nav-container {
    padding: 2px 12px 0;
    gap: 8px;
  }
  .nav-tile {
    padding: 10px 2px 8px;
    border-radius: 16px;
  }
  .tile-icon-box {
    width: 38px;
    height: 38px;
    border-radius: 12px;
    margin-bottom: 6px;
  }
  .tile-emoji {
    font-size: 19px;
  }
  .tile-main-text {
    font-size: 12px;
  }
  .tile-sub-text {
    font-size: 9px;
  }
}
</style>
