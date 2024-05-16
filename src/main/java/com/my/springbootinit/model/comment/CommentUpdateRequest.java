package com.my.springbootinit.model.comment;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新请求
 *

 */
@Data
public class CommentUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}