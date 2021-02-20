package com.wyf.elasticsearch_api.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author wangyifan
 * @create 2021/2/18 11:59
 */

@Data
@NoArgsConstructor  //无参数构造器
@AllArgsConstructor //全参数构造器
@Component //注入spring中
public class User {
    private String name;
    private int age;
}
