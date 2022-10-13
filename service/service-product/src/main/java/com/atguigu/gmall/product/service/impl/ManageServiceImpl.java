package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Resource
    private SpuInfoMapper spuInfoMapper;
    @Resource
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Resource
    private SpuImageMapper spuImageMapper;
    @Resource
    private SpuPosterMapper spuPosterMapper;
    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Resource
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private SkuImageMapper skuImageMapper;
    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;
    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<BaseAttrInfo> selectBaseAttrInfoListBySkuId(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }

    @Override
    @GmallCache(prefix = "skuValue:")
    public Map getSkuValueIdsMap(Long spuId) {
        Map map = new HashMap();
        List<Map> mapList = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        if (!CollectionUtils.isEmpty(mapList)) {
            mapList.forEach(map1 -> {
                map.put(map1.get("value_ids"), map1.get("sku_id"));
            });
        }
        return map;
    }


    @Override
    @GmallCache(prefix = "attrList:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectOne(new QueryWrapper<SkuInfo>().eq("id", skuId).select("price"));
        if (skuInfo != null) {
            return skuInfo.getPrice();
        }
        return null;
    }

    @Override
    @GmallCache(prefix = "categoryView:")
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    @GmallCache(prefix = "spuPoster:")
    public List<SpuPoster> getSpuPosterBySpuId(Long spuId) {
        return spuPosterMapper.selectList(new QueryWrapper<SpuPoster>().eq("spu_id", spuId));
    }

    //  redisson
    private SkuInfo getSkuInfoByRedisson(Long skuId) {
        //  声明一个skuInfo
        SkuInfo skuInfo = null;
        //  判断缓存中是否有数据!
        //  通过key 获取数据;  redis 使用类型！
        //  hash 存储实体类 -- 便于修改，商品详情只是渲染数据，没有修改。
        //  key = sku:skuId:info
        String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        try {
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //  判断
            if (skuInfo == null) {
                //  说明缓存中没有数据; 防止缓存击穿
                //  key = sku:skuId:lock
                String lockey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockey);
                //  判断是否获取到锁,上分布式锁
                Boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (result) {
                    try {
                        //               获得锁
                        skuInfo = getSkuInfoDB(skuId);
                        //                如果数据库也没有
                        if (skuInfo == null) {
                            SkuInfo skuInfo1 = new SkuInfo();
                            redisTemplate.opsForValue().set(skuKey, skuInfo1, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfo1;
                        }
                        redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return skuInfo;
                    } finally {
                        lock.unlock();
                    }
                } else {
                    try {
                        Thread.sleep(200);//自旋
                        return getSkuInfo(skuId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  查询数据库 -- 兜底.
        return this.getSkuInfoDB(skuId);
    }

    //redis
    private SkuInfo getSkuInfoByRedis(Long skuId) {
        //  声明一个skuInfo
        SkuInfo skuInfo = null;
        //  判断缓存中是否有数据!
        //  通过key 获取数据;  redis 使用类型！
        //  hash 存储实体类 -- 便于修改，商品详情只是渲染数据，没有修改。
        //  key = sku:skuId:info
        String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        try {
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //  判断
            if (skuInfo == null) {
                //  说明缓存中没有数据; 防止缓存击穿
                //  key = sku:skuId:lock
                String lockey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                String uuid = UUID.randomUUID().toString();
                //  判断是否获取到锁,上分布式锁
                Boolean result = redisTemplate.opsForValue().setIfAbsent(lockey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (result) {
                    //               获得锁
                    skuInfo = getSkuInfoDB(skuId);
                    //                如果数据库也没有
                    if (skuInfo == null) {
                        SkuInfo skuInfo1 = new SkuInfo();
                        redisTemplate.opsForValue().set(skuKey, skuInfo1, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        delLocKey(lockey, uuid);
                        return skuInfo1;
                    }
                    redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    delLocKey(lockey, uuid);
                    return skuInfo;
                } else {
                    try {
                        Thread.sleep(200);//自旋
                        return getSkuInfo(skuId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  查询数据库 -- 兜底.
        return this.getSkuInfoDB(skuId);
    }

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = getSkuInfoDB(skuId);
        return skuInfo;
    }

    //  删除锁key 方法。
    private void delLocKey(String locKey, String uuId) {
        //  删除数据key  使用lua 脚本.
        String scriptText = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript defaultRedisScript = new DefaultRedisScript<>();
        //  设置lua脚本
        defaultRedisScript.setScriptText(scriptText);
        defaultRedisScript.setResultType(Long.class);
        this.redisTemplate.execute(defaultRedisScript, Arrays.asList(locKey), uuId);
    }

    private SkuInfo getSkuInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        if (skuInfo != null) {
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public IPage getSkuInfoPage(Page<SkuInfo> skuInfoPage, SkuInfo skuInfo) {
        return skuInfoMapper.selectPage(skuInfoPage, new QueryWrapper<SkuInfo>().eq("category3_id", skuInfo.getCategory3Id()).orderByDesc("id"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
//        调用mapper接口修改表数据，注意前端传参可能与表字段不一定一致
//        sku_attr_value
//sku_image
//sku_info
//sku_sale_attr_value
        skuInfoMapper.insert(skuInfo);
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            });
        }
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            });
        }
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }
        RBloomFilter<Object> rbloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        rbloomFilter.add(skuInfo.getId());
    }

    @Override
    @GmallCache(prefix = "spuSaleAttr:")
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.getSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id", spuId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        //1找表spu_image
        //spu_info
        //spu_poster
        //spu_sale_attr
        //spu_sale_attr_value
        spuInfoMapper.insert(spuInfo);
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)) {
            spuImageList.forEach(spuImage -> {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            });
        }
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            spuPosterList.forEach(spuPoster -> {
                spuPoster.setSpuId(spuInfo.getId());
                spuPosterMapper.insert(spuPoster);
            });
        }
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {
            spuSaleAttrList.forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {
                    spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    });
                }
            });
        }
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
        return spuInfoMapper.selectPage(spuInfoPage, new QueryWrapper<SpuInfo>().eq("category3_id", spuInfo.getCategory3Id()).orderByDesc("id"));
    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo != null) {
            baseAttrInfo.setAttrValueList(this.getAttrValueList(attrId));
        }
        return baseAttrInfo;
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        return baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>().eq("attr_id", attrId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null) {//修改，先删除再添加
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //无法修改valuelist，先进行删除
            baseAttrValueMapper.delete(new QueryWrapper<BaseAttrValue>().eq("attr_id", baseAttrInfo.getId()));
        } else {//删除
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)) {
            attrValueList.forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }
    }

    @Override
    public List<BaseAttrInfo> selectAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.selectAttrInfoList(category1Id, category2Id, category3Id);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id", category2Id));
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id", category1Id));

    }

    @Override
    public List<BaseCategory1> getCategory1() {

        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public void updateCategory3ById(BaseCategory3 baseCategory3) {

        redisTemplate.delete("index:[]");

        this.baseCategory3Mapper.updateById(baseCategory3);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        redisTemplate.delete("index:[]");
    }

    //    主页展示信息
    @Override
    @GmallCache(prefix = "index:")
    public List<JSONObject> getCategoryList() {
        ArrayList<JSONObject> list = new ArrayList<>();
        List<BaseCategoryView> baseCategoryViewsList = baseCategoryViewMapper.selectList(null);
//一按一级分类id进行分组
        int index=1;
        Map<Long, List<BaseCategoryView>> categoryView1Map = baseCategoryViewsList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        for (Map.Entry<Long, List<BaseCategoryView>> entry : categoryView1Map.entrySet()) {
            JSONObject category1 = new JSONObject();
            Long category1Id = entry.getKey();
            List<BaseCategoryView> baseCategoryViewList1 = entry.getValue();
            category1.put("index",index);
            index++;
            category1.put("categoryId",category1Id);
            category1.put("categoryName",baseCategoryViewList1.get(0).getCategory1Name());
//            二级分类集合
            List<JSONObject> category2Child = new ArrayList<>();

            Map<Long, List<BaseCategoryView>> categoryView2Map = baseCategoryViewList1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : categoryView2Map.entrySet()) {
                JSONObject category2 = new JSONObject();
                Long category2Id = entry2.getKey();
                List<BaseCategoryView> baseCategoryViewList2 = entry2.getValue();
                category2.put("categoryId",category2Id);
                category2.put("categoryName",baseCategoryViewList2.get(0).getCategory2Name());
                List<JSONObject> category3Child = baseCategoryViewList2.stream().map(baseCategoryView -> {
                    JSONObject category3 = new JSONObject();
                    Long category3Id = baseCategoryView.getCategory3Id();
                    String category3Name = baseCategoryView.getCategory3Name();
                    category3.put("categoryId", category3Id);
                    category3.put("categoryName", category3Name);
                    return category3;
                }).collect(Collectors.toList());

                category2.put("categoryChild",category3Child);
                category2Child.add(category2);
            }
            category1.put("categoryChild", category2Child);
            list.add(category1);
        }
        return list;
    }
}
