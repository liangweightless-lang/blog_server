<template>
  <div class="mobile-bottom-nav">
    <div class="nav-item" :class="{ active: $route.path === '/' }" @click="$router.push('/')">
      <i class="el-icon-house"></i>
      <span>首页</span>
    </div>
    <div class="nav-item" :class="{ active: $route.path === '/store' }" @click="$router.push('/store')">
      <i class="el-icon-shopping-bag-2"></i>
      <span>灵感</span>
    </div>
    <div class="nav-item" :class="{ active: $route.path === '/admin' }" @click="handleAdminClick">
      <i class="el-icon-user"></i>
      <span>我的</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MobileBottomNav',
  methods: {
    handleAdminClick() {
      const token = localStorage.getItem('token');
      if (token) {
        this.$router.push('/admin');
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
  height: 60px;
  background: #FFFFFF;
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid #FDF0E6;
  z-index: 2000;
  padding-bottom: env(safe-area-inset-bottom);
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8C6A5D;
  font-size: 11px;
}
.nav-item i {
  font-size: 22px;
  margin-bottom: 4px;
}
.nav-item.active {
  color: #FF7E67;
}
.nav-item.active i {
  font-weight: bold;
}
</style>
