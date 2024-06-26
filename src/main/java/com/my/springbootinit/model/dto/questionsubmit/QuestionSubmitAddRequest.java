package com.my.springbootinit.model.dto.questionsubmit;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *

 */
@Data
public class QuestionSubmitAddRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 题目id
     */
    private Long questionId;


    private static final long serialVersionUID = 1L;
}