package wiki.ayue.todo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.templates.TemplateFormats
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import wiki.ayue.todo.controller.AuthController
import wiki.ayue.todo.model.entity.SysUser
import wiki.ayue.todo.repostiory.SysUserRepository


@SpringBootTest
@ExtendWith(RestDocumentationExtension::class, SpringExtension::class)
@AutoConfigureRestDocs
class AuthControllerTest {

  private lateinit var client: WebTestClient

  @MockBean
  private lateinit var sysUserRepository: SysUserRepository

  @Autowired
  private lateinit var authController: AuthController

  @Autowired
  private lateinit var passwordEncoder: PasswordEncoder

  @BeforeEach
  internal fun setUp(restDocumentation: RestDocumentationContextProvider) {
    client = WebTestClient.bindToController(authController)
      .configureClient()
      .baseUrl("/auth")
      .filter(documentationConfiguration(restDocumentation).snippets().withTemplateFormat(TemplateFormats.asciidoctor()))
      .build()
    given(sysUserRepository.findFirstByName("test"))
      .willReturn(Mono.just(SysUser(name = "test", password = passwordEncoder.encode("test"), project = "test")))
    given(sysUserRepository.existsByNameAndProject("test1", "test1"))
      .willReturn(Mono.just(false))
    given(sysUserRepository.save(any(SysUser::class.java)))
      .willReturn(Mono.just(SysUser(id = "test", name = "test1", password = null, project = "test1")))
  }

  @Test
  @Throws(Exception::class)
  fun `test user login success`() {
    client.post()
      .uri("/login")
      .bodyValue(mapOf(
        "name" to "test",
        "password" to "test",
        "project" to "test"
      ))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody().jsonPath("token").isNotEmpty
      .consumeWith(document("auth-register",
        requestFields(
          fieldWithPath("name").description("User name"),
          fieldWithPath("password").description("User password"),
          fieldWithPath("project").description("Client project id")
        ),
        responseFields(
          fieldWithPath("token").description("Authorization Bearer Token")
        )))
  }


  @Test
  internal fun `test user register success`() {
    client.post()
      .uri("/register")
      .body(BodyInserters.fromValue(mapOf(
        "name" to "test1",
        "password" to "test1",
        "project" to "test1",
        "profile" to mapOf(
          "email" to "test@163.com"
        )
      )))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody()
      .jsonPath("id").isNotEmpty
      .jsonPath("name").isEqualTo("test1")
      .jsonPath("project").isEqualTo("test1")
      .consumeWith(document("auth-login",
        requestFields(
          fieldWithPath("name").description("Register user name"),
          fieldWithPath("password").description("Register user password"),
          fieldWithPath("project").description("Client project id"),
          fieldWithPath("profile.*").type("JsonObject").description("User other profile").optional()
        ),
        responseFields(
          fieldWithPath("id").description("Register user id"),
          fieldWithPath("name").description("Register user name"),
          fieldWithPath("project").description("Register user project"),
          fieldWithPath("password").ignored(),
          fieldWithPath("profile").ignored()
        )))
  }
}