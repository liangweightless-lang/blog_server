<template>
  <div id="app">
    <el-container>
      <el-header>
        <h2>Blog Server Dashboard</h2>
      </el-header>
      <el-main>
        <el-card>
          <div slot="header" class="clearfix">
            <span>Welcome to the Blog System</span>
            <el-button style="float: right; padding: 3px 0" type="text">Create Post</el-button>
          </div>
          <div>前端基础结构已搭载完毕 (Vue2 + Element UI)。</div>
        </el-card>
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
.el-main {
  padding: 20px;
}
</style>
