<template>
  <div class="user-profile-header">
    <div class="profile-ambient-glow"></div>
    <div class="header-content" v-if="user">
      <div class="user-info-main">
        <div class="avatar-ring-box">
          <img :src="user.avatarUrl || '/img/avatar.png'" class="user-avatar-big" />
          <span class="user-role-badge" v-if="user.role === 'ADMIN' || user.role === 'CREATOR'">✦</span>
        </div>
        <div class="user-text-info">
          <div class="name-row">
            <h2 class="user-nickname">{{ user.nickname || '未设置昵称' }}</h2>
            <span class="user-identity-tag" v-if="user.role === 'ADMIN'">超级管理员</span>
            <span class="user-identity-tag creator" v-else-if="user.role === 'CREATOR'">认证主理人</span>
          </div>
          <div class="user-account-capsule">
            <span>账号: {{ user.username }}</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <button 
          class="daily-checkin-pill"
          :class="{ 'is-done': isCheckedIn }"
          :disabled="isCheckedIn || loading"
          @click="handleCheckin"
        >
          <icon-check-circle-fill v-if="isCheckedIn" />
          <icon-trophy v-else />
          <span>{{ isCheckedIn ? '已签到' : '每日签到' }}</span>
        </button>
        <button class="edit-profile-circle" @click="$emit('edit')" aria-label="编辑资料">
          <icon-edit />
        </button>
      </div>
    </div>
    
    <!-- Admin / Creator VIP Banner (黑金奢感卡片) -->
    <div v-if="user && (user.role === 'ADMIN' || user.role === 'CREATOR')" class="admin-vip-card" @click="$router.push('/admin')">
      <div class="banner-left">
        <div class="vip-icon-box">
          <icon-star class="vip-icon" />
        </div>
        <div class="vip-text">
          <span class="vip-title">{{ user.role === 'ADMIN' ? '超级管理后台' : '小柴包主理人工作台' }}</span>
          <span class="vip-subtitle">{{ user.role === 'ADMIN' ? '全站权限管理、数据看板与配置中心' : '管理专属商品、快团活动与订单' }}</span>
        </div>
      </div>
      <icon-right class="vip-arrow" />
    </div>

    <!-- 普通用户入驻申请/审核中 Banner -->
    <div 
      v-else-if="user" 
      class="creator-apply-card" 
      :class="{ 'is-pending': creatorStatus && creatorStatus.application && creatorStatus.application.status === 0 }"
      @click="$emit('apply-creator')"
    >
      <div class="banner-left">
        <div class="apply-icon-box">
          <icon-clock-circle v-if="creatorStatus && creatorStatus.application && creatorStatus.application.status === 0" />
          <icon-exclamation-circle v-else-if="creatorStatus && creatorStatus.application && creatorStatus.application.status === 2" />
          <icon-star v-else />
        </div>
        <div class="vip-text">
          <template v-if="creatorStatus && creatorStatus.application && creatorStatus.application.status === 0">
            <span class="vip-title">主理人入驻审核中</span>
            <span class="vip-subtitle">您的申请已提交，平台管理员将尽快处理</span>
          </template>
          <template v-else-if="creatorStatus && creatorStatus.application && creatorStatus.application.status === 2">
            <span class="vip-title">主理人申请未通过</span>
            <span class="vip-subtitle">{{ creatorStatus.application.rejectReason || '资料不完整' }} (点击重新提交)</span>
          </template>
          <template v-else>
            <span class="vip-title">申请成为小柴包主理人</span>
            <span class="vip-subtitle">入驻开启专属空间，自主策划商品与快团</span>
          </template>
        </div>
      </div>
      <icon-right class="vip-arrow" />
    </div>
  </div>
</template>

<script>
import { userCheckIn } from '@/api/user';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'UserHeader',
  props: {
    user: Object,
    creatorStatus: Object
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
  position: relative;
  background: #FFFFFF;
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 4px 24px rgba(17, 24, 39, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.02);
  overflow: hidden;
}

/* 柔和极光微光环境背景 */
.profile-ambient-glow {
  position: absolute;
  top: -30px;
  right: -20px;
  width: 180px;
  height: 120px;
  background: radial-gradient(circle, rgba(255, 94, 58, 0.12) 0%, rgba(255, 126, 103, 0.03) 60%, transparent 80%);
  filter: blur(30px);
  pointer-events: none;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.user-info-main {
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar-ring-box {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  padding: 2.5px;
  background: linear-gradient(135deg, #FF9A8B 0%, #FF6A88 100%);
  box-shadow: 0 4px 14px rgba(255, 106, 136, 0.22);
}

.user-avatar-big {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #FFFFFF;
  display: block;
}

.user-role-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFB800 0%, #FF8A00 100%);
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 800;
  border: 2px solid #FFFFFF;
}

.user-text-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-nickname {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.3px;
}

.user-identity-tag {
  font-size: 10px;
  font-weight: 700;
  color: #FF5E3A;
  background: rgba(255, 94, 58, 0.08);
  padding: 2px 6px;
  border-radius: 6px;
}
.user-identity-tag.creator {
  color: #FA8C16;
  background: rgba(250, 140, 22, 0.08);
}

.user-account-capsule {
  font-size: 11px;
  color: #86909C;
  font-family: monospace;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.daily-checkin-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  color: #FFFFFF;
  border: none;
  font-size: 12px;
  font-weight: 700;
  padding: 7px 14px;
  border-radius: 18px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255, 42, 84, 0.25);
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.daily-checkin-pill.is-done {
  background: #F2F3F5;
  color: #00B42A;
  box-shadow: none;
  cursor: default;
}
.daily-checkin-pill:active:not(:disabled) {
  transform: scale(0.94);
}

.edit-profile-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #F2F3F5;
  color: #4E5969;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.edit-profile-circle:active {
  background: #E5E6EB;
  transform: scale(0.92);
}

/* 黑金超管/主理人工作台卡片 */
.admin-vip-card {
  margin-top: 18px;
  background: linear-gradient(135deg, #1A1D20 0%, #2A2F35 100%);
  border-radius: 16px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #FFFFFF;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  transition: all 0.2s ease;
}
.admin-vip-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
}
.admin-vip-card:active {
  transform: scale(0.98);
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.vip-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1A1D20;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

.vip-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.vip-title {
  font-size: 14px;
  font-weight: 700;
  color: #FFFFFF;
}

.vip-subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
}

.vip-arrow {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

/* 普通入驻卡片 */
.creator-apply-card {
  margin-top: 18px;
  background: linear-gradient(135deg, #FFF9F2 0%, #FFF3E6 100%);
  border: 1px solid #FFE4CC;
  border-radius: 16px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.2s ease;
}
.creator-apply-card.is-pending {
  background: linear-gradient(135deg, #F6F8FA 0%, #EDF1F5 100%);
  border-color: #E2E8F0;
}
.creator-apply-card:active {
  transform: scale(0.98);
}

.apply-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #FF5E3A;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(255, 94, 58, 0.3);
}

.creator-apply-card .vip-title {
  color: #1D2129;
}
.creator-apply-card .vip-subtitle {
  color: #86909C;
}
.creator-apply-card .vip-arrow {
  color: #86909C;
}

@media (max-width: 768px) {
  .user-profile-header {
    padding: 16px 14px;
    border-radius: 18px;
  }
  .avatar-ring-box {
    width: 52px;
    height: 52px;
  }
  .user-nickname {
    font-size: 16px;
  }
  .daily-checkin-pill {
    padding: 6px 10px;
    font-size: 11px;
  }
}
</style>
