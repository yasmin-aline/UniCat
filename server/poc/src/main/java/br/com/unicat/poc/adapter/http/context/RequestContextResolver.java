package br.com.unicat.poc.adapter.http.context;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class RequestContextResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(RequestContext.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    String targetClassName = webRequest.getParameter("targetClassName");
    String targetClassCode = webRequest.getParameter("targetClassCode");
    String targetClassPackage = webRequest.getParameter("targetClassPackage");

    RequestContext context =
        RequestContext.builder()
            .targetClassName(targetClassName)
            .targetClassCode(targetClassCode)
            .targetClassPackage(targetClassPackage)
            .build();

    return context;
  }
}
