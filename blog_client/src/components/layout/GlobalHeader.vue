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
      </el-menu>
    </div>
  </el-header>
</template>

<script>
export default {
  name: 'GlobalHeader'
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
}
</style>
