package com.njupt.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.njupt.gmall.bean.PmsSearchSkuInfo;
import com.njupt.gmall.bean.PmsSkuInfo;
import com.njupt.gmall.service.PmsSkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    PmsSkuService pmsSkuService;
    @Autowired
    JestClient jestClient;

    //！！！ 向es中存取数据的方法和用工具包装sql操作MySQL数据库一样的原理 ！！！
    @Test
    public void contextLoads() throws IOException {
        //将包装好的api()方法放入类加载初始化机制中
        put();
    }

    //将放入es数据包装成一个api方法，方便调用
    public void put() throws IOException {
        // 查询mysql数据
        List<PmsSkuInfo> pmsSkuInfoList = new ArrayList<>();
        pmsSkuInfoList = pmsSkuService.getAllSku("61");
        // 转化为es的数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();

            //因为pmsSkuInfo和pmsSearchSkuInfo是具有很多相同属性的JavaBean，
            //为了避免需要我们手动将数据库的信息导入到es中
            //我们可以采用（Struts里的PO对象（持久对象）和对应的ActionForm）的方法将信息导入到es中：相当于我们把数据信息取出来
            //存到一个对应es数据结构的集合对象中，然后把集合对象中的数据遍历存入es中
            //具体做法：我们先创建一个对应es数据库的Bean实例类，然后使用方法将数据库中的数据全部取出来封装到一个List<PmsSkuInfo>中
            //然后使用Struts中的BeanUtils.copyProperties()方法，将数据信息映射到具有多个相同属性的List<PmsSearchSkuInfo>中
            //注意：若是相同属性的话直接映射，若是不同属性的话，必须手动映射
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            //不同的属性必须手动映射导入
            pmsSearchSkuInfo.setId(Long.parseLong(pmsSkuInfo.getId()));
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }
        // 最后把这些信息导入es中
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall0511").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()+"").build();
            jestClient.execute(put);
        }
    }

    //将从es中取(查询)数据包装成一个api可以供服务调用
    public void get() throws IOException {
        //使用jest的dsl工具编写复杂查询的json语句：包括过滤等
        // jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // filter
//        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId","66");
//        boolQueryBuilder.filter(termQueryBuilder);
        // must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","华为");
        boolQueryBuilder.must(matchQueryBuilder);
        // query
        searchSourceBuilder.query(boolQueryBuilder);
        // from
        searchSourceBuilder.from(0);
        // size
        searchSourceBuilder.size(20);
        // highlight
        searchSourceBuilder.highlight(null);
        String dslStr = searchSourceBuilder.toString();
        System.err.println(dslStr);

        // 用api执行复杂查询
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        //dslStr相当于是一个已经编写好的复杂查询的语句
        Search search = new Search.Builder(dslStr).addIndex("gmall0511").addType("PmsSkuInfo").build();
        //调用方法去执行这个语句
        SearchResult execute = jestClient.execute(search);
        //es使用hits来包装根据复杂语句查询到的数据集合(相当于查询到多个商品的index(库，也包含了一些索引字段名等信息))，
        //我们可以调用getHits()方法获取到查询出来的结果集
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        //而查询到的具体数据详情信息是封装在hits的source中，我们可以遍历获取到对应的商品信息
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            //然后把商品信息封装到对应的Bean对象实例中，最后向页面展示改对象实例即可
            pmsSearchSkuInfos.add(source);
        }
        System.out.println(pmsSearchSkuInfos.size());
    }

}
