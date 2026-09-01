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
        <span class="stats-label">账户余额</span>
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
  margin: 12px 0 0;
  z-index: 10;
  position: relative;
}
.stats-bar-card {
  background: #FFFFFF;
  border-radius: 18px;
  display: flex;
  padding: 16px 0;
  box-shadow: 0 4px 20px rgba(17, 24, 39, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.02);
  margin: 0 auto;
}
.stats-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: transform 0.2s ease;
  position: relative;
}
.stats-item:active {
  transform: scale(0.95);
}
.stats-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 20%;
  height: 60%;
  width: 1px;
  background: #F2F3F5;
}
.stats-val {
  font-size: 18px;
  font-weight: 800;
  color: #1A1D20;
  letter-spacing: -0.5px;
}
.stats-label {
  font-size: 11px;
  color: #86909C;
  font-weight: 500;
}
</style>
