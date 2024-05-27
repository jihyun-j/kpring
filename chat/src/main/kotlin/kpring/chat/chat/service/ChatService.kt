package kpring.chat.chat.service

import kpring.chat.chat.model.Chat
import kpring.chat.chat.model.ServerChat
import kpring.chat.chat.repository.RoomChatRepository
import kpring.chat.chat.repository.ServerChatRepository
import kpring.chat.chatroom.repository.ChatRoomRepository
import kpring.chat.global.exception.ErrorCode
import kpring.chat.global.exception.GlobalException
import kpring.core.chat.chat.dto.request.CreateRoomChatRequest
import kpring.core.chat.chat.dto.request.CreateServerChatRequest
import kpring.core.chat.chat.dto.response.ChatResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ChatService(
  private val roomChatRepository: RoomChatRepository,
  private val serverChatRepository: ServerChatRepository,
  private val chatRoomRepository: ChatRoomRepository,
  @Value("\${page.size}") val pageSize: Int = 100,
) {
  /*
     business logic
   */
  fun createRoomChat(
    request: CreateRoomChatRequest,
    userId: String,
  ) {
    val chat =
      roomChatRepository.save(
        Chat(
          userId = userId,
          roomId = request.room,
          content = request.content,
        ),
      )
  }

  fun getChatsByChatRoom(
    chatRoomId: String,
    userId: String,
    page: Int,
  ): List<ChatResponse> {
    verifyIfAuthorizedForChatRoom(chatRoomId, userId)

    // find chats by chatRoomId and convert them into DTOs
    val pageable: Pageable = PageRequest.of(page, pageSize)
    val chats: List<Chat> = roomChatRepository.findAllByRoomId(chatRoomId, pageable)

    return convertRoomChatsToResponses(chats)
  }

  fun createServerChat(
    request: CreateServerChatRequest,
    userId: String,
  ) {
    val chat =
      serverChatRepository.save(
        ServerChat(
          userId = userId,
          serverId = request.server,
          content = request.content,
        ),
      )
  }

  fun getChatsByServer(
    serverId: String,
    userId: String,
    page: Int,
  ): List<ChatResponse> {
    val pageable: Pageable = PageRequest.of(page, pageSize)
    val chats: List<ServerChat> = serverChatRepository.findAllByServerId(serverId, pageable)

    return convertServerChatsToResponses(chats)
  }

  fun verifyIfAuthorizedForChatRoom(
    chatRoomId: String,
    userId: String,
  ) {
    if (!chatRoomRepository.existsByIdAndMembersContaining(chatRoomId, userId)) {
      throw GlobalException(ErrorCode.UNAUTHORIZED_CHATROOM)
    }
  }

  fun convertRoomChatsToResponses(chats: List<Chat>): List<ChatResponse> {
    val chatResponse =
      chats.map { chat ->
        ChatResponse(chat.id!!, chat.isEdited(), chat.createdAt, chat.content)
      }
    return chatResponse
  }

  fun convertServerChatsToResponses(chats: List<ServerChat>): List<ChatResponse> {
    val chatResponse =
      chats.map { chat ->
        ChatResponse(chat.id!!, chat.isEdited(), chat.createdAt, chat.content)
      }
    return chatResponse
  }
}
