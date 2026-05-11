<template>
  <div class="section-card tools-section">
    <div class="section-header">
      <span class="section-title">常用工具</span>
    </div>
    <div class="tools-list">
      <div class="tool-cell" @click="$emit('address')">
        <div class="cell-left">
          <i class="el-icon-location-outline tool-icon" style="color: #409EFF"></i>
          <span>收货地址</span>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>
      <div class="tool-cell" @click="$emit('invite')">
        <div class="cell-left">
          <i class="el-icon-present tool-icon" style="color: #F56C6C"></i>
          <span>邀请有礼</span>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>
      <div class="tool-cell" v-if="user && user.role === 'ADMIN'" @click="$router.push('/admin')">
        <div class="cell-left">
          <i class="el-icon-set-up tool-icon" style="color: #E6A23C"></i>
          <span>管理后台</span>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>
      <div class="tool-cell" @click="$emit('logout')">
        <div class="cell-left">
          <i class="el-icon-switch-button tool-icon" style="color: #909399"></i>
          <span>退出登录</span>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>
    </div>
  </div>
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
  background: white;
  margin: 15px;
  border-radius: 12px;
  padding: 15px;
  max-width: 600px;
  margin: 15px auto;
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
