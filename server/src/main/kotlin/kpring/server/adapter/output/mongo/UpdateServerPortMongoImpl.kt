package kpring.server.adapter.output.mongo

import kpring.core.server.dto.request.UpdateHostAtServerRequest
import kpring.server.adapter.output.mongo.entity.ServerEntity
import kpring.server.adapter.output.mongo.entity.ServerProfileEntity
import kpring.server.adapter.output.mongo.repository.ServerProfileRepository
import kpring.server.adapter.output.mongo.repository.ServerRepository
import kpring.server.application.port.output.UpdateServerPort
import kpring.server.domain.ServerProfile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class UpdateServerPortMongoImpl(
  val serverRepository: ServerRepository,
  val serverProfileRepository: ServerProfileRepository,
  val template: MongoTemplate,
) : UpdateServerPort {
  override fun addUser(profile: ServerProfile) {
    template.updateFirst(
      query(
        where("_id").`is`(profile.server.id)
          .and("invitedUserIds").`in`(profile.userId),
      ),
      Update()
        .pull("invitedUserIds", profile.userId)
        .push("users", profile.userId),
      ServerEntity::class.java,
    )

    serverProfileRepository.save(ServerProfileEntity(profile))
  }

  override fun inviteUser(
    serverId: String,
    userId: String,
  ) {
    template.updateFirst(
      query(
        where("_id").`is`(serverId)
          .and("invitedUserIds").nin(userId),
      ),
      Update().push("invitedUserIds").value(userId),
      ServerEntity::class.java,
    )
  }

  override fun updateServerHost(
    serverId: String,
    userId: String,
    otherUser: UpdateHostAtServerRequest,
  ) {
    template.updateFirst(
      query(
        where("_id").`is`(serverId)
          .and("hostId").`is`(userId),
      ),
      Update().set("hostId", otherUser.userId)
        .set("hostName", otherUser.userName),
      ServerEntity::class.java,
    )
  }
}
