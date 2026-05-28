<template>
  <div>
    <a-modal 
      title="修改个人信息" 
      :visible="visible" 
      :width="isMobile ? '90%' : '500px'" 
      @cancel="handleCancel" 
      @ok="handleUpdate" 
      :ok-loading="updating">
      <a-form :model="profileForm" layout="vertical">
        <a-form-item label="头像">
          <a-upload
            :action="uploadAction"
            :show-file-list="false"
            @success="handleAvatarSuccess"
            @before-upload="beforeAvatarUpload">
            <template #upload-button>
              <div class="avatar-uploader-box">
                <a-avatar v-if="profileForm.avatarUrl" :size="80" class="edit-avatar">
                  <img :src="profileForm.avatarUrl" />
                </a-avatar>
                <icon-plus v-else class="avatar-uploader-icon" />
              </div>
            </template>
          </a-upload>
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model="profileForm.nickname" />
        </a-form-item>
        <a-form-item label="微信号">
          <a-input v-model="profileForm.wechatId" placeholder="方便后续沟通" />
        </a-form-item>
        <a-form-item label="收货地址">
          <div style="display: flex; gap: 10px; align-items: flex-start; width: 100%;">
            <a-textarea v-model="profileForm.address" placeholder="详细地址" style="flex: 1;" :auto-size="{minRows:2}" />
            <a-button type="primary" shape="circle" @click="openMapDialog" title="地图定位"><icon-location /></a-button>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 高德地图选点弹窗 -->
    <MapLocationDialog v-model:show="mapDialogVisible" @select="confirmMapLocation" />
  </div>
</template>

<script>
import axios from 'axios';
import { Message } from '@arco-design/web-vue';
import MapLocationDialog from '@/components/common/MapLocationDialog.vue';
import { mapState } from 'pinia'
import { useUserStore } from '@/stores/user'

export default {
  name: 'ProfileEditDialog',
  components: {
    MapLocationDialog
  },
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      updating: false,
      profileForm: {
        nickname: '',
        avatarUrl: '',
        wechatId: '',
        age: 18,
        gender: 'OTHER',
        address: ''
      },
      mapDialogVisible: false,
      isMobile: window.innerWidth <= 768
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    visible: {
      get() {
        return this.show;
      },
      set(val) {
        this.$emit('update:show', val);
      }
    },
    uploadAction() {
      const base = (axios.defaults.baseURL || '').replace(/\/$/, '');
      return base + '/api/files/upload';
    }
  },
  watch: {
    show(newVal) {
      if (newVal && this.userInfo) {
        this.profileForm = {
          nickname: this.userInfo.nickname,
          avatarUrl: this.userInfo.avatarUrl,
          wechatId: this.userInfo.wechatId || '',
          age: this.userInfo.age || 18,
          gender: this.userInfo.gender || 'OTHER',
          address: this.userInfo.address || ''
        };
      }
    }
  },
  created() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    async handleUpdate() {
      const token = localStorage.getItem('token');
      this.updating = true;
      try {
        await axios.put('/api/users/profile', this.profileForm, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        Message.success('个人信息更新成功');
        this.visible = false;
        this.$emit('updated');
        window.dispatchEvent(new CustomEvent('refresh-user'));
      } catch (error) {
        Message.error('更新失败');
      } finally {
        this.updating = false;
      }
    },
    handleCancel() {
      this.visible = false;
    },
    handleAvatarSuccess(fileItem) {
      const res = fileItem.response;
      if (typeof res === 'string' && (res.trim().startsWith('<!DOCTYPE') || res.trim().startsWith('<html'))) {
        Message.error('头像上传失败，服务器返回了错误的格式。');
        return;
      }
      let url = (res && res.url) ? res.url : (typeof res === 'string' ? res : '');
      if (url && (url.trim().startsWith('<!DOCTYPE') || url.trim().startsWith('<html'))) {
        Message.error('头像上传失败，服务器返回了错误的格式。');
        return;
      }
      this.profileForm.avatarUrl = url;
      Message.success('头像上传成功');
    },
    beforeAvatarUpload(file) {
      const isJPGorPNG = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPGorPNG) {
        Message.error('上传头像图片只能是 JPG 或 PNG 格式!');
      }
      if (!isLt2M) {
        Message.error('上传头像图片大小不能超过 2MB!');
      }
      return isJPGorPNG && isLt2M;
    },
    openMapDialog() {
      this.mapDialogVisible = true;
    },
    confirmMapLocation(address) {
      this.profileForm.address = address;
      this.mapDialogVisible = false;
    }
  }
}
</script>

<style scoped>
.avatar-uploader-box {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
  cursor: pointer;
}
.edit-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>
