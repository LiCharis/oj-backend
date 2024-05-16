package com.my.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.springbootinit.model.dto.question.QuestionQueryRequest;
import com.my.springbootinit.model.entity.Post;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子服务测试
 *

 */
@SpringBootTest
class PostServiceTest {

    @Resource
    private PostService postService;

//    @Test
//    void searchFromEs() {
//        QuestionQueryRequest questionQueryRequest = new QuestionQueryRequest();
//        questionQueryRequest.setUserId(1L);
//        Page<Post> postPage = postService.searchFromEs(questionQueryRequest);
//        Assertions.assertNotNull(postPage);
//    }

}