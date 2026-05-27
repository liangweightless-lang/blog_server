<template>
  <a-layout id="app" :class="{ 'header-hidden': isMobile && (isLoggedIn || $route.meta.hideHeaderMobile) }">
    <GlobalHeader v-if="!isMobile" />
    <MobileHeader v-else-if="!isLoggedIn && !$route.meta.hideHeaderMobile" />
    <a-layout-content class="main-content">
      <router-view></router-view>
    </a-layout-content>
    <GlobalFooter v-if="!isMobile" />
    <template v-else>
      <div class="bottom-nav-spacer"></div>
      <MobileBottomNav />
    </template>
    
    <!-- 全局登录组件 -->
    <LoginDialog :show="loginDialogVisible" @update:show="val => loginDialogVisible = val" />
  </a-layout>
</template>

<script>
import GlobalHeader from './components/layout/GlobalHeader.vue'
import MobileHeader from './components/layout/MobileHeader.vue'
import MobileBottomNav from './components/layout/MobileBottomNav.vue'
import GlobalFooter from './components/layout/GlobalFooter.vue'
import LoginDialog from './components/auth/LoginDialog.vue'
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'

export default {
  name: 'App',
  components: {
    GlobalHeader,
    MobileHeader,
    MobileBottomNav,
    GlobalFooter,
    LoginDialog
  },
  data() {
    return {
      loginDialogVisible: false,
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo', 'isLoggedIn']),
    user() {
      return this.userInfo;
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
    window.addEventListener('open-login', this.showLogin);
    window.addEventListener('auth-expired', this.handleAuthExpired);
    
    // Auth success event to trigger fetchUser
    window.addEventListener('auth-success', this.fetchUser);
    
    this.fetchUser();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    window.removeEventListener('open-login', this.showLogin);
    window.removeEventListener('auth-expired', this.handleAuthExpired);
    window.removeEventListener('auth-success', this.fetchUser);
  },
  methods: {
    ...mapActions(useUserStore, ['fetchUser', 'clearUser']),
    handleAuthExpired() {
      this.clearUser();
      this.showLogin();
      // Optional: if on a protected route, redirect to home
      if (this.$route.path === '/profile' || this.$route.path.startsWith('/admin')) {
        this.$router.push('/');
      }
    },
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    showLogin() {
      this.loginDialogVisible = true;
    }
  }
}
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", Helvetica, "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
  background-color: #FAFAFA; /* Modern clean background */
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #333333; /* Darker grey for better readability */
  letter-spacing: 0.2px;
  box-sizing: border-box;
}

*, *::before, *::after {
  box-sizing: inherit;
}

.main-content {
  padding: 30px 15px; /* Better mobile padding */
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}
.header-hidden .main-content {
  padding-top: 0;
}
</style>
