package com.lianbei.vc.config;

import com.lianbei.vc.common.MediaUrlResolver;
import com.lianbei.vc.common.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** API 响应中的 /uploads 相对路径统一补全为绝对地址 */
@RestControllerAdvice(basePackages = "com.lianbei.vc")
public class MediaUrlResponseBodyAdvice implements ResponseBodyAdvice<Object> {

  private final MediaUrlResolver mediaUrlResolver;

  public MediaUrlResponseBodyAdvice(MediaUrlResolver mediaUrlResolver) {
    this.mediaUrlResolver = mediaUrlResolver;
  }

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body instanceof Result<?> result && result.getData() != null) {
      mediaUrlResolver.resolveDeep(result.getData());
    }
    return body;
  }
}
