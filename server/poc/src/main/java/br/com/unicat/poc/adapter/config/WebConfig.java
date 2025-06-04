package br.com.unicat.poc.adapter.config;

import br.com.unicat.poc.adapter.http.context.RequestContextResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final RequestContextResolver requestContextResolver;

  public WebConfig(RequestContextResolver requestContextResolver) {
    this.requestContextResolver = requestContextResolver;
  }

  @Override
  public void addArgumentResolvers(
      List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(requestContextResolver);
  }
}
