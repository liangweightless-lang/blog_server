<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1 class="profile-title">个人信息维护</h1>
      <p class="profile-subtitle">完善您的基本信息，以便我们提供更好的配送与沟通服务。</p>
    </div>

    <el-card class="profile-card">
      <el-form :model="profileForm" label-width="100px" v-loading="loading">
        <el-form-item label="头像">
          <div class="avatar-uploader-container">
            <el-upload
              class="avatar-uploader"
              action="/api/files/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload">
              <img v-if="profileForm.avatarUrl" :src="profileForm.avatarUrl" class="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <p class="upload-tip">点击预览图上传新头像</p>
          </div>
        </el-form-item>
        
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname"></el-input>
        </el-form-item>

        <el-form-item label="注册手机号">
          <el-input :value="user ? user.username : ''" disabled placeholder="注册账号"></el-input>
          <p class="upload-tip">注册账号不可修改</p>
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
        :description="'您当前拥有 ' + user.points + ' 积分。通过邀请好友注册并购买可获得更多积分奖励！'"
        show-icon
        :closable="false">
      </el-alert>
    </div>

    <!-- 邀请好友 -->
    <el-card class="invite-card" v-if="user">
      <div slot="header" class="invite-header">
        <span>邀请好友加入</span>
        <el-tag size="small" type="warning">每一位好友注册双方均得50积分</el-tag>
      </div>
      <div class="invite-content">
        <div class="invite-code-box">
          <p class="label">您的专属邀请码</p>
          <h2 class="invite-code">{{ user.inviteCode }}</h2>
        </div>
        <div class="share-actions">
          <el-button type="primary" icon="el-icon-document-copy" round @click="copyInviteLink">复制邀请链接</el-button>
          <p class="share-tip">将链接发给好友，注册时将自动填入您的邀请码</p>
        </div>
      </div>
    </el-card>
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
    },
    handleAvatarSuccess(res) {
      this.profileForm.avatarUrl = res.url;
      this.$message.success('头像上传成功');
    },
    beforeAvatarUpload(file) {
      const isJPGorPNG = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPGorPNG) {
        this.$message.error('上传头像图片只能是 JPG 或 PNG 格式!');
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!');
      }
      return isJPGorPNG && isLt2M;
    },
    copyInviteLink() {
      const baseUrl = window.location.origin;
      const link = `${baseUrl}/?invite=${this.user.inviteCode}`;
      
      const input = document.createElement('input');
      input.value = link;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      
      this.$message.success('邀请链接已复制到剪贴板，快去发给好友吧！');
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
.avatar-uploader-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}
.avatar-uploader:hover {
  border-color: #FF7E67;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
.points-info {
  max-width: 600px;
  margin: 20px auto;
}
.invite-card {
  max-width: 600px;
  margin: 20px auto;
  border-radius: 20px;
  background: linear-gradient(135deg, #FFFDF8 0%, #FDF0E6 100%);
  border: 1px solid #FFE4D6;
}
.invite-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.invite-content {
  text-align: center;
  padding: 10px 0;
}
.invite-code-box {
  background: #FFFFFF;
  padding: 15px;
  border-radius: 12px;
  display: inline-block;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.invite-code-box .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}
.invite-code {
  font-size: 32px;
  letter-spacing: 4px;
  color: #FF7E67;
  margin: 0;
}
.share-tip {
  font-size: 12px;
  color: #8C6A5D;
  margin-top: 15px;
}

@media (max-width: 768px) {
  .profile-card, .invite-card, .points-info {
    border-radius: 12px;
    margin: 10px;
  }
}
</style>
