<template>
  <a-layout id="app" :class="{ 'header-hidden': isMobile && (isLoggedIn || $route.meta.hideHeaderMobile) }">
    <GlobalHeader v-if="!isMobile" />
    <MobileHeader v-else-if="!isLoggedIn && !$route.meta.hideHeaderMobile" />
    <a-layout-content class="main-content">
      <router-view></router-view>
    </a-layout-content>
    <GlobalFooter v-if="!isMobile" />
    <template v-else-if="!$route.meta.hideBottomNav">
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
import { App as CapApp } from '@capacitor/app'

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

    // Intercept Android hardware Back button / swipe back gesture
    const isCapacitor = typeof window !== 'undefined' && window.Capacitor;
    if (isCapacitor) {
      CapApp.addListener('backButton', () => {
        // If login dialog is open, just close it instead of exiting or routing back
        if (this.loginDialogVisible) {
          this.loginDialogVisible = false;
          return;
        }
        
        // Define top-level tabs where back button should close/exit the App
        const topLevelPaths = ['/', '/store', '/profile'];
        if (topLevelPaths.includes(this.$route.path)) {
          CapApp.exitApp();
        } else {
          // Otherwise go back to the previous screen
          this.$router.back();
        }
      });
    }
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
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", Helvetica, "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
  background: linear-gradient(180deg, #FAFAFA 0%, #F4F6F9 100%); /* Modern soft gradient background */
  background-attachment: fixed;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #1D2129; /* Darker grey for better readability */
  letter-spacing: 0.2px;
  box-sizing: border-box;
}

*, *::before, *::after {
  box-sizing: inherit;
}

.main-content {
  padding: 20px 10px 100px 10px; /* Enhanced mobile padding, bottom padding accounts for floating nav */
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}
.header-hidden .main-content {
  padding-top: 0;
}
@media (min-width: 768px) {
  .main-content {
    padding: 30px 15px 40px 15px;
  }
}
</style>
