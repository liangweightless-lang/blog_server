package com.wtls.blog_server.mapper.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wtls.blog_server.entity.product.CampaignProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CampaignProductMapper extends BaseMapper<CampaignProduct> {

    /**
     * 原子扣减团购活动商品专属库存（防并发超卖与负库存）
     * 当 stock_limit 为 -1 时表示不限购保持 -1
     */
    @Update("UPDATE campaign_product SET stock_limit = CASE WHEN stock_limit = -1 THEN -1 ELSE stock_limit - #{count} END " +
            "WHERE id = #{id} AND (stock_limit = -1 OR stock_limit >= #{count})")
    int reduceStock(@Param("id") Long id, @Param("count") int count);
}
