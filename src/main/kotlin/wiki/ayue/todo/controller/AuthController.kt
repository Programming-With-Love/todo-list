package wiki.ayue.todo.controller

import com.mongodb.BasicDBObject
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import wiki.ayue.todo.component.JwtProcessor
import wiki.ayue.todo.model.BadRequestException
import wiki.ayue.todo.model.ResourceException
import wiki.ayue.todo.model.ResourceNotFoundException
import wiki.ayue.todo.model.entity.SysUser
import wiki.ayue.todo.repostiory.SysUserRepository
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping("/auth")
class AuthController(
    private val sysUserRepository: SysUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProcessor: JwtProcessor
) {

  /**
   * Login.
   */
  @PostMapping("/login")
  fun login(@RequestBody @Valid sysUser: SysUser) =
    sysUserRepository.findFirstByName(sysUser.name)
      .switchIfEmpty(Mono.error(ResourceNotFoundException("Can not find user ${sysUser.name}.")))
      .flatMap { user ->
        if (passwordEncoder.matches(sysUser.password, user.password)) Mono.just(user)
        else Mono.error(BadRequestException("The password don't match."))
      }.map { user ->
        val claims = (sysUser.profile ?: BasicDBObject())
        claims["project"] = sysUser.project as Any
        val token = jwtProcessor
          .generate("${user.project}->${user.name}", claims.toMap())
        Token(token)
      }

  /**
   * Register
   */
  @PostMapping("/register")
  fun register(@RequestBody @Valid sysUser: SysUser) =
    sysUserRepository.existsByNameAndProject(sysUser.name, sysUser.project)
      .flatMap { exist ->
        if (exist) Mono.error(ResourceException("The user ${sysUser.name} already exist"))
        else {
          val password = passwordEncoder.encode(sysUser.password)
          sysUserRepository.save(sysUser.copy(password = password, id = null))
        }
      }
      .map { user ->
        user.copy(password = null)
      }

  data class Token(
    val token: String
  )

}