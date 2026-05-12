<template>
  <div class="user-center-container">
    <!-- 使用抽离的子组件 -->
    <UserHeader :user="user" @edit="showEditDialog" />
    <UserStats :user="user" />
    
    <!-- 订单快捷状态栏，点击切换到订单 Tab -->
    <UserOrderGrid :orders="orders" @view-all="activeTab = 'orders'" />
    
    <div class="user-tabs-section">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane name="favorites">
          <span slot="label"><i class="el-icon-star-off"></i> 我的收藏</span>
          <div class="tab-content-wrapper">
            <ArticleGrid :articles="favoriteArticles" :loading="loadingFavorites" />
            <div v-if="!loadingFavorites && favoriteArticles.length === 0" class="empty-placeholder">
              <i class="el-icon-collection-tag"></i>
              <p>还没有收藏任何灵感，快去首页逛逛吧</p>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane name="orders">
          <span slot="label"><i class="el-icon-s-order"></i> 我的订单</span>
          <div class="tab-content-wrapper">
            <div v-if="orders.length === 0" class="empty-placeholder">
              <i class="el-icon-shopping-cart-2"></i>
              <p>暂无订单记录</p>
            </div>
            <div v-else class="order-full-list">
              <div v-for="order in orders" :key="order.id" class="order-card-item">
                <div class="order-card-header">
                  <span class="order-id">订单号: {{ order.id.substring(0, 12) }}...</span>
                  <el-tag :type="getOrderStatusType(order.status)" size="mini" effect="plain">
                    {{ getOrderStatusText(order.status) }}
                  </el-tag>
                </div>
                <div class="order-card-body">
                  <div class="order-main-info">
                    <p class="order-pname">商品ID: {{ order.productId }}</p>
                    <p class="order-spec" v-if="order.selectedSpec">规格: {{ order.selectedSpec }}</p>
                    <p class="order-time">{{ formatTime(order.createTime) }}</p>
                  </div>
                  <div class="order-price-info">
                    <span class="price-val">¥{{ order.amount }}</span>
                  </div>
                </div>
              </div>
              <p class="list-end-tip">已展示全部 {{ orders.length }} 个订单</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <UserToolList 
      :user="user" 
      @address="showAddressDialog" 
      @invite="showInviteDialog" 
      @groups="showGroupsDialog"
      @logout="handleLogout" 
    />

    <!-- 我的拼团弹窗 -->
    <el-dialog title="我的拼团" :visible.sync="groupsDialogVisible" :width="isMobile ? '95%' : '600px'" round center>
      <div v-loading="loadingGroups" style="min-height: 200px;">
        <div v-if="myGroups.length === 0" class="empty-groups">
          <i class="el-icon-warning-outline" style="font-size: 40px; color: #C0C4CC; margin-bottom: 10px;"></i>
          <p>暂无拼团记录</p>
        </div>
        <div v-else class="groups-list">
          <div v-for="group in myGroups" :key="group.id" class="group-item-card" @click="goToGroupDetail(group)">
            <div class="group-main">
              <div class="group-info">
                <p class="group-pname">{{ group.productName }}</p>
                <p class="group-time">{{ formatTime(group.createTime) }}</p>
              </div>
              <div class="group-status-box">
                <el-tag :type="getStatusType(group.status)" size="small" effect="dark">
                  {{ getStatusText(group.status) }}
                </el-tag>
                <i class="el-icon-arrow-right"></i>
              </div>
            </div>
            <div class="group-progress">
              <el-progress 
                :percentage="Math.floor((group.currentNum / group.requiredNum) * 100)" 
                :status="group.status === 1 ? 'success' : (group.status === 2 ? 'exception' : '')"
                :stroke-width="10">
              </el-progress>
              <div class="progress-labels">
                <span>当前: {{ group.currentNum }} / {{ group.requiredNum }} 人</span>
                <span v-if="group.status === 0" class="click-tip">点击查看详情</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

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
import ArticleGrid from '@/components/home/ArticleGrid.vue';

export default {
  name: 'UserProfile',
  components: {
    UserHeader,
    UserStats,
    UserOrderGrid,
    UserToolList,
    ArticleGrid
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
      groupsDialogVisible: false,
      myGroups: [],
      loadingGroups: false,
      activeTab: 'favorites',
      favoriteArticles: [],
      loadingFavorites: false,
      isMobile: window.innerWidth <= 768
    }
  },
  created() {
    this.fetchUser();
    this.fetchMyFavorites();
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
    handleTabClick(tab) {
      if (tab.name === 'favorites') {
        this.fetchMyFavorites();
      } else if (tab.name === 'orders') {
        this.fetchMyOrders();
      }
    },
    async fetchMyFavorites() {
      const token = localStorage.getItem('token');
      if (!token) return;
      this.loadingFavorites = true;
      try {
        const res = await axios.get('/api/favorites/me', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.favoriteArticles = res.data.data;
      } catch (error) {
        console.error('获取收藏失败');
      } finally {
        this.loadingFavorites = false;
      }
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
    async showGroupsDialog() {
      this.groupsDialogVisible = true;
      this.fetchMyGroups();
    },
    async fetchMyGroups() {
      const token = localStorage.getItem('token');
      if (!token) return;
      this.loadingGroups = true;
      try {
        const res = await axios.get('/api/groups/me', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        this.myGroups = res.data.data;
      } catch (error) {
        this.$message.error('加载拼团信息失败');
      } finally {
        this.loadingGroups = false;
      }
    },
    getStatusType(status) {
      const types = ['warning', 'success', 'danger'];
      return types[status] || 'info';
    },
    getStatusText(status) {
      const texts = ['拼团中', '拼团成功', '拼团失败'];
      return texts[status] || '未知';
    },
    goToGroupDetail(group) {
      this.groupsDialogVisible = false;
      this.$router.push(`/product/group/${group.id}`);
    },
    handleLogout() {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.dispatchEvent(new CustomEvent('user-updated', { detail: null }));
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
        this.user = res.data.data;
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
        this.orders = res.data.data;
      } catch (error) {
        console.error('加载订单失败');
      } finally {
        this.loadingOrders = false;
      }
    },
    getOrderStatusType(status) {
      const types = ['info', 'success', 'danger', 'primary'];
      return types[status] || 'info';
    },
    getOrderStatusText(status) {
      const texts = ['待支付', '已支付', '已取消', '已发货'];
      return texts[status] || '未知';
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

.user-tabs-section {
  background: #FFFFFF;
  margin: 15px 0;
  padding: 10px 15px;
}

::v-deep .el-tabs__nav-wrap::after {
  height: 1px;
  background-color: #FDF0E6;
}

::v-deep .el-tabs__active-bar {
  background-color: #FF7E67;
}

::v-deep .el-tabs__item.is-active {
  color: #FF7E67;
  font-weight: 800;
}

::v-deep .el-tabs__item {
  color: #8C6A5D;
  font-weight: 600;
  font-size: 15px;
}

.tab-content-wrapper {
  padding: 15px 0;
  min-height: 200px;
}

.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #D3C1BA;
}

.empty-placeholder i {
  font-size: 48px;
  margin-bottom: 15px;
  opacity: 0.5;
}

.empty-placeholder p {
  font-size: 14px;
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

/* My Groups Styles */
.empty-groups {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  color: #909399;
}
.group-item-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #F2F6FC;
}
.group-item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.1);
  border-color: #FFE4D6;
}
.group-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.group-pname {
  font-weight: 800;
  font-size: 15px;
  color: #5C433B;
  margin-bottom: 4px;
}
.group-time {
  font-size: 11px;
  color: #909399;
}
.group-status-box {
  display: flex;
  align-items: center;
  gap: 8px;
}
.group-status-box i {
  color: #C0C4CC;
  font-size: 14px;
}
.group-progress {
  padding: 0 2px;
}
.progress-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #8C6A5D;
  margin-top: 10px;
}
.click-tip {
  color: #FF7E67;
  font-weight: bold;
}

/* Order Card Styles */
.order-card-item {
  background: white;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 12px;
  border: 1px solid #f0f0f0;
}
.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #eee;
}
.order-id {
  font-size: 11px;
  color: #999;
}
.order-card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-pname {
  font-weight: bold;
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}
.order-spec {
  font-size: 11px;
  color: #FF7E67;
  background: #FFF0ED;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 4px;
}
.order-time {
  font-size: 11px;
  color: #bbb;
}
.price-val {
  font-weight: bold;
  font-size: 16px;
  color: #F56C6C;
}
.list-end-tip {
  text-align: center;
  color: #ccc;
  font-size: 12px;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .user-center-container {
    padding-bottom: 60px;
  }
}
</style>
