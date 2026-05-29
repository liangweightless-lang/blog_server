<template>
  <a-modal
    v-model:visible="visible"
    title="裁剪图片"
    @cancel="handleCancel"
    @ok="handleConfirm"
    :width="'90%'"
    :style="{ maxWidth: '500px' }"
    unmount-on-close
  >
    <div class="crop-container" :key="cropKey" v-if="imageUrl" style="text-align: center; background: #f5f5f5; border-radius: 8px; overflow: hidden; height: 350px; width: 100%; position: relative;">
      <img ref="cropImageRef" :src="imageUrl" style="width: 100%; height: 100%; object-fit: contain;" />
    </div>
    <div class="crop-toolbar" style="margin-top: 16px; display: flex; gap: 8px; flex-wrap: wrap; justify-content: center;">
      <a-button @click="setCropAspectRatio(NaN)" size="small">自由比例</a-button>
      <a-button @click="setCropAspectRatio(1)" size="small">1:1</a-button>
      <a-button @click="setCropAspectRatio(3/4)" size="small">3:4</a-button>
      <a-button @click="setCropAspectRatio(16/9)" size="small">16:9</a-button>
      <a-button @click="rotateCrop(-90)" size="small"><icon-undo />左转</a-button>
      <a-button @click="rotateCrop(90)" size="small"><icon-redo />右转</a-button>
      <a-button @click="resetCrop" size="small">重置</a-button>
    </div>
  </a-modal>
</template>

<script>
import Cropper from 'cropperjs';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'ImageCropperDialog',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    imageUrl: {
      type: String,
      default: ''
    },
    currentFile: {
      type: File,
      default: null
    }
  },
  data() {
    return {
      cropKey: 0,
      cropperInstance: null
    };
  },
  computed: {
    visible: {
      get() {
        return this.show;
      },
      set(val) {
        this.$emit('update:show', val);
      }
    }
  },
  watch: {
    visible(newVal) {
      if (newVal && this.imageUrl) {
        this.initCropperWhenReady();
      } else {
        this.destroyCropper();
      }
    }
  },
  beforeUnmount() {
    this.destroyCropper();
  },
  methods: {
    destroyCropper() {
      if (this.cropperInstance) {
        this.cropperInstance.destroy();
        this.cropperInstance = null;
      }
    },
    initCropperWhenReady() {
      this.$nextTick(() => {
        const image = this.$refs.cropImageRef;
        if (!image) return;
        
        const initCropper = () => {
          this.destroyCropper();
          // Timeout needed because a-modal takes ~300ms to open.
          // Cropperjs v2 needs the final container dimensions.
          setTimeout(() => {
            if (this.$refs.cropImageRef) {
               this.cropperInstance = new Cropper(image, {
                 template: `
                  <cropper-canvas background>
                    <cropper-image rotatable scalable skewable translatable></cropper-image>
                    <cropper-shade hidden></cropper-shade>
                    <cropper-handle action="select" plain></cropper-handle>
                    <cropper-selection initial-coverage="1" movable resizable>
                      <cropper-grid role="grid" bordered covered></cropper-grid>
                      <cropper-crosshair centered></cropper-crosshair>
                      <cropper-handle action="move" theme-color="rgba(255, 255, 255, 0.35)"></cropper-handle>
                      <cropper-handle action="n-resize"></cropper-handle>
                      <cropper-handle action="e-resize"></cropper-handle>
                      <cropper-handle action="s-resize"></cropper-handle>
                      <cropper-handle action="w-resize"></cropper-handle>
                      <cropper-handle action="ne-resize"></cropper-handle>
                      <cropper-handle action="nw-resize"></cropper-handle>
                      <cropper-handle action="se-resize"></cropper-handle>
                      <cropper-handle action="sw-resize"></cropper-handle>
                    </cropper-selection>
                  </cropper-canvas>
                 `
               });
            }
          }, 350);
        };
        
        if (image.complete) {
          initCropper();
        } else {
          image.onload = initCropper;
        }
      });
    },
    setCropAspectRatio(ratio) {
      if (this.cropperInstance) {
        const selection = this.cropperInstance.getCropperSelection();
        if (selection) selection.aspectRatio = ratio;
      }
    },
    rotateCrop(degree) {
      if (this.cropperInstance) {
        const image = this.cropperInstance.getCropperImage();
        if (image) image.$rotate(`${degree}deg`);
      }
    },
    resetCrop() {
      this.destroyCropper();
      this.cropKey += 1;
      this.initCropperWhenReady();
    },
    async handleConfirm() {
      if (!this.cropperInstance) {
        this.$emit('confirm', this.currentFile);
        return;
      }
      try {
        const selection = this.cropperInstance.getCropperSelection();
        if (!selection) throw new Error('No selection');
        const canvas = await selection.$toCanvas();
        canvas.toBlob((blob) => {
          if (!blob) {
            Message.error('裁剪失败');
            this.handleCancel();
            return;
          }
          const croppedFile = new File([blob], (this.currentFile && this.currentFile.name) || 'cropped.jpg', {
            type: blob.type || 'image/jpeg',
            lastModified: Date.now()
          });
          this.$emit('confirm', croppedFile);
        }, (this.currentFile && this.currentFile.type) || 'image/jpeg', 0.9);
      } catch (e) {
        Message.error('裁剪失败');
        this.handleCancel();
      }
    },
    handleCancel() {
      this.$emit('cancel');
    }
  }
}
</script>

<style scoped>
:deep(cropper-canvas) {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
