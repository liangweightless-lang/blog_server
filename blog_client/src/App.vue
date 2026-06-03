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
    
    // 监听授权成功事件，触发获取用户信息
    window.addEventListener('auth-success', this.fetchUser);
    window.addEventListener('refresh-user', this.fetchUser);
    
    this.fetchUser();

    // 拦截 Android 硬件返回键 / 侧滑返回手势
    const isCapacitor = typeof window !== 'undefined' && window.Capacitor;
    if (isCapacitor) {
      CapApp.addListener('backButton', () => {
        // 如果登录弹窗处于打开状态，仅关闭弹窗而不是退出应用或返回上一页
        if (this.loginDialogVisible) {
          this.loginDialogVisible = false;
          return;
        }
        
        // 定义顶级标签页路径，在这些页面点击返回键将退出应用
        const topLevelPaths = ['/', '/store', '/profile'];
        if (topLevelPaths.includes(this.$route.path)) {
          CapApp.exitApp();
        } else {
          // 否则返回上一级页面
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
    window.removeEventListener('refresh-user', this.fetchUser);
  },
  methods: {
    ...mapActions(useUserStore, ['fetchUser', 'clearUser']),
    handleAuthExpired() {
      this.clearUser();
      this.showLogin();
      // 可选：如果处于受保护的路由中，则重定向到首页
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
:root {
  /* 全局安全区高度变量，后续任何新页面都可以直接使用 var(--safe-top) */
  --safe-top: env(safe-area-inset-top, 0px);
  --safe-bottom: env(safe-area-inset-bottom, 0px);
  --brand-primary: #FF4B2B;
  --brand-gradient: linear-gradient(135deg, #FF4B2B 0%, #FF416C 100%);
  --glass-bg: rgba(255, 255, 255, 0.75);
  --glass-blur: blur(30px);
}

body {
  margin: 0;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", Helvetica, "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
  background: linear-gradient(180deg, #FAFAFA 0%, #F4F6F9 100%); /* 现代感柔和渐变背景 */
  background-attachment: fixed;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #1D2129; /* 深灰色以提高可读性 */
  letter-spacing: 0.2px;
  box-sizing: border-box;
}

*, *::before, *::after {
  box-sizing: inherit;
}

.main-content {
  padding: 20px 10px 100px 10px; /* 增强移动端内边距，底部留白以适配悬浮导航栏 */
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

/* ===== 全局移动端体验优化 ===== */
@media (max-width: 768px) {
  /* 调整 Toast 容器位置，避免被刘海/灵动岛遮挡 */
  .arco-message-list {
    top: max(60px, env(safe-area-inset-top, 60px)) !important;
  }

  /* 优化 Toast 提示：更大、更圆润、阴影更深 */
  .arco-message {
    padding: 12px 20px !important;
    border-radius: 30px !important;
    font-size: 14px !important;
    box-shadow: 0 8px 24px rgba(0,0,0,0.12) !important;
    background-color: rgba(255, 255, 255, 0.95) !important;
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
  }
  
  /* 解决长弹窗“不知道能不能滑”的问题：强制显示自定义滚动条 */
  .arco-modal-body::-webkit-scrollbar,
  .arco-drawer-body::-webkit-scrollbar {
    width: 4px;
    background-color: transparent;
  }
  .arco-modal-body::-webkit-scrollbar-thumb,
  .arco-drawer-body::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.15);
    border-radius: 10px;
  }
  .arco-modal-body::-webkit-scrollbar-thumb:active,
  .arco-drawer-body::-webkit-scrollbar-thumb:active {
    background-color: rgba(0, 0, 0, 0.3);
  }

  /* 解决弹窗在移动端超出屏幕宽度的问题，并避免居中偏移 */
  .arco-modal, .arco-modal-simple {
    width: auto !important;
    max-width: calc(100vw - 32px) !important;
    margin: 0 16px !important;
    box-sizing: border-box !important;
  }
  
  .arco-modal-body, .arco-modal-header {
    word-break: break-all !important;
  }

  /* 统一将部分特定弹窗在移动端转为底部抽屉式，并确保宽度100% */
  .checkout-modal,
  .buy-modal,
  .group-dialog,
  .custom-share-modal {
    width: 100% !important;
    max-width: 100% !important;
    position: absolute !important;
    bottom: 0 !important;
    margin: 0 !important;
    border-radius: 24px 24px 0 0 !important;
    padding-bottom: env(safe-area-inset-bottom) !important;
    animation: slideUpModal 0.4s cubic-bezier(0.25, 1, 0.5, 1);
  }
}

@keyframes slideUpModal {
  from { transform: translateY(100%); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>
