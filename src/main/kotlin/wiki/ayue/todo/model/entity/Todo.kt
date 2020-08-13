package wiki.ayue.todo.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.bson.BsonDocument
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank

@Document
data class Todo(
  /**
   * id
   */
  @Id val id: String? = null,
  /**
   * 名称
   */
  @NotBlank val name: String? = null,
  /**
   * 描述
   */
  val description: String? = null,
  /**
   * 关联项目
   */
  val project: String = "",
  /**
   * 是否完成
   */
  val finished: Boolean = false,
  /**
   * 其他信息
   */
  val information: BasicDBObject? = null,
  /**
   * 创建时间
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @CreatedDate val createDate: LocalDateTime = LocalDateTime.now(),
  /**
   * 修改时间
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @LastModifiedDate val modifyDate: LocalDateTime = LocalDateTime.now()
)