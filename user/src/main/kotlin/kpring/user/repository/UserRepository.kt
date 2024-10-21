package kpring.user.repository

import kpring.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
  fun existsByEmail(email: String): Boolean

  fun findByEmail(email: String): User?

  fun findAllByEmailContaining(email: String?): Set<User>?

  fun findAllByUsernameContaining(username: String?): Set<User>?
}
