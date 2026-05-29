<template>
  <div class="group-detail-container">
    <!-- 顶部导航 -->
    <div class="detail-header">
      <div class="header-back" @click="$router.back()">
        <icon-left style="font-size: 22px;" />
      </div>
      <span class="header-title">拼团详情</span>
      <div class="header-placeholder"></div>
    </div>

    <div v-if="loading" class="loading-state" style="padding: 40px 16px;">
      <a-skeleton animation>
        <a-space direction="vertical" :style="{width:'100%'}" size="large">
          <a-skeleton-line :rows="10" />
        </a-space>
      </a-skeleton>
    </div>

    <div v-else-if="group" class="detail-content">
      <!-- 商品概览卡片 -->
      <div class="section-card product-summary-card">
        <img :src="product.image" class="product-img">
        <div class="product-text">
          <h2 class="p-title">{{ product.name }}</h2>
          <p class="p-desc">{{ product.description }}</p>
          <div class="price-box">
            <span class="group-price">拼团价 ¥{{ product.groupPrice || (product.price * 0.8).toFixed(2) }}</span>
            <span class="origin-price">单买价 ¥{{ product.price }}</span>
          </div>
        </div>
      </div>

      <!-- 拼团进度与倒计时 -->
      <div class="section-card status-card" :class="getStatusClass(group.status)">
        <div class="status-header">
          <span class="status-tag">{{ getStatusText(group.status) }}</span>
          <div class="countdown" v-if="group.status === 0">
            剩余: <span class="time-value">{{ countdownText }}</span>
          </div>
        </div>
        <div class="progress-section">
          <div class="progress-info">
            <span>已凑齐 <b>{{ group.currentNum }}</b> 人</span>
            <span>还差 <b>{{ group.requiredNum - group.currentNum }}</b> 人</span>
          </div>
          <a-progress 
            :percent="group.currentNum / group.requiredNum" 
            :stroke-width="12" 
            :show-text="false"
            color="#FF4B2B" />
        </div>
      </div>

      <!-- 参团成员 -->
      <div class="section-card members-section">
        <h3 class="card-title"><icon-user-group /> 参团成员</h3>
        <div class="member-list">
          <div v-for="(member, index) in members" :key="member.userId || index" class="member-item" :class="{ initiator: index === 0 }">
            <div class="avatar-box">
              <a-avatar :image-url="member.avatarUrl || '/img/avatar.png'" :size="44"></a-avatar>
              <span class="badge" v-if="index === 0">团长</span>
            </div>
            <span class="member-name">{{ member.nickname || '匿名用户' }}</span>
          </div>
          <!-- 占位符 -->
          <div v-for="i in Math.max(0, group.requiredNum - group.currentNum)" :key="'empty-'+i" class="member-item empty">
            <div class="avatar-placeholder">?</div>
            <span class="member-name">待加入</span>
          </div>
        </div>
      </div>

      <!-- 底部操作按钮 -->
      <div class="action-footer" v-if="group.status === 0">
        <a-button 
          type="primary" 
          class="join-btn" 
          shape="round" 
          size="large"
          @click="showJoinDialog = true"
          :disabled="isMember">
          {{ isMember ? '您已在团中' : '立即加入拼团' }}
        </a-button>
        <p class="share-tip">分享给好友，成团更快哦！</p>
      </div>
    </div>

    <!-- 加入拼团弹窗 -->
    <GroupActionModal
      v-model:show="showJoinDialog"
      actionType="join"
      :product="product"
      :groupId="id"
      @success="handleGroupSuccess" />
  </div>
</template>

<script>
import { getGroupDetail, getGroupMembers, getMyGroups } from '@/api/product';
import { getProductDetail } from '@/api/product';
import { Message } from '@arco-design/web-vue';
import GroupActionModal from '@/components/product/GroupActionModal.vue';
import { mapState } from 'pinia'
import { useUserStore } from '@/stores/user'

export default {
  name: 'GroupDetail',
  components: {
    GroupActionModal
  },
  computed: {
    ...mapState(useUserStore, ['userInfo'])
  },
  data() {
    return {
      id: this.$route.params.id,
      group: null,
      product: {},
      members: [],
      loading: true,
      countdownText: '',
      timer: null,
      showJoinDialog: false,
      isMember: false,
      isMobile: window.innerWidth <= 768
    };
  },
  async created() {
    window.addEventListener('resize', this.handleResize);
    await this.fetchData();
    this.startCountdown();
    this.checkMembership();
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    async fetchData() {
      try {
        this.loading = true;
        const res = await getGroupDetail(this.id);
        this.group = res.data.data;
        
        const memRes = await getGroupMembers(this.id);
        this.members = memRes.data.data || [];
        
        const prodRes = await getProductDetail(this.group.productId);
        this.product = prodRes.data.data;
      } catch (e) {
        Message.error('获取拼团信息失败');
        this.$router.push('/store');
      } finally {
        this.loading = false;
      }
    },
    async checkMembership() {
      const token = localStorage.getItem('token');
      if (!token) return;
      try {
        const res = await getMyGroups();
        const myGroups = res.data.data || [];
        this.isMember = myGroups.some(g => g.id === parseInt(this.id));
      } catch (e) {
        console.error('Check membership failed');
      }
    },
    getStatusText(status) {
      return ['拼团中', '拼团成功', '拼团失败'][status] || '未知';
    },
    getStatusClass(status) {
      return ['status-active', 'status-success', 'status-fail'][status];
    },
    startCountdown() {
      const update = () => {
        if (!this.group || this.group.status !== 0) return;
        const expire = new Date(this.group.expireTime).getTime();
        const now = new Date().getTime();
        const diff = expire - now;

        if (diff <= 0) {
          this.countdownText = '已过期';
          this.group.status = 2;
          return;
        }

        const h = Math.floor(diff / (1000 * 60 * 60));
        const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const s = Math.floor((diff % (1000 * 60)) / 1000);
        this.countdownText = `${h}时${m}分${s}秒`;
      };
      update();
      this.timer = setInterval(update, 1000);
    },
    handleGroupSuccess() {
      this.checkMembership();
      this.fetchData();
    }
  }
};
</script>

<style scoped>
.group-detail-container {
  max-width: 600px;
  margin: 0 auto;
  min-height: 100vh;
  background: transparent;
  padding-bottom: 120px;
}

/* 顶部导航栏 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}
.header-back {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s;
  color: #1D2129;
}
.header-back:active {
  transform: scale(0.9);
  background: rgba(0, 0, 0, 0.08);
}
.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #1D2129;
}
.header-placeholder {
  width: 36px;
}

/* 通用卡片 */
.section-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  padding: 20px;
  margin: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
}

/* 商品概览 */
.product-summary-card {
  display: flex;
  gap: 16px;
}
.product-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 16px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}
.product-text {
  flex: 1;
  min-width: 0;
}
.p-title {
  margin: 0 0 6px 0;
  font-size: 17px;
  font-weight: 800;
  color: #1D2129;
  line-height: 1.4;
}
.p-desc {
  font-size: 12px;
  color: #86909C;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.price-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.group-price {
  color: #FF4B2B;
  font-weight: 800;
  font-size: 16px;
}
.origin-price {
  font-size: 11px;
  color: #C9CDD4;
  text-decoration: line-through;
}

/* 拼团状态 */
.status-card {
  text-align: center;
}
.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.status-tag {
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
}
.status-active .status-tag {
  background: linear-gradient(135deg, rgba(255,75,43,0.1) 0%, rgba(255,65,108,0.1) 100%);
  color: #FF4B2B;
}
.status-success .status-tag {
  background: rgba(0, 180, 42, 0.1);
  color: #00B42A;
}
.status-fail .status-tag {
  background: rgba(245, 63, 63, 0.1);
  color: #F53F3F;
}
.countdown {
  font-size: 12px;
  color: #86909C;
}
.time-value {
  color: #FF4B2B;
  font-weight: 700;
}
.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 8px;
  color: #4E5969;
  font-weight: 600;
}
.progress-info b {
  color: #FF4B2B;
  font-size: 16px;
}

/* 参团成员 */
.card-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 800;
  color: #1D2129;
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-title svg {
  color: #FF4B2B;
}
.member-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.member-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 64px;
}
.avatar-box {
  position: relative;
}
.avatar-box .badge {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%));
  color: white;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
  white-space: nowrap;
  font-weight: 700;
  box-shadow: 0 2px 6px rgba(255,75,43,0.3);
}
.member-name {
  font-size: 11px;
  color: #86909C;
  width: 100%;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}
.avatar-placeholder {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(0,0,0,0.03);
  border: 2px dashed #C9CDD4;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #C9CDD4;
  font-size: 16px;
  font-weight: 700;
}

/* 底部操作 */
.action-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0,0,0,0.05);
  box-shadow: 0 -4px 20px rgba(0,0,0,0.04);
  padding: 14px 20px;
  padding-bottom: calc(14px + env(safe-area-inset-bottom));
  text-align: center;
  z-index: 100;
  max-width: 600px;
  margin: 0 auto;
}
.join-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 800;
  height: 48px;
  background: var(--brand-gradient, linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%)) !important;
  border: none !important;
  box-shadow: 0 6px 20px rgba(255, 75, 43, 0.35);
}
.join-btn[disabled] {
  background: #C9CDD4 !important;
  box-shadow: none;
}
.share-tip {
  font-size: 12px;
  color: #86909C;
  margin: 10px 0 0;
}

@media (max-width: 768px) {
  .section-card {
    margin: 12px;
    padding: 16px;
    border-radius: 16px;
  }
}
</style>
