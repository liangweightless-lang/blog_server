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
        <el-button 
          :type="isCheckedIn ? 'success' : 'warning'" 
          size="small" 
          round 
          :disabled="isCheckedIn"
          :loading="loading"
          @click="handleCheckin"
        >
          {{ isCheckedIn ? '今日已签到' : '每日签到' }}
        </el-button>
        <el-button icon="el-icon-edit-outline" circle size="small" @click="$emit('edit')"></el-button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'UserHeader',
  props: {
    user: Object
  },
  data() {
    return {
      loading: false
    }
  },
  computed: {
    isCheckedIn() {
      if (!this.user || !this.user.lastCheckinDate) return false;
      const today = new Date().toISOString().split('T')[0];
      // Handling both array [2026, 5, 11] and string formats
      let dateStr = this.user.lastCheckinDate;
      if (Array.isArray(dateStr)) {
        dateStr = `${dateStr[0]}-${String(dateStr[1]).padStart(2, '0')}-${String(dateStr[2]).padStart(2, '0')}`;
      }
      return dateStr === today;
    }
  },
  methods: {
    async handleCheckin() {
      if (this.isCheckedIn) return;
      this.loading = true;
      try {
        const res = await axios.post('/api/users/checkin', {}, {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        this.$message.success(res.data.message);
        // Refresh globally to update points and lastCheckinDate
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        this.$message.error(error.response?.data?.error || '签到失败');
      } finally {
        this.loading = false;
      }
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
</style>
