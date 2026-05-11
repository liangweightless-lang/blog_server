<template>
  <div id="app">
    <el-container direction="vertical">
      <GlobalHeader />
      <el-main>
        <router-view></router-view>
      </el-main>
      <GlobalFooter />
    </el-container>
  </div>
</template>

<script>
import GlobalHeader from './components/layout/GlobalHeader.vue'
import GlobalFooter from './components/layout/GlobalFooter.vue'

export default {
  name: 'App',
  components: {
    GlobalHeader,
    GlobalFooter
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
</style>
