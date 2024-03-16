package com.my.springbootinit.judge.strategy;

import com.my.springbootinit.judge.codesandbox.model.JudgeInfo;

/**
 * @author 黎海旭
 * 判题策略，根据不同的语言执行不同的判题逻辑
 * 运用了策略模式
 **/
public interface JudgeStrategy {
    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
