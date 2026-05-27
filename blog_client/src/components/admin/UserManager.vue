<template>
  <div class="user-manager">
    <a-table v-if="!isMobile" :data="users" :loading="loadingUsers" stripe style="margin-top: 20px;" :pagination="{ pageSize: 10 }">
      <template #columns>
        <a-table-column title="头像" :width="80">
          <template #cell="{ record }">
            <a-avatar :size="32" :image-url="record.avatarUrl || '/img/avatar.png'"></a-avatar>
          </template>
        </a-table-column>
        <a-table-column title="用户信息">
          <template #cell="{ record }">
            <div style="font-weight: bold; font-size: 15px; color: #333;">{{ record.nickname }} ({{ record.username }})</div>
            <div style="font-size: 12px; color: #999;">微信: {{ record.wechatId || '未设置' }} | 地址: {{ record.address || '未设置' }}</div>
          </template>
        </a-table-column>
        <a-table-column title="角色/积分" :width="150">
          <template #cell="{ record }">
            <a-tag size="small" :color="record.role === 'ADMIN' ? 'red' : 'gray'">{{ record.role }}</a-tag>
            <div style="font-size: 12px; margin-top: 4px; color: #E6A23C; font-weight: bold;">{{ record.points }} 积分</div>
          </template>
        </a-table-column>
        <a-table-column title="加入时间" :width="180">
          <template #cell="{ record }">
            <span style="font-size: 12px; color: #999;">{{ formatTime(record.createTime) }}</span>
          </template>
        </a-table-column>
      </template>
    </a-table>

    <!-- 移动端视图: 卡片列表 -->
    <div v-else class="mobile-card-list">
      <a-spin :loading="loadingUsers" style="width: 100%; display: block;">
        <div v-for="user in users" :key="user.id" class="mobile-card-item">
          <a-avatar :size="50" :image-url="user.avatarUrl || '/img/avatar.png'" class="card-avatar"></a-avatar>
          <div class="card-info">
            <div class="card-header">
              <h4 class="card-title">{{ user.nickname }} ({{ user.username }})</h4>
              <a-tag size="small" :color="user.role === 'ADMIN' ? 'red' : 'gray'" class="role-tag">{{ user.role }}</a-tag>
            </div>
            <div class="card-desc">
              <icon-wechat style="color: #07C160; margin-right: 4px;" />{{ user.wechatId || '未设置' }}
            </div>
            <div class="card-desc">
              <icon-location style="color: #165DFF; margin-right: 4px;" />{{ user.address || '未设置' }}
            </div>
            <div class="card-meta">
              <span class="points-text">{{ user.points }} 积分</span>
              <span class="card-time">{{ formatTime(user.createTime) }}</span>
            </div>
          </div>
        </div>
        <a-empty v-if="users.length === 0 && !loadingUsers" description="暂无用户" />
      </a-spin>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'UserManager',
  props: {
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      users: [],
      loadingUsers: false
    }
  },
  created() {
    this.fetchUsers();
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      return new Date(timeStr).toLocaleString();
    },
    async fetchUsers() {
      this.loadingUsers = true;
      try {
        const res = await axios.get('/api/users', { headers: this.getAuthHeader() });
        this.users = res.data.data;
      } catch (error) {
        Message.error('加载用户列表失败');
      } finally {
        this.loadingUsers = false;
      }
    }
  }
}
</script>

<style scoped>
/* 移动端卡片列表样式 */
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 15px;
}
.mobile-card-item {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 15px;
  gap: 12px;
  align-items: flex-start;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.card-avatar {
  flex-shrink: 0;
  border: 1px solid #F0F0F0;
}
.card-info {
  flex: 1;
  min-width: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.card-title {
  margin: 0;
  font-size: 15px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.role-tag {
  transform: scale(0.9);
  transform-origin: right center;
}
.card-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #F0F0F0;
}
.points-text {
  font-weight: bold;
  color: #E6A23C;
  font-size: 13px;
}
.card-time {
  font-size: 11px;
  color: #999;
}
</style>
