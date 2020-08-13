package wiki.ayue.todo.repostiory

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import wiki.ayue.todo.model.entity.SysUser

@Repository
interface SysUserRepository : ReactiveMongoRepository<SysUser, String> {

  fun findFirstByName(name: String?): Mono<SysUser>

  fun existsByNameAndProject(name: String?, project: String?): Mono<Boolean>

}