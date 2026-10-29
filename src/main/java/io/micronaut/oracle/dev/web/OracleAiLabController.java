package io.micronaut.oracle.dev.web;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.ModelAndView;

import java.util.Map;

@Controller
@Produces(MediaType.TEXT_HTML)
@Secured(SecurityRule.IS_ANONYMOUS)
public class OracleAiLabController {

  @Get("/")
  public ModelAndView<Map<String, Object>> index() {
    return view("index", "home", "Oracle AI Lab");
  }

  @Get("/memory")
  public ModelAndView<Map<String, Object>> memory() {
    return view("memory", "memory", "Durable Memory · Oracle AI Lab");
  }

  @Get("/rag")
  public ModelAndView<Map<String, Object>> rag() {
    return view("rag", "rag", "RAG · Oracle AI Lab");
  }

  @Get("/agent")
  public ModelAndView<Map<String, Object>> agent() {
    return view("agent", "agent", "Agent · Oracle AI Lab");
  }

  private ModelAndView<Map<String, Object>> view(String template, String activePage, String pageTitle) {
    return new ModelAndView<>(template, Map.of("applicationName", "Oracle AI Lab", "activePage", activePage, "apiBase",
        "/api", "pageTitle", pageTitle));
  }
}
