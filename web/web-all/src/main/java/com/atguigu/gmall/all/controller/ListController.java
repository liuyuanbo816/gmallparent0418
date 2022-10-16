package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * title:
 * author: bai
 * date: 2022/10/16
 * description:
 */
@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;


    @GetMapping("list.html")
    public String list(Model model, SearchParam searchParam){

        Result<Map> result=listFeignClient.list(searchParam);
//        品牌面包屑
        String trademarkParam=makeTrademarkParam(searchParam.getTrademark());
//      属性面包屑
        List<SearchAttr> searchAttrList=makeAttrList(searchParam.getProps());
//        排序
        Map<String,Object> orderMap=makeOrderMap(searchParam.getOrder());
//        制作urlParam
        String urlParam=makeUrlParam(searchParam);
        model.addAllAttributes(result.getData());
        model.addAttribute("searchParam",searchParam);
        model.addAttribute("urlParam",urlParam);
        model.addAttribute("trademarkParam",trademarkParam);
        model.addAttribute("propsParamList",searchAttrList);
        model.addAttribute("orderMap",orderMap);
        return "list/index";
    }

    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            stringBuilder.append("keyword=").append(searchParam.getKeyword());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            stringBuilder.append("category3Id=").append(searchParam.getCategory3Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            stringBuilder.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            stringBuilder.append("category1Id=").append(searchParam.getCategory1Id());
        }

//
        if (!StringUtils.isEmpty(searchParam.getTrademark())){
            if (stringBuilder.length()>0){
                stringBuilder.append("&trademark=").append(searchParam.getTrademark());
            }
        }
        String[] props = searchParam.getProps();
        if (props!=null && props.length>0){
            for (String prop : props) {
                if (stringBuilder.length()>0){
                    stringBuilder.append("&props=").append(prop);
                }
            }
        }

        return "list.html?"+stringBuilder.toString();
    }

    private Map<String, Object> makeOrderMap(String order) {
        Map<String, Object> map = new HashMap<>();
        //  判断
        if (!StringUtils.isEmpty(order)){
            //  order=2:desc
            String[] split = order.split(":");
            if (split!=null && split.length==2){
                map.put("type",split[0]);
                map.put("sort",split[1]);
            }
        }else {
            map.put("type","1");
            map.put("sort","desc");
        }
        return map;
    }

    private List<SearchAttr> makeAttrList(String[] props) {
        List<SearchAttr> searchAttrList=new ArrayList<>();
        if (props!=null&&props.length>0){
            for (String prop : props) {
                String[] split = prop.split(":");
                if (split!=null&&split.length==3){
                    SearchAttr searchAttr = new SearchAttr();
                    searchAttr.setAttrId(Long.parseLong(split[0]));
                    searchAttr.setAttrValue(split[1]);
                    searchAttr.setAttrName(split[2]);
                    searchAttrList.add(searchAttr);
                }
            }
        }
        return searchAttrList;
    }

    private String makeTrademarkParam(String trademark) {
        if (!StringUtils.isEmpty(trademark)){
            String[] split = trademark.split(":");
            if (split!=null&&split.length==2){
                return "品牌"+split[1];
            }
        }
        return null;
    }
}
