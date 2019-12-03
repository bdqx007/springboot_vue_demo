package com.wg.config.exception;

import com.wg.config.ResultMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * @Author: wanggang
 * @Date: 2018/9/25 11:43
 * @todo
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    Logger logger = LoggerFactory.getLogger(getClass().getName());

    @ExceptionHandler(value = Throwable.class)
    @ResponseBody
    public ResultMsg jsonErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        ResultMsg r = new ResultMsg();
        String msg = e.getMessage();
        logger.info(msg);
        r.setResultMsg(msg);
        r.setResult("FAILED");
        r.setResultCode(500);
        e.printStackTrace();
        return r;
    }

    /**
     * 处理 接口无权访问异常AccessDeniedException
     */
//    @ExceptionHandler(value = AccessDeniedException.class)
//    @ResponseBody
//    public ResultMsg handleAccessDeniedException(AccessDeniedException e){
//        // 打印堆栈信息
//        ResultMsg msg = new ResultMsg();
//        msg.setResult("FAILED");
//        msg.setResultMsg(e==null?"Unauthorized":e.getMessage());
//        msg.setResultCode(FORBIDDEN.value());
//        return msg;
//    }


}
