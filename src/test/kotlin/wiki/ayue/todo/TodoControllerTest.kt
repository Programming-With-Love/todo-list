package wiki.ayue.todo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.templates.TemplateFormats
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import wiki.ayue.todo.component.JwtProcessor
import wiki.ayue.todo.controller.TodoController
import wiki.ayue.todo.model.entity.Todo
import wiki.ayue.todo.repostiory.TodoRepository

@SpringBootTest
@ExtendWith(RestDocumentationExtension::class, SpringExtension::class)
@AutoConfigureRestDocs
class TodoControllerTest {

  private lateinit var client: WebTestClient

  @Autowired
  private lateinit var todoRepository: TodoRepository

  @Autowired
  private lateinit var todoController: TodoController

  @Autowired
  private lateinit var jwtProcessor: JwtProcessor

  private lateinit var token: String

  @BeforeEach
  internal fun setUp(restDocumentation: RestDocumentationContextProvider) {
    client = WebTestClient.bindToController(todoController)
      .configureClient()
      .baseUrl("/todo")
      .filter(WebTestClientRestDocumentation.documentationConfiguration(restDocumentation).snippets().withTemplateFormat(TemplateFormats.asciidoctor()))
      .build()

    val save = todoRepository.saveAll(Flux.range(1, 50).map {
      Todo(
        name = "todo${it}",
        description = "todoList${it}",
        project = "group-${it % 2}",
        finished = (it % 2 == 0)
      )
    })
    StepVerifier.create(save)
      .expectNextCount(50)
      .verifyComplete()
    token = jwtProcessor.generate("test", mapOf(
      "project" to "group-0"
    ))
  }

  @Test
  @WithMockUser
  internal fun `test get all todo`() {
    client
      .get()
      .uri("/")
      .headers { header -> header.setBearerAuth("token") }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody()
      .consumeWith(WebTestClientRestDocumentation.document("todo-list",
        requestHeaders(
          headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ), responseTodo("[].")))
  }

  @Test
  @WithMockUser
  internal fun `test add todo item`() {
    client
      .post()
      .uri("/")
      .body(BodyInserters.fromValue(mapOf(
        "name" to "test1",
        "description" to "test1",
        "finished" to true
      )))
      .headers { header -> header.setBearerAuth("token") }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody()
      .consumeWith(WebTestClientRestDocumentation.document("todo-add",
        requestHeaders(
          headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        requestFields(
          fieldWithPath("name").description("Register user name"),
          fieldWithPath("description").optional().description("Register user password"),
          fieldWithPath("finished").optional().type(JsonFieldType.BOOLEAN).description("Client project id"),
          fieldWithPath("information").optional().type(JsonFieldType.OBJECT).description("Todo Item Information")
        ), responseTodo()
      ))
  }

  @Test
  @WithMockUser
  internal fun `test delete todo item`() {
    StepVerifier.create(todoRepository.save(Todo(id = "123456")))
      .expectNextCount(1)
      .verifyComplete()
    client
      .delete()
      .uri("/{id}", "123456")
      .headers { header -> header.setBearerAuth("token") }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody()
      .consumeWith(WebTestClientRestDocumentation.document("todo-delete",
        requestHeaders(
          headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        pathParameters(
          parameterWithName("id").description("Todo Item Id")
        )))
  }

  @Test
  @WithMockUser
  internal fun `test get one todo`() {
    StepVerifier.create(todoRepository.save(
      Todo(id = "123456789", project = "group-0", name = "save",
        description = "des")
    ))
      .expectNextCount(1)
      .verifyComplete()
    client
      .get()
      .uri("/{id}", "123456789")
      .headers { header -> header.setBearerAuth("token") }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody()
      .consumeWith(WebTestClientRestDocumentation.document("todo-one",
        requestHeaders(
          headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        pathParameters(
          parameterWithName("id").description("Todo Item Id")
        ),
        responseTodo()
      ))
  }

  @Test
  @WithMockUser
  internal fun `test update todo item`() {
    StepVerifier.create(todoRepository.save(Todo(id = "23456", name = "2345")))
      .expectNextCount(1)
      .verifyComplete()
    client.put()
      .uri("/")
      .bodyValue(mapOf(
        "id" to "23456",
        "name" to "todo",
        "description" to "description"
      ))
      .headers { header -> header.setBearerAuth("token") }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody().jsonPath("name").isEqualTo("todo")
      .consumeWith(WebTestClientRestDocumentation.document("todo-update",
        requestHeaders(
          headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer Token")
        ),
        requestFields(
          fieldWithPath("id").description("Todo item id"),
          fieldWithPath("name").description("Todo item name"),
          fieldWithPath("description").optional().type(JsonFieldType.STRING).description("Register user password"),
          fieldWithPath("finished").optional().type(JsonFieldType.BOOLEAN).description("Client project id"),
          fieldWithPath("information").optional().type(JsonFieldType.OBJECT).description("Todo Item Information")
        ),
        responseTodo()
      ))
  }

  private fun responseTodo(prefix: String = "") =
    responseFields(
      fieldWithPath("${prefix}id").type(JsonFieldType.STRING).description("Todo Item Id"),
      fieldWithPath("${prefix}name").type(JsonFieldType.STRING).description("Todo Item Name"),
      fieldWithPath("${prefix}description").type(JsonFieldType.STRING).description("Todo Item Description"),
      fieldWithPath("${prefix}project").type(JsonFieldType.STRING).description("Todo Item Project"),
      fieldWithPath("${prefix}finished").type(JsonFieldType.BOOLEAN).description("Todo Item Finished"),
      fieldWithPath("${prefix}information").optional().type(JsonFieldType.OBJECT).description("Todo Item Information"),
      fieldWithPath("${prefix}createDate").type("DATETIME").description("Todo Item Create Date"),
      fieldWithPath("${prefix}modifyDate").type("DATETIME").description("Todo Item Modify Date")
    )
}