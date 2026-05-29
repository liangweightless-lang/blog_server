<template>
  <div class="mobile-header">
    <div class="logo" @click="$router.push('/')">小柴包</div>
    <div class="user-avatar" v-if="user" @click="$router.push('/profile')">
      <a-avatar :size="32">
        <img :src="user.avatarUrl" />
      </a-avatar>
    </div>
    <a-button v-else type="text" style="color: #FF7E67;" @click="triggerLogin">登录</a-button>
  </div>
</template>

<script>
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'

export default {
  name: 'MobileHeader',
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    user() {
      return this.userInfo;
    }
  },
  methods: {
    ...mapActions(useUserStore, ['clearUser']),
    handleDropdown(val) {
      if (val === 'logout') {
        this.clearUser();
        this.$router.push('/');
        window.location.reload();
      } else if (val === 'profile') {
        this.$router.push('/profile');
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
  height: calc(50px + env(safe-area-inset-top, 0px));
  background: rgba(250, 250, 250, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: env(safe-area-inset-top, 0px) 15px 0;
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
