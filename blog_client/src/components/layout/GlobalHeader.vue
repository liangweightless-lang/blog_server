<template>
  <el-header>
    <div class="logo" @click="$router.push('/')">
      <i class="el-icon-sunny" style="margin-right: 8px;"></i>焙刻生活
    </div>
    <div class="nav-menu">
      <el-menu :default-active="$route.path" mode="horizontal" background-color="transparent" text-color="#8C6A5D"
        active-text-color="#FF7E67" router>
        <el-menu-item index="/">品牌故事</el-menu-item>
        <el-menu-item index="/store">Ta的灵感橱窗</el-menu-item>
        <el-menu-item v-if="user && user.role === 'ADMIN'" index="/admin">后台管理</el-menu-item>
      </el-menu>
    </div>
    <div class="user-info">
      <template v-if="user">
        <el-tooltip class="item" effect="dark" :content="'专属邀请码: ' + user.inviteCode" placement="bottom">
          <div class="points-tag">
            <i class="el-icon-coin"></i> {{ user.points }} 积分
          </div>
        </el-tooltip>
        <el-button type="text" icon="el-icon-share" style="margin-right: 15px; color: #8C6A5D;"
          @click="copyInviteLink">邀请有礼</el-button>
        <el-dropdown>
          <span class="el-dropdown-link">
            <el-avatar :size="32" :src="user.avatarUrl" style="margin-right: 8px; vertical-align: middle;"></el-avatar>
            <span class="nickname">{{ user.nickname }}</span>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="$router.push('/profile')">个人资料</el-dropdown-item>
            <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </template>
      <el-button v-else type="primary" size="small" round @click="loginDialogVisible = true">登录 / 注册</el-button>
    </div>
  </el-header>
</template>

<script>
import axios from 'axios';

export default {
  name: 'GlobalHeader',
  data() {
    return {
      loginDialogVisible: false,
      user: null
    }
  },
  created() {
    this.checkUser();
    window.addEventListener('refresh-user', this.checkUser);
    window.addEventListener('user-updated', (e) => {
      this.user = e.detail;
    });

    // Check for invite code in URL
    const urlParams = new URLSearchParams(window.location.search);
    const invite = urlParams.get('invite');
    if (invite && !localStorage.getItem('token')) {
      this.loginDialogVisible = true;
    }
  },
  watch: {
    // Sync local loginDialogVisible with global one if needed
    // But since we use events, we can just dispatch open-login
    loginDialogVisible(val) {
      if (val) {
        window.dispatchEvent(new CustomEvent('open-login'));
        this.loginDialogVisible = false; // Reset local state
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener('refresh-user', this.checkUser);
  },
  methods: {
    checkUser() {
      const token = localStorage.getItem('token');
      if (token && token !== 'undefined' && token !== 'null') {
        axios.get('/api/users/me', {
          headers: { 'Authorization': `Bearer ${token}` }
        }).then(res => {
          if (res.data && res.data.data) {
            this.user = res.data.data;
            window.dispatchEvent(new CustomEvent('user-updated', { detail: res.data.data }));
          }
        }).catch(() => {
          localStorage.removeItem('token');
          this.user = null;
        });
      }
    },
    logout() {
      localStorage.removeItem('token');
      this.user = null;
      this.$message.info('已退出登录');
      window.dispatchEvent(new CustomEvent('user-updated', { detail: null }));
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
          this.$message.success('邀请链接已复制');
        } catch (err) {
          document.body.removeChild(textArea);
          this.$message.error('复制失败，请手动复制');
        }
      };

      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(link).then(() => {
          this.$message.success('邀请链接已复制，快去发给好友吧！');
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
.el-header {
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

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.nickname {
  font-weight: 700;
  font-size: 14px;
  color: #5C433B;
}

::v-deep .el-menu.el-menu--horizontal {
  border-bottom: none !important;
}

::v-deep .el-menu-item {
  font-size: 15px !important;
  font-weight: 600;
}

@media (max-width: 768px) {
  .el-header {
    padding: 0 15px;
  }

  .logo {
    font-size: 18px;
    margin-right: 15px;
  }

  .logo i {
    display: none;
  }

  ::v-deep .el-menu-item {
    padding: 0 12px !important;
    font-size: 15px !important;
  }

  .points-tag {
    display: none;
  }
}
</style>
