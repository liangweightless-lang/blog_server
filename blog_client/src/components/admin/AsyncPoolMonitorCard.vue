<template>
  <div class="async-pool-card">
    <!-- 头部区域 -->
    <div class="card-header">
      <div class="header-title-box">
        <div class="icon-wrap">
          <icon-thunderbolt class="head-icon" />
        </div>
        <div class="title-meta">
          <div class="title-row">
            <h3 class="card-title">异步任务线程池监控</h3>
            <span class="tech-pill">Spring @Async</span>
          </div>
          <p class="card-subtitle">负责微信通知发送、企业微信机器人推送及耗时后台计算</p>
        </div>
      </div>

      <div class="header-actions">
        <!-- 健康状态指示灯 -->
        <div class="health-badge" :class="getHealthClass">
          <span class="pulse-dot"></span>
          <span class="health-text">{{ getHealthText }}</span>
        </div>

        <!-- 刷新按钮 -->
        <a-button 
          size="small" 
          type="outline" 
          shape="round" 
          :loading="loading" 
          @click="fetchStatus"
          class="refresh-btn"
        >
          <template #icon><icon-refresh /></template>
          刷新
        </a-button>
      </div>
    </div>

    <!-- 4 宫格核心指标区 -->
    <div class="metrics-grid">
      <!-- 1. 线程活跃数 -->
      <div class="metric-item">
        <div class="metric-label">
          <span>活跃线程 / 最大容量</span>
          <a-tooltip content="当前正在执行异步任务的工作线程数与线程池最大上限">
            <icon-info-circle class="info-icon" />
          </a-tooltip>
        </div>
        <div class="metric-value-row">
          <span class="metric-value">{{ statusData.activeCount ?? 0 }}</span>
          <span class="metric-unit">/ {{ statusData.maxPoolSize ?? 16 }}</span>
        </div>
        <div class="metric-progress-box">
          <a-progress 
            :percent="calcUsagePercent" 
            :color="getProgressColor" 
            :show-text="false" 
            size="small" 
          />
        </div>
        <div class="metric-subtext">
          核心线程: {{ statusData.corePoolSize ?? 8 }} · 利用率: {{ statusData.usagePercent ?? 0 }}%
        </div>
      </div>

      <!-- 2. 缓冲队列排队 -->
      <div class="metric-item">
        <div class="metric-label">
          <span>等待队列排队中</span>
          <a-tooltip content="线程池满负荷时进入队列缓冲的任务数量">
            <icon-info-circle class="info-icon" />
          </a-tooltip>
        </div>
        <div class="metric-value-row">
          <span class="metric-value" :class="{ 'text-warn': (statusData.queueSize || 0) > 0 }">
            {{ statusData.queueSize ?? 0 }}
          </span>
          <span class="metric-unit">/ {{ statusData.queueCapacity ?? 500 }}</span>
        </div>
        <div class="queue-status-tag">
          <span v-if="(statusData.queueSize || 0) === 0" class="tag-smooth">无排队积压 · 状态极佳</span>
          <span v-else class="tag-warn">正在排队消化 {{ statusData.queueSize }} 个任务</span>
        </div>
        <div class="metric-subtext">
          剩余缓冲容量: {{ statusData.queueRemainingCapacity ?? 500 }}
        </div>
      </div>

      <!-- 3. 累计执行任务 -->
      <div class="metric-item">
        <div class="metric-label">
          <span>累计完成任务数</span>
          <a-tooltip content="自服务启动以来累计已成功完成的异步任务总数">
            <icon-info-circle class="info-icon" />
          </a-tooltip>
        </div>
        <div class="metric-value-row">
          <span class="metric-value highlight">{{ statusData.completedTaskCount ?? 0 }}</span>
          <span class="metric-unit">项</span>
        </div>
        <div class="policy-capsule">
          <span>拒绝策略: CallerRuns (不丢单)</span>
        </div>
        <div class="metric-subtext">
          总分发任务: {{ statusData.totalTaskCount ?? 0 }} 项
        </div>
      </div>

      <!-- 4. Redis 基础设施 -->
      <div class="metric-item">
        <div class="metric-label">
          <span>Redis 基础设施状态</span>
          <a-tooltip content="用于分布式锁、防超卖及后续缓存持久化的 Redis 运行连通性">
            <icon-info-circle class="info-icon" />
          </a-tooltip>
        </div>
        <div class="metric-value-row">
          <span class="metric-value redis-status" :class="{ 'is-alive': statusData.redisAlive }">
            {{ statusData.redisAlive ? '已连接' : (statusData.redisPing === 'DISABLED' ? '未配置' : '未连接') }}
          </span>
          <span class="metric-unit" v-if="statusData.redisAlive">({{ statusData.redisPing }})</span>
        </div>
        <div class="redis-desc-capsule">
          <span v-if="statusData.redisAlive" class="redis-ok">已就绪 · 支持分布式锁</span>
          <span v-else class="redis-off">本地未启动或由配置注入</span>
        </div>
        <div class="metric-subtext">
          驱动模式: Lettuce + 连接池
        </div>
      </div>
    </div>

    <!-- 底部特性与配置小字 -->
    <div class="card-footer-bar">
      <div class="bar-left">
        <span class="tag-item">线程前缀: {{ statusData.threadPrefix || 'blog-async-' }}</span>
        <span class="tag-item">优雅停机: 开启 (等待60s)</span>
        <span class="tag-item">异常拦截: 全局日志捕获</span>
      </div>
      <div class="bar-right">
        <span class="update-time" v-if="lastUpdateTime">更新于: {{ lastUpdateTime }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { getAsyncPoolStatus } from '@/api/common';
import { Message } from '@arco-design/web-vue';

export default {
  name: 'AsyncPoolMonitorCard',
  data() {
    return {
      loading: false,
      statusData: {},
      lastUpdateTime: '',
      pollTimer: null
    };
  },
  computed: {
    calcUsagePercent() {
      const p = this.statusData.usagePercent || 0;
      return Math.min(100, Math.max(0, p / 100));
    },
    getProgressColor() {
      const p = this.statusData.usagePercent || 0;
      if (p >= 80) return '#F53F3F';
      if (p >= 50) return '#FF7D00';
      return '#00B42A';
    },
    getHealthClass() {
      const s = this.statusData.healthStatus;
      if (s === 'BUSY') return 'health-busy';
      if (s === 'WARNING') return 'health-warn';
      return 'health-ok';
    },
    getHealthText() {
      const s = this.statusData.healthStatus;
      if (s === 'BUSY') return '高负荷运转';
      if (s === 'WARNING') return '排队积压中';
      return '健康运转中';
    }
  },
  mounted() {
    this.fetchStatus();
    // 开启 10 秒静默轮询更新
    this.pollTimer = setInterval(() => {
      this.fetchStatus(true);
    }, 10000);
  },
  beforeUnmount() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  },
  methods: {
    async fetchStatus(isSilent = false) {
      if (!isSilent) {
        this.loading = true;
      }
      try {
        const res = await getAsyncPoolStatus();
        this.statusData = res.data.data || {};
        const now = new Date();
        this.lastUpdateTime = now.toTimeString().split(' ')[0];
      } catch (e) {
        if (!isSilent) {
          Message.warning('获取异步线程池状态异常: ' + (e?.response?.data?.message || '请检查登录态'));
        }
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.async-pool-card {
  background: #FFFFFF;
  border-radius: 16px;
  border: 1px solid rgba(229, 230, 235, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  padding: 20px 24px;
  margin-bottom: 24px;
  transition: all 0.25s ease;
}

.async-pool-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.07);
}

/* 头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F2F3F5;
}

.header-title-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #FFF7E8 0%, #FFE8CC 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.head-icon {
  font-size: 22px;
  color: #FF7D00;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1D2129;
}

.tech-pill {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #E8F3FF;
  color: #165DFF;
  font-weight: 600;
}

.card-subtitle {
  margin: 2px 0 0 0;
  font-size: 12px;
  color: #86909C;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 健康状态呼吸灯 */
.health-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: relative;
}

.pulse-dot::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  opacity: 0.75;
  animation: pulse 1.8s infinite cubic-bezier(0.4, 0, 0.6, 1);
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 0.9; }
  50% { transform: scale(1.6); opacity: 0; }
  100% { transform: scale(0.8); opacity: 0; }
}

.health-ok {
  background: #E8FFEA;
  color: #00B42A;
}
.health-ok .pulse-dot { background: #00B42A; }
.health-ok .pulse-dot::after { background: #00B42A; }

.health-busy {
  background: #FFF7E8;
  color: #FF7D00;
}
.health-busy .pulse-dot { background: #FF7D00; }
.health-busy .pulse-dot::after { background: #FF7D00; }

.health-warn {
  background: #FFECE8;
  color: #F53F3F;
}
.health-warn .pulse-dot { background: #F53F3F; }
.health-warn .pulse-dot::after { background: #F53F3F; }

.refresh-btn {
  border-color: #E5E6EB;
  color: #4E5969;
}
.refresh-btn:hover {
  border-color: #C9CDD4;
  color: #1D2129;
}

/* 4 宫格网格 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 18px;
}

@media (max-width: 900px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 540px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
}

.metric-item {
  background: #F7F8FA;
  border-radius: 12px;
  padding: 14px 16px;
  border: 1px solid rgba(229, 230, 235, 0.6);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #4E5969;
  font-weight: 500;
}

.info-icon {
  color: #C9CDD4;
  cursor: pointer;
  font-size: 13px;
}
.info-icon:hover {
  color: #86909C;
}

.metric-value-row {
  margin-top: 6px;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.metric-value {
  font-size: 26px;
  font-weight: 800;
  color: #1D2129;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  letter-spacing: -0.5px;
}

.metric-value.highlight {
  color: #165DFF;
}

.metric-value.text-warn {
  color: #FF7D00;
}

.metric-value.redis-status {
  font-size: 20px;
  color: #86909C;
}
.metric-value.redis-status.is-alive {
  color: #00B42A;
}

.metric-unit {
  font-size: 13px;
  color: #86909C;
  font-weight: 500;
}

.metric-progress-box {
  margin: 6px 0;
}

.queue-status-tag, .policy-capsule, .redis-desc-capsule {
  margin: 6px 0;
  font-size: 11px;
}

.tag-smooth {
  color: #00B42A;
  background: rgba(0, 180, 42, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.tag-warn {
  color: #FF7D00;
  background: rgba(255, 125, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.policy-capsule span {
  color: #165DFF;
  background: rgba(22, 93, 255, 0.08);
  padding: 2px 6px;
  border-radius: 4px;
}

.redis-ok {
  color: #00B42A;
  background: rgba(0, 180, 42, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.redis-off {
  color: #86909C;
  background: #E5E6EB;
  padding: 2px 6px;
  border-radius: 4px;
}

.metric-subtext {
  font-size: 11px;
  color: #86909C;
  margin-top: 4px;
}

/* 底部状态条 */
.card-footer-bar {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #E5E6EB;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #86909C;
}

.bar-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tag-item {
  display: inline-flex;
  align-items: center;
}

.update-time {
  font-size: 11px;
  color: #C9CDD4;
}
</style>
