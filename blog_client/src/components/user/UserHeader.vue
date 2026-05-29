<template>
  <div class="user-profile-header">
    <div class="header-content" v-if="user">
      <div class="user-info-main">
        <div class="avatar-wrapper">
          <img :src="user.avatarUrl || '/img/avatar.png'" class="user-avatar-big" />
        </div>
        <div class="user-text-info">
          <h2 class="user-nickname">{{ user.nickname || '未设置昵称' }}</h2>
          <p class="user-account-id">账号: {{ user.username }}</p>
        </div>
      </div>
      <div class="header-actions">
        <a-button 
          :type="isCheckedIn ? 'primary' : 'outline'" 
          :status="isCheckedIn ? 'success' : 'normal'"
          class="checkin-btn"
          size="small" 
          shape="round" 
          :disabled="isCheckedIn"
          :loading="loading"
          @click="handleCheckin"
        >
          {{ isCheckedIn ? '今日已签到' : '每日签到' }}
        </a-button>
        <a-button shape="circle" size="small" class="edit-btn" @click="$emit('edit')" style="margin-left: 8px;">
          <icon-edit />
        </a-button>
      </div>
    </div>
    
    <!-- Admin VIP Banner -->
    <div v-if="user && user.role === 'ADMIN'" class="admin-vip-banner" @click="$router.push('/admin')">
      <div class="banner-left">
        <div class="vip-icon-wrapper">
          <icon-user class="vip-icon" />
        </div>
        <div class="vip-text">
          <span class="vip-title">👑 管理后台</span>
          <span class="vip-subtitle">您拥有最高管理权限，点击进入</span>
        </div>
      </div>
      <icon-right />
    </div>
  </div>
</template>

<script>
import { userCheckIn } from '@/api/user';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'UserHeader',
  props: {
    user: Object
  },
  data() {
    return {
      loading: false,
      checkinTimer: null
    }
  },
  computed: {
    isCheckedIn() {
      if (!this.user || !this.user.lastCheckinDate) return false;
      const today = new Date().toISOString().split('T')[0];
      let dateStr = this.user.lastCheckinDate;
      if (Array.isArray(dateStr)) {
        dateStr = `${dateStr[0]}-${String(dateStr[1]).padStart(2, '0')}-${String(dateStr[2]).padStart(2, '0')}`;
      }
      return dateStr === today;
    }
  },
  methods: {
    handleCheckin() {
      if (this.isCheckedIn) return;
      if (this.checkinTimer) clearTimeout(this.checkinTimer);
      
      this.checkinTimer = setTimeout(async () => {
        if (this.loading) return;
        this.loading = true;
        try {
          const res = await userCheckIn();
          Message.success(res.data.message);
          window.dispatchEvent(new CustomEvent('refresh-user'));
        } catch (error) {
          Message.error(error.response?.data?.message || '签到失败');
        } finally {
          this.loading = false;
        }
      }, 300);
    }
  }
}
</script>

<style scoped>
.user-profile-header {
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%));
  padding: calc(50px + env(safe-area-inset-top, 0px)) 20px 70px;
  color: white;
  position: relative;
  overflow: hidden;
}



.user-profile-header::after {
  content: '';
  position: absolute;
  bottom: -50px;
  left: 0;
  width: 100%;
  height: 100px;
  background: white;
  border-radius: 50%;
  transform: scaleX(1.5);
  opacity: 0.1;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 600px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}
.checkin-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: white;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
.checkin-btn:not([disabled]):hover {
  background: rgba(255, 255, 255, 0.3);
}
.edit-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.4);
}
.user-info-main {
  display: flex;
  align-items: center;
  gap: 15px;
}
.avatar-wrapper {
  position: relative;
}
.avatar-wrapper::before {
  content: '';
  position: absolute;
  top: -4px; left: -4px; right: -4px; bottom: -4px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  z-index: 0;
  animation: pulseAvatar 2s infinite;
}
@keyframes pulseAvatar {
  0% { transform: scale(0.95); opacity: 1; }
  100% { transform: scale(1.15); opacity: 0; }
}

.user-avatar-big {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  border: 3px solid #FFF;
  object-fit: cover;
  position: relative;
  z-index: 1;
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.4);
}
.user-nickname {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.user-account-id {
  margin: 4px 0 0;
  font-size: 13px;
  opacity: 0.9;
  background: rgba(255,255,255,0.2);
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-block;
  backdrop-filter: blur(4px);
}

/* Admin VIP Banner Styles */
.admin-vip-banner {
  margin: 25px auto 0;
  max-width: 600px;
  background: var(--glass-bg, rgba(255, 255, 255, 0.85));
  backdrop-filter: var(--glass-blur, blur(30px));
  -webkit-backdrop-filter: var(--glass-blur, blur(30px));
  border: 1px solid rgba(255, 215, 0, 0.6);
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 32px rgba(250, 140, 22, 0.1);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
  z-index: 2;
}
.admin-vip-banner::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; height: 100%;
  background: linear-gradient(135deg, rgba(255,215,0,0.05) 0%, transparent 100%);
  pointer-events: none;
}
.admin-vip-banner:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(250, 140, 22, 0.15);
  border-color: rgba(255, 215, 0, 0.8);
}
.admin-vip-banner:active {
  transform: translateY(1px);
}
.banner-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.vip-icon-wrapper {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #FFF7D6, #FFF0B3);
  border: 1px solid rgba(250, 140, 22, 0.2);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.vip-icon {
  font-size: 20px;
  color: #FA8C16;
}
.vip-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.vip-title {
  font-size: 15px;
  font-weight: bold;
  background: linear-gradient(90deg, #FA8C16, #FAAD14);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.vip-subtitle {
  font-size: 12px;
  color: #86909C;
}
.admin-vip-banner .arco-icon-right {
  color: #86909C;
}
</style>
