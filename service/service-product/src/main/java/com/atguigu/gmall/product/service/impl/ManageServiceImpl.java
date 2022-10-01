package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/9/29
 * description:
 */
@Service
public class ManageServiceImpl implements ManageService {
    @Resource
    private BaseCategory1Mapper baseCategory1Mapper;
    @Resource
    private BaseCategory2Mapper baseCategory2Mapper;

    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;

    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Resource
    private BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public BaseAttrInfo getBaseAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo=baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo!=null){
            baseAttrInfo.setAttrValueList(this.getAttrValueList(attrId));
        }
        return baseAttrInfo;
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        return  baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>().eq("attr_id",attrId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId()!=null){//修改，先删除再添加
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //无法修改valuelist，先进行删除
            baseAttrValueMapper.delete(new QueryWrapper<BaseAttrValue>().eq("attr_id",baseAttrInfo.getId()));
        }else {//删除
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)){
            attrValueList.forEach(baseAttrValue->{
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }
    }

    @Override
    public List<BaseAttrInfo> selectAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.selectAttrInfoList(category1Id,category2Id,category3Id);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id",category2Id));
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id", category1Id));

    }

    @Override
    public List<BaseCategory1> getCategory1() {

        return baseCategory1Mapper.selectList(null);
    }
}
