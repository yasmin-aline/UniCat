package br.com.unicat.poc.shared.config;

import br.com.unicat.poc.v1.controller.context.RequestContextResolver;
import br.com.unicat.poc.v2.controller.context.RequestContextResolverV2;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final RequestContextResolver requestContextResolver;
  private final RequestContextResolverV2 requestContextResolverV2;

  public WebConfig(RequestContextResolver requestContextResolver, RequestContextResolverV2 requestContextResolverV2) {
    this.requestContextResolver = requestContextResolver;
	  this.requestContextResolverV2 = requestContextResolverV2;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(requestContextResolver);
    resolvers.add(requestContextResolverV2);
  }
}
