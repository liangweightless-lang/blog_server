<template>
  <div class="vector-nav-grid">
    <!-- 1. 今日现烤 (高频爆款) -->
    <div class="nav-card" @click="$router.push('/store')">
      <div class="icon-bubble bubble-store">
        <icon-bulb class="vector-icon" />
      </div>
      <div class="text-group">
        <span class="main-label">今日现烤</span>
        <span class="sub-label">鲜焙出炉</span>
      </div>
    </div>

    <!-- 2. 社区快团 (多人成团/省钱) -->
    <div class="nav-card" @click="handleCampaignClick">
      <div class="icon-bubble bubble-campaign">
        <icon-fire class="vector-icon" />
        <span class="hot-badge-pill">HOT</span>
      </div>
      <div class="text-group">
        <span class="main-label">社区快团</span>
        <span class="sub-label">拼团立省</span>
      </div>
    </div>

    <!-- 3. 积分专区 (当钱花/免单) -->
    <div class="nav-card" @click="$router.push('/store')">
      <div class="icon-bubble bubble-points">
        <icon-gift class="vector-icon" />
      </div>
      <div class="text-group">
        <span class="main-label">积分专区</span>
        <span class="sub-label">抵扣免单</span>
      </div>
    </div>

    <!-- 4. 每日签到 (无需跳走，首页一键签到) -->
    <div class="nav-card" @click="handleDailyCheckin">
      <div class="icon-bubble bubble-rewards">
        <icon-trophy class="vector-icon" />
      </div>
      <div class="text-group">
        <span class="main-label">每日签到</span>
        <span class="sub-label">领积分抵扣</span>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '@/utils/request';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'HomeNavGrid',
  methods: {
    handleCampaignClick() {
      const campaignEl = document.querySelector('.campaign-spotlight-card') || document.querySelector('.campaign-banner-wrapper');
      if (campaignEl) {
        campaignEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else {
        this.$router.push('/store');
      }
    },
    async handleDailyCheckin() {
      const token = localStorage.getItem('token');
      if (!token) {
        window.dispatchEvent(new CustomEvent('open-login'));
        return;
      }
      try {
        const res = await axios.post('/api/users/checkin');
        Message.success(res.data?.message || '🎉 签到成功！积分已到账');
      } catch (err) {
        Message.info(err.response?.data?.message || '今天已经签过到啦，明天再来哦');
      }
    }
  }
};
</script>


<style scoped>
.vector-nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 4px 16px 0;
}

.nav-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 14px 4px 12px;
  background: #FFFFFF;
  border-radius: 18px;
  border: 1px solid rgba(0, 0, 0, 0.03);
  box-shadow: 0 4px 20px rgba(17, 24, 39, 0.03), 0 1px 3px rgba(0, 0, 0, 0.01);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  user-select: none;
}

.nav-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(255, 126, 103, 0.12);
  border-color: rgba(255, 126, 103, 0.2);
}

.nav-card:active {
  transform: scale(0.95);
}

/* 高级双色气泡容器 */
.icon-bubble {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 8px;
  position: relative;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.nav-card:hover .icon-bubble {
  transform: scale(1.08);
}

.vector-icon {
  font-size: 22px;
}

/* 灵感手作 - 琥珀落日橙 */
.bubble-store {
  background: linear-gradient(135deg, #FFF4EB 0%, #FFE6D4 100%);
  color: #FF7A45;
}

/* 社区快团 - 活力珊瑚红 */
.bubble-campaign {
  background: linear-gradient(135deg, #FFF0F0 0%, #FFD6D6 100%);
  color: #FF4D4F;
}

/* 积分专区 - 香槟金 */
.bubble-points {
  background: linear-gradient(135deg, #FFFDF0 0%, #FFF3C4 100%);
  color: #FAAD14;
}


/* 每日签到 - 极光青蓝 */
.bubble-rewards {
  background: linear-gradient(135deg, #F0F7FF 0%, #D8ECFF 100%);
  color: #1890FF;
}

.hot-badge-pill {
  position: absolute;
  top: -5px;
  right: -7px;
  background: linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%);
  color: #FFFFFF;
  font-size: 9px;
  font-weight: 800;
  padding: 1px 5px;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(255, 65, 108, 0.35);
  letter-spacing: 0.2px;
}

.text-group {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.main-label {
  font-size: 13px;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.2;
  letter-spacing: -0.2px;
}

.sub-label {
  font-size: 10px;
  color: #86909C;
  margin-top: 3px;
}

@media (max-width: 768px) {
  .vector-nav-grid {
    padding: 2px 12px 0;
    gap: 8px;
  }
  .nav-card {
    padding: 11px 2px 9px;
    border-radius: 16px;
  }
  .icon-bubble {
    width: 38px;
    height: 38px;
    border-radius: 12px;
    margin-bottom: 6px;
  }
  .vector-icon {
    font-size: 19px;
  }
  .main-label {
    font-size: 12px;
  }
  .sub-label {
    font-size: 9px;
  }
}
</style>
