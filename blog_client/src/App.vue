<template>
  <div id="app">
    <el-container>
      <el-header>
        <div class="logo" @click="$router.push('/')">博客管理后台</div>
      </el-header>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
body {
  margin: 0;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  background-color: #f0f2f5;
}
.el-header {
  background-color: #409EFF;
  color: white;
  line-height: 60px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  cursor: pointer;
}
.el-main {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}
</style>
