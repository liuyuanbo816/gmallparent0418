package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * title:
 * author: bai
 * date: 2022/9/29
 * description:
 */

public interface ManageService {

    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long category1Id);

    List<BaseCategory3> getCategory3(Long category2Id);

    List<BaseAttrInfo> selectAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(Long attrId);

    BaseAttrInfo getBaseAttrInfo(Long attrId);

}
