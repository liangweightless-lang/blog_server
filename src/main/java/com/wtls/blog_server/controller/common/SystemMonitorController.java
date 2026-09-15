package com.wtls.blog_server.controller.common;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 系统基础设施与异步线程池运行监控接口
 */
@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
@Tag(name = "系统监控", description = "异步线程池与基础设施运行状态监控")
public class SystemMonitorController {

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/async-pool/status")
    @Operation(summary = "获取异步线程池实时监控指标", description = "获取当前 Spring @Async 通用线程池的活跃线程、排队中任务及累计完成数")
    public Result<Map<String, Object>> getAsyncPoolStatus(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 权限校验：限管理员或创作官
        JwtUtils.checkAdminOrCreator(authHeader);

        Map<String, Object> data = new HashMap<>();

        if (taskExecutor != null) {
            ThreadPoolExecutor rawExecutor = taskExecutor.getThreadPoolExecutor();

            int corePoolSize = taskExecutor.getCorePoolSize();
            int maxPoolSize = taskExecutor.getMaxPoolSize();
            int currentPoolSize = taskExecutor.getPoolSize();
            int activeCount = taskExecutor.getActiveCount();
            int queueSize = taskExecutor.getQueueSize();
            int remainingCapacity = rawExecutor.getQueue().remainingCapacity();
            int queueCapacity = queueSize + remainingCapacity;
            long completedTasks = rawExecutor.getCompletedTaskCount();
            long totalTasks = rawExecutor.getTaskCount();

            // 计算线程活跃利用率（%）
            double usagePercent = maxPoolSize > 0 ? (double) activeCount / maxPoolSize * 100.0 : 0.0;

            // 计算健康状态：HEALTHY（健康正常）、BUSY（高负载）、WARNING（排队积压）
            String status = "HEALTHY";
            if (queueSize > 200 || (maxPoolSize > 0 && activeCount >= maxPoolSize)) {
                status = "BUSY";
            } else if (queueSize > 50) {
                status = "WARNING";
            }

            data.put("corePoolSize", corePoolSize);
            data.put("maxPoolSize", maxPoolSize);
            data.put("currentPoolSize", currentPoolSize);
            data.put("activeCount", activeCount);
            data.put("queueSize", queueSize);
            data.put("queueCapacity", queueCapacity);
            data.put("queueRemainingCapacity", remainingCapacity);
            data.put("completedTaskCount", completedTasks);
            data.put("totalTaskCount", totalTasks);
            data.put("usagePercent", Math.round(usagePercent * 10.0) / 10.0);
            data.put("threadPrefix", taskExecutor.getThreadNamePrefix());
            data.put("rejectionPolicy", "CallerRunsPolicy");
            data.put("healthStatus", status);
        }

        // 探测 Redis 基础设施连通状态
        boolean redisAlive = false;
        String redisPing = "DISABLED";
        if (redisTemplate != null) {
            try {
                RedisConnection conn = redisTemplate.getConnectionFactory() != null 
                        ? redisTemplate.getConnectionFactory().getConnection() 
                        : null;
                if (conn != null) {
                    redisPing = conn.ping();
                    redisAlive = "PONG".equalsIgnoreCase(redisPing);
                    conn.close();
                }
            } catch (Exception e) {
                redisPing = "UNAVAILABLE";
                redisAlive = false;
            }
        }
        data.put("redisAlive", redisAlive);
        data.put("redisPing", redisPing);

        return Result.success(data, "获取异步线程池指标成功");
    }
}
