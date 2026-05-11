package com.wtls.blog_server.task;

import com.wtls.blog_server.service.product.GroupBuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GroupBuyTask {

    @Autowired
    private GroupBuyService groupBuyService;

    // 每分钟执行一次
    @Scheduled(fixedRate = 60000)
    public void settleExpiredGroups() {
        groupBuyService.checkExpiredGroups();
    }
}
