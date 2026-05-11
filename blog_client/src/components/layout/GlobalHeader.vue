<template>
  <el-header>
    <div class="logo" @click="$router.push('/')">
      <i class="el-icon-sunny" style="margin-right: 8px;"></i>Weightless
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
        <el-button type="text" icon="el-icon-share" style="margin-right: 15px; color: #8C6A5D;" @click="copyInviteLink">邀请有礼</el-button>
        <el-dropdown>
          <span class="el-dropdown-link">
            <el-avatar :size="32" :src="user.avatarUrl" style="margin-right: 8px; vertical-align: middle;"></el-avatar>
            <span class="nickname">{{ user.nickname }}</span>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </template>
      <el-button v-else type="primary" size="small" round @click="loginDialogVisible = true">登录 / 注册</el-button>
    </div>

    <!-- 登录弹窗 -->
    <el-dialog title="登录 / 注册" :visible.sync="loginDialogVisible" width="360px" center append-to-body>
      <el-form :model="loginForm" label-position="top">
        <el-form-item label="账号">
          <el-input v-model="loginForm.username" placeholder="请输入任意账号名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="邀请码 (可选)">
          <el-input v-model="loginForm.inviteCode" placeholder="如果有邀请码请填写"></el-input>
        </el-form-item>
      </el-form>
      <div style="font-size: 12px; color: #909399; margin-bottom: 20px;">
        * 如果账号不存在，将自动为您创建新账号。
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="loginDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleLogin" :loading="loading">立即进入</el-button>
      </span>
    </el-dialog>
  </el-header>
</template>

<script>
import axios from 'axios';

export default {
  name: 'GlobalHeader',
  data() {
    return {
      loginDialogVisible: false,
      loading: false,
      user: null,
      loginForm: {
        username: '',
        password: '',
        inviteCode: ''
      }
    }
  },
  created() {
    this.checkUser();
    // Listen for manual refresh requests
    window.addEventListener('refresh-user', this.checkUser);
    
    // Check if there is an invite code in URL
    const urlParams = new URLSearchParams(window.location.search);
    const invite = urlParams.get('invite');
    if (invite) {
      this.loginForm.inviteCode = invite;
      if (!this.user) {
        this.loginDialogVisible = true;
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener('refresh-user', this.checkUser);
  },
  methods: {
    checkUser() {
      const token = localStorage.getItem('token');
      if (token) {
        axios.get('/api/users/me', {
          headers: { 'Authorization': `Bearer ${token}` }
        }).then(res => {
          this.user = res.data;
          window.dispatchEvent(new CustomEvent('user-updated', { detail: res.data }));
        }).catch(() => {
          localStorage.removeItem('token');
          this.user = null;
        });
      }
    },
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        return this.$message.warning('请填写完整信息');
      }
      this.loading = true;
      try {
        const res = await axios.post('/api/users/login', this.loginForm);
        localStorage.setItem('token', res.data.token);
        this.user = res.data.user;
        this.loginDialogVisible = false;
        this.$message.success('欢迎回来, ' + this.user.nickname);
        window.dispatchEvent(new CustomEvent('user-updated', { detail: this.user }));
      } catch (error) {
        this.$message.error(error.response?.data?.error || '登录失败');
      } finally {
        this.loading = false;
      }
    },
    logout() {
      localStorage.removeItem('token');
      this.user = null;
      this.$message.info('已退出登录');
      window.dispatchEvent(new CustomEvent('user-updated', { detail: null }));
    },
    copyInviteLink() {
      const link = `${window.location.origin}/?invite=${this.user.inviteCode}`;
      navigator.clipboard.writeText(link).then(() => {
        this.$message.success('邀请链接已复制到剪贴板！发给好友购买，您可获得积分奖励。');
      });
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
