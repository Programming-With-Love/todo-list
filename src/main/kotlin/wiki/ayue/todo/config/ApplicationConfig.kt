package wiki.ayue.todo.config

import com.nimbusds.jose.crypto.RSADecrypter
import com.nimbusds.jwt.EncryptedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import wiki.ayue.todo.model.ResourceException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyStore
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.RSAPublicKeySpec
import java.time.Duration

const val TODO_LIST = "todo-list"

/**
 * Application config.
 */
@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class ApplicationConfig {

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
    .authorizeExchange { exchanges -> exchanges.anyExchange().permitAll() }
    .oauth2ResourceServer { resourceServer ->
      resourceServer.jwt { jwt ->
        jwt.jwtDecoder(jwtDecoder())
      }
    }
    .csrf().disable()
    .formLogin().disable()
    .httpBasic().disable()
    .build()

  private fun jwtDecoder(): ReactiveJwtDecoder {
    val withClockSkew: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(
      JwtTimestampValidator(Duration.ofDays(7)))
    val jwtDecoder = NimbusReactiveJwtDecoder { jwt ->
      try {
        val jwe = EncryptedJWT.parse(jwt.parsedString)
        jwe.decrypt(RSADecrypter(keyPair().private))
        return@NimbusReactiveJwtDecoder Mono.just(jwe.jwtClaimsSet)
      } catch (e: Exception) {
        throw ResourceException("Token parsing failed, invalid token.", e)
      }
    }
    jwtDecoder.setJwtValidator(withClockSkew)
    return jwtDecoder
  }

  @Bean
  fun keyPair(): KeyPair {
    return try {
      val store = KeyStore.getInstance("jks")
      val stream = ClassPathResource("${TODO_LIST}.jks").inputStream
      val password: CharArray = TODO_LIST.toCharArray()
      store.load(stream, TODO_LIST.toCharArray())
      val privateCrtKey = store.getKey(TODO_LIST, password) as RSAPrivateCrtKey
      val spec = RSAPublicKeySpec(privateCrtKey.modulus, privateCrtKey.publicExponent)
      val publicKey: PublicKey = KeyFactory.getInstance("RSA").generatePublic(spec)
      KeyPair(publicKey, privateCrtKey)
    } catch (e: Exception) {
      throw ResourceException("Unable to generate key pair information.", e)
    }
  }

  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(11)

  @Bean
  fun indexRouter(@Value("classpath:/static/index.html") indexHtml: Resource) = router {
    GET("/") {
      ServerResponse.ok().contentType(TEXT_HTML).bodyValue(indexHtml)
    }
  }

}