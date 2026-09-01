<template>
  <a-modal 
    :visible="visible" 
    :width="isMobile ? '100%' : '560px'" 
    :footer="false"
    :header="false"
    :mask-closable="true"
    @cancel="handleCancel"
    unmount-on-close
  >
    <div class="sheet-modern-container">
      <div class="sheet-handle-bar" v-if="isMobile"></div>
      
      <button class="sheet-circle-close" @click="handleCancel" aria-label="关闭">
        <icon-close />
      </button>

      <div class="sheet-header">
        <h3 class="sheet-title">我的拼团记录</h3>
        <p class="sheet-subtitle">查看正在进行中及已完成的拼团进度</p>
      </div>

      <div class="sheet-body">
        <a-spin :loading="loadingGroups" style="width: 100%; min-height: 200px; display: block;">
          <a-empty v-if="myGroups.length === 0 && !loadingGroups" description="暂无拼团记录" style="margin: 40px 0;">
            <template #image><icon-user-group style="font-size: 44px; color: #D3C1BA; opacity: 0.5;" /></template>
          </a-empty>

          <div v-else class="groups-list">
            <div v-for="group in myGroups" :key="group.id" class="group-item-card" @click="goToGroupDetail(group)">
              <div class="group-main">
                <img :src="group.productImage" class="group-img" />
                <div class="group-info">
                  <p class="group-pname">{{ group.productName }}</p>
                  <p class="group-time">{{ $formatTime(group.createTime) }}</p>
                </div>
                <div class="group-status-box">
                  <a-tag :color="getStatusType(group.status)" size="small">
                    {{ getStatusText(group.status) }}
                  </a-tag>
                  <icon-right class="group-arrow" />
                </div>
              </div>
              <div class="group-progress-box">
                <a-progress 
                  :percent="group.currentNum / group.requiredNum" 
                  :status="group.status === 1 ? 'success' : (group.status === 2 ? 'danger' : 'normal')" 
                />
                <div class="progress-labels">
                  <span>成团进度: {{ group.currentNum }} / {{ group.requiredNum }} 人</span>
                  <span v-if="group.status === 0" class="click-tip">点击查看详情</span>
                </div>
              </div>
            </div>
          </div>
        </a-spin>
      </div>
    </div>
  </a-modal>
</template>

<script>
import { getMyGroups, getProducts } from '@/api/product';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'MyGroupsDialog',
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      myGroups: [],
      loadingGroups: false,
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    visible: {
      get() {
        return this.show;
      },
      set(val) {
        this.$emit('update:show', val);
      }
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.fetchGroups();
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    handleCancel() {
      this.visible = false;
    },
    async fetchGroups() {
      this.loadingGroups = true;
      try {
        const [groupRes, prodRes] = await Promise.all([
          getMyGroups(),
          getProducts().catch(() => ({ data: { data: [] } }))
        ]);
        
        const products = prodRes.data.data || [];
        const prodMap = {};
        products.forEach(p => prodMap[p.id] = p);
        
        const raw = groupRes.data.data || [];
        this.myGroups = raw.map(g => ({
          ...g,
          productName: prodMap[g.productId]?.name || '未知商品',
          productImage: prodMap[g.productId]?.image || '/img/avatar.png'
        }));
      } catch (e) {
        Message.error('获取拼团记录失败');
      } finally {
        this.loadingGroups = false;
      }
    },
    getStatusType(status) {
      if (status === 0) return 'orange';
      if (status === 1) return 'green';
      return 'gray';
    },
    getStatusText(status) {
      if (status === 0) return '拼团中';
      if (status === 1) return '已成团';
      return '已失败';
    },
    goToGroupDetail(group) {
      this.visible = false;
      this.$router.push(`/group/${group.id}`);
    }
  }
}
</script>

<style scoped>
.sheet-modern-container {
  padding: 16px 18px 24px;
  position: relative;
}

.sheet-header {
  text-align: center;
  margin-bottom: 16px;
}

.sheet-title {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.3px;
}

.sheet-subtitle {
  margin: 0;
  font-size: 12px;
  color: #86909C;
}

.groups-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.group-item-card {
  background: #F7F8FA;
  border-radius: 16px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.group-item-card:active {
  transform: scale(0.98);
}

.group-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.group-img {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.group-info {
  flex: 1;
  min-width: 0;
}

.group-pname {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-time {
  margin: 0;
  font-size: 11px;
  color: #86909C;
}

.group-status-box {
  display: flex;
  align-items: center;
  gap: 4px;
}

.group-arrow {
  color: #C9CDD4;
  font-size: 12px;
}

.group-progress-box {
  margin-top: 10px;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #86909C;
  margin-top: 6px;
}

.click-tip {
  color: #FF5E3A;
  font-weight: 600;
}
</style>
