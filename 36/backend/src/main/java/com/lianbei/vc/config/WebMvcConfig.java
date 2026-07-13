package com.lianbei.vc.config;

import com.lianbei.vc.admin.config.AdminAuthInterceptor;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final AuthInterceptor authInterceptor;
  private final AdminAuthInterceptor adminAuthInterceptor;
  private final Path uploadRoot;

  public WebMvcConfig(
      AuthInterceptor authInterceptor,
      AdminAuthInterceptor adminAuthInterceptor,
      @Value("${vc.upload-dir:uploads}") String uploadDir) {
    this.authInterceptor = authInterceptor;
    this.adminAuthInterceptor = adminAuthInterceptor;
    this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOriginPatterns("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(false)
        .maxAge(3600);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(adminAuthInterceptor)
        .addPathPatterns("/api/admin/**")
        .excludePathPatterns("/api/admin/auth/login");
    registry
        .addInterceptor(authInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/admin/**");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = uploadRoot.toUri().toString();
    if (!location.endsWith("/")) {
      location = location + "/";
    }
    registry.addResourceHandler("/uploads/**").addResourceLocations(location);
  }
}
