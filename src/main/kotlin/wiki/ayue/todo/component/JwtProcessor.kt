package wiki.ayue.todo.component

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTClaimsSet.Builder
import org.springframework.stereotype.Component
import wiki.ayue.todo.config.TODO_LIST
import wiki.ayue.todo.model.ResourceException
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.*

/**
 * JWT Processor
 *
 * @author echo
 */
@Component
class JwtProcessor(keyPair: KeyPair) {

  private lateinit var rsaJwk: RSAKey

  private val jweHeader = JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM)

  init {
    try {
      rsaJwk = RSAKey.Builder(keyPair.public as RSAPublicKey)
        .privateKey(keyPair.private)
        .keyUse(KeyUse.SIGNATURE)
        .algorithm(JWSAlgorithm.RS256)
        .keyID(TODO_LIST)
        .build()
    } catch (e: Exception) {
      throw ResourceException("Jwt Generate Exception", e)
    }
  }

  fun generate(subject: String, claims: Map<Any?, Any?> = emptyMap()): String {
    val now = Date()
    val builder: Builder = Builder()
      .subject(subject)
      .issuer("ayue.wiki")
      .expirationTime(Date(now.time + Duration.ofDays(7).toMillis()))
      .notBeforeTime(now)
      .jwtID(UUID.nameUUIDFromBytes(subject.toByteArray()).toString())
      .audience(TODO_LIST)
      .issueTime(now)
    claims.forEach { (key, value) -> builder.claim(key.toString(), value) }
    val jwtClaims: JWTClaimsSet = builder.build()
    return try {
      val jwt = EncryptedJWT(jweHeader, jwtClaims)
      jwt.encrypt(RSAEncrypter(rsaJwk))
      jwt.serialize()
    } catch (e: JOSEException) {
      throw ResourceException("Jwe Generate Exception.", e)
    }
  }
}