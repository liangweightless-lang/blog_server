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
                  <el-image :src="order.productImage" class="full-order-img" fit="cover">
                    <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
                  </el-image>
                  <div class="order-main-info">
                    <p class="order-pname">{{ order.productName || '商品ID: ' + order.productId }}</p>
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
              <el-image :src="group.productImage" class="group-img" fit="cover">
                <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
              </el-image>
              <div class="group-info" style="flex: 1; min-width: 0;">
                <p class="group-pname">{{ group.productName }}</p>
                <p class="group-time">{{ formatTime(group.createTime) }}</p>
              </div>
              <div class="group-status-box">
                <el-tag :type="getStatusType(group.status)" size="small" effect="dark">
                  {{ getStatusText(group.status) }}
                </el-tag>
                <i class="el-icon-arrow-right" style="margin-left: 5px; color: #909399;"></i>
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
          <div style="display: flex; gap: 10px; align-items: flex-start;">
            <el-input type="textarea" v-model="profileForm.address" placeholder="详细地址" style="flex: 1;"></el-input>
            <el-button type="primary" icon="el-icon-location" circle @click="openMapDialog" title="地图定位"></el-button>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">保存</el-button>
      </div>
    </el-dialog>

    <!-- 高德地图选点弹窗 -->
    <el-dialog title="地图定位" :visible.sync="mapDialogVisible" :width="isMobile ? '95%' : '600px'" append-to-body destroy-on-close @opened="initMap">
      <div v-if="!amapKey" style="margin-bottom: 15px;">
        <el-alert title="地图服务未配置" type="warning" show-icon :closable="false">
          管理员尚未在后台系统配置中配置“高德地图 Key”与安全密钥，暂时无法使用地图定位功能。<br>
          <span style="font-size: 12px; color: #909399;">若您是管理员，请前往 控制台 -> 首页信息配置 -> 第三方服务配置 填写相关信息。</span>
        </el-alert>
      </div>
      
      <div style="margin-bottom: 10px; display: flex; gap: 10px;">
        <el-input id="map-search-input" v-model="mapSearchKeyword" placeholder="搜索地点..." prefix-icon="el-icon-search" clearable></el-input>
        <el-button type="primary" @click="searchMap">搜索</el-button>
      </div>
      
      <div id="amap-container" style="width: 100%; height: 300px; border-radius: 8px; border: 1px solid #DCDFE6;" v-loading="mapLoading"></div>
      
      <div v-if="selectedAddress" style="margin-top: 15px; background: #F2F6FC; padding: 10px; border-radius: 8px;">
        <span style="color: #909399; font-size: 12px;">已选地址：</span>
        <div style="font-weight: bold; color: #303133; margin-top: 4px;">{{ selectedAddress }}</div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="mapDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmMapLocation" :disabled="!selectedAddress">确 定</el-button>
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
import AMapLoader from '@amap/amap-jsapi-loader';
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
      
      // 高德地图相关
      mapDialogVisible: false,
      mapLoading: false,
      mapSearchKeyword: '',
      selectedAddress: '',
      mapInstance: null,
      markerInstance: null,
      geocoderInstance: null,
      amapKey: '', // 取自后端配置
      amapSecurityCode: '', // 取自后端配置
      
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
    this.fetchMapConfig();
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
    async fetchMapConfig() {
      try {
        const res = await axios.get('/api/home/config');
        if (res.data && res.data.data) {
          this.amapKey = res.data.data.amapKey || '';
          this.amapSecurityCode = res.data.data.amapSecurityCode || '';
        }
      } catch (e) {
        console.error('获取地图配置失败', e);
      }
    },
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
        this.favoriteArticles = res.data.data || [];
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
        const [groupRes, prodRes] = await Promise.all([
          axios.get('/api/groups/me', { headers: { 'Authorization': `Bearer ${token}` } }),
          axios.get('/api/products').catch(() => ({ data: { data: [] } }))
        ]);
        
        const products = prodRes.data.data || [];
        const productMap = {};
        products.forEach(p => productMap[p.id] = p);
        
        const rawGroups = groupRes.data.data || [];
        this.myGroups = rawGroups.map(group => ({
          ...group,
          productName: group.productName || productMap[group.productId]?.name || '未知商品',
          productImage: productMap[group.productId]?.image || ''
        }));
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
        const [orderRes, prodRes] = await Promise.all([
          axios.get('/api/orders/me', { headers: { 'Authorization': `Bearer ${token}` } }),
          axios.get('/api/products').catch(() => ({ data: { data: [] } }))
        ]);
        
        const products = prodRes.data.data || [];
        const productMap = {};
        products.forEach(p => productMap[p.id] = p);
        
        const rawOrders = orderRes.data.data || [];
        this.orders = rawOrders.map(order => ({
          ...order,
          productName: productMap[order.productId]?.name || '',
          productImage: productMap[order.productId]?.image || ''
        }));
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
    },
    
    // 地图相关方法
    openMapDialog() {
      this.mapDialogVisible = true;
      this.selectedAddress = '';
      this.mapSearchKeyword = '';
      if (!this.amapKey) {
        this.fetchMapConfig(); // 如果还没加载到，弹窗时重试加载
      }
    },
    initMap() {
      if (!this.amapKey) return;
      
      this.mapLoading = true;
      window._AMapSecurityConfig = {
        securityJsCode: this.amapSecurityCode,
      };

      AMapLoader.load({
        key: this.amapKey,
        version: "2.0",
        plugins: ['AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.Geolocation']
      }).then((AMap) => {
        this.mapInstance = new AMap.Map('amap-container', {
          zoom: 14,
          center: [116.397428, 39.90923] // 默认天安门
        });

        this.markerInstance = new AMap.Marker({
          map: this.mapInstance
        });

        this.geocoderInstance = new AMap.Geocoder();

        // 尝试自动定位
        const geolocation = new AMap.Geolocation({
          enableHighAccuracy: true,
          timeout: 10000,
          zoomToAccuracy: true,
          buttonPosition: 'RB'
        });
        this.mapInstance.addControl(geolocation);
        geolocation.getCurrentPosition((status, result) => {
          if (status === 'complete') {
             this.markerInstance.setPosition(result.position);
             this.getAddress(result.position);
          }
        });

        // 点击选点
        this.mapInstance.on('click', (e) => {
          this.markerInstance.setPosition(e.lnglat);
          this.getAddress(e.lnglat);
        });

        this.mapLoading = false;
      }).catch(e => {
        console.error(e);
        this.mapLoading = false;
        this.$message.error('地图加载失败，请检查 Key 和网络');
      });
    },
    getAddress(lnglat) {
      if (!this.geocoderInstance) return;
      this.geocoderInstance.getAddress(lnglat, (status, result) => {
        if (status === 'complete' && result.info === 'OK') {
          this.selectedAddress = result.regeocode.formattedAddress;
        }
      });
    },
    searchMap() {
      if (!this.mapSearchKeyword || !this.mapInstance) return;
      AMapLoader.load({}).then((AMap) => {
         const placeSearch = new AMap.PlaceSearch({
            map: this.mapInstance
         });
         placeSearch.search(this.mapSearchKeyword, (status, result) => {
            if (status === 'complete' && result.info === 'OK') {
               const poi = result.poiList.pois[0];
               if (poi) {
                  const lnglat = [poi.location.lng, poi.location.lat];
                  this.mapInstance.setCenter(lnglat);
                  this.markerInstance.setPosition(lnglat);
                  this.selectedAddress = poi.pname + poi.cityname + poi.adname + poi.address + poi.name;
               }
            } else {
               this.$message.warning('未找到相关地点');
            }
         });
      });
    },
    confirmMapLocation() {
      this.profileForm.address = this.selectedAddress;
      this.mapDialogVisible = false;
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
  background: #fdf5f5;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.group-item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.group-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 12px;
}

.group-img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  flex-shrink: 0;
}

.group-pname {
  margin: 0 0 5px 0;
  font-weight: bold;
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  align-items: center;
  gap: 15px;
}
.full-order-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  flex-shrink: 0;
  background-color: #f5f7fa;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #c0c4cc;
  font-size: 20px;
}
.order-main-info {
  flex: 1;
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
