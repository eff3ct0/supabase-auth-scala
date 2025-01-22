/*
 * MIT License
 *
 * Copyright (c) 2024 Rafael Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.eff3ct.supabase.auth.api

import io.circe.Decoder
import io.circe.generic.extras._

import java.util.UUID

object response {

  @ConfiguredJsonCodec
  sealed trait Session

  @ConfiguredJsonCodec
  case class IdentityData(
      email: String,
      emailVerified: Boolean,
      phoneVerified: Boolean,
      sub: UUID
  )
  @ConfiguredJsonCodec
  case class Identity(
      identityId: UUID,
      id: UUID,
      userId: UUID,
      identityData: IdentityData,
      provider: String,
      lastSignInAt: String,
      createdAt: String,
      updatedAt: String,
      email: String
  )
  @ConfiguredJsonCodec
  case class AppMetadata(
      provider: String,
      providers: List[String]
  )

  @ConfiguredJsonCodec
  case class UserMetadata(
      email: String,
      emailVerified: Boolean,
      phoneVerified: Boolean,
      sub: UUID
  )

  /**
   * @param id The user's unique ID.
   * @param aud The audience of the JWT.
   * @param role The user's role.
   * @param email The user's email address.
   * @param phone The user's phone number.
   * @param appMetadata The app metadata.
   * @param userMetadata The user metadata.
   * @param identities The user's identities.
   * @param createdAt The timestamp of when the user was created.
   * @param updatedAt The timestamp of when the user was last updated.
   * @param isAnonymous Whether the user is anonymous.
   */
  @ConfiguredJsonCodec
  case class UserSession(
      id: UUID,
      aud: String,
      role: String,
      email: String,
      phone: Option[String],
      appMetadata: AppMetadata,
      userMetadata: UserMetadata,
      identities: List[Identity],
      createdAt: String,
      updatedAt: String,
      isAnonymous: Boolean
  ) extends Session

  /**
   * @param accessToken The access token that can be used to make authenticated requests to the API.
   * @param tokenType The type of token that was issued. Always "bearer".
   * @param expiresIn The number of seconds until the token expires (since it was issued). Returned when a login is confirmed.
   * @param expiresAt A timestamp of when the token will expire. Returned when a login is confirmed.
   * @param refreshToken The refresh token that can be used to get a new access token. Returned when a login is confirmed.
   * @param providerToken The oauth provider token. If present, this can be used to make external API  requests to the oauth provider used.
   * @param providerRefreshToken The oauth provider refresh token. If present, this can be used to refresh  the provider_token via the oauth provider's API.
   * @param user The user session object.
   */
  @ConfiguredJsonCodec
  case class TokenSession(
      accessToken: String,
      tokenType: String,
      expiresIn: Int,
      expiresAt: Option[Long],
      refreshToken: String,
      providerToken: Option[String],
      providerRefreshToken: Option[String],
      user: UserSession
  ) extends Session

  @ConfiguredJsonCodec
  case class NoneResponse(message: None.type = None)

  implicit val sessionDecoder: Decoder[Session] = Decoder.instance { c =>
    if (c.downField("access_token").succeeded) c.as[TokenSession]
    else c.as[UserSession]
  }

  trait As[I, O] {
    def as(input: I): Option[O]
  }

  implicit val sessionAsTokenSession: As[Session, TokenSession] = {
    case t: TokenSession => Some(t)
    case _               => None
  }

  implicit class ImplicitAs[I](input: I) {
    def /=>[O: As[I, *]]: Option[O] = implicitly[As[I, O]].as(input)
  }
}
