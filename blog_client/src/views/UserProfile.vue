<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1 class="profile-title">个人信息维护</h1>
      <p class="profile-subtitle">完善您的基本信息，以便我们提供更好的配送与沟通服务。</p>
    </div>

    <el-card class="profile-card">
      <el-form :model="profileForm" label-width="100px" v-loading="loading">
        <el-form-item label="头像">
          <div class="avatar-uploader">
            <el-avatar :size="80" :src="profileForm.avatarUrl"></el-avatar>
            <el-input v-model="profileForm.avatarUrl" placeholder="头像URL" style="margin-top: 10px;"></el-input>
          </div>
        </el-form-item>
        
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname"></el-input>
        </el-form-item>

        <el-form-item label="微信号">
          <el-input v-model="profileForm.wechatId" placeholder="方便后续沟通与发货通知"></el-input>
        </el-form-item>

        <el-form-item label="年龄">
          <el-input-number v-model="profileForm.age" :min="1" :max="120"></el-input-number>
        </el-form-item>

        <el-form-item label="性别">
          <el-radio-group v-model="profileForm.gender">
            <el-radio label="MALE">男</el-radio>
            <el-radio label="FEMALE">女</el-radio>
            <el-radio label="OTHER">其他</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="常用配送地址">
          <el-input type="textarea" v-model="profileForm.address" :rows="3" placeholder="请填写详细的收货地址"></el-input>
        </el-form-item>

        <el-form-item v-if="user && user.role === 'ADMIN'" label="管理权限">
          <el-button type="danger" plain round @click="$router.push('/admin')">进入管理后台</el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" round @click="handleUpdate" :loading="updating">保存修改</el-button>
          <el-button round @click="$router.go(-1)">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="points-info" v-if="user">
      <el-alert
        title="当前积分状态"
        type="success"
        :description="'您当前拥有 ' + user.points + ' 积分。通过邀请好友购买可获得更多积分奖励！'"
        show-icon
        :closable="false">
      </el-alert>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'UserProfile',
  data() {
    return {
      user: null,
      loading: false,
      updating: false,
      profileForm: {
        nickname: '',
        avatarUrl: '',
        wechatId: '',
        age: 18,
        gender: 'OTHER',
        address: ''
      }
    }
  },
  created() {
    this.fetchUser();
  },
  methods: {
    async fetchUser() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.$router.push('/');
        return;
      }
      this.loading = true;
      try {
        const res = await axios.get('/api/users/me', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.user = res.data;
        this.profileForm = {
          nickname: this.user.nickname,
          avatarUrl: this.user.avatarUrl,
          wechatId: this.user.wechatId || '',
          age: this.user.age || 18,
          gender: this.user.gender || 'OTHER',
          address: this.user.address || ''
        };
      } catch (error) {
        this.$message.error('加载用户信息失败');
      } finally {
        this.loading = false;
      }
    },
    async handleUpdate() {
      const token = localStorage.getItem('token');
      this.updating = true;
      try {
        await axios.put('/api/users/profile', this.profileForm, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.$message.success('个人信息更新成功');
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        this.$message.error('更新失败');
      } finally {
        this.updating = false;
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px 0 60px;
}
.profile-header {
  text-align: center;
  margin-bottom: 30px;
}
.profile-title {
  font-size: 28px;
  font-weight: 800;
  color: #5C433B;
}
.profile-subtitle {
  color: #8C6A5D;
  font-size: 14px;
}
.profile-card {
  border-radius: 20px;
  max-width: 600px;
  margin: 0 auto;
  box-shadow: 0 4px 20px rgba(255, 126, 103, 0.05);
}
.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.points-info {
  max-width: 600px;
  margin: 20px auto;
}

@media (max-width: 768px) {
  .profile-card {
    border-radius: 12px;
    margin: 0 10px;
  }
}
</style>
