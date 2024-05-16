package kpring.user.controller

import kpring.core.auth.client.AuthClient
import kpring.core.auth.enums.TokenType
import kpring.core.global.exception.ServiceException
import kpring.user.dto.request.CreateUserRequest
import kpring.user.dto.request.UpdateUserProfileRequest
import kpring.user.dto.response.CreateUserResponse
import kpring.user.dto.response.GetUserProfileResponse
import kpring.user.dto.response.UpdateUserProfileResponse
import kpring.user.exception.UserErrorCode
import kpring.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class UserController(
  val userService: UserService,
  val authClient: AuthClient,
) {
  @GetMapping("/user/{userId}")
  fun getUserProfile(
    @RequestHeader("Authorization") token: String,
    @PathVariable userId: Long,
  ): ResponseEntity<GetUserProfileResponse> {
    checkRequestUserHasPermission(token, userId.toString())
    val response = userService.getProfile(userId)
    return ResponseEntity.ok(response)
  }

  @PostMapping("/user")
  fun createUser(
    @Validated @RequestBody request: CreateUserRequest,
  ): ResponseEntity<CreateUserResponse> {
    val response = userService.createUser(request)
    return ResponseEntity.ok(response)
  }

  @PatchMapping("/user/{userId}")
  fun updateUserProfile(
    @RequestHeader("Authorization") token: String,
    @PathVariable userId: Long,
    @RequestBody request: UpdateUserProfileRequest,
  ): ResponseEntity<UpdateUserProfileResponse> {
    checkRequestUserHasPermission(token, userId.toString())
    val response = userService.updateProfile(userId, request)
    return ResponseEntity.ok(response)
  }

  @DeleteMapping("/user/{userId}")
  fun exitUser(
    @RequestHeader("Authorization") token: String,
    @PathVariable userId: Long,
  ): ResponseEntity<Any> {
    checkRequestUserHasPermission(token, userId.toString())
    val isExit = userService.exitUser(userId)

    return if (isExit) {
      ResponseEntity.ok().build()
    } else {
      ResponseEntity.badRequest().build()
    }
  }

  private fun checkRequestUserHasPermission(
    token: String,
    userId: String,
  ) {
    val validationResult = authClient.validateToken(token)
    if (!validationResult.body!!.isValid || userId != validationResult.body!!.userId) {
      throw ServiceException(UserErrorCode.NOT_ALLOWED)
    }
    if (validationResult.body!!.type != TokenType.ACCESS) {
      throw ServiceException(UserErrorCode.BAD_REQUEST)
    }
  }
}
