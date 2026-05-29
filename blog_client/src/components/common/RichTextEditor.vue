<template>
  <div class="xhs-rich-editor">
    <Toolbar
      style="border-bottom: 1px solid rgba(0,0,0,0.05);"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      :mode="mode"
    />
    <Editor
      style="height: 350px; overflow-y: hidden;"
      :modelValue="modelValue"
      @update:modelValue="handleChange"
      :defaultConfig="editorConfig"
      :mode="mode"
      @onCreated="handleCreated"
    />
  </div>
</template>

<script>
import '@wangeditor/editor/dist/css/style.css';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { shallowRef } from 'vue';

export default {
  name: 'RichTextEditor',
  components: { Editor, Toolbar },
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    customUpload: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      editorRef: shallowRef(null),
      mode: 'default',
      toolbarConfig: {
        toolbarKeys: [
          'fontSize',
          'bold',
          'numberedList',
          'emotion',
          'uploadImage'
        ]
      }
    };
  },
  computed: {
    editorConfig() {
      const config = {
        placeholder: '添加正文...'
      };
      if (this.customUpload) {
        config.MENU_CONF = {
          uploadImage: {
            customUpload: this.customUpload
          }
        };
      }
      return config;
    }
  },
  beforeUnmount() {
    const editor = this.editorRef;
    if (editor == null) return;
    editor.destroy();
  },
  methods: {
    handleCreated(editor) {
      this.editorRef = Object.seal(editor);
    },
    handleChange(val) {
      this.$emit('update:modelValue', val);
    }
  }
}
</script>

<style scoped>
/* Scoped styles can be added here if necessary */
</style>
