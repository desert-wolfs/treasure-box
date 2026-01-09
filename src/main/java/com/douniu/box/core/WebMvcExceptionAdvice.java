package com.douniu.box.core;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * WebMvcExceptionAdvice
 * WebMvc异常处理（全局异常）
 *
 */
@RestControllerAdvice
public class WebMvcExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(WebMvcExceptionAdvice.class);

    /**
     * 业务异常
     *
     * @return error
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Resp<?> businessException(BusinessException e) {
        return Resp.init(e.getIntegerCode(), e.getMsg(), e.getData());
    }

  
    /**
     * 请求类型不支持异常
     *
     * @param exception exception
     * @return error
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Resp<Void> requestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        String[] supportedMethods = exception.getSupportedMethods();
        return Resp.error(400, "Method 仅支持: " + Arrays.toString(supportedMethods));
    }

    /**
     * 请求方式不支持异常
     *
     * @param exception exception
     * @return error
     */
    @ExceptionHandler(HttpMediaTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Resp<Void> httpMediaTypeException(HttpMediaTypeException exception) {
        return Resp.error(400, exception.getMessage());
    }

    /**
     * 请求缺少必要参数异常
     *
     * @param exception exception
     * @return error
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public Resp<Void> missingServletRequestParameterException(Exception exception) {
        String parameterName = exception instanceof MissingServletRequestParameterException
                ? ((MissingServletRequestParameterException) exception).getParameterName()
                : exception instanceof MissingServletRequestPartException
                ? ((MissingServletRequestPartException) exception).getRequestPartName()
                : "null";

        return Resp.error(412, "缺少必要参数: " + parameterName);
    }

    /**
     * 参数类型不匹配
     *
     * @param exception TypeMismatchException
     * @return Resp<Void>
     */
    @ExceptionHandler(TypeMismatchException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public Resp<Void> typeMismatchException(TypeMismatchException exception) {
        String propertyName = exception.getPropertyName();
        return Resp.error(412, "参数类型不匹配: " + propertyName);
    }

    /**
     * 消息无法读取异常
     *
     * @return error
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public Resp<Void> messageNotReadableException() {
        return Resp.error(BusinessException.CodeKey.paramErr);
    }

    /**
     * 数据校验异常
     *
     * @param exception exception
     * @return error
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public Resp<?> validExceptionHandler(Exception exception) {
        BindingResult bindingResp = exception instanceof MethodArgumentNotValidException
                ? ((MethodArgumentNotValidException) exception).getBindingResult()
                : ((BindException) exception).getBindingResult();

        return bindingResp.getFieldErrors()
                .stream()
                .map(error -> {
                    String message = error.getField() + ": " + error.getDefaultMessage();
                    return Resp.error(90000, message);
                })
                .findFirst()
                .orElse(Resp.error(BusinessException.CodeKey.paramErr));
    }

    /**
     * Multipart表单异常
     *
     * @param exception exception
     * @return error
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MultipartException.class)
    public Resp<Void> multipartExceptionHandle(MultipartException exception) {
        if (exception instanceof MaxUploadSizeExceededException) {
            long maxUploadSize = ((MaxUploadSizeExceededException) exception).getMaxUploadSize();
            String message = MessageFormat.format("超过最大上传文件大小 {0} bytes",
                    maxUploadSize > 0 ? maxUploadSize : "unknown");

            return Resp.error(400, message);
        }

        // 未知异常返回异常消息
        return Resp.error(400, exception.getMessage());
    }

    /**
     * Feign调用异常
     *
     * @return error
     */
//    @ExceptionHandler(RetryableException.class)
//    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
//    public Resp<Void> retryableException(RetryableException e) {
//        log.error("Feign调用异常", e);
//        return Resp.error(CodeKey.remoteInvoke);
//    }

    /**
     * 系统异常，请联系管理员！
     *
     * @return error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Resp<Void> defaultSystemException(Exception e) {
        if (e instanceof ClientAbortException) {
            log.error("系统异常，响应式客户端连接已经断开，请联系管理员！");
            return Resp.error("系统异常，请联系管理员2！");
        }
        log.error("系统异常，请联系管理员！", e);
        return Resp.error("系统异常，请联系管理员！");
    }

}
