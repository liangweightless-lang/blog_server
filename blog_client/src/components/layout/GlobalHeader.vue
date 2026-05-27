<template>
  <a-layout-header class="global-header">
    <div class="logo" @click="$router.push('/')">
      <icon-sun style="margin-right: 8px;" />小柴包
    </div>
    <div class="nav-menu">
      <a-menu :selected-keys="[$route.path]" mode="horizontal" style="background-color: transparent;" @menu-item-click="(key) => $router.push(key)">
        <a-menu-item key="/">品牌故事</a-menu-item>
        <a-menu-item key="/store">Ta的灵感橱窗</a-menu-item>
        <a-menu-item v-if="user && user.role === 'ADMIN'" key="/admin">后台管理</a-menu-item>
      </a-menu>
    </div>
    <div class="user-info">
      <template v-if="user">
        <a-tooltip :content="'专属邀请码: ' + user.inviteCode" position="bottom">
          <div class="points-tag">
            <icon-trophy /> {{ user.points }} 积分
          </div>
        </a-tooltip>
        <a-button type="text" style="margin-right: 15px; color: #8C6A5D;" @click="copyInviteLink">
          <template #icon><icon-share-alt /></template>
          邀请有礼
        </a-button>
        <a-dropdown @select="handleDropdown">
          <span class="dropdown-link">
            <a-avatar :size="32" style="margin-right: 8px; vertical-align: middle;">
              <img :src="user.avatarUrl" />
            </a-avatar>
            <span class="nickname">{{ user.nickname }}</span>
          </span>
          <template #content>
            <a-doption value="profile">个人资料</a-doption>
            <a-doption value="logout">退出登录</a-doption>
          </template>
        </a-dropdown>
      </template>
      <a-button v-else type="primary" size="small" shape="round" @click="loginDialogVisible = true">登录 / 注册</a-button>
    </div>
  </a-layout-header>
</template>

<script>
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'
import { Message } from '@arco-design/web-vue';

export default {
  name: 'GlobalHeader',
  data() {
    return {
      loginDialogVisible: false
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    user() {
      return this.userInfo;
    }
  },
  created() {
    // Check for invite code in URL
    const urlParams = new URLSearchParams(window.location.search);
    const invite = urlParams.get('invite');
    if (invite && !localStorage.getItem('token')) {
      this.loginDialogVisible = true;
    }
  },
  watch: {
    loginDialogVisible(val) {
      if (val) {
        window.dispatchEvent(new CustomEvent('open-login'));
        this.loginDialogVisible = false;
      }
    }
  },
  methods: {
    ...mapActions(useUserStore, ['clearUser']),
    handleDropdown(val) {
      if (val === 'profile') this.$router.push('/profile');
      if (val === 'logout') this.logout();
    },
    logout() {
      this.clearUser();
      Message.info('已退出登录');
      this.$router.push('/');
    },
    copyInviteLink() {
      const link = `${window.location.origin}/register?invite=${this.user.inviteCode}`;
      
      const fallbackCopy = (text) => {
        const textArea = document.createElement("textarea");
        textArea.value = text;
        textArea.style.position = "fixed";
        textArea.style.left = "-9999px";
        textArea.style.top = "-9999px";
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        try {
          document.execCommand('copy');
          document.body.removeChild(textArea);
          Message.success('邀请链接已复制');
        } catch (err) {
          document.body.removeChild(textArea);
          Message.error('复制失败，请手动复制');
        }
      };

      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(link).then(() => {
          Message.success('邀请链接已复制，快去发给好友吧！');
        }).catch(() => {
          fallbackCopy(link);
        });
      } else {
        fallbackCopy(link);
      }
    }
  }
}
</script>

<style scoped>
.global-header {
  background: #FFFFFF;
  color: #5C433B;
  line-height: 60px;
  border-bottom: 1px solid #FDF0E6;
  display: flex;
  align-items: center;
}

.logo {
  font-size: 20px;
  font-weight: 800;
  cursor: pointer;
  margin-right: 40px;
  letter-spacing: -0.5px;
  color: #FF7E67;
}

.nav-menu {
  flex-grow: 1;
}

.user-info {
  display: flex;
  align-items: center;
}

.points-tag {
  background: #FFF5F2;
  color: #FF7E67;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  margin-right: 16px;
  border: 1px solid #FFE4DE;
  cursor: help;
}

.dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.nickname {
  font-weight: 700;
  font-size: 14px;
  color: #5C433B;
}

::v-deep .arco-menu-horizontal {
  border-bottom: none !important;
}

::v-deep .arco-menu-item {
  font-size: 15px !important;
  font-weight: 600;
}

@media (max-width: 768px) {
  .global-header {
    padding: 0 15px;
  }

  .logo {
    font-size: 18px;
    margin-right: 15px;
  }

  .logo i {
    display: none;
  }

  ::v-deep .arco-menu-item {
    padding: 0 12px !important;
    font-size: 15px !important;
  }

  .points-tag {
    display: none;
  }
}
</style>
