package br.com.unicat.poc.v2.controller.context;

public class RequestContextHolderV2 {
  private static final ThreadLocal<RequestContextV2> contextHolder = new ThreadLocal<>();

  public static void setContext(RequestContextV2 context) {
    contextHolder.set(context);
  }

  public static RequestContextV2 getContext() {
    return contextHolder.get();
  }

  public static void clear() {
    contextHolder.remove();
  }
}
