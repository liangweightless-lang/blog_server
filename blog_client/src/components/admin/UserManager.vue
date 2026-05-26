<template>
  <div class="user-manager">
    <el-table :data="users" v-loading="loadingUsers" stripe style="width: 100%; margin-top: 20px;">
      <el-table-column label="头像" width="80">
        <template slot-scope="scope">
          <el-avatar :size="32" :src="scope.row.avatarUrl"></el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="用户信息">
        <template slot-scope="scope">
          <div style="font-weight: bold; font-size: 15px; color: #333;">{{ scope.row.nickname }} ({{ scope.row.username }})</div>
          <div style="font-size: 12px; color: #999;">微信: {{ scope.row.wechatId || '未设置' }} | 地址: {{ scope.row.address || '未设置' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="角色/积分" width="150">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.role === 'ADMIN' ? 'danger' : 'info'">{{ scope.row.role }}</el-tag>
          <div style="font-size: 12px; margin-top: 4px; color: #E6A23C; font-weight: bold;">{{ scope.row.points }} 积分</div>
        </template>
      </el-table-column>
      <el-table-column label="加入时间" width="180">
        <template slot-scope="scope">
          <span style="font-size: 12px; color: #999;">{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'UserManager',
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
      return new Date(timeStr).toLocaleString();
    },
    async fetchUsers() {
      this.loadingUsers = true;
      try {
        const res = await axios.get('/api/users', { headers: this.getAuthHeader() });
        this.users = res.data.data;
      } catch (error) {
        this.$message.error('加载用户列表失败');
      } finally {
        this.loadingUsers = false;
      }
    }
  }
}
</script>
