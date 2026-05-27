<template>
  <div class="search-bar-container" :class="{ 'is-focused': isFocused }">
    <div class="search-input-wrapper">
      <icon-search class="search-icon" />
      <input 
        type="text" 
        v-model="query" 
        :placeholder="placeholder" 
        @focus="isFocused = true" 
        @blur="isFocused = false"
        @input="handleInput"
        class="search-input"
      />
      <icon-close-circle v-if="query" class="clear-icon" @click="clearSearch" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'SearchBar',
  props: {
    placeholder: {
      type: String,
      default: '搜索灵感、面包、咖啡...'
    }
  },
  data() {
    return {
      query: '',
      isFocused: false
    }
  },
  methods: {
    handleInput() {
      this.$emit('search', this.query);
    },
    clearSearch() {
      this.query = '';
      this.$emit('search', '');
    }
  }
}
</script>

<style scoped>
.search-bar-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background: #F5F5F5;
  border: 2px solid #EFEFEF;
  border-radius: 25px;
  padding: 8px 18px;
  gap: 10px;
  box-shadow: none;
  transition: all 0.3s ease;
}

.is-focused .search-input-wrapper {
  border-color: #FF7E67;
  box-shadow: 0 6px 16px rgba(255, 126, 103, 0.1);
  background: #FFFFFF;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #5C433B;
  font-weight: 500;
}

.search-input::placeholder {
  color: #D3C1BA;
}

.search-icon {
  font-size: 18px;
  color: #FF7E67;
  font-weight: bold;
}

.clear-icon {
  font-size: 18px;
  color: #D3C1BA;
  cursor: pointer;
  transition: color 0.3s;
}

.clear-icon:hover {
  color: #FF7E67;
}

@media (max-width: 768px) {
  .search-input-wrapper {
    padding: 6px 15px;
  }
}
</style>
