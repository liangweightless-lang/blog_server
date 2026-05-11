<template>
  <div class="mobile-header">
    <div class="logo" @click="$router.push('/')">生活家</div>
    <div class="user-avatar" v-if="user" @click="$router.push('/admin')">
      <el-avatar :size="32" :src="user.avatarUrl"></el-avatar>
    </div>
    <el-button v-else type="text" style="color: #FF7E67;" @click="triggerLogin">登录</el-button>
  </div>
</template>

<script>
export default {
  name: 'MobileHeader',
  data() {
    return { user: null }
  },
  created() {
    window.addEventListener('user-updated', (e) => {
      this.user = e.detail;
    });
  },
  methods: {
    triggerLogin() {
      // Logic to trigger login dialog in GlobalHeader (since they share the same root mostly)
      // or we can use a global event.
      window.dispatchEvent(new CustomEvent('open-login'));
    }
  }
}
</script>

<style scoped>
.mobile-header {
  height: 50px;
  background: #FFF;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
  border-bottom: 1px solid #FDF0E6;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.logo {
  font-weight: 800;
  color: #FF7E67;
  font-size: 18px;
}
</style>
