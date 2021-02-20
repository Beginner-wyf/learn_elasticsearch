package com.wyf.elasticsearch_api;

import com.alibaba.fastjson.JSON;
import com.wyf.elasticsearch_api.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ElasticsearchApiApplicationTests {

    @Autowired
    @Qualifier("restHighLevelClient") //用于指定自动注入的资源名称，若不加词注解指定，创建的对象名需要与资源名保持一致
    private RestHighLevelClient client;
    //private RestHighLevelClient restHighLevelClient;

    /**
     * 创建索引 PUT wang_index
     */
    @Test
    void testCreateIndex() throws IOException {
        //1、创建所以请求
        CreateIndexRequest reques = new CreateIndexRequest("wang_index");
        //2、执行请求 IndicesClient,请求后获得响应
        CreateIndexResponse createIndexResponse =
                client.indices().create(reques, RequestOptions.DEFAULT);

        System.out.println(createIndexResponse.index());
    }

    /**
     * 测试获取索引
     */
    @Test
    void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("wang_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * 测试删除索引
     */
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("wang_index");
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete);
    }

    /**
     * 测试添加文档
     */
    @Test
    void testAddDocument() throws IOException {
        //创建对象
        User user = new User("大小王", 23);
        //创建请求
        IndexRequest request = new IndexRequest("wang_index");
        //创建规则 put/wang_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        //效果同上
        request.timeout("1s");

        //放入数据请求放入请求json
        request.source(JSON.toJSONString(user), XContentType.JSON);

        //客户端发送请求
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

        //索引使用状态
        System.out.println(indexResponse.status());
        System.out.println(indexResponse.toString());
    }

    /**
     * 测试文档是否存在get/index/doc/1
     */
    @Test
    void testExistDocument() throws IOException {
        GetRequest request = new GetRequest("wang_index", "1");
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");
        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * 测试获取文档
     */
    @Test
    void testGetDocument() throws IOException {
        GetRequest request = new GetRequest("wang_index", "1");
        GetResponse getResponse = client.get(request, RequestOptions.DEFAULT);

        System.out.println(getResponse.getIndex());
        System.out.println(getResponse.getId());
        System.out.println(getResponse.getSourceAsString());
        System.out.println(getResponse.getSource());
        System.out.println(getResponse.getSourceAsMap());
        System.out.println(getResponse);
    }

    /**
     * 测试更新文档
     */
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("wang_index", "1");
        request.timeout("1s");

        User user = new User("小小王", 2);
        request.doc(JSON.toJSONString(user), XContentType.JSON);

        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);

        System.out.println(update.getIndex());
        System.out.println(update.getId());
        System.out.println(update.getVersion());
        System.out.println(update.status());

    }

    /**
     * 测试删除文档
     */
    @Test
    void testDeleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("wang_index", "1");
        request.timeout("1s");

        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);

        System.out.println(delete.getIndex());
        System.out.println(delete.status());
        System.out.println(delete.status());
    }

    /**
     * 测试批量导入数据
     */
    @Test
    void testBulkDocument() throws IOException {

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueSeconds(10));

        ArrayList<User> users = new ArrayList<>();
        users.add(new User("java1", 22));
        users.add(new User("java2", 23));
        users.add(new User("java3", 24));
        users.add(new User("java4", 25));
        users.add(new User("java5", 26));
        users.add(new User("java6", 27));
        users.add(new User("java7", 28));

        //批量添加请求
        for (int i = 0; i < users.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("wang_index")
                            //.id(""+(i+1)) //此处可以不添加id，会自动生成随机id
                            .source(JSON.toJSONString(users.get(i)), XContentType.JSON)
            );
        }
        BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        System.out.println(bulk.getItems());
        System.out.println(bulk);

    }

    /**
     * 测试查询数据
     */
    @Test
    void testSearchDocument() throws IOException {
        SearchRequest request = new SearchRequest("wang_index");
        //构建查询条件
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //高亮查询
        builder.highlighter();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮字段
        highlightBuilder.field("name");
        //前置标签
        highlightBuilder.preTags("<span style='color:red'>");
        //后置标签
        highlightBuilder.postTags("</span>");
        //是否开启多字段高亮匹配
        highlightBuilder.requireFieldMatch(false);
        //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
        highlightBuilder.fragmentSize(800000); //最大高亮分片数
        highlightBuilder.numOfFragments(0); //从第一个分片获取高亮片段

        builder.highlighter(highlightBuilder);
        //精确匹配
        //TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("age", "23");
        //匹配所有
        //MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "飞行员1");

        builder.query(matchQueryBuilder);
        builder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        request.source(builder);
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField name = highlightFields.get("name");
            Text[] fragments = name.fragments();
            String n_name = "";
            for (Text fragment : fragments) {
                n_name += fragment;
            }
            System.out.println("原字段:"+hit.getSourceAsMap()+"高亮字段:"+n_name);
            System.out.println("================");
        }
    }

}
