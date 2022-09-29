package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/9/29
 * description:
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    List<BaseAttrInfo> selectAttrInfoList(@Param("category1Id") Long category1Id,@Param("category2Id")  Long category2Id,@Param("category3Id")  Long category3Id);
}
