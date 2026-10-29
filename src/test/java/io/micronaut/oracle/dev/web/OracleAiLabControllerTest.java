package io.micronaut.oracle.dev.web;

import io.micronaut.views.ModelAndView;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OracleAiLabControllerTest {

  @Test
  void servesSeparateFeatureTemplatesWithSharedModelValues() {
    OracleAiLabController controller = new OracleAiLabController();

    assertView(controller.index(), "index", "home", "Oracle AI Lab");
    assertView(controller.memory(), "memory", "memory", "Durable Memory · Oracle AI Lab");
    assertView(controller.rag(), "rag", "rag", "RAG · Oracle AI Lab");
    assertView(controller.agent(), "agent", "agent", "Agent · Oracle AI Lab");
  }

  private void assertView(ModelAndView<Map<String, Object>> view, String template, String activePage,
      String pageTitle) {
    assertEquals(template, view.getView().orElseThrow());
    Map<String, Object> model = view.getModel().orElseThrow();
    assertEquals("Oracle AI Lab", model.get("applicationName"));
    assertEquals("/api", model.get("apiBase"));
    assertEquals(activePage, model.get("activePage"));
    assertEquals(pageTitle, model.get("pageTitle"));
  }
}
