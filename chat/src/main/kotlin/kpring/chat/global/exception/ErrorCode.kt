package kpring.chat.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val httpStatus: Int, val message: String) {
  // 400
  INVALID_CHAT_TYPE(HttpStatus.BAD_REQUEST.value(), "잘못된 ChatType입니다."),

  // 401
  MISSING_TOKEN(HttpStatus.UNAUTHORIZED.value(), "인증 토큰이 누락되었습니다."),

  // 403
  FORBIDDEN_CHATROOM(HttpStatus.FORBIDDEN.value(), "접근이 제한된 채팅방 입니다"),
  FORBIDDEN_SERVER(HttpStatus.FORBIDDEN.value(), "접근이 제한된 서버 입니다"),
  FORBIDDEN_CHAT(HttpStatus.FORBIDDEN.value(), "접근이 제한된 채팅 입니다"),

  // 404
  CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 id로 chatroom을 찾을 수 없습니다"),
  CHAT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 id로 채팅을 찾을 수 없습니다"),

  // 410
  EXPIRED_INVITATION(HttpStatus.GONE.value(), "만료된 Invitation입니다."),

  // 500
  INVITATION_LINK_SAVE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Invitation Code가 저장되지 않았습니다"),
}
