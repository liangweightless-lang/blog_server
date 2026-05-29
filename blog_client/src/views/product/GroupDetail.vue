<template>
  <div class="group-detail-container">
    <div class="back-nav" @click="$router.back()">
      <icon-left /> 返回商城
    </div>

    <div v-if="loading" class="loading-state">
      <a-skeleton animation>
        <a-space direction="vertical" :style="{width:'100%'}" size="large">
          <a-skeleton-line :rows="10" />
        </a-space>
      </a-skeleton>
    </div>

    <div v-else-if="group" class="detail-content">
      <!-- 1. 商品概览卡片 -->
      <div class="product-summary-card">
        <img :src="product.image" class="product-img">
        <div class="product-text">
          <h2 class="title">{{ product.name }}</h2>
          <p class="desc">{{ product.description }}</p>
          <div class="price-box">
            <span class="group-price">拼团价 ¥{{ product.groupPrice || (product.price * 0.8).toFixed(2) }}</span>
            <span class="origin-price">单买价 ¥{{ product.price }}</span>
          </div>
        </div>
      </div>

      <!-- 2. 拼团进度与倒计时 -->
      <div class="status-card" :class="getStatusClass(group.status)">
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
            color="#FF7E67" />
        </div>
      </div>

      <!-- 3. 参团成员 -->
      <div class="members-section">
        <h3 class="section-title">参团成员</h3>
        <div class="member-list">
          <div v-for="(member, index) in members" :key="member.userId || index" class="member-item" :class="{ initiator: index === 0 }">
            <div class="avatar-box">
              <a-avatar :image-url="member.avatarUrl || '/img/avatar.png'" :size="40"></a-avatar>
              <span class="badge" v-if="index === 0">团长</span>
            </div>
            <span class="name">{{ member.nickname || '匿名用户' }}</span>
          </div>
          <!-- 占位符 -->
          <div v-for="i in Math.max(0, group.requiredNum - group.currentNum)" :key="'empty-'+i" class="member-item empty">
            <div class="avatar-placeholder">?</div>
            <span class="name">待加入</span>
          </div>
        </div>
      </div>

      <!-- 4. 操作按钮 -->
      <div class="action-footer" v-if="group.status === 0">
        <a-button 
          type="primary" 
          status="warning"
          class="join-btn" 
          shape="round" 
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
  padding: 15px;
  max-width: 600px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f8f9fa;
}

.back-nav {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
  cursor: pointer;
}

.product-summary-card {
  background: white;
  border-radius: 16px;
  padding: 15px;
  display: flex;
  gap: 15px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.product-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 12px;
}

.product-text {
  flex: 1;
}

.product-text .title {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #2c3e50;
}

.product-text .desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.price-box {
  display: flex;
  flex-direction: column;
}

.group-price {
  color: #FF7E67;
  font-weight: bold;
  font-size: 16px;
}

.origin-price {
  font-size: 11px;
  color: #C0C4CC;
  text-decoration: line-through;
}

.status-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
  text-align: center;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}

.status-active .status-tag { background: #FFE4D6; color: #FF7E67; }
.status-success .status-tag { background: #e1f3d8; color: #67C23A; }
.status-fail .status-tag { background: #fef0f0; color: #F56C6C; }

.countdown {
  font-size: 12px;
  color: #909399;
}

.time-value {
  color: #FF7E67;
  font-weight: bold;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 8px;
  color: #606266;
}

.members-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 100px;
}

.section-title {
  font-size: 15px;
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
}

.member-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.member-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  width: 60px;
}

.avatar-box {
  position: relative;
}

.avatar-box .badge {
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  background: #FF7E67;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  white-space: nowrap;
}

.name {
  font-size: 11px;
  color: #606266;
  width: 100%;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #F2F6FC;
  border: 1px dashed #DCDFE6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #C0C4CC;
}

.action-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 15px 20px 30px;
  box-shadow: 0 -4px 12px rgba(0,0,0,0.05);
  text-align: center;
  z-index: 10;
}

.join-btn {
  width: 100%;
  font-size: 16px;
  font-weight: bold;
  height: 45px;
}

.share-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}


</style>
