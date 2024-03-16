package com.my.springbootinit.judge;

import com.my.springbootinit.model.entity.QuestionSubmit;
import com.my.springbootinit.model.vo.QuestionVO;

/**
 * @author 黎海旭
 * 判题服务
 **/

public interface JudgeService {

    QuestionSubmit doJudge(long questionSubmitId);
}
