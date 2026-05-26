<template>
  <div class="order-manager">
    <el-table :data="orders" v-loading="loadingOrders" stripe style="width: 100%; margin-top: 20px;">
      <el-table-column label="订单号" width="120">
        <template slot-scope="scope">
          <el-tooltip :content="scope.row.id" placement="top" :open-delay="300">
            <span style="display: inline-block; width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 11px; cursor: help;">#{{ scope.row.id }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="详情/地址">
        <template slot-scope="scope">
          <div style="font-weight: bold; font-size: 13px; color: #333; margin-bottom: 4px;">
            商品ID: {{ scope.row.productId }}
            <el-tag :type="scope.row.orderType === 'GROUP' ? 'warning' : 'primary'" size="mini" effect="plain" style="margin-left: 8px;">
              {{ scope.row.orderType === 'GROUP' ? '拼团' : '个买' }}
            </el-tag>
          </div>
          <div style="font-size: 12px; color: #999;">地址: {{ scope.row.shippingAddress || '无' }} | 时间: {{ formatTime(scope.row.createTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="实付" width="120">
        <template slot-scope="scope">
          <div style="font-weight: bold; color: #F56C6C;">¥{{ scope.row.amount }}</div>
          <div style="font-size: 11px; color: #999;" v-if="scope.row.pointsUsed">抵扣: {{ scope.row.pointsUsed }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态/操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-tag :type="getOrderStatusType(scope.row.status)" size="mini" style="margin-bottom: 5px; display: block; width: fit-content;">
            {{ getOrderStatusText(scope.row.status) }}
          </el-tag>
          <div v-if="scope.row.status === 1">
            <el-button size="mini" type="success" plain @click="handleShip(scope.row)">标记发货</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'OrderManager',
  data() {
    return {
      orders: [],
      loadingOrders: false
    }
  },
  created() {
    this.fetchOrders();
  },
  methods: {
    getAuthHeader() {
      return { 'Authorization': `Bearer ${localStorage.getItem('token')}` };
    },
    formatTime(timeStr) {
      return new Date(timeStr).toLocaleString();
    },
    async fetchOrders() {
      this.loadingOrders = true;
      try {
        const res = await axios.get('/api/orders', { headers: this.getAuthHeader() });
        this.orders = res.data.data;
      } catch (error) {
        this.$message.error('加载订单列表失败');
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
    async handleShip(order) {
      try {
        await axios.post(`/api/orders/${order.id}/ship`, {}, { headers: this.getAuthHeader() });
        this.$message.success('发货成功');
        this.fetchOrders();
      } catch (error) {
        this.$message.error('操作失败');
      }
    }
  }
}
</script>
