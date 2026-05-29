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
  background: var(--glass-bg, rgba(255, 255, 255, 0.85));
  backdrop-filter: var(--glass-blur, blur(16px));
  -webkit-backdrop-filter: var(--glass-blur, blur(16px));
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  display: flex;
  padding: 18px 0;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06), 0 2px 8px rgba(0,0,0,0.02);
  max-width: 600px;
  margin: 0 auto;
}
.stats-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
}
.stats-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 10%;
  height: 80%;
  width: 1px;
  background: linear-gradient(to bottom, transparent, rgba(0,0,0,0.05), transparent);
}
.stats-item:hover {
  transform: translateY(-2px);
}
.stats-item:hover .stats-val {
  background-position: 100% center;
}
.stats-val {
  font-weight: 800;
  font-size: 18px;
  background: linear-gradient(135deg, #FF7E67, #FFAE9B);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: background-position 0.3s ease;
}
.stats-label {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}
</style>
