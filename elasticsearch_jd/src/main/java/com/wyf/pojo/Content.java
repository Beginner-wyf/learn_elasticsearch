package com.wyf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangyifan
 * @create 2021/2/20 10:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Content {
    private String img;
    private String title;
    private String price;
}
