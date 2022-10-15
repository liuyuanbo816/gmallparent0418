package com.atguigu.gmall.list.repository;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * title:
 * author: bai
 * date: 2022/10/14
 * description:
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
