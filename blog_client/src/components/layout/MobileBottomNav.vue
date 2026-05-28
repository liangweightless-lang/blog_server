<template>
  <div class="mobile-bottom-nav">
    <div class="nav-item" :class="{ active: $route.path === '/' }" @click="$router.push('/')">
      <icon-home />
      <span>首页</span>
    </div>
    <div class="nav-item" :class="{ active: $route.path === '/store' }" @click="$router.push('/store')">
      <icon-gift />
      <span>橱窗</span>
    </div>
    <div class="nav-item" :class="{ active: $route.path === '/profile' }" @click="handleProfileClick">
      <icon-user />
      <span>我的</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MobileBottomNav',
  methods: {
    handleProfileClick() {
      const token = localStorage.getItem('token');
      if (token) {
        this.$router.push('/profile');
      } else {
        window.dispatchEvent(new CustomEvent('open-login'));
      }
    }
  }
}
</script>

<style scoped>
.mobile-bottom-nav {
  position: fixed;
  bottom: 20px;
  left: 20px;
  right: 20px;
  height: 64px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-radius: 32px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.6);
  z-index: 99;
  padding: 0 10px;
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8C6A5D;
  font-size: 11px;
  flex: 1;
  height: 100%;
  border-radius: 24px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
}
.nav-item::before {
  content: '';
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%) scale(0);
  width: 50px;
  height: 50px;
  background: rgba(255, 126, 103, 0.1);
  border-radius: 50%;
  transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  z-index: -1;
}
.nav-item:active {
  transform: scale(0.9);
}
.nav-item i {
  font-size: 24px;
  margin-bottom: 2px;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.nav-item.active {
  color: #FF7E67;
}
.nav-item.active i {
  font-weight: bold;
  transform: translateY(-2px);
}
.nav-item.active::before {
  transform: translate(-50%, -50%) scale(1);
}
</style>
