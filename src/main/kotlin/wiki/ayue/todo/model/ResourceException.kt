package wiki.ayue.todo.model

import java.lang.RuntimeException

class ResourceException : RuntimeException {
  constructor(message: String = "Resource Exception") : super(message)
  constructor(message: String = "Resource Exception", cause: Exception) : super(message, cause)
}

class ResourceNotFoundException : RuntimeException {
  constructor(message: String = "Resource Not Found Exception") : super(message)
  constructor(message: String = "Resource Not Found Exception", cause: Exception) : super(message, cause)
}

class BadRequestException : RuntimeException {
  constructor(message: String = "Bad Request Exception") : super(message)
  constructor(message: String = "Bad Request Exception", cause: Exception) : super(message, cause)
}