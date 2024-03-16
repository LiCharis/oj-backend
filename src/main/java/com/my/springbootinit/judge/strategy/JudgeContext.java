package com.my.springbootinit.judge.strategy;

import com.my.springbootinit.judge.codesandbox.model.JudgeInfo;
import com.my.springbootinit.model.entity.Question;
import com.my.springbootinit.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * @author 黎海旭
 * 判题所需要的上下文信息
 **/
@Data
public class JudgeContext {

    private List<String> correct_outputList;

    private List<String> outputList;

    private Question question;

   private JudgeInfo judgeInfo;

   private QuestionSubmit questionSubmit;
}
