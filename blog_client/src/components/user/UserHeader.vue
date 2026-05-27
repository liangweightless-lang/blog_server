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
          :type="isCheckedIn ? 'primary' : 'primary'" 
          :status="isCheckedIn ? 'success' : 'warning'"
          size="small" 
          shape="round" 
          :disabled="isCheckedIn"
          :loading="loading"
          @click="handleCheckin"
        >
          {{ isCheckedIn ? '今日已签到' : '每日签到' }}
        </a-button>
        <a-button shape="circle" size="small" @click="$emit('edit')" style="margin-left: 8px;">
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
import axios from 'axios';
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
          const res = await axios.post('/api/users/checkin', {}, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
          });
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
  background: linear-gradient(135deg, #FF7E67 0%, #FF9E8D 100%);
  padding: 40px 20px 60px;
  color: white;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 600px;
  margin: 0 auto;
}
.user-info-main {
  display: flex;
  align-items: center;
  gap: 15px;
}
.user-avatar-big {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.3);
  object-fit: cover;
}
.user-nickname {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
}
.user-account-id {
  margin: 4px 0 0;
  font-size: 12px;
  opacity: 0.8;
}

/* Admin VIP Banner Styles */
.admin-vip-banner {
  margin: 25px auto 0;
  max-width: 600px;
  background: linear-gradient(135deg, #3A3A3A 0%, #1A1A1A 100%);
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
  overflow: hidden;
}
.admin-vip-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,215,0,0.5), transparent);
}
.admin-vip-banner:active {
  transform: scale(0.98);
}
.banner-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.vip-icon-wrapper {
  width: 40px;
  height: 40px;
  background: rgba(255, 215, 0, 0.15);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.vip-icon {
  font-size: 20px;
  color: #FFD700;
}
.vip-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.vip-title {
  font-size: 15px;
  font-weight: bold;
  color: #FFF;
  background: linear-gradient(90deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.vip-subtitle {
  font-size: 12px;
  color: rgba(255,255,255,0.6);
}
</style>
