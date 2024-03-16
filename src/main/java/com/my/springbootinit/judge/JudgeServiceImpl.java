package com.my.springbootinit.judge;

import cn.hutool.json.JSONUtil;
import com.my.springbootinit.common.ErrorCode;
import com.my.springbootinit.exception.BusinessException;
import com.my.springbootinit.judge.codesandbox.CodeSandBox;
import com.my.springbootinit.judge.codesandbox.CodeSandBoxFactory;
import com.my.springbootinit.judge.codesandbox.CodeSandBoxProxy;
import com.my.springbootinit.judge.codesandbox.model.ExecuteCodeRequest;
import com.my.springbootinit.judge.codesandbox.model.ExecuteCodeResponse;
import com.my.springbootinit.judge.strategy.JudgeContext;
import com.my.springbootinit.model.dto.question.JudgeCase;
import com.my.springbootinit.judge.codesandbox.model.JudgeInfo;
import com.my.springbootinit.model.entity.Question;
import com.my.springbootinit.model.entity.QuestionSubmit;
import com.my.springbootinit.model.enums.JudgeInfoMessageEnum;
import com.my.springbootinit.model.enums.QuestionSubmitStateEnum;
import com.my.springbootinit.service.QuestionService;
import com.my.springbootinit.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 黎海旭
 **/
@Service
public class JudgeServiceImpl implements JudgeService {


    //从配置文件读取代码沙箱类型属性(不设置的话默认值为example)
    @Value("${codesandbox.type}")
    private String type;

    /**
     * 获取题目信息
     */
    @Resource
    private QuestionService questionService;

    /**
     * 获取题目提交信息
     */
    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;


    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);

        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Question question = questionService.getById(questionSubmit.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目信息不存在");
        }
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();

        //如果不为等待状态
        //todo 等待完善
        if (!questionSubmit.getStatus().equals(QuestionSubmitStateEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中或已经判题完毕");
        }

        //修改此刻的判题状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStateEnum.RUNNING.getValue());
        boolean b = questionSubmitService.updateById(questionSubmitUpdate);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");
        }


        /**
         * 1.调用代码沙箱
         */

        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        CodeSandBoxProxy codeSendBoxProxy = new CodeSandBoxProxy(codeSandBox);

        //获取输入输出用例
        String judgeCase = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCase, JudgeCase.class);
        //预期输入列表
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        //预期输出列表
        List<String> correct_outputList = judgeCaseList.stream().map(JudgeCase::getOutput).collect(Collectors.toList());

        //设置传给沙箱的请求
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code).language(language).inputList(inputList).build();

         //========================
        //代理类调用代码沙箱完成上传代码的编译执行然后返回结果
        ExecuteCodeResponse executeCodeResponse = codeSendBoxProxy.codeExecute(executeCodeRequest);
        //=========================

        /**
         *  2.校验题目是否正确 start
         *    1)得到返回结果，并根据返回结果判断用户的代码是否正确(是否符合题目规定的输出)
         *    2)根据沙箱的执行结果，来设置题目的判断状态和信息
         */
        Integer status = executeCodeResponse.getStatus();

        //判题信息
        JudgeInfo judgeResponse = null;

        //不是沙箱错误或者代码编译错误，就是所有用例运行正常或者部分有错
        if (status != 2) {
            List<String> outputList = executeCodeResponse.getOutputList();
            JudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();


            //给定判题上下文信息
            JudgeContext judgeContext = new JudgeContext();
            judgeContext.setCorrect_outputList(correct_outputList);
            judgeContext.setOutputList(outputList);
            judgeContext.setQuestion(question);
            judgeContext.setJudgeInfo(judgeInfo);
            judgeContext.setQuestionSubmit(questionSubmit);


            //  交由判题管理来选取对应的判题策略(亮点)
            judgeResponse = judgeManager.doJudgeManage(judgeContext);
            /**
             * 校验题目是否正确 end
             */

            /**
             * 4.修改数据库的判题结果
             */
            //判题状态的成功
            questionSubmitUpdate.setStatus(QuestionSubmitStateEnum.SUCCEED.getValue());
        } else {
            //判题状态失败，因沙箱错误或者代码编译失败
            judgeResponse = new JudgeInfo();
            judgeResponse.setMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getText());
            judgeResponse.setMemory(0L);
            judgeResponse.setTime(0L);

            questionSubmitUpdate.setStatus((QuestionSubmitStateEnum.FAILED.getValue()));
        }


        //存入判题结果
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeResponse));
        boolean save = questionSubmitService.updateById(questionSubmitUpdate);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目判题结果更新失败");
        }

        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionSubmitId);

        //submitNum增加，根据判题结果改变acceptNum数量
        Question questionResult = questionService.getById(questionSubmitResult.getQuestionId());
        questionResult.setSubmitNum(questionResult.getSubmitNum() + 1);
        if (judgeResponse.getMessage().equals(JudgeInfoMessageEnum.ACCEPT.getText())) {
            questionResult.setAcceptedNum(questionResult.getAcceptedNum() + 1);
        }
        boolean update = questionService.updateById(questionResult);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目接收/提交数量更新错误");
        }

        return questionSubmitResult;
    }
}
