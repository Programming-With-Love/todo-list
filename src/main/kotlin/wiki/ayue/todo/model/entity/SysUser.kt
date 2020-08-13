package wiki.ayue.todo.model.entity

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.bson.BsonDocument
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.NotBlank

@Document
data class SysUser(
  /**
   * id
   */
  @Id val id: String? = null,
  /**
   * 用户名
   */
  @NotBlank val name: String? = null,
  /**
   * 密码
   */
  @NotBlank val password: String? = null,
  /**
   * 项目标示
   */
  @NotBlank val project: String? = null,
  /**
   * 其他个人信息
   */
  val profile: BasicDBObject? = null
)