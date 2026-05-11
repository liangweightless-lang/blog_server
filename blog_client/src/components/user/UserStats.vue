<template>
  <div class="stats-bar-wrapper">
    <div class="stats-bar-card">
      <div class="stats-item">
        <span class="stats-val">{{ user ? user.points : 0 }}</span>
        <span class="stats-label">我的积分</span>
      </div>
      <div class="stats-item">
        <span class="stats-val">0</span>
        <span class="stats-label">优惠券</span>
      </div>
      <div class="stats-item">
        <span class="stats-val">¥0.00</span>
        <span class="stats-label">余额</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserStats',
  props: {
    user: Object
  }
}
</script>

<style scoped>
.stats-bar-wrapper {
  margin: -30px 15px 0;
  z-index: 10;
  position: relative;
}
.stats-bar-card {
  background: white;
  border-radius: 12px;
  display: flex;
  padding: 15px 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  max-width: 600px;
  margin: 0 auto;
}
.stats-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.stats-val {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}
.stats-label {
  font-size: 11px;
  color: #909399;
}
</style>
