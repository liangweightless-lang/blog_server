<template>
  <div class="recommended-product" v-if="product && product.id" @click="handleClick">
    <div class="product-card-inner">
      <div class="product-badge">创作者推荐</div>
      <img :src="product.image" class="product-thumb" />
      <div class="product-info-mini">
        <h4 class="product-name-mini">{{ product.name }}</h4>
        <div class="product-price-row">
          <span class="product-price-mini">¥{{ product.price }}</span>
          <el-button type="primary" size="mini" round class="buy-now-btn">立即去买</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ArticleProductCard',
  props: {
    product: {
      type: Object,
      default: () => null
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.product);
    }
  }
}
</script>

<style scoped>
.recommended-product {
  margin: 30px 0;
  cursor: pointer;
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.recommended-product:hover {
  transform: translateY(-4px);
}
.product-card-inner {
  display: flex;
  background: #FFFDF8;
  border: 1px solid #FFE4D6;
  border-radius: 16px;
  padding: 16px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(255, 126, 103, 0.05);
}
.product-badge {
  position: absolute;
  top: 0;
  left: 0;
  background: #FF7E67;
  color: white;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 0 0 12px 0;
  font-weight: bold;
  z-index: 1;
}
.product-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 12px;
  margin-right: 15px;
}
.product-info-mini {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.product-name-mini {
  margin: 0 0 10px 0;
  font-size: 15px;
  color: #5C433B;
  font-weight: 700;
}
.product-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.product-price-mini {
  font-size: 18px;
  font-weight: 800;
  color: #FF7E67;
}
.buy-now-btn {
  background: #FF7E67;
  border: none;
  font-size: 12px;
  padding: 5px 15px;
}
</style>
