package com.my.springbootinit.judge.codesandbox.Impl;

import com.my.springbootinit.judge.codesandbox.model.ExecuteCodeRequest;
import com.my.springbootinit.judge.codesandbox.CodeSandBox;
import com.my.springbootinit.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * @author 黎海旭
 * 第三方代码沙箱
 **/
public class ThirdPartyCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse codeExecute(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
