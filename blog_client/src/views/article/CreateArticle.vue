<template>
  <div class="xhs-create-container">
    <!-- 顶部导航条 -->
    <div class="xhs-create-header">
      <div class="header-left" @click="$router.push('/')">
        <i class="el-icon-arrow-left"></i>
      </div>
      <div class="header-title">发布日记</div>
      <div class="header-right">
        <el-button class="publish-btn" type="primary" round size="small" :loading="submitting" @click="onSubmit">
          发布
        </el-button>
      </div>
    </div>

    <!-- 真实的上传照片区 -->
    <div class="xhs-upload-area">
      <el-upload
        action="/api/files/upload"
        list-type="picture-card"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :on-remove="handleRemove"
        :file-list="fileList"
        multiple
        accept="image/*,video/*"
      >
        <i class="el-icon-plus"></i>
      </el-upload>
      <div class="upload-tip" style="font-size: 12px; color: #D3C1BA; margin-top: 8px;">支持多张图片与短视频上传</div>
    </div>

    <!-- 标题与内容输入区 -->
    <div class="xhs-editor-area">
      <input class="xhs-title-input" v-model="form.title" placeholder="填写标题会有更多赞哦~" />
      <div class="xhs-divider"></div>
      <textarea class="xhs-content-input" v-model="form.content" placeholder="添加正文" rows="12"></textarea>
    </div>

    <!-- 小红书特色底部操作栏 -->
    <div class="xhs-bottom-actions">
      <div class="action-item"><span class="icon">#</span> 参与话题</div>
      <div class="action-item"><span class="icon">@</span> 提醒谁看</div>
      <div class="action-item"><i class="el-icon-location-outline"></i> 添加地点</div>
    </div>

    <!-- 关联商品选择 -->
    <div class="xhs-product-link">
      <div class="link-header">
        <i class="el-icon-shopping-bag-2"></i>
        <span>好物推荐</span>
      </div>
      <el-select v-model="form.productId" clearable placeholder="选择日记中提到的商品" style="width: 100%">
        <el-option
          v-for="item in products"
          :key="item.id"
          :label="item.name"
          :value="item.id">
          <span style="float: left">{{ item.name }}</span>
          <span style="float: right; color: #8492a6; font-size: 13px">¥{{ item.price }}</span>
        </el-option>
      </el-select>
      <p class="link-tip">关联后，读者将在文末直接看到购买入口</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'CreateArticle',
  data() {
    return {
      fileList: [],
      form: {
        title: '',
        content: '',
        coverUrl: '',
        mediaUrls: '[]',
        productId: null
      },
      products: [],
      submitting: false
    }
  },
  created() {
    this.fetchProducts();
  },
  methods: {
    handleUploadSuccess(res, file, fileList) {
      file.url = res; // res is the URL string from FileController
      this.fileList = fileList;
    },
    handleRemove(file, fileList) {
      this.fileList = fileList;
    },
    handleUploadError(err, file, fileList) {
      this.$message.error('文件上传失败，请重试');
    },
    async fetchProducts() {
      try {
        const res = await axios.get('/api/products');
        this.products = res.data;
      } catch (error) {
        console.error('获取商品列表失败');
      }
    },
    async onSubmit() {
      if (!this.form.title.trim()) {
        this.$message.warning('标题不能为空哦~')
        return
      }
      if (!this.form.content.trim()) {
        this.$message.warning('多写点正文吧~')
        return
      }
      
      const urls = this.fileList.map(f => f.response ? f.response : f.url);
      this.form.mediaUrls = JSON.stringify(urls);
      this.form.coverUrl = urls.length > 0 ? urls[0] : '';

      this.submitting = true
      try {
        const token = localStorage.getItem('token');
        const res = await axios.post('/api/articles', this.form, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (res.data) {
          this.$message.success('日记发布成功！')
          this.$router.push('/admin')
        } else {
          this.$message.error('日记发布失败，请稍后重试')
        }
      } catch (error) {
        this.$message.error(error.response?.data?.error || '日记发布失败，无管理员权限')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.xhs-create-container {
  background: #FFFFFF;
  border-radius: 24px;
  min-height: 80vh;
  padding: 30px;
  box-shadow: 0 8px 24px rgba(255, 126, 103, 0.08);
}

.xhs-create-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-left {
  font-size: 20px;
  cursor: pointer;
  color: #5C433B;
  width: 60px;
}
.header-title {
  font-size: 18px;
  font-weight: 800;
  color: #5C433B;
}
.header-right {
  width: 60px;
  text-align: right;
}
.publish-btn {
  background: #FF7E67;
  border: none;
  font-weight: bold;
}
.publish-btn:hover {
  background: #E56A54;
}

.xhs-upload-area {
  margin-bottom: 24px;
}
::v-deep .el-upload--picture-card {
  background: #FFFDF8;
  border: 1px dashed #FFC1B6;
  border-radius: 12px;
  width: 100px;
  height: 100px;
  line-height: 100px;
  color: #FF7E67;
  transition: all 0.3s;
}
::v-deep .el-upload--picture-card:hover {
  background: #FFF0ED;
}
::v-deep .el-upload--picture-card i {
  font-size: 24px;
}
::v-deep .el-upload-list--picture-card .el-upload-list__item {
  width: 100px;
  height: 100px;
  border-radius: 12px;
}

.xhs-editor-area {
  display: flex;
  flex-direction: column;
}

.xhs-title-input {
  border: none;
  font-size: 20px;
  font-weight: 700;
  color: #5C433B;
  padding: 10px 0;
  outline: none;
  background: transparent;
}
.xhs-title-input::placeholder {
  color: #D3C1BA;
  font-weight: 600;
}

.xhs-divider {
  height: 1px;
  background: #FDF0E6;
  margin: 10px 0;
}

.xhs-content-input {
  border: none;
  font-size: 16px;
  color: #8C6A5D;
  line-height: 1.6;
  padding: 10px 0;
  outline: none;
  resize: none;
  font-family: inherit;
  background: transparent;
}
.xhs-content-input::placeholder {
  color: #D3C1BA;
}

.xhs-bottom-actions {
  display: flex;
  gap: 16px;
  margin-top: 20px;
  border-top: 1px solid #FDF0E6;
  padding-top: 20px;
  flex-wrap: wrap;
}
.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #FFFDF8;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  color: #8C6A5D;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid #FDF0E6;
}
.action-item .icon {
  font-weight: 800;
  color: #FF7E67;
}
.action-item i {
  color: #FF7E67;
  font-size: 15px;
}

.xhs-product-link {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #FDF0E6;
}
.link-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  font-weight: bold;
  color: #FF7E67;
}
.link-tip {
  font-size: 12px;
  color: #D3C1BA;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .xhs-create-container {
    padding: 20px 15px;
    border-radius: 16px;
  }
}
</style>
