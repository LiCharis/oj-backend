package com.my.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.springbootinit.annotation.AuthCheck;
import com.my.springbootinit.common.BaseResponse;
import com.my.springbootinit.common.ErrorCode;
import com.my.springbootinit.common.ResultUtils;
import com.my.springbootinit.constant.UserConstant;
import com.my.springbootinit.exception.BusinessException;
import com.my.springbootinit.model.dto.question.QuestionQueryRequest;
import com.my.springbootinit.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.my.springbootinit.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.my.springbootinit.model.entity.Question;
import com.my.springbootinit.model.entity.QuestionSubmit;
import com.my.springbootinit.model.entity.User;
import com.my.springbootinit.model.vo.QuestionSubmitVO;
import com.my.springbootinit.service.QuestionSubmitService;
import com.my.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 *

 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
@Deprecated
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 题目提交 / 取消题目提交
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return resultNum 本次题目提交的id
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能题目提交
        final User loginUser = userService.getLoginUser(request);
        Long result = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(result);
    }



    /**
     * 分页获取题目提交列表,除管理员外，普通用户只能看到本人的所有提交信息及非本人的提交问题的非代码信息
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    //@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)不需要权限
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        //从数据库中得到经分页后的原始的数据
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        // 登录才能题目提交
        final User loginUser = userService.getLoginUser(request);
        //返回脱敏信息
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage,request,loginUser));
    }

}
