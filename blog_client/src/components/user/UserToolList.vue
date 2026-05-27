<template>
  <a-card class="section-card tools-section" :bordered="false" :body-style="{ padding: '15px' }">
    <div class="section-header">
      <span class="section-title">常用工具</span>
    </div>
    <div class="tools-list">
      <div class="tool-cell" @click="$emit('address')">
        <div class="cell-left">
          <icon-location class="tool-icon" style="color: #409EFF" />
          <span>收货地址</span>
        </div>
        <icon-right />
      </div>
      <div class="tool-cell" @click="$emit('invite')">
        <div class="cell-left">
          <icon-gift class="tool-icon" style="color: #F56C6C" />
          <span>邀请有礼</span>
        </div>
        <icon-right />
      </div>
      <div class="tool-cell" @click="$emit('groups')">
        <div class="cell-left">
          <icon-user-group class="tool-icon" style="color: #67C23A" />
          <span>我的拼团</span>
        </div>
        <icon-right />
      </div>

      <div class="tool-cell" @click="$emit('logout')">
        <div class="cell-left">
          <icon-poweroff class="tool-icon" style="color: #909399" />
          <span>退出登录</span>
        </div>
        <icon-right />
      </div>
    </div>
  </a-card>
</template>

<script>
export default {
  name: 'UserToolList',
  props: {
    user: Object
  }
}
</script>

<style scoped>
.section-card {
  background: transparent !important;
  margin: 15px auto;
  border-radius: 12px;
  max-width: 600px;
}
:deep(.arco-card-body) {
  padding: 15px;
  background: white;
  border-radius: 12px;
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
</style>
