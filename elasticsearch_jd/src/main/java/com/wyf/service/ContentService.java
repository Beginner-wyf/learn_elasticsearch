package com.wyf.service;

import com.alibaba.fastjson.JSON;
import com.wyf.pojo.Content;
import com.wyf.util.HtmlParseUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author wangyifan
 * @create 2021/2/20 10:35
 */
@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private HtmlParseUtils htmlParseUtils;

    /**
     * 1、将所解析的数据放入es库中
     */
    public boolean parseContent(String keywords) {
        ArrayList<Content> contents = htmlParseUtils.parseJD(keywords);

        BulkRequest bulkRequest = new BulkRequest().timeout(TimeValue.timeValueNanos(10));

        for (Content content : contents) {
            bulkRequest.add(
                    new IndexRequest("wang_index")
                            .source(JSON.toJSONString(content), XContentType.JSON)
            );
        }

        BulkResponse bulk = null;
        try {
            bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return !bulk.hasFailures();
    }


    public ArrayList<Map<String, Object>> searchByPage(String keywords, int pageNo, int pageSize) {

        SearchRequest search = new SearchRequest("wang_index");
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //精准匹配
        TermQueryBuilder title = QueryBuilders.termQuery("title", keywords);

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder
                .field("title")
                .preTags("<span color='red'>")
                .postTags("</span>")
                .requireFieldMatch(false)
                .fragmentSize(80000)
                .numOfFragments(0);

        //添加进条件构造器
        builder
                .query(title)
                .from(pageNo)
                .size(pageSize)
                .timeout(TimeValue.timeValueNanos(60))
                .highlighter(highlightBuilder);

        search.source(builder);

        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(search, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //解析结果
        ArrayList<Map<String, Object>> resMap = new ArrayList<>();
        if (searchResponse.getHits() != null && searchResponse.getHits().getHits() != null) {
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                //原数据
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //高亮数据
                HighlightField highlightField = hit.getHighlightFields().get("title");
                String new_title = "";
                for (Text fragment : highlightField.fragments()) {
                    new_title += fragment;
                }
                //将原字段替换成高亮字段
                sourceAsMap.put("title",new_title);
                //将结果加入集合
                resMap.add(sourceAsMap);
            }
        }

        return resMap;

    }

}
