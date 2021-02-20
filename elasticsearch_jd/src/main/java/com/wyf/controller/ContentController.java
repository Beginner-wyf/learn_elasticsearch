package com.wyf.controller;

import com.wyf.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author wangyifan
 * @create 2021/2/20 14:52
 */
@RestController
public class ContentController {
    @Autowired
    private ContentService contentService;

    @GetMapping("/parse/{keywords}")
    public boolean parse(@PathVariable("keywords") String keywords) {
        return contentService.parseContent(keywords);
    }


    @GetMapping({"/search/{keywords}/{pageNo}/{pageSize}", "/search/{keywords}"})
    public ArrayList<Map<String, Object>> searchByPage(@PathVariable(value = "keywords", required = false) String keywords,
                                                       @PathVariable(value = "pageNo", required = false) Integer pageNo,
                                                       @PathVariable(value = "pageSize", required = false) Integer pageSize) {
        if (pageNo == null && pageSize == null) {
            return contentService.searchByPage(keywords, 1, 10);
        }
        return contentService.searchByPage(keywords, pageNo, pageSize);
    }
}
