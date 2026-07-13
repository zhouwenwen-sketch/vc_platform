package com.lianbei.vc.common;

import com.lianbei.vc.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

  private Integer code;
  private String message;
  private T data;

  public static <T> Result<T> ok(T data) {
    return Result.<T>builder().code(Constants.SUCCESS_CODE).message("success").data(data).build();
  }

  public static <T> Result<T> ok() {
    return ok(null);
  }

  public static <T> Result<T> fail(int code, String message) {
    return Result.<T>builder().code(code).message(message).data(null).build();
  }

  public static <T> Result<T> fail(String message) {
    return fail(400, message);
  }
}
