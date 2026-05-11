<template>
  <el-footer class="global-footer" height="auto">
    <div class="footer-content">
      <div class="footer-links">
        <a href="javascript:void(0)">关于我们</a>
        <el-divider direction="vertical"></el-divider>
        <a href="javascript:void(0)">联系我们</a>
        <el-divider direction="vertical"></el-divider>
        <a href="javascript:void(0)">合作伙伴与客户评价</a>
        <el-divider direction="vertical"></el-divider>
        <a href="javascript:void(0)">免责声明</a>
        <el-divider direction="vertical"></el-divider>
        <a href="javascript:void(0)">用户协议</a>
        <el-divider direction="vertical"></el-divider>
        <a href="javascript:void(0)">隐私政策</a>
      </div>
      <div class="footer-bottom">
        <div class="copyright">
          &copy; {{ new Date().getFullYear() }} Weightless 独立创意工作室 版权所有
        </div>
        <div class="wechat-qr">
          <p>扫描二维码添加微信</p>
          <img src="/img/qr_code.png" alt="微信二维码" class="qr-image" />
        </div>
      </div>
    </div>
  </el-footer>
</template>

<script>
export default {
  name: 'GlobalFooter'
}
</script>

<style scoped>
.global-footer {
  background-color: #FFFDF8;
  border-top: 1px solid #FDF0E6;
  padding: 50px 20px 30px 20px;
  text-align: center;
  margin-top: 40px;
}
.footer-content {
  max-width: 1000px;
  margin: 0 auto;
}
.footer-links {
  margin-bottom: 30px;
}
.footer-links a {
  color: #8C6A5D;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}
.footer-links a:hover {
  color: #FF7E67;
}
.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed #FDF0E6;
  padding-top: 30px;
  color: #8C6A5D;
  font-size: 14px;
}
.wechat-qr {
  text-align: center;
}
.wechat-qr p {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #8C6A5D;
}
.qr-image {
  width: 90px;
  height: 90px;
  border-radius: 12px;
  border: 3px solid #FFE1D9;
  object-fit: contain;
  background-color: white;
}
@media (max-width: 768px) {
  .footer-bottom {
    flex-direction: column;
    gap: 20px;
  }
  .footer-links {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
    line-height: 1.8;
  }
  .footer-links .el-divider {
    display: none;
  }
}
</style>
