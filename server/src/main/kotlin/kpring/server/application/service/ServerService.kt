package kpring.server.application.service

import kpring.core.global.exception.CommonErrorCode
import kpring.core.global.exception.ServiceException
import kpring.core.server.dto.ServerInfo
import kpring.core.server.dto.ServerSimpleInfo
import kpring.core.server.dto.ServerUserInfo
import kpring.core.server.dto.request.AddUserAtServerRequest
import kpring.core.server.dto.request.CreateServerRequest
import kpring.core.server.dto.request.GetServerCondition
import kpring.core.server.dto.response.CreateServerResponse
import kpring.server.application.port.input.AddUserAtServerUseCase
import kpring.server.application.port.input.CreateServerUseCase
import kpring.server.application.port.input.DeleteServerUseCase
import kpring.server.application.port.input.GetServerInfoUseCase
import kpring.server.application.port.output.DeleteServerPort
import kpring.server.application.port.output.GetServerPort
import kpring.server.application.port.output.GetServerProfilePort
import kpring.server.application.port.output.SaveServerPort
import kpring.server.application.port.output.UpdateServerPort
import kpring.server.domain.Category
import kpring.server.domain.Server
import kpring.server.domain.ServerAuthority
import kpring.server.util.toInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServerService(
  val createServerPort: SaveServerPort,
  val getServer: GetServerPort,
  val getServerProfilePort: GetServerProfilePort,
  val updateServerPort: UpdateServerPort,
  val deleteServerPort: DeleteServerPort,
) : CreateServerUseCase, GetServerInfoUseCase, AddUserAtServerUseCase, DeleteServerUseCase {
  override fun createServer(req: CreateServerRequest): CreateServerResponse {
    val server =
      createServerPort.create(
        Server(
          name = req.serverName,
          users = mutableSetOf(req.userId),
          theme = req.theme,
          categories = req.categories,
          hostName = req.hostName,
          hostId = req.userId,
        ),
      )
    return CreateServerResponse(
      serverId = server.id!!,
      serverName = server.name,
      theme = server.theme.toInfo(),
      hostName = server.host.name,
      categories = server.categories.map(Category::toInfo),
    )
  }

  override fun getServerInfo(serverId: String): ServerInfo {
    val server = getServer.get(serverId)
    val serverProfiles = getServerProfilePort.getAll(server.id!!)
    return ServerInfo(
      id = server.id,
      name = server.name,
      users =
        serverProfiles.map { profile ->
          ServerUserInfo(
            id = profile.userId,
            name = profile.name,
            profileImage = profile.imagePath,
          )
        },
      theme = server.theme.toInfo(),
      categories = server.categories.map(Category::toInfo),
    )
  }

  override fun getServerList(
    condition: GetServerCondition,
    userId: String,
  ): List<ServerSimpleInfo> {
    return getServerProfilePort.getProfiles(condition, userId)
      .map { profile ->
        ServerSimpleInfo(
          id = profile.server.id!!,
          name = profile.server.name,
          bookmarked = profile.bookmarked,
          categories = profile.server.categories.map(Category::toInfo),
          theme = profile.server.theme.toInfo(),
          hostName = profile.server.host.name,
        )
      }
  }

  override fun getOwnedServerList(userId: String): List<ServerSimpleInfo> {
    return getServerProfilePort.getOwnedProfiles(userId)
      .map { profile ->
        ServerSimpleInfo(
          id = profile.server.id!!,
          name = profile.server.name,
          hostName = profile.server.host.name,
          bookmarked = profile.bookmarked,
          categories = profile.server.categories.map(Category::toInfo),
          theme = profile.server.theme.toInfo(),
        )
      }
  }

  @Transactional
  override fun inviteUser(
    serverId: String,
    invitorId: String,
    userId: String,
  ) {
    // validate invitor authority
    val serverProfile = getServerProfilePort.get(serverId, invitorId)
    if (serverProfile.dontHasRole(ServerAuthority.INVITE)) {
      throw ServiceException(CommonErrorCode.FORBIDDEN)
    }
    // register invitation
    val server = serverProfile.server
    server.registerInvitation(userId)
    updateServerPort.inviteUser(server.id!!, userId)
  }

  @Transactional
  override fun addInvitedUser(
    serverId: String,
    req: AddUserAtServerRequest,
  ) {
    val server = getServer.get(serverId)
    val profile = server.addUser(req.userId, req.userName, req.profileImage)
    updateServerPort.addUser(profile)
  }

  override fun deleteServer(
    serverId: String,
    userId: String,
  ) {
    val serverProfile = getServerProfilePort.get(serverId, userId)
    if (serverProfile.dontHasRole(ServerAuthority.DELETE)) {
      throw ServiceException(CommonErrorCode.FORBIDDEN)
    }
    deleteServerPort.delete(serverId)
  }
}
