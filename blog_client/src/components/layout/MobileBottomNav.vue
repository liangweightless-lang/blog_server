<template>
  <div class="mobile-bottom-nav">
    <div class="nav-item" :class="{ active: $route.path === '/' }" @click="$router.push('/')">
      <icon-home />
      <span>首页</span>
    </div>
    <div v-if="showStore" class="nav-item" :class="{ active: $route.path === '/store' }" @click="$router.push('/store')">
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
  computed: {
    showStore() {
      return import.meta.env.VITE_SHOW_STORE !== 'false';
    }
  },
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
  bottom: 0;
  left: 0;
  right: 0;
  height: calc(60px + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.04);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  z-index: 99;
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #86909C;
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
  background: rgba(255, 75, 43, 0.08);
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
  color: var(--brand-primary);
}
.nav-item.active i {
  font-weight: 800;
  transform: translateY(-2px);
}
.nav-item.active::before {
  transform: translate(-50%, -50%) scale(1);
}
</style>
