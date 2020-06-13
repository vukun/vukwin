package com.njupt.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.njupt.gmall.bean.PmsSearchParam;
import com.njupt.gmall.bean.PmsSearchSkuInfo;
import com.njupt.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhaokun
 * @create 2020-05-30 22:06
 */
@Service
public class SearchServiceImpl implements SearchService {

    //使用es进行资源搜索时候，不是用的mapper而是用的JestClient工具
    @Autowired
    JestClient jestClient;

    /**
     *  可以根据关键字、catalog3Id进行elasticSearch搜索查询数据
     *  单独创建一个PmsSearchParam实体类Bean，
     *  用来封装接收来自前端es查询的参数进行一系列的后台业务处理
     *  参数主要有：{catalog3Id、keyword、skuAttrValueList(平台属性值列表)}
     *  也就是前端可以根据catalog3Id进行搜索、可以根据keyword进行搜索、可以根据skuAttrValueList进行搜索
     *
     *  具体操作可以参考
     *          test\java\com\njupt\gmall\search\GmallSearchServiceApplicationTests.java文件中的过程
     * @param pmsSearchParam
     * @return
     */
    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
        String dslStr = getSearchDsl(pmsSearchParam);
//        System.err.println(dslStr);
        // 用api执行复杂查询
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        Search search = new Search.Builder(dslStr).addIndex("gmall0511").addType("PmsSkuInfo").build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;

            //如果用户没有对关键字进行搜索，或者只是使用了平台属性值进行搜索，这儿的highlight为空，可能会出现空指针异常
            Map<String, List<String>> highlight = hit.highlight;
            //取出搜索的关键字，进行最后几行的处理得到关键词高亮显示
            if(highlight != null){
                String skuName = highlight.get("skuName").get(0);
                source.setSkuName(skuName);
            }
            pmsSearchSkuInfos.add(source);
        }

//        System.out.println(pmsSearchSkuInfos.size());
        return pmsSearchSkuInfos;
    }

    private String getSearchDsl(PmsSearchParam pmsSearchParam) {

        String[] skuAttrValueList = pmsSearchParam.getValueId();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();

        // jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // filter catalog3Id筛选
        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        // 平台属性值SkuAttrValue 筛选
        if(skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",pmsSkuAttrValue);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        // must 筛选
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        // query 根据筛选的条件查询
        searchSourceBuilder.query(boolQueryBuilder);

        // highlight 高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);
        // sort 根据评分Score进行排序，分数高的放在最前面
        searchSourceBuilder.sort("id", SortOrder.DESC);
        // from
        searchSourceBuilder.from(0);
        // size 限制取数据的数量
        searchSourceBuilder.size(20);
        // aggs
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        //返回筛选条件查询的结果
        return searchSourceBuilder.toString();
    }

}
