package wiki.ayue.todo.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import wiki.ayue.todo.model.BadRequestException
import wiki.ayue.todo.model.ResourceNotFoundException
import wiki.ayue.todo.model.entity.Todo
import wiki.ayue.todo.repostiory.TodoRepository

@CrossOrigin
@RestController
@RequestMapping("/todo")
@PreAuthorize("isAuthenticated()")
class TodoController(
  private val todoRepository: TodoRepository
) {

  /**
   * Get list.
   */
  @GetMapping
  fun todos() = project().flatMap {
    todoRepository.findAllByProject(it).collectList()
  }

  /**
   * Add item from [todo].
   */
  @PostMapping
  fun addTodo(@RequestBody todo: Todo) = project()
    .flatMap { project ->
      todoRepository.save(todo.copy(project = project))
    }

  /**
   * Get item from [id].
   */
  @GetMapping("/{id}")
  fun getTodo(@PathVariable id: String) = project().flatMap { project ->
    todoRepository.findFirstByIdAndProject(id, project)
      .switchIfEmpty(Mono.error(ResourceNotFoundException("Todo $id do not exist.")))
  }

  /**
   * Delete item by [id].
   */
  @DeleteMapping("/{id}")
  fun deleteTodo(@PathVariable id: String) = project().flatMap { project ->
    todoRepository.deleteByIdAndProject(id, project)
  }

  /**
   * Update item.
   */
  @PutMapping
  fun updateTodo(@RequestBody todo: Mono<Todo>) =
    todo.map {
      if (it.id === null) throw BadRequestException("The todo entity do not have id field.")
      it
    }.flatMap { entity ->
      project().flatMap { project ->
        todoRepository.save(entity.copy(project = project))
      }
    }

  /**
   * Get current project id.
   */
  private fun project(): Mono<String> = ReactiveSecurityContextHolder.getContext()
    .map { context -> context.authentication.principal }
    .map {
      if (it is Jwt) it.claims["project"] as String
      else "group-0"
    }

}