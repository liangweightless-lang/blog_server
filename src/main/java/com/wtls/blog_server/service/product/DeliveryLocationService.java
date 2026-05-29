package com.wtls.blog_server.service.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wtls.blog_server.entity.product.DeliveryLocation;
import com.wtls.blog_server.mapper.product.DeliveryLocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryLocationService {

    @Autowired
    private DeliveryLocationMapper mapper;

    public List<DeliveryLocation> getAllLocations() {
        QueryWrapper<DeliveryLocation> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return mapper.selectList(queryWrapper);
    }
    
    public List<DeliveryLocation> getActiveLocations() {
        QueryWrapper<DeliveryLocation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1).orderByDesc("create_time");
        return mapper.selectList(queryWrapper);
    }

    public void createLocation(DeliveryLocation location) {
        location.setCreateTime(LocalDateTime.now());
        location.setUpdateTime(LocalDateTime.now());
        if (location.getStatus() == null) {
            location.setStatus(1);
        }
        mapper.insert(location);
    }

    public void updateLocation(DeliveryLocation location) {
        location.setUpdateTime(LocalDateTime.now());
        mapper.updateById(location);
    }

    public void deleteLocation(Long id) {
        mapper.deleteById(id);
    }
}
