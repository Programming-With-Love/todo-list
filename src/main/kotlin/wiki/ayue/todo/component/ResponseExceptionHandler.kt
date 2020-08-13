package wiki.ayue.todo.component

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono
import wiki.ayue.todo.model.BadRequestException
import wiki.ayue.todo.model.ResourceNotFoundException

/**
 * Response exception handler
 */
@RestControllerAdvice
class ResponseExceptionHandler {

  @ExceptionHandler(BadRequestException::class)
  fun badRequest(exception: Exception) = wrapException(BAD_REQUEST, exception)

  @ExceptionHandler(ResourceNotFoundException::class)
  fun noFound(exception: Exception) = wrapException(NOT_FOUND, exception)

  @ExceptionHandler(Exception::class)
  fun internalServerError(exception: Exception) = wrapException(INTERNAL_SERVER_ERROR, exception)

  private fun wrapException(status: HttpStatus, exception: Exception) =
    ResponseEntity
      .status(status)
      .body(Mono.just(exception.message ?: "Unknown exception"))

}

