package br.com.unicat.poc.v2.controller.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class RequestContextResolverV2 implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return RequestContextV2.class.equals(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
    String classCode = request.getParameter("targetClassCode");
    String dependenciesCode = request.getParameter("dependenciesCode");
    String guidelines = request.getParameter("guidelines");

    RequestContextV2 context = new RequestContextV2(classCode, dependenciesCode, guidelines);
    RequestContextHolderV2.setContext(context);

    return context;
  }
}
