<template>
  <div class="mobile-bottom-nav">
    <div 
      class="nav-item" 
      :class="{ active: $route.path === '/' }" 
      @click="navTo('/')"
    >
      <icon-home class="nav-icon" />
      <span class="nav-label">首页</span>
    </div>

    <div 
      v-if="showStore" 
      class="nav-item" 
      :class="{ active: $route.path === '/store' }" 
      @click="navTo('/store')"
    >
      <icon-gift class="nav-icon" />
      <span class="nav-label">橱窗</span>
    </div>

    <div 
      class="nav-item" 
      :class="{ active: $route.path === '/profile' }" 
      @click="handleProfileClick"
    >
      <icon-user class="nav-icon" />
      <span class="nav-label">我的</span>
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
    navTo(path) {
      if (this.$route.path === path) {
        // 如果已经在当前页，平滑滚回顶部
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
      }
      this.$router.push(path);
    },
    handleProfileClick() {
      const token = localStorage.getItem('token');
      if (token) {
        this.navTo('/profile');
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
  height: calc(56px + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  z-index: 999;
  user-select: none;
  -webkit-user-select: none;
  -webkit-touch-callout: none;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #86909C;
  flex: 1;
  height: 100%;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition: color 0.15s ease, transform 0.1s ease;
}

.nav-item:active {
  transform: scale(0.92);
  color: #FF7E67;
}

.nav-icon {
  font-size: 22px;
  margin-bottom: 2px;
  transition: transform 0.15s ease;
}

.nav-label {
  font-size: 11px;
  font-weight: 500;
  line-height: 1.2;
}

.nav-item.active {
  color: #FF4B2B;
}

.nav-item.active .nav-icon {
  transform: scale(1.1);
}

.nav-item.active .nav-label {
  font-weight: 700;
}
</style>
