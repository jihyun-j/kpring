package kpring.chat.chat.api.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.junit5.MockKExtension
import kpring.chat.chat.service.ChatService
import kpring.chat.global.ChatRoomTest
import kpring.chat.global.ChatTest
import kpring.chat.global.CommonTest
import kpring.chat.global.config.TestMongoConfig
import kpring.core.auth.client.AuthClient
import kpring.core.auth.dto.response.TokenInfo
import kpring.core.auth.enums.TokenType
import kpring.core.chat.chat.dto.request.CreateChatRequest
import kpring.core.chat.chat.dto.request.UpdateChatRequest
import kpring.core.chat.chat.dto.response.ChatResponse
import kpring.core.chat.model.ChatType
import kpring.core.chat.model.MessageType
import kpring.core.global.dto.response.ApiResponse
import kpring.core.server.client.ServerClient
import kpring.core.server.dto.ServerSimpleInfo
import kpring.test.restdoc.dsl.restDoc
import kpring.test.restdoc.json.JsonDataType
import kpring.test.web.URLBuilder
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDateTime

@WebMvcTest(controllers = [ChatController::class])
@ExtendWith(RestDocumentationExtension::class)
@ExtendWith(SpringExtension::class)
@ExtendWith(MockKExtension::class)
@Import(TestMongoConfig::class)
class ChatControllerTest(
  private val om: ObjectMapper,
  webContext: WebApplicationContext,
  @MockkBean val chatService: ChatService,
  @MockkBean val serverClient: ServerClient,
  @MockkBean val authClient: AuthClient,
) : DescribeSpec({

    val restDocument = ManualRestDocumentation()
    val webTestClient: WebTestClient =
      MockMvcWebTestClient.bindToApplicationContext(webContext).configureClient()
        .baseUrl("http://localhost:8081")
        .filter(
          WebTestClientRestDocumentation.documentationConfiguration(restDocument).operationPreprocessors()
            .withRequestDefaults(Preprocessors.prettyPrint()).withResponseDefaults(Preprocessors.prettyPrint()),
        )
        .build()

    beforeSpec { restDocument.beforeTest(this.javaClass, "chat controller") }

    afterSpec { restDocument.afterTest() }

    describe("POST /api/v1/chat : createChat api test") {

      val url = "/api/v1/chat"
      it("createChat api test") {

        // Given
        val content = "create_chat_test"
        val id = ChatRoomTest.TEST_ROOM_ID
        val type = ChatType.Room
        val request = CreateChatRequest(content = content, type = type, id = id)

        val data = true

        every { authClient.getTokenInfo(any()) } returns
          ApiResponse(
            data =
              TokenInfo(
                type =
                  TokenType.ACCESS,
                userId = CommonTest.TEST_USER_ID,
              ),
          )

        every { chatService.createRoomChat(request, CommonTest.TEST_USER_ID) } returns data

        // When
        val result =
          webTestClient.post().uri(URLBuilder(url).build())
            .header("Authorization", "Bearer mock_token")
            .bodyValue(request)
            .exchange()

        // Then
        val docs =
          result
            .expectStatus()
            .isCreated()
            .expectBody()
            .json(om.writeValueAsString(ApiResponse(data = null, status = 201)))

        docs.restDoc(
          identifier = "create_chat_201",
          description = "채팅 생성 api",
        ) {
          response {
            body {
              "status" type JsonDataType.Integers mean "successfully created a chat"
            }
          }
        }
      }
    }
    describe("GET /api/v1/chat : getServerChats api test") {

      val url = "/api/v1/chat"
      it("getServerChats api test") {

        // Given
        val serverId = "test_server_id"
        val chatId = ChatTest.TEST_CHAT_ID
        val userId = CommonTest.TEST_USER_ID
        val data =
          listOf(
            ChatResponse(
              id = chatId,
              sender = userId,
              messageType = MessageType.CHAT,
              isEdited = false,
              sentAt = LocalDateTime.now().toString(),
              content = "content",
            ),
          )
        val serverList =
          listOf(
            ServerSimpleInfo(
              id = serverId,
              name = "test_server_name",
              bookmarked = true,
            ),
          )

        every { authClient.getTokenInfo(any()) } returns
          ApiResponse(
            data =
              TokenInfo(
                type = TokenType.ACCESS, userId = CommonTest.TEST_USER_ID,
              ),
          )

        every { serverClient.getServerList(any(), any()) } returns
          ResponseEntity.ok().body(ApiResponse(data = serverList))

        every {
          chatService.getServerChats(
            serverId,
            CommonTest.TEST_USER_ID,
            1,
            serverList,
          )
        } returns data

        // When
        val result =
          webTestClient.get().uri(
            URLBuilder(url)
              .query("id", serverId)
              .query("type", "Server")
              .query("page", 1)
              .build(),
          )
            .header("Authorization", "Bearer mock_token")
            .exchange()

        // Then
        val docs =
          result
            .expectStatus()
            .isOk
            .expectBody()
            .json(om.writeValueAsString(ApiResponse(data = data)))

        docs.restDoc(
          identifier = "get_server_chats_200",
          description = "서버 채팅 조회 api",
        ) {

          request {
            query {

              "type" mean "Server / Room"
              "id" mean "서버 ID"
              "page" mean "페이지 번호"
            }
          }

          response {
            body {
              "status" type JsonDataType.Integers mean "상태 코드"
              "data[].id" type JsonDataType.Strings mean "서버 ID"
              "data[].isEdited" type JsonDataType.Booleans mean "메시지가 수정되었는지 여부"
              "data[].sentAt" type JsonDataType.Strings mean "메시지 생성 시간"
              "data[].content" type JsonDataType.Strings mean "메시지 내용"
            }
          }
        }
      }
    }

    describe("GET /api/v1/chat : getRoomChats api test") {

      val url = "/api/v1/chat"
      it("getRoomChats api test") {

        // Given
        val roomId = "test_room_id"
        val chatId = ChatTest.TEST_CHAT_ID
        val userId = CommonTest.TEST_USER_ID
        val data =
          listOf(
            ChatResponse(
              id = chatId,
              sender = userId,
              messageType = MessageType.CHAT,
              isEdited = false,
              sentAt = LocalDateTime.now().toString(),
              content = "content",
            ),
          )

        every { authClient.getTokenInfo(any()) } returns
          ApiResponse(
            data =
              TokenInfo(
                type = TokenType.ACCESS, userId = CommonTest.TEST_USER_ID,
              ),
          )

        every {
          chatService.getRoomChats(
            roomId,
            CommonTest.TEST_USER_ID,
            1,
          )
        } returns data

        // When
        val result =
          webTestClient.get().uri(
            URLBuilder(url)
              .query("id", roomId)
              .query("type", "Room")
              .query("page", 1)
              .build(),
          )
            .header("Authorization", "Bearer mock_token")
            .exchange()

        // Then
        val docs =
          result
            .expectStatus()
            .isOk
            .expectBody()
            .json(om.writeValueAsString(ApiResponse(data = data)))

        docs.restDoc(
          identifier = "get_room_chats_200",
          description = "채팅방 채팅 조회 api",
        ) {
          request {
            query {
              "type" mean "Server / Room"
              "id" mean "채팅방 ID"
              "page" mean "페이지 번호"
            }
          }

          response {
            body {
              "status" type JsonDataType.Integers mean "상태 코드"
              "data[].id" type JsonDataType.Strings mean "채팅방 ID"
              "data[].isEdited" type JsonDataType.Booleans mean "메시지가 수정되었는지 여부"
              "data[].sentAt" type JsonDataType.Strings mean "메시지 생성 시간"
              "data[].content" type JsonDataType.Strings mean "메시지 내용"
            }
          }
        }
      }
    }

    describe("PATCH /api/v1/chat : updateChat api test") {

      val url = "/api/v1/chat"
      it("updateRoomChat api test") {

        // Given
        val roomId = "test_room_id"
        val content = "edit test"
        val request = UpdateChatRequest(id = roomId, type = ChatType.Room, content = content)
        val userId = CommonTest.TEST_USER_ID

        every { authClient.getTokenInfo(any()) } returns
          ApiResponse(
            data =
              TokenInfo(
                type = TokenType.ACCESS, userId = userId,
              ),
          )

        every {
          chatService.updateRoomChat(
            request,
            CommonTest.TEST_USER_ID,
          )
        } returns true

        // When
        val result =
          webTestClient.patch().uri(url)
            .bodyValue(request)
            .header("Authorization", "Bearer mock_token")
            .exchange()

        // Then
        val docs =
          result
            .expectStatus()
            .isOk
            .expectBody()
            .json(om.writeValueAsString(ApiResponse<Nothing>(status = 200)))

        docs.restDoc(
          identifier = "update_chats_200",
          description = "채팅방 채팅 업데이트 api",
        ) {
          response {
            body {
              "status" type JsonDataType.Integers mean "상태 코드"
            }
          }
        }
      }
      it("updateServerChat api test") {

        // Given
        val serverId = "test_server_id"
        val content = "edit test"
        val request = UpdateChatRequest(id = serverId, type = ChatType.Server, content = content)
        val userId = CommonTest.TEST_USER_ID

        val serverList =
          listOf(
            ServerSimpleInfo(
              id = serverId,
              name = "test_server_name",
              bookmarked = true,
            ),
          )

        every { authClient.getTokenInfo(any()) } returns
          ApiResponse(
            data =
              TokenInfo(
                type = TokenType.ACCESS, userId = userId,
              ),
          )

        every { serverClient.getServerList(any(), any()) } returns
          ResponseEntity.ok().body(ApiResponse(data = serverList))

        every {
          chatService.updateServerChat(
            request,
            userId,
          )
        } returns
          true

        // When
        val result =
          webTestClient.patch().uri(url)
            .bodyValue(request)
            .header("Authorization", "Bearer mock_token")
            .exchange()

        // Then
        val docs =
          result
            .expectStatus()
            .isOk
            .expectBody()
            .json(om.writeValueAsString(ApiResponse<Nothing>(status = 200)))

        docs.restDoc(
          identifier = "update_chats_200",
          description = "서버 채팅 업데이트 api",
        ) {
          response {
            body {
              "status" type JsonDataType.Integers mean "상태 코드"
            }
          }
        }
      }
    }

    describe("DELETE /api/v1/chat : deleteChat api test") {

      val url = "/api/v1/chat/{chatId}"
      // Given
      val userId = CommonTest.TEST_USER_ID

      it("deleteRoomChat api test") {

        // Given
        val chatId = ChatTest.TEST_CHAT_ID

        every { authClient.getTokenInfo(any()) } returns
          ApiResponse(
            data =
              TokenInfo(
                type = TokenType.ACCESS, userId = userId,
              ),
          )

        every {
          chatService.deleteRoomChat(
            chatId,
            userId,
          )
        } returns true

        // When
        val result =
          webTestClient.delete().uri(
            URLBuilder(url)
              .query("type", "Room")
              .build(),
            chatId,
          )
            .header("Authorization", "Bearer mock_token")
            .exchange()

        // Then
        val docs =
          result
            .expectStatus()
            .isOk
            .expectBody()
            .json(om.writeValueAsString(ApiResponse<Nothing>(status = 200)))

        docs.restDoc(
          identifier = "delete_room_chat_200",
          description = "채팅방 채팅 삭제 api",
        ) {
          request {
            query {
              "type" mean "Server / Room"
            }
            path {
              "chatId" mean "채팅 ID"
            }
          }

          response {
            body {
              "status" type JsonDataType.Integers mean "상태 코드"
            }
          }
        }
      }

      it("deleteServerChat api test") {

        // Given
        val chatId = ChatTest.TEST_CHAT_ID

        every { authClient.getTokenInfo(any()) } returns
          ApiResponse(
            data =
              TokenInfo(
                type = TokenType.ACCESS, userId = userId,
              ),
          )

        every {
          chatService.deleteServerChat(
            chatId,
            userId,
          )
        } returns true

        // When
        val result =
          webTestClient.delete().uri(
            URLBuilder(url)
              .query("type", "Server")
              .build(),
            chatId,
          )
            .header("Authorization", "Bearer mock_token")
            .exchange()

        // Then
        val docs =
          result
            .expectStatus()
            .isOk
            .expectBody()
            .json(om.writeValueAsString(ApiResponse<Nothing>(status = 200)))

        docs.restDoc(
          identifier = "delete_server_chat_200",
          description = "서버 채팅 삭제 api",
        ) {
          request {
            query {
              "type" mean "Server / Room"
            }
            path {
              "chatId" mean "채팅 ID"
            }
          }

          response {
            body {
              "status" type JsonDataType.Integers mean "상태 코드"
            }
          }
        }
      }
    }
  })
