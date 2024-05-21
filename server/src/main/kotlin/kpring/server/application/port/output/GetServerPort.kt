package kpring.server.application.port.output

import kpring.server.domain.Server

interface GetServerPort {
  fun get(id: String): Server

  fun getServerWith(userId: String): List<Server>
}
