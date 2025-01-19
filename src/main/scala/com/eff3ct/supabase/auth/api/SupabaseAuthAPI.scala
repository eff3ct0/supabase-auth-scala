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

import cats.effect._
import com.eff3ct.supabase.auth.api.request._
import com.eff3ct.supabase.auth.api.response._
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.client.Client
import org.typelevel.ci.CIString

import java.util.UUID

trait SupabaseAuthAPI[F[_]] {

  /**
   * Creates a new user.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param email The user's email address.
   * @param password The user's password.
   * @param metadata Optional metadata to associate with the user.
   * @return A `UserSession`
   */
  def createUser(
      email: String,
      password: String,
      metadata: Option[Map[String, String]]
  ): F[UserSession]

  /**
   * Lists all users.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @return A list of `UserSession`
   */
  def listUsers(): F[List[UserSession]]

  /**
   * Signs up a new user with an email and password.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param email The user's email address.
   * @param password The user's password.
   * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
   * @param metadata Optional metadata to associate with the user.
   * @return A `Session`
   */
  def signUpWithEmail(
      email: String,
      password: String,
      redirectTo: Option[String],
      metadata: Option[Map[String, String]]
  ): F[Session]

  /**
   * Signs in an existing user with an email and password.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param email The user's email address.
   * @param password The user's password.
   * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
   * @return A `Session`
   */
  def signInWithEmail(email: String, password: String, redirectTo: Option[String]): F[Session]

  /**
   * Signs up a new user with a phone number and password.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param phone The user's phone number WITH international prefix
   * @param password The user's password.
   * @param metadata Optional metadata to associate with the user.
   * @return A `Session`
   */
  def signUpWithPhone(
      phone: String,
      password: String,
      metadata: Option[Map[String, String]]
  ): F[Session]

  /**
   * Signs in an existing user with a phone number and password.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param phone The user's phone number WITH international prefix
   * @param password The user's password.
   * @return A `Session`
   */
  def signInWithPhone(phone: String, password: String): F[Session]

  /**
   * Sends a magic link to the user's email address.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param email The user's email address.
   * @param createUser If `true`, creates a new user if one doesn't exist.
   * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
   * @return Unit
   */
  def sendMagicLinkEmail(email: String, createUser: Boolean, redirectTo: Option[String]): F[Unit]

  /**
   * Sends a mobile OTP to the user's phone number.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param phone The user's phone number WITH international prefix
   * @param createUser If `true`, creates a new user if one doesn't exist.
   * @return Unit
   */
  def sendMobileOtp(phone: String, createUser: Boolean): F[Unit]

  /**
   * Verifies a mobile OTP.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param phone The user's phone number WITH international prefix
   * @param token The OTP token.
   * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
   * @return A `Session`
   */
  def verifyMobileOtp(phone: String, token: String, redirectTo: Option[String]): F[Session]

  /**
   * Invites a user by email.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param email The user's email address.
   * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
   * @param metadata Optional metadata to associate with the user.
   * @return A `UserSession`
   */
  def inviteUserByEmail(
      email: String,
      redirectTo: Option[String],
      metadata: Option[Map[String, String]]
  ): F[UserSession]

  /**
   * Requests a password reset for the user's email address.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param email The user's email address.
   * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
   * @return Unit
   */
  def requestPasswordForEmail(email: String, redirectTo: Option[String]): F[Unit]

  /**
   * Signs out the user.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param jwt The user's JWT.
   * @return Unit
   */
  def signOut(jwt: String): F[Unit]

  /**
   * Gets the URL for a provider.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param provider The provider to get the URL for.
   * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
   * @param scopes The scopes to request.
   * @return A URL
   */
  def getUrlForProvider(
      provider: String,
      redirectTo: Option[String],
      scopes: Option[List[String]]
  ): F[Uri]

  def getUser(jwt: String): F[UserSession]

  def updateUser(
      jwt: String,
      attributes: UserAttributesRequest[Map[String, String]]
  ): F[UserSession]

  def deleteUser(userId: UUID, jwt: String, shouldSoftDelete: Boolean): F[Unit]
}

object SupabaseAuthAPI {

  private def client[F[_]: Client]: Client[F] = implicitly[Client[F]]

  def apply[F[_]: SupabaseAuthAPI]: SupabaseAuthAPI[F] = implicitly[SupabaseAuthAPI[F]]

  def build[F[_]: Async: Client](baseUrl: Uri, headers: Map[String, String]): SupabaseAuthAPI[F] =
    new SupabaseAuthAPI[F] {

      private def buildHeaders: Headers =
        Headers(headers.toList.map { case (k, v) =>
          Header.Raw(CIString(k), v)
        })

      private def createHeaders(other: (String, String), others: (String, String)*): Headers = {
        val news: Map[String, String] = Map(other) ++ others.toMap
        Headers((headers ++ news).toList.map { case (k, v) =>
          Header.Raw(CIString(k), v)
        })
      }

      def createUser(
          email: String,
          password: String,
          metadata: Option[Map[String, String]]
      ): F[UserSession] =
        postF(endpoint = "admin/users")(
          UserAttributesRequest(
            email = Option(email),
            password = Option(password),
            metadata = metadata
          )
        )

      def listUsers(): F[List[UserSession]] = {
        val uri: Uri            = baseUrl / "admin" / "users"
        val request: Request[F] = Request[F](Method.GET, uri).withHeaders(buildHeaders)
        client.expect[List[UserSession]](request)
      }

      def signUpWithEmail(
          email: String,
          password: String,
          redirectTo: Option[String],
          metadata: Option[Map[String, String]]
      ): F[Session] = postF(endpoint = "signup", redirectTo = redirectTo)(
        EmailPasswordRequest(email, password, metadata)
      )

      override def signUpWithPhone(
          phone: String,
          password: String,
          metadata: Option[Map[String, String]]
      ): F[Session] = postF(endpoint = "signup")(PhonePasswordRequest(phone, password, metadata))

      override def signInWithEmail(
          email: String,
          password: String,
          redirectTo: Option[String]
      ): F[Session] =
        postF(endpoint = "token", redirectTo = redirectTo)(EmailPasswordRequest(email, password))

      override def signInWithPhone(phone: String, password: String): F[Session] =
        postF(endpoint = "token")(PhonePasswordRequest(phone, password))

      def sendMagicLinkEmail(
          email: String,
          createUser: Boolean,
          redirectTo: Option[String]
      ): F[Unit] = postF(endpoint = "magiclink", redirectTo = redirectTo)(
        SendMagicLinkRequest(email, createUser)
      )

      override def sendMobileOtp(phone: String, createUser: Boolean): F[Unit] =
        postF(endpoint = "otp")(SendMobileOtpRequest(phone, createUser))

      override def verifyMobileOtp(
          phone: String,
          token: String,
          redirectTo: Option[String]
      ): F[Session] =
        postF(endpoint = "verify", redirectTo = redirectTo)(
          VerifyMobileOtpRequest(phone, token, "sms")
        )

      override def inviteUserByEmail(
          email: String,
          redirectTo: Option[String],
          metadata: Option[Map[String, String]]
      ): F[UserSession] =
        postF(endpoint = "invite", redirectTo = redirectTo)(
          InviteUserByEmailRequest(email, metadata)
        )

      override def requestPasswordForEmail(email: String, redirectTo: Option[String]): F[Unit] =
        postF(endpoint = "recover", redirectTo = redirectTo)(RequestPasswordForEmailRequest(email))

      override def signOut(jwt: String): F[Unit] = {
        val uri: Uri         = baseUrl / "logout"
        val headers: Headers = createHeaders("Authorization" -> s"Bearer $jwt")
        val request: Request[F] = Request[F](Method.POST, uri)
          .withHeaders(headers)

        client.expect[Unit](request)
      }

      override def getUrlForProvider(
          provider: String,
          redirectTo: Option[String],
          scopes: Option[List[String]]
      ): F[Uri] = Async[F].delay {
        val base: Uri = baseUrl / "authorize"

        val scopeQueryParams: Map[String, String] =
          scopes.map(s => Map("scopes" -> s.mkString(","))).getOrElse(Map.empty)

        val redirectToQueryParams: Map[String, String] =
          redirectTo.map(r => Map("redirect_to" -> Uri.encode(r))).getOrElse(Map.empty)

        val queryParams: Map[String, String] = scopeQueryParams ++ redirectToQueryParams

        queryParams.foldLeft(base) { case (uri, (pk, pv)) =>
          uri.withQueryParam(pk, pv)
        }
      }

      override def getUser(jwt: String): F[UserSession] = {
        val uri: Uri = baseUrl / "user"
        val request: Request[F] = Request[F](Method.GET, uri)
          .withHeaders(createHeaders("Authorization" -> s"Bearer $jwt"))

        client.expect[UserSession](request)
      }

      override def updateUser(
          jwt: String,
          attributes: UserAttributesRequest[Map[String, String]]
      ): F[UserSession] = {
        val uri: Uri = baseUrl / "user"
        val request: Request[F] = Request[F](Method.PUT, uri)
          .withHeaders(createHeaders("Authorization" -> s"Bearer $jwt"))
          .withEntity(attributes.asJson)

        client.expect[UserSession](request)
      }

      override def deleteUser(userId: UUID, jwt: String, shouldSoftDelete: Boolean): F[Unit] = {
        val uri: Uri = baseUrl / "admin" / "users" / userId.toString
        val request: Request[F] = Request[F](Method.DELETE, uri)
          .withHeaders(createHeaders("Authorization" -> s"Bearer $jwt"))
          .withEntity(ShouldSoftDeleteRequest(shouldSoftDelete).asJson)

        client.expect[Unit](request)
      }

      /** Private methods */
      private def postF[T: Encoder, O: EntityDecoder[F, *]](
          endpoint: String,
          redirectTo: Option[String] = None
      )(payload: T): F[O] = {
        val qp: Map[String, String] =
          redirectTo.map(r => Map("redirect_to" -> Uri.encode(r))).getOrElse(Map.empty)
        postF(endpoint, qp)(payload)
      }

      private def postF[T: Encoder, O: EntityDecoder[F, *]](
          endpoint: String,
          queryParams: Map[String, String]
      )(payload: T): F[O] = {
        val base: Uri = baseUrl / endpoint
        val uri: Uri = queryParams.foldLeft(base) { case (uri, (pk, pv)) =>
          uri.withQueryParam(pk, pv)
        }
        val request: Request[F] = Request[F](Method.POST, uri)
          .withHeaders(buildHeaders)
          .withEntity(payload.asJson)

        client.expect[O](request)
      }
    }
}
