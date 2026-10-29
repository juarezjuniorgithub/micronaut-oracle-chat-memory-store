package io.micronaut.oracle.dev.api;

import io.micronaut.oracle.dev.agent.OracleKnowledgeAgent;
import io.micronaut.oracle.dev.assistant.OracleMemoryAssistant;
import io.micronaut.oracle.dev.observability.RequestTraceLoggingFilter;
import io.micronaut.oracle.dev.rag.KnowledgeDocumentService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.MediaType;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.opentelemetry.context.Context;

import java.util.List;

@Validated
@Controller("/api")
@Produces(MediaType.APPLICATION_JSON)
@Secured(SecurityRule.IS_ANONYMOUS)
@ExecuteOn(TaskExecutors.BLOCKING)
public class ChatController {

  private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

  private static final String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

  private final OracleMemoryAssistant assistant;
  private final KnowledgeDocumentService knowledgeDocuments;
  private final OracleKnowledgeAgent agent;

  public ChatController(OracleMemoryAssistant assistant, KnowledgeDocumentService knowledgeDocuments,
      OracleKnowledgeAgent agent) {
    this.assistant = assistant;
    this.knowledgeDocuments = knowledgeDocuments;
    this.agent = agent;
  }

  @Get("/architecture")
  public String architecture() {
    LOG.info("api.architecture.request");
    return "Oracle-backed MessageWindowChatMemory + Oracle AI Vector Search RAG + Micronaut agentic service";
  }

  @Post("/conversations")
  public HttpResponse<ConversationResponse> startConversation(@Valid @Body AskRequest request,
      HttpRequest<?> httpRequest) {
    LOG.info("api.conversation.start messageCharacters={}", request.message().length());
    return HttpResponse.created(assistant.start(request.message(), traceParentContext(httpRequest)));
  }

  @Post("/conversations/{conversationId}/messages")
  public ConversationResponse continueConversation(@PathVariable @Pattern(regexp = UUID_PATTERN) String conversationId,
      @Valid @Body AskRequest request, HttpRequest<?> httpRequest) {
    LOG.info("api.conversation.continue conversationId={} messageCharacters={}", conversationId,
        request.message().length());
    return assistant.respond(conversationId, request.message(), traceParentContext(httpRequest));
  }

  private static Context traceParentContext(HttpRequest<?> request) {
    return request.getAttribute(RequestTraceLoggingFilter.TRACE_PARENT_CONTEXT_ATTRIBUTE, Context.class)
        .orElse(Context.current());
  }

  @Post("/knowledge/documents")
  public HttpResponse<IndexResponse> indexDocument(@Valid @Body KnowledgeDocumentRequest request) {
    LOG.info("api.rag.index titleCharacters={} contentCharacters={}", request.title().length(),
        request.content().length());
    return HttpResponse.created(knowledgeDocuments.index(request));
  }

  @Post("/knowledge/demo-corpus")
  public List<IndexResponse> seedDemoCorpus() {
    LOG.info("api.rag.demo-corpus.seed");
    return knowledgeDocuments.seedDemoCorpus();
  }

  @Post("/agent/ask")
  public ConversationResponse askDeclarativeAgent(@Valid @Body AskRequest request) {
    LOG.info("api.agent.ask messageCharacters={}", request.message().length());
    return new ConversationResponse("agent:oracle-knowledge", agent.answer(request.message()), List.of());
  }
}
