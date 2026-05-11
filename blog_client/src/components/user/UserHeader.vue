<template>
  <div class="user-profile-header">
    <div class="header-content" v-if="user">
      <div class="user-info-main">
        <div class="avatar-wrapper">
          <img :src="user.avatarUrl || '/img/avatar.png'" class="user-avatar-big" />
        </div>
        <div class="user-text-info">
          <h2 class="user-nickname">{{ user.nickname || '未设置昵称' }}</h2>
          <p class="user-account-id">账号: {{ user.username }}</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="el-icon-edit-outline" circle size="small" @click="$emit('edit')"></el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserHeader',
  props: {
    user: Object
  }
}
</script>

<style scoped>
.user-profile-header {
  background: linear-gradient(135deg, #FF7E67 0%, #FF9E8D 100%);
  padding: 40px 20px 60px;
  color: white;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 600px;
  margin: 0 auto;
}
.user-info-main {
  display: flex;
  align-items: center;
  gap: 15px;
}
.user-avatar-big {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.3);
  object-fit: cover;
}
.user-nickname {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
}
.user-account-id {
  margin: 4px 0 0;
  font-size: 12px;
  opacity: 0.8;
}
</style>
