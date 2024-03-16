package com.my.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.my.springbootinit.annotation.AuthCheck;
import com.my.springbootinit.common.BaseResponse;
import com.my.springbootinit.common.DeleteRequest;
import com.my.springbootinit.common.ErrorCode;
import com.my.springbootinit.common.ResultUtils;
import com.my.springbootinit.constant.UserConstant;
import com.my.springbootinit.exception.BusinessException;
import com.my.springbootinit.exception.ThrowUtils;
import com.my.springbootinit.model.comment.CommentAddRequest;
import com.my.springbootinit.model.comment.CommentQueryRequest;
import com.my.springbootinit.model.comment.CommentUpdateRequest;
import com.my.springbootinit.model.entity.Comment;
import com.my.springbootinit.model.entity.User;
import com.my.springbootinit.model.vo.CommentVO;
import com.my.springbootinit.service.CommentService;
import com.my.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 黎海旭
 **/
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {
    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param commentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Comment comment = new Comment();

        List<String> tags = commentAddRequest.getTags();
        if (tags != null) {
            comment.setTags(GSON.toJson(tags));
        }

        BeanUtils.copyProperties(commentAddRequest, comment);
        User loginUser = userService.getLoginUser(request);
        comment.setUserId(loginUser.getId());
        comment.setThumbNum(0);
        boolean result = commentService.save(comment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        long newCommentId = comment.getId();
        /**
         * 检查是否是回复别人的评论，是的话就往被回复的评论的tags里加入自己的id
         */
        Long resCommentId = commentAddRequest.getResCommentId();
        if (resCommentId != null){
            Comment comment1 = commentService.getById(resCommentId);

            List<String> tagList = JSONUtil.toList(comment1.getTags(), String.class);
            tagList.add(String.valueOf(newCommentId));
            comment1.setTags(GSON.toJson(tagList));
            boolean update = commentService.updateById(comment1);
            ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        }

        return ResultUtils.success(newCommentId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Comment oldComment = commentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldComment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = commentService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param commentUpdateRequest
     * @return
     */
    @PostMapping("/update")
    //AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateComment(@RequestBody CommentUpdateRequest commentUpdateRequest) {
        if (commentUpdateRequest == null || commentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentUpdateRequest, comment);
        List<String> tags = commentUpdateRequest.getTags();
        if (tags != null) {
            comment.setTags(GSON.toJson(tags));
        }

        long id = commentUpdateRequest.getId();
        // 判断是否存在
        Comment oldComment = commentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = commentService.updateById(comment);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Comment> getCommentVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId",id);
        Comment comment = commentService.getOne(queryWrapper);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(comment);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentQueryRequest commentQueryRequest,
                                                             HttpServletRequest request) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Comment> commentPage = commentService.page(new Page<>(current, size),
                commentService.getQueryWrapper(commentQueryRequest));
        return ResultUtils.success(commentService.getCommentVOPage(commentPage, request));
    }


}
