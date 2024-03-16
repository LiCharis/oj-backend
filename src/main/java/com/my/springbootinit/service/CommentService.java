package com.my.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.springbootinit.model.comment.CommentQueryRequest;
import com.my.springbootinit.model.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.my.springbootinit.model.entity.Post;
import com.my.springbootinit.model.vo.CommentVO;
import com.my.springbootinit.model.vo.PostVO;

import javax.servlet.http.HttpServletRequest;


/**
* @author Li
* @description 针对表【comment(评论表)】的数据库操作Service
* @createDate 2023-11-21 22:09:36
*/
public interface CommentService extends IService<Comment> {
    /**
     * 获取查询条件
     *
     * @param commentQueryRequest
     * @return
     */
    QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);


    /**
     * 分页获取帖子封装
     *
     * @param commentPage
     * @param request
     * @return
     */
    Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request);

}
