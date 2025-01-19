package com.eff3ct.supabase.auth

import io.circe.generic.extras.Configuration

package object api {
  private[api] implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}
