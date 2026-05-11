<template>
  <div class="user-center-container">
    <!-- 使用抽离的子组件 -->
    <UserHeader :user="user" @edit="showEditDialog" />
    <UserStats :user="user" />
    <UserOrderGrid :orders="orders" />
    <UserToolList 
      :user="user" 
      @address="showAddressDialog" 
      @invite="showInviteDialog" 
      @logout="handleLogout" 
    />

    <!-- 编辑资料弹窗 (保持在父组件以方便状态管理) -->
    <el-dialog title="修改个人信息" :visible.sync="editDialogVisible" :width="isMobile ? '90%' : '500px'" round>
      <el-form :model="profileForm" label-width="80px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader-box"
            action="/api/files/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <img v-if="profileForm.avatarUrl" :src="profileForm.avatarUrl" class="edit-avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname"></el-input>
        </el-form-item>
        <el-form-item label="微信号">
          <el-input v-model="profileForm.wechatId" placeholder="方便后续沟通"></el-input>
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input type="textarea" v-model="profileForm.address" placeholder="详细地址"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">保存</el-button>
      </div>
    </el-dialog>

    <!-- 邀请码弹窗 -->
    <el-dialog title="我的邀请码" :visible.sync="inviteDialogVisible" :width="isMobile ? '85%' : '400px'" round center>
      <div class="invite-dialog-content" v-if="user">
        <div class="invite-box">
          <p class="invite-label">专属邀请码</p>
          <h2 class="invite-code-text">{{ user.inviteCode }}</h2>
        </div>
        <el-button type="primary" round style="width: 100%" @click="copyInviteLink">复制邀请链接</el-button>
        <p class="invite-tip">每邀请一位好友注册，双方均可获得50积分奖励</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import UserHeader from '@/components/user/UserHeader.vue';
import UserStats from '@/components/user/UserStats.vue';
import UserOrderGrid from '@/components/user/UserOrderGrid.vue';
import UserToolList from '@/components/user/UserToolList.vue';

export default {
  name: 'UserProfile',
  components: {
    UserHeader,
    UserStats,
    UserOrderGrid,
    UserToolList
  },
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
      },
      orders: [],
      loadingOrders: false,
      editDialogVisible: false,
      inviteDialogVisible: false,
      activeOrderTab: 'all',
      isMobile: window.innerWidth <= 768
    }
  },
  created() {
    this.fetchUser();
    this.fetchMyOrders();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  computed: {
    unpaidCount() {
      return this.orders.filter(o => o.status === 0).length;
    }
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    showEditDialog() {
      this.editDialogVisible = true;
    },
    showAddressDialog() {
      this.editDialogVisible = true; // Address is in edit dialog
    },
    showInviteDialog() {
      this.inviteDialogVisible = true;
    },
    handleLogout() {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      this.$router.push('/');
      this.$message.success('已安全退出');
    },
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
    async fetchMyOrders() {
      const token = localStorage.getItem('token');
      if (!token) return;
      this.loadingOrders = true;
      try {
        const res = await axios.get('/api/orders/me', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.orders = res.data;
      } catch (error) {
        console.error('加载订单失败');
      } finally {
        this.loadingOrders = false;
      }
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      return new Date(timeStr).toLocaleString();
    },
    async handleUpdate() {
      const token = localStorage.getItem('token');
      this.updating = true;
      try {
        await axios.put('/api/users/profile', this.profileForm, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.$message.success('个人信息更新成功');
        this.editDialogVisible = false;
        this.fetchUser();
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
.user-center-container {
  background: #F7F8FA;
  min-height: 100vh;
  padding-bottom: 80px;
}

/* Edit Dialog Styles */
.avatar-uploader-box {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
  cursor: pointer;
}
.edit-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

/* Invite Dialog Styles */
.invite-dialog-content {
  text-align: center;
}
.invite-box {
  background: #FDF0E6;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
}
.invite-label {
  font-size: 12px;
  color: #8C6A5D;
  margin-bottom: 8px;
}
.invite-code-text {
  font-size: 32px;
  color: #FF7E67;
  letter-spacing: 2px;
  margin: 0;
}
.invite-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 15px;
}

@media (max-width: 768px) {
  .user-center-container {
    padding-bottom: 60px;
  }
}
</style>
