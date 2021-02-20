package com.wyf.util;

import com.wyf.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author wangyifan
 * @create 2021/2/20 9:31
 */
@Component
public class HtmlParseUtils {

    public ArrayList<Content> parseJD(String keywords){
        //获取京东请求 https://search.jd.com/Search?keyword=elasticsearch
        String url = "https://search.jd.com/Search?keyword=" + keywords;

        //解析网页
        Document document = null;
        try {
            document = Jsoup.parse(new URL(url), 30 * 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //document元素可以使用js里的方法
        Element j_goodsList = document.getElementById("J_goodsList");
        //获取所有的li标签
        Elements elements = j_goodsList.getElementsByTag("li");

        //创建内容集合
        ArrayList<Content> goodsList = new ArrayList<>();

        for (Element el : elements) {
            //关于图片：企业级开发中，图片都是懒加载，不会直接显示出来，所以要从data-lazy-img中获取
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();

            //加入集合
            goodsList.add(new Content(img,title,price));
        }
        return goodsList;
    }
}
