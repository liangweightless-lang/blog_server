<template>
  <a-modal title="我的拼团" :visible="visible" :width="isMobile ? '95%' : '600px'" @cancel="handleCancel" :footer="false">
    <a-spin :loading="loadingGroups" style="width: 100%; min-height: 200px; display: block;">
      <a-empty v-if="myGroups.length === 0" description="暂无拼团记录" style="margin: 40px 0;">
        <template #image><icon-info-circle style="font-size: 40px; color: #C0C4CC;" /></template>
      </a-empty>
      <a-list v-else class="groups-list" :bordered="false" :split="false">
        <a-list-item v-for="group in myGroups" :key="group.id" class="group-item-card" @click="goToGroupDetail(group)">
          <div class="group-main">
            <a-image :src="group.productImage" class="group-img" width="50" height="50" fit="cover" />
            <div class="group-info" style="flex: 1; min-width: 0;">
              <p class="group-pname">{{ group.productName }}</p>
              <p class="group-time">{{ formatTime(group.createTime) }}</p>
            </div>
            <div class="group-status-box">
              <a-tag :color="getStatusType(group.status)" size="small">
                {{ getStatusText(group.status) }}
              </a-tag>
              <icon-right style="margin-left: 5px; color: #909399;" />
            </div>
          </div>
          <div class="group-progress">
            <a-progress 
              :percent="group.currentNum / group.requiredNum" 
              :status="group.status === 1 ? 'success' : (group.status === 2 ? 'danger' : 'normal')" />
            <div class="progress-labels">
              <span>当前: {{ group.currentNum }} / {{ group.requiredNum }} 人</span>
              <span v-if="group.status === 0" class="click-tip">点击查看详情</span>
            </div>
          </div>
        </a-list-item>
      </a-list>
    </a-spin>
  </a-modal>
</template>

<script>
import axios from 'axios';
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
        this.fetchMyGroups();
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
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
        Message.error('加载拼团信息失败');
      } finally {
        this.loadingGroups = false;
      }
    },
    getStatusType(status) {
      const types = ['orange', 'green', 'red'];
      return types[status] || 'gray';
    },
    getStatusText(status) {
      const texts = ['拼团中', '拼团成功', '拼团失败'];
      return texts[status] || '未知';
    },
    goToGroupDetail(group) {
      this.visible = false;
      this.$router.push(`/product/group/${group.id}`);
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      return new Date(timeStr).toLocaleString();
    },
    handleCancel() {
      this.visible = false;
    }
  }
}
</script>

<style scoped>
.group-item-card {
  background: #fdf5f5;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}
::v-deep .arco-list-item.group-item-card {
  padding: 15px;
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
  object-fit: cover;
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
</style>
