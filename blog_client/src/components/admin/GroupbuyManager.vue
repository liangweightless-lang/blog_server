<template>
  <div class="groupbuy-manager">
    <a-table v-if="!isMobile" :data="groupbuys" :loading="loadingGroups" stripe style="margin-top: 20px;" :pagination="{ pageSize: 10 }">
      <template #columns>
        <a-table-column title="商品/发起人">
          <template #cell="{ record }">
            <div style="font-weight: bold; font-size: 15px; color: #333; margin-bottom: 4px;">{{ record.productName }}</div>
            <div style="font-size: 12px; color: #999;">发起人: {{ record.initiatorNickname }} | 截止: {{ formatTime(record.expireTime) }}</div>
          </template>
        </a-table-column>
        <a-table-column title="进度" :width="200">
          <template #cell="{ record }">
            <div style="font-size: 13px; margin-bottom: 5px;">{{ record.currentNum }} / {{ record.requiredNum }} 人</div>
            <a-progress 
              :percent="Math.min(record.currentNum / record.requiredNum, 1)" 
              :status="record.status === 1 ? 'success' : 'normal'"
              :show-text="false" 
              :stroke-width="8"
            ></a-progress>
          </template>
        </a-table-column>
        <a-table-column title="状态/操作" :width="180" fixed="right">
          <template #cell="{ record }">
            <a-tag :color="getStatusType(record.status)" size="small" style="margin-bottom: 5px; display: block; width: fit-content;">
              {{ getStatusText(record.status) }}
            </a-tag>
            <div v-if="record.status === 0">
              <a-button size="small" type="primary" status="success" @click="handleForceSuccess(record)" style="margin-right: 8px;">成团</a-button>
              <a-button size="small" type="primary" status="danger" @click="handleForceFail(record)">解散</a-button>
            </div>
          </template>
        </a-table-column>
      </template>
    </a-table>

    <!-- 移动端视图: 卡片列表 -->
    <div v-else class="mobile-card-list">
      <a-spin :loading="loadingGroups" style="width: 100%; display: block;">
        <div v-for="group in groupbuys" :key="group.id" class="mobile-card-item">
          <div class="card-header">
            <span class="group-title">{{ group.productName }}</span>
            <a-tag :color="getStatusType(group.status)" size="small" class="status-tag">
              {{ getStatusText(group.status) }}
            </a-tag>
          </div>
          <div class="card-body">
            <div class="card-info">
              <span class="label">发起人:</span>
              <span>{{ group.initiatorNickname }}</span>
            </div>
            <div class="card-info">
              <span class="label">截止时间:</span>
              <span class="time-text">{{ formatTime(group.expireTime) }}</span>
            </div>
            <div class="progress-box">
              <div class="progress-header">
                <span>拼团进度</span>
                <span class="progress-text">{{ group.currentNum }} / {{ group.requiredNum }} 人</span>
              </div>
              <a-progress 
                :percent="Math.min(group.currentNum / group.requiredNum, 1)" 
                :status="group.status === 1 ? 'success' : 'normal'"
                :show-text="false" 
                :stroke-width="8"
              ></a-progress>
            </div>
          </div>
          <div class="card-footer" v-if="group.status === 0">
            <a-button size="small" type="primary" status="success" @click="handleForceSuccess(group)" shape="round" style="flex: 1;">强制成团</a-button>
            <a-button size="small" type="primary" status="danger" @click="handleForceFail(group)" shape="round" style="flex: 1;">解散/退款</a-button>
          </div>
        </div>
        <a-empty v-if="groupbuys.length === 0 && !loadingGroups" description="暂无拼团记录" />
      </a-spin>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { Message, Modal } from '@arco-design/web-vue';

export default {
  name: 'GroupbuyManager',
  props: {
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      groupbuys: [],
      loadingGroups: false
    }
  },
  created() {
    this.fetchGroups();
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      return new Date(timeStr).toLocaleString();
    },
    async fetchGroups() {
      this.loadingGroups = true;
      try {
        const res = await axios.get('/api/groups', { headers: this.getAuthHeader() });
        this.groupbuys = res.data.data;
      } catch (error) {
        Message.error('加载拼团列表失败');
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
    handleForceSuccess(group) {
      Modal.confirm({
        title: '危险操作',
        content: '确定要手动强制该团【成团】吗？所有成员订单将变为待发货。',
        onOk: async () => {
          try {
            await axios.post(`/api/groups/${group.id}/force-success`, {}, { headers: this.getAuthHeader() });
            Message.success('操作成功，已强制成团');
            this.fetchGroups();
          } catch (e) {
            Message.error(e.response?.data?.message || '操作失败');
          }
        }
      });
    },
    handleForceFail(group) {
      Modal.confirm({
        title: '危险操作',
        content: '确定要【强制失败】该团吗？系统将自动取消订单并退回成员积分！',
        onOk: async () => {
          try {
            await axios.post(`/api/groups/${group.id}/force-fail`, {}, { headers: this.getAuthHeader() });
            Message.success('已强制失败并退款');
            this.fetchGroups();
          } catch (e) {
            Message.error(e.response?.data?.message || '操作失败');
          }
        }
      });
    }
  }
}
</script>

<style scoped>
/* 移动端卡片列表样式 */
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 15px;
}
.mobile-card-item {
  background: white;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #F0F0F0;
}
.group-title {
  font-weight: bold;
  font-size: 15px;
  color: #333;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-right: 8px;
}
.status-tag {
  flex-shrink: 0;
}
.card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}
.card-info {
  font-size: 13px;
  display: flex;
}
.card-info .label {
  color: #8C6A5D;
  margin-right: 8px;
}
.time-text {
  color: #FF7E67;
  font-weight: bold;
}
.progress-box {
  background: #FFFDF8;
  padding: 10px;
  border-radius: 8px;
  margin-top: 6px;
}
.progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}
.progress-text {
  font-weight: bold;
  color: #165DFF;
}
.card-footer {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}
</style>
