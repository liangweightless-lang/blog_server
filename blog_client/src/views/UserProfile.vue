  <div class="user-center-container">
    <!-- 1. 顶部用户信息区 -->
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
          <el-button icon="el-icon-edit-outline" circle size="small" @click="showEditDialog"></el-button>
        </div>
      </div>
    </div>

    <!-- 2. 资产统计条 -->
    <div class="stats-bar-wrapper">
      <div class="stats-bar-card">
        <div class="stats-item">
          <span class="stats-val">{{ user ? user.points : 0 }}</span>
          <span class="stats-label">我的积分</span>
        </div>
        <div class="stats-item">
          <span class="stats-val">0</span>
          <span class="stats-label">优惠券</span>
        </div>
        <div class="stats-item">
          <span class="stats-val">¥0.00</span>
          <span class="stats-label">余额</span>
        </div>
      </div>
    </div>

    <!-- 3. 我的订单板块 -->
    <div class="section-card order-section">
      <div class="section-header">
        <span class="section-title">我的订单</span>
        <el-button type="text" class="view-all" @click="activeOrderTab = 'all'">全部订单 <i class="el-icon-arrow-right"></i></el-button>
      </div>
      <div class="order-status-grid">
        <div class="status-item" @click="activeOrderTab = 'unpaid'">
          <el-badge :value="unpaidCount" :hidden="unpaidCount === 0" class="badge-item">
            <i class="el-icon-wallet icon"></i>
          </el-badge>
          <span>待付款</span>
        </div>
        <div class="status-item">
          <i class="el-icon-box icon"></i>
          <span>待发货</span>
        </div>
        <div class="status-item">
          <i class="el-icon-truck icon"></i>
          <span>待收货</span>
        </div>
        <div class="status-item">
          <i class="el-icon-chat-line-round icon"></i>
          <span>评价</span>
        </div>
      </div>
      
      <!-- 快捷查看最近订单 -->
      <div class="recent-orders" v-if="orders.length > 0">
        <div v-for="order in orders.slice(0, 2)" :key="order.id" class="mini-order-card">
          <div class="mini-order-info">
            <span class="mini-id">#{{ order.id.substring(0, 8) }}</span>
            <span class="mini-status">{{ order.status === 1 ? '已完成' : '待支付' }}</span>
          </div>
          <div class="mini-order-price">¥{{ order.amount }}</div>
        </div>
      </div>
    </div>

    <!-- 4. 常用工具/服务区 -->
    <div class="section-card tools-section">
      <div class="section-header">
        <span class="section-title">常用工具</span>
      </div>
      <div class="tools-list">
        <div class="tool-cell" @click="showAddressDialog">
          <div class="cell-left">
            <i class="el-icon-location-outline tool-icon" style="color: #409EFF"></i>
            <span>收货地址</span>
          </div>
          <i class="el-icon-arrow-right"></i>
        </div>
        <div class="tool-cell" @click="showInviteDialog">
          <div class="cell-left">
            <i class="el-icon-present tool-icon" style="color: #F56C6C"></i>
            <span>邀请有礼</span>
          </div>
          <i class="el-icon-arrow-right"></i>
        </div>
        <div class="tool-cell" v-if="user && user.role === 'ADMIN'" @click="$router.push('/admin')">
          <div class="cell-left">
            <i class="el-icon-set-up tool-icon" style="color: #E6A23C"></i>
            <span>管理后台</span>
          </div>
          <i class="el-icon-arrow-right"></i>
        </div>
        <div class="tool-cell" @click="handleLogout">
          <div class="cell-left">
            <i class="el-icon-switch-button tool-icon" style="color: #909399"></i>
            <span>退出登录</span>
          </div>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog title="修改个人信息" :visible.sync="editDialogVisible" :width="isMobile ? '90%' : '500px'" round>
      <el-form :model="profileForm" label-width="80px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="/api/files/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <img v-if="profileForm.avatarUrl" :src="profileForm.avatarUrl" class="avatar">
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

/* Header Section */
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

/* Stats Bar */
.stats-bar-wrapper {
  margin: -30px 15px 0;
  z-index: 10;
  position: relative;
}
.stats-bar-card {
  background: white;
  border-radius: 12px;
  display: flex;
  padding: 15px 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  max-width: 600px;
  margin: 0 auto;
}
.stats-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.stats-val {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}
.stats-label {
  font-size: 11px;
  color: #909399;
}

/* Sections */
.section-card {
  background: white;
  margin: 15px;
  border-radius: 12px;
  padding: 15px;
  max-width: 600px;
  margin: 15px auto;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.section-title {
  font-weight: bold;
  font-size: 15px;
  color: #303133;
}
.view-all {
  font-size: 12px;
  color: #909399;
  padding: 0;
}

/* Orders Grid */
.order-status-grid {
  display: flex;
  padding: 5px 0;
}
.status-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.status-item .icon {
  font-size: 24px;
  color: #5C433B;
}
.status-item span {
  font-size: 12px;
  color: #606266;
}

/* Recent Orders */
.recent-orders {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #F2F6FC;
}
.mini-order-card {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 8px;
  color: #909399;
}
.mini-status {
  color: #FF7E67;
  margin-left: 8px;
}

/* Tools List */
.tools-list {
  display: flex;
  flex-direction: column;
}
.tool-cell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #F2F6FC;
  cursor: pointer;
}
.tool-cell:last-child {
  border-bottom: none;
}
.cell-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.tool-icon {
  font-size: 20px;
}
.cell-left span {
  font-size: 14px;
  color: #303133;
}

/* Invite Dialog */
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

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
}
.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
}

@media (max-width: 768px) {
  .user-profile-header {
    padding: 30px 15px 50px;
  }
  .section-card {
    margin: 12px;
  }
}
</style>
