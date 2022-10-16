package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * title:
 * author: bai
 * date: 2022/10/15
 * description:
 */
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //点击增加热度
    @Override
    public void incrHotScore(Long skuId) {
        String hotKey = "hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);

        if (score % 10 == 0) {
            Optional<Goods> optionalGoods = goodsRepository.findById(skuId);
            Goods goods = optionalGoods.get();
            goods.setHotScore(score.longValue());
            goodsRepository.save(goods);
        }
    }

    //商品上架，信息放到es中
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        goods.setId(skuId);
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        goods.setPrice(skuPrice.doubleValue());
        goods.setCreateTime(new Date());
        BaseTrademark trademark = productFeignClient.getTrademarkById(skuInfo.getTmId());
        goods.setTmId(trademark.getId());
        goods.setTmName(trademark.getTmName());
        goods.setTmLogoUrl(trademark.getLogoUrl());
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());

        List<BaseAttrInfo> baseAttrInfoList = productFeignClient.selectBaseAttrInfoListBySkuId(skuId);
        List<SearchAttr> searchAttrList = baseAttrInfoList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrList);

        goodsRepository.save(goods);
    }

    //商品下架
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    //执行查询dsl返回vo
    @Override
    public SearchResponseVo search(SearchParam searchParam){
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchRequest searchRequest = this.queryBuildDsl(searchParam);
        SearchResponse searchResponse = null;
        try {
            searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        searchResponseVo = this.parseResultResponseVo(searchResponse);
//            //品牌 此时vo对象中的id字段保留（不用写） name就是“品牌” value: [{id:100,name:华为,logo:xxx},{id:101,name:小米,log:yyy}]
//    private List<SearchResponseTmVo> trademarkList;
//    //所有商品的顶头显示的筛选属性
//    private List<SearchResponseAttrVo> attrsList = new ArrayList<>();
//
//    //检索出来的商品信息
//    private List<Goods> goodsList = new ArrayList<>();
//
//    private Long total;//总记录数

//    private Integer pageSize;//每页显示的内容
        searchResponseVo.setPageSize(searchParam.getPageSize());
//    private Integer pageNo;//当前页面
        searchResponseVo.setPageNo(searchParam.getPageNo());
//    private Long totalPages;
        Long totalPages = (searchResponseVo.getTotal() + searchParam.getPageSize() - 1) / searchParam.getPageSize();
        searchResponseVo.setTotalPages(totalPages);
        return searchResponseVo;
    }

    //    dsl结果转换成vo
    private SearchResponseVo parseResultResponseVo(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();
//            获取总记录
        searchResponseVo.setTotal(hits.getTotalHits().value);
        SearchHit[] subHits = hits.getHits();
        List<Goods> goodsList = new ArrayList<>();
        if (subHits!=null&&subHits.length>0){
            for (SearchHit subHit : subHits) {
                String sourceAsString = subHit.getSourceAsString();
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                if (subHit.getHighlightFields().get("title")!=null){
                    String title = subHit.getHighlightFields().get("title").getFragments()[0].toString();
                    goods.setTitle(title);
                }
                goodsList.add(goods);
            }
        }
//设置商品集合
        searchResponseVo.setGoodsList(goodsList);
//            从聚合获得品牌
        Map<String, Aggregation> stringAggregationMap = searchResponse.getAggregations().asMap();
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) stringAggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            String tmId = ((Terms.Bucket) bucket).getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(tmId));
            ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) bucket).getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(trademarkList);
//            从聚合获得平台属性
        ParsedNested attrsAgg = (ParsedNested) stringAggregationMap.get("attrsAgg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrsList = attrIdAgg.getBuckets().stream().map(bucket -> {
            //  创建一个平台属性对象
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //  获取到平台属性Id
            String attrId = ((Terms.Bucket) bucket).getKeyAsString();
            searchResponseAttrVo.setAttrId(Long.parseLong(attrId));

            //  获取平台属性名
            ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            //  获取平台属性值
            ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
            //  获取到平台属性值名称
            List<String> attrValueList = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());

            //            List<String> collect = attrValueAgg.getBuckets().stream().map(buckets -> {
            //                String valueName = ((Terms.Bucket) buckets).getKeyAsString();
            //                return valueName;
            //            }).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(attrValueList);
            //  返回数据.
            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        searchResponseVo.setAttrsList(attrsList);
        return searchResponseVo;
    }


    //  建立dsl语句
    private SearchRequest queryBuildDsl(SearchParam searchParam) {
//        {}
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        query bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        是否通过分类id进行检索
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }

//        全文检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));
//            设置高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<span style=color:red>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

//        通过品牌过滤  trademark = 3:华为
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }
//        平台属性 属性值
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
//                props=23:4G:运行内存
                String[] split = prop.split(":");
                if (split != null && split.length == 3) {
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    BoolQueryBuilder innerBoolQuery = QueryBuilders.boolQuery();
                    innerBoolQuery.must(QueryBuilders.matchQuery("attrs.attrId", split[0]));
                    innerBoolQuery.must(QueryBuilders.matchQuery("attrs.attrValue", split[1]));
                    boolQuery.must(QueryBuilders.nestedQuery("attrs", innerBoolQuery, ScoreMode.None));
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);

//        分页
        int form = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(form);
        searchSourceBuilder.size(searchParam.getPageSize());
//      排序
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] split = order.split(":");
            if (split != null && split.length == 2) {
                String field = "";
                switch (split[0]) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field="price";
                        break;
                }
                searchSourceBuilder.sort(field,"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
            }
        }else {
            searchSourceBuilder.sort("hotScore",SortOrder.DESC);
        }
//品牌聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));
//        平台属性值聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg","attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")))
        );
//GET /goods/_search
        SearchRequest searchRequest = new SearchRequest("goods");
        System.out.println("dsl:\t"+searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);

        searchSourceBuilder.fetchSource(new String []{"id","defaultImg","title","price","createTime"},null);


        return searchRequest;
    }
}
