<template>
  <div id="app" :class="{ 'header-hidden': isMobile && $route.meta.hideHeaderMobile }">
    <el-container direction="vertical">
      <GlobalHeader v-if="!isMobile" />
      <MobileHeader v-else-if="!$route.meta.hideHeaderMobile" />
      <el-main>
        <router-view></router-view>
      </el-main>
      <GlobalFooter v-if="!isMobile" />
      <template v-else>
        <div class="bottom-nav-spacer"></div>
        <MobileBottomNav />
      </template>
      
      <!-- 全局登录组件 -->
      <LoginDialog :show.sync="loginDialogVisible" />
    </el-container>
  </div>
</template>

<script>
import GlobalHeader from './components/layout/GlobalHeader.vue'
import MobileHeader from './components/layout/MobileHeader.vue'
import MobileBottomNav from './components/layout/MobileBottomNav.vue'
import GlobalFooter from './components/layout/GlobalFooter.vue'
import LoginDialog from './components/auth/LoginDialog.vue'

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
  created() {
    window.addEventListener('resize', this.handleResize);
    window.addEventListener('open-login', this.showLogin);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    window.removeEventListener('open-login', this.showLogin);
  },
  methods: {
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
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
  background-color: #FFFDF8; /* Vanilla Cream background */
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #5C433B; /* Cocoa Brown text */
  letter-spacing: 0.2px;
}
.el-main {
  padding: 30px 15px; /* Better mobile padding */
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}
.header-hidden .el-main {
  padding-top: 0;
}
</style>
