<template>
  <a-card class="section-card tools-section" :bordered="false" :body-style="{ padding: '15px' }">
    <div class="section-header">
      <span class="section-title">常用工具</span>
    </div>
    <div class="tools-list">
      <div class="tool-cell" @click="$emit('address')">
        <div class="cell-left">
          <div class="icon-wrapper" style="background: rgba(64, 158, 255, 0.1);">
            <icon-location class="tool-icon" style="color: #409EFF" />
          </div>
          <span>收货地址</span>
        </div>
        <icon-right class="cell-right-icon" />
      </div>
      <div class="tool-cell" @click="$emit('invite')">
        <div class="cell-left">
          <div class="icon-wrapper" style="background: rgba(245, 108, 108, 0.1);">
            <icon-gift class="tool-icon" style="color: #F56C6C" />
          </div>
          <span>邀请有礼</span>
        </div>
        <icon-right class="cell-right-icon" />
      </div>
      <div class="tool-cell" @click="$emit('groups')">
        <div class="cell-left">
          <div class="icon-wrapper" style="background: rgba(103, 194, 58, 0.1);">
            <icon-user-group class="tool-icon" style="color: #67C23A" />
          </div>
          <span>我的拼团</span>
        </div>
        <icon-right class="cell-right-icon" />
      </div>

      <div class="tool-cell" @click="$emit('logout')">
        <div class="cell-left">
          <div class="icon-wrapper" style="background: rgba(144, 147, 153, 0.1);">
            <icon-poweroff class="tool-icon" style="color: #909399" />
          </div>
          <span>退出登录</span>
        </div>
        <icon-right class="cell-right-icon" />
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
  font-size: 18px;
}
.icon-wrapper {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cell-left span {
  font-size: 14px;
  color: #1D2129;
  font-weight: 500;
}
.cell-right-icon {
  color: #C9CDD4;
}
</style>
