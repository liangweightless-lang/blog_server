<template>
  <div class="search-bar-luxury" :class="{ 'is-focused': isFocused }">
    <div class="search-capsule">
      <icon-search class="search-lens-icon" />
      
      <!-- 动态热搜词滚动轮播 (用户未输入且未聚焦时平滑上下切换) -->
      <div class="ticker-box" v-if="!query && !isFocused">
        <transition name="ticker-slide" mode="out-in">
          <span :key="currentTickerIdx" class="ticker-text" @click="applyTicker">
            {{ hotKeywords[currentTickerIdx] }}
          </span>
        </transition>
      </div>

      <input 
        type="text" 
        ref="inputRef"
        v-model="query" 
        :placeholder="isFocused ? '输入你想探索的灵感...' : ''" 
        @focus="isFocused = true" 
        @blur="isFocused = false"
        @input="handleInput"
        @keyup.enter="handleInput"
        class="search-text-input"
      />

      <icon-close-circle-fill v-if="query" class="clear-lens-icon" @click="clearSearch" />
      
      <button class="search-action-pill" v-if="query" @click="handleInput">
        <span>搜索</span>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SearchBar',
  data() {
    return {
      query: '',
      isFocused: false,
      currentTickerIdx: 0,
      tickerTimer: null,
      hotKeywords: [
        '搜索 “法式特浓提拉米苏”',
        '探索 “手作烘焙灵感”',
        '查看 “社区快团甄选”',
        '搜索 “独立咖啡品牌”',
        '发现 “主理人手作日记”'
      ]
    }
  },
  mounted() {
    this.startTicker();
  },
  beforeUnmount() {
    this.stopTicker();
  },
  methods: {
    startTicker() {
      this.stopTicker();
      this.tickerTimer = setInterval(() => {
        this.currentTickerIdx = (this.currentTickerIdx + 1) % this.hotKeywords.length;
      }, 3500);
    },
    stopTicker() {
      if (this.tickerTimer) {
        clearInterval(this.tickerTimer);
        this.tickerTimer = null;
      }
    },
    applyTicker() {
      const keyword = this.hotKeywords[this.currentTickerIdx].replace(/^搜索 |^探索 |^查看 |^发现 |[“”]/g, '');
      this.query = keyword;
      this.$emit('search', this.query);
      this.isFocused = true;
      this.$nextTick(() => {
        this.$refs.inputRef?.focus();
      });
    },
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
.search-bar-luxury {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.search-capsule {
  display: flex;
  align-items: center;
  position: relative;
  background: #FFFFFF;
  border-radius: 22px;
  padding: 6px 14px 6px 16px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 4px 20px rgba(17, 24, 39, 0.03), 0 1px 3px rgba(0, 0, 0, 0.01);
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  height: 44px;
}

.search-bar-luxury.is-focused .search-capsule {
  border-color: rgba(255, 94, 58, 0.35);
  box-shadow: 0 6px 24px rgba(255, 94, 58, 0.12), 0 0 0 3px rgba(255, 94, 58, 0.06);
  transform: translateY(-1px);
}

.search-lens-icon {
  color: #86909C;
  font-size: 16px;
  margin-right: 10px;
  flex-shrink: 0;
  transition: color 0.2s ease;
}

.search-bar-luxury.is-focused .search-lens-icon {
  color: #FF5E3A;
}

/* 热搜词轮播 */
.ticker-box {
  position: absolute;
  left: 42px;
  right: 60px;
  height: 100%;
  display: flex;
  align-items: center;
  pointer-events: auto;
  cursor: text;
  overflow: hidden;
}

.ticker-text {
  font-size: 13px;
  color: #86909C;
  font-weight: 500;
  letter-spacing: -0.2px;
  white-space: nowrap;
}

.ticker-slide-enter-active,
.ticker-slide-leave-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.ticker-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.ticker-slide-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

.search-text-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #1D2129;
  font-weight: 600;
  outline: none;
  padding: 0;
  height: 100%;
  z-index: 2;
}

.search-text-input::placeholder {
  color: #C9CDD4;
  font-weight: 500;
}

.clear-lens-icon {
  color: #C9CDD4;
  font-size: 16px;
  cursor: pointer;
  margin-right: 6px;
  transition: color 0.2s ease;
  z-index: 3;
}
.clear-lens-icon:hover {
  color: #86909C;
}

.search-action-pill {
  background: linear-gradient(135deg, #FF5E3A 0%, #FF2A54 100%);
  color: #FFFFFF;
  border: none;
  font-size: 12px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 14px;
  cursor: pointer;
  z-index: 3;
  box-shadow: 0 2px 8px rgba(255, 42, 84, 0.25);
  transition: transform 0.15s ease;
}
.search-action-pill:active {
  transform: scale(0.94);
}

@media (max-width: 768px) {
  .search-capsule {
    height: 40px;
    padding: 4px 12px;
    border-radius: 20px;
  }
  .ticker-box {
    left: 36px;
  }
  .ticker-text {
    font-size: 12px;
  }
  .search-text-input {
    font-size: 13px;
  }
}
</style>
