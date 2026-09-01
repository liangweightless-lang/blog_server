<template>
  <div class="luxury-tools-card">
    <div class="tools-card-header">
      <span class="tools-title">常用服务</span>
    </div>
    <div class="tools-list">
      <div class="tool-cell" @click="$emit('address')">
        <div class="cell-left">
          <div class="icon-wrapper bg-blue">
            <icon-location class="tool-icon" />
          </div>
          <span class="tool-name">收货地址管理</span>
        </div>
        <icon-right class="cell-right-icon" />
      </div>

      <div class="tool-cell" @click="$emit('invite')">
        <div class="cell-left">
          <div class="icon-wrapper bg-red">
            <icon-gift class="tool-icon" />
          </div>
          <span class="tool-name">邀请有礼 · 赚积分</span>
        </div>
        <icon-right class="cell-right-icon" />
      </div>

      <div class="tool-cell" @click="$emit('groups')">
        <div class="cell-left">
          <div class="icon-wrapper bg-green">
            <icon-user-group class="tool-icon" />
          </div>
          <span class="tool-name">我的拼团记录</span>
        </div>
        <icon-right class="cell-right-icon" />
      </div>

      <div v-if="user && user.role !== 'ADMIN' && user.role !== 'CREATOR'" class="tool-cell" @click="$emit('apply-creator')">
        <div class="cell-left">
          <div class="icon-wrapper bg-orange">
            <icon-star class="tool-icon" />
          </div>
          <span class="tool-name">申请成为小柴包主理人</span>
        </div>
        <icon-right class="cell-right-icon" />
      </div>

      <div class="tool-cell" @click="$emit('logout')">
        <div class="cell-left">
          <div class="icon-wrapper bg-gray">
            <icon-poweroff class="tool-icon" />
          </div>
          <span class="tool-name">安全退出登录</span>
        </div>
        <icon-right class="cell-right-icon" />
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
.luxury-tools-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 16px 18px 8px;
  margin-top: 14px;
  box-shadow: 0 4px 24px rgba(17, 24, 39, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.tools-card-header {
  margin-bottom: 12px;
}

.tools-title {
  font-size: 14px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.2px;
}

.tools-list {
  display: flex;
  flex-direction: column;
}

.tool-cell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  cursor: pointer;
  border-bottom: 1px solid #F7F8FA;
  transition: all 0.2s ease;
}
.tool-cell:last-child {
  border-bottom: none;
}
.tool-cell:active {
  transform: scale(0.98);
  opacity: 0.85;
}

.cell-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrapper {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.bg-blue { background: #E8F3FF; color: #165DFF; }
.bg-red { background: #FFECE8; color: #F53F3F; }
.bg-green { background: #E8FFEA; color: #00B42A; }
.bg-orange { background: #FFF7E8; color: #FF7D00; }
.bg-gray { background: #F2F3F5; color: #4E5969; }

.tool-name {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

.cell-right-icon {
  color: #C9CDD4;
  font-size: 14px;
}

@media (max-width: 768px) {
  .luxury-tools-card {
    border-radius: 16px;
    padding: 14px 14px 6px;
  }
  .tool-name {
    font-size: 13px;
  }
}
</style>
