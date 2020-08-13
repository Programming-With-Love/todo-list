package wiki.ayue.todo.repostiory

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import wiki.ayue.todo.model.entity.Todo

@Repository
interface TodoRepository : ReactiveMongoRepository<Todo, String> {

  fun findAllByProject(project: String): Flux<Todo>

  fun findFirstByIdAndProject(id: String, project: String): Mono<Todo>

  fun deleteByIdAndProject(id: String, project: String): Mono<Void>

}