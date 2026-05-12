<template>
  <div class="mobile-header">
    <div class="logo" @click="$router.push('/')">焙刻生活</div>
    <div class="user-avatar" v-if="user" @click="$router.push('/profile')">
      <el-avatar :size="32" :src="user.avatarUrl"></el-avatar>
    </div>
    <el-button v-else type="text" style="color: #FF7E67;" @click="triggerLogin">登录</el-button>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'MobileHeader',
  data() {
    return { user: null }
  },
  created() {
    this.checkUser();
    window.addEventListener('refresh-user', this.checkUser);
    window.addEventListener('user-updated', (e) => {
      this.user = e.detail;
    });
  },
  beforeDestroy() {
    window.removeEventListener('refresh-user', this.checkUser);
  },
  methods: {
    checkUser() {
      const token = localStorage.getItem('token');
      if (token && token !== 'undefined') {
        axios.get('/api/users/me', {
          headers: { 'Authorization': `Bearer ${token}` }
        }).then(res => {
          this.user = res.data.data;
        }).catch(() => {
          localStorage.removeItem('token');
          this.user = null;
        });
      } else {
        this.user = null;
      }
    },
    triggerLogin() {
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
