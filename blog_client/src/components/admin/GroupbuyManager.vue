<template>
  <div class="groupbuy-manager">
    <el-table :data="groupbuys" v-loading="loadingGroups" stripe style="width: 100%; margin-top: 20px;">
      <el-table-column label="商品/发起人">
        <template slot-scope="scope">
          <div style="font-weight: bold; font-size: 15px; color: #333; margin-bottom: 4px;">{{ scope.row.productName }}</div>
          <div style="font-size: 12px; color: #999;">发起人: {{ scope.row.initiatorNickname }} | 截止: {{ formatTime(scope.row.expireTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="进度" width="200">
        <template slot-scope="scope">
          <div style="font-size: 13px; margin-bottom: 5px;">{{ scope.row.currentNum }} / {{ scope.row.requiredNum }} 人</div>
          <el-progress 
            :percentage="Math.min((scope.row.currentNum / scope.row.requiredNum) * 100, 100)" 
            :status="scope.row.status === 1 ? 'success' : null"
            :show-text="false" 
            :stroke-width="8"
          ></el-progress>
        </template>
      </el-table-column>
      <el-table-column label="状态/操作" width="180" fixed="right">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="mini" style="margin-bottom: 5px; display: block; width: fit-content;">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
          <div v-if="scope.row.status === 0">
            <el-button size="mini" type="success" plain @click="handleForceSuccess(scope.row)">成团</el-button>
            <el-button size="mini" type="danger" plain @click="handleForceFail(scope.row)">解散</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'GroupbuyManager',
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
      return new Date(timeStr).toLocaleString();
    },
    async fetchGroups() {
      this.loadingGroups = true;
      try {
        const res = await axios.get('/api/groups', { headers: this.getAuthHeader() });
        this.groupbuys = res.data.data;
      } catch (error) {
        this.$message.error('加载拼团列表失败');
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
    async handleForceSuccess(group) {
      try {
        await this.$confirm('确定要手动强制该团【成团】吗？所有成员订单将变为待发货。', '危险操作', { type: 'warning' });
        await axios.post(`/api/groups/${group.id}/force-success`, {}, { headers: this.getAuthHeader() });
        this.$message.success('操作成功，已强制成团');
        this.fetchGroups();
      } catch (e) {
        if (e !== 'cancel') this.$message.error(e.response?.data?.message || '操作失败');
      }
    },
    async handleForceFail(group) {
      try {
        await this.$confirm('确定要【强制失败】该团吗？系统将自动取消订单并退回成员积分！', '危险操作', { type: 'error' });
        await axios.post(`/api/groups/${group.id}/force-fail`, {}, { headers: this.getAuthHeader() });
        this.$message.success('已强制失败并退款');
        this.fetchGroups();
      } catch (e) {
        if (e !== 'cancel') this.$message.error(e.response?.data?.message || '操作失败');
      }
    }
  }
}
</script>
