package com.lianbei.vc.exception;

import com.lianbei.vc.common.Result;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoResourceFoundException.class)
  public Result<Void> handleNotFound(NoResourceFoundException e) {
    return Result.fail(404, "接口不存在: " + e.getResourcePath());
  }

  @ExceptionHandler(BusinessException.class)
  public Result<Void> handleBusiness(BusinessException e) {
    return Result.fail(e.getCode(), e.getMessage());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public Result<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
    return Result.fail(
        400,
        "上传文件过大，单个文件不超过 50MB，总请求不超过 100MB（可去掉头像 ZIP 后重试）");
  }

  @ExceptionHandler(Exception.class)
  public Result<Void> handleException(Exception e) {
    return Result.fail(500, "服务器内部错误: " + e.getMessage());
  }
}
