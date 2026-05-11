<template>
  <div class="hero-section">
    <div class="hero-overlay"></div>
    <div class="profile-content">
      <img src="/img/avatar.png" class="avatar" alt="Avatar" />
      <h1 class="author-name">创作者 Weightless</h1>
      <p class="author-bio">记录灵感，探索生活美学。在这里分享品牌的成长脉络，以及创作者的生活方式碎片。</p>
      <div class="social-links">
        <el-tag size="small" type="info">#生活方式</el-tag>
        <el-tag size="small" type="info">#独立品牌</el-tag>
        <el-tag size="small" type="info">#创作手记</el-tag>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProfileHero'
}
</script>

<style scoped>
.hero-section {
  position: relative;
  height: 340px;
  border-radius: 24px;
  background: linear-gradient(135deg, #FFE1D9 0%, #FFC1B6 100%);
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(255, 126, 103, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.hero-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image: radial-gradient(circle at center, rgba(255,255,255,0.4) 0%, transparent 70%);
}
.profile-content {
  position: relative;
  z-index: 1;
  color: #5C433B;
  animation: fadeIn 1s ease-out;
  padding: 0 20px;
}
.avatar {
  width: 105px;
  height: 105px;
  border-radius: 50%;
  border: 4px solid #FFFFFF;
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.25);
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  object-fit: cover;
  background-color: white;
}
.avatar:hover {
  transform: scale(1.08) rotate(5deg);
}
.author-name {
  margin: 20px 0 10px 0;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(255,255,255,0.5);
}
.author-bio {
  font-size: 15px;
  color: #8C6A5D;
  max-width: 600px;
  margin: 0 auto 20px auto;
  line-height: 1.6;
}
.social-links {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}
.social-links .el-tag {
  background: #FFF0ED;
  border: none;
  color: #FF7E67;
  border-radius: 20px;
  padding: 0 16px;
  font-weight: 700;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
@media (max-width: 768px) {
  .hero-section {
    height: auto;
    padding: 40px 15px;
  }
  .avatar {
    width: 80px;
    height: 80px;
  }
  .author-name {
    font-size: 24px;
  }
  .author-bio {
    font-size: 14px;
  }
  .social-links {
    flex-wrap: wrap;
  }
}
</style>
