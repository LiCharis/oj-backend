package com.my.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.springbootinit.constant.CommonConstant;
import com.my.springbootinit.model.comment.CommentQueryRequest;
import com.my.springbootinit.model.entity.*;
import com.my.springbootinit.model.vo.CommentVO;
import com.my.springbootinit.model.vo.QuestionVO;
import com.my.springbootinit.service.CommentService;
import com.my.springbootinit.mapper.CommentMapper;
import com.my.springbootinit.service.UserService;
import com.my.springbootinit.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Li
 * @description 针对表【comment(评论表)】的数据库操作Service实现
 * @createDate 2023-11-21 22:09:36
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {
    /**
     * 获取查询包装类
     *
     * @param commentQueryRequest
     * @return
     */

    @Resource
    private UserService userService;

    @Override
    public QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (commentQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = commentQueryRequest.getSortField();
        String sortOrder = commentQueryRequest.getSortOrder();
        Long id = commentQueryRequest.getId();
        String content = commentQueryRequest.getContent();
        List<String> tagList = commentQueryRequest.getTags();
        Long userId = commentQueryRequest.getUserId();
        Long questionId = commentQueryRequest.getQuestionId();
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);

        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request) {
        List<Comment> commentList = commentPage.getRecords();
        Page<CommentVO> CommentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        if (CollectionUtils.isEmpty(commentList)) {
            return CommentVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = commentList.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<CommentVO> CommentVOList = commentList.stream().map(comment -> {
            CommentVO commentVO = CommentVO.objToVo(comment);
            Long userId = comment.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            commentVO.setUser(user);
            return commentVO;
        }).collect(Collectors.toList());
        CommentVOPage.setRecords(CommentVOList);
        return CommentVOPage;
    }

}




