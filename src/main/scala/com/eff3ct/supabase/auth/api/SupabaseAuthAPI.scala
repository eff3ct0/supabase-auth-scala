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
import cats.implicits.catsSyntaxApplicativeId
import com.eff3ct.supabase.auth.api.request._
import com.eff3ct.supabase.auth.api.response._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
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
  @deprecated("Use signUpX methods instead", "0.1.0")
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
      redirectTo: Option[String] = None,
      metadata: Option[Map[String, String]] = None
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
  def signInWithEmail(
      email: String,
      password: String,
      redirectTo: Option[String] = None
  ): F[Session]

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
      metadata: Option[Map[String, String]] = None
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
  def sendMagicLinkEmail(
      email: String,
      createUser: Boolean,
      redirectTo: Option[String] = None
  ): F[Status]

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
  def sendMobileOtp(phone: String, createUser: Boolean): F[Status]

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
  def verifyMobileOtp(phone: String, token: String, redirectTo: Option[String] = None): F[Session]

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
  @deprecated("Use signUpX methods instead", "0.1.0")
  def inviteUserByEmail(
      email: String,
      jwt: String,
      redirectTo: Option[String] = None,
      metadata: Option[Map[String, String]] = None
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
  def resetPasswordForEmail(email: String, redirectTo: Option[String] = None): F[Status]

  /**
   * Signs out the user.
   *
   * This function should only be called on a server.
   * **Important:** Never expose your `service_role` key in the browser.
   *
   * @param jwt The user's JWT.
   * @return Unit
   */
  def signOut(jwt: String): F[Status]

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
      redirectTo: Option[String] = None,
      scopes: Option[List[String]] = None
  ): F[Uri]

  def getUser(jwt: String): F[UserSession]

  def updateUser(
      jwt: String,
      attributes: UserAttributesRequest[Map[String, String]]
  ): F[UserSession]

  def deleteUser(userId: UUID, jwt: String, shouldSoftDelete: Boolean): F[Status]
}

object SupabaseAuthAPI {

  private def client[F[_]: Client]: Client[F] = implicitly[Client[F]]

  def apply[F[_]: SupabaseAuthAPI]: SupabaseAuthAPI[F] = implicitly[SupabaseAuthAPI[F]]

  def create[F[_]: Async: ClientR](baseUrl: Uri, apiKey: String): F[SupabaseAuthAPI[F]] =
    implicitly[ClientR[F]]
      .map { client =>
        implicit val c: Client[F] = client
        build[F](baseUrl, apiKey)
      }
      .use(api => Async[F].delay(api))

  def build[F[_]: Async: Client](baseUrl: Uri, apiKey: String): SupabaseAuthAPI[F] =
    build[F](baseUrl, Map("apiKey" -> apiKey))

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

      override def createUser(
          email: String,
          password: String,
          metadata: Option[Map[String, String]]
      ): F[UserSession] =
        client.expect[UserSession] {
          Request[F](Method.POST, baseUrl / "admin" / "users")
            .withHeaders(buildHeaders)
            .withEntity[Json](
              UserAttributesRequest(
                email = Option(email),
                password = Option(password),
                data = metadata
              )
            )
        }

      override def listUsers(): F[List[UserSession]] =
        client.expect[List[UserSession]] {
          Request[F](Method.GET, baseUrl / "admin" / "users")
            .withHeaders(buildHeaders)
        }

      override def signUpWithEmail(
          email: String,
          password: String,
          redirectTo: Option[String],
          metadata: Option[Map[String, String]]
      ): F[Session] =
        client.expect[Session] {
          Request[F](Method.POST, baseUrl / "signup" :? redirectTo)
            .withHeaders(buildHeaders)
            .withEntity[Json](EmailPasswordRequest(email, password, metadata))
        }

      override def signUpWithPhone(
          phone: String,
          password: String,
          metadata: Option[Map[String, String]]
      ): F[Session] =
        client.expect[Session] {
          Request[F](Method.POST, baseUrl / "signup")
            .withHeaders(buildHeaders)
            .withEntity[Json](PhonePasswordRequest(phone, password, metadata))

        }

      override def signInWithEmail(
          email: String,
          password: String,
          redirectTo: Option[String]
      ): F[Session] =
        client.expect[Session] {
          Request[F](Method.POST, baseUrl / "token" +? ("grant_type" -> "password") :? redirectTo)
            .withHeaders(buildHeaders)
            .withEntity[Json](EmailPasswordRequest(email, password))

        }

      override def signInWithPhone(phone: String, password: String): F[Session] =
        client.expect[Session] {
          Request[F](Method.POST, baseUrl / "token" +? ("grant_type" -> "password"))
            .withHeaders(buildHeaders)
            .withEntity[Json](PhonePasswordRequest(phone, password))
        }

      override def sendMagicLinkEmail(
          email: String,
          createUser: Boolean,
          redirectTo: Option[String]
      ): F[Status] =
        client
          .run {
            Request[F](Method.POST, baseUrl / "magiclink" :? redirectTo)
              .withHeaders(buildHeaders)
              .withEntity[Json](SendMagicLinkRequest(email, createUser))

          }
          .use(resp => resp.status.pure[F])

      override def sendMobileOtp(phone: String, createUser: Boolean): F[Status] =
        client
          .run {
            Request[F](Method.POST, baseUrl / "otp")
              .withHeaders(buildHeaders)
              .withEntity[Json](SendMobileOtpRequest(phone, createUser))
          }
          .use(resp => resp.status.pure[F])

      override def verifyMobileOtp(
          phone: String,
          token: String,
          redirectTo: Option[String]
      ): F[Session] =
        client.expect[Session] {
          Request[F](Method.POST, baseUrl / "verify" :? redirectTo)
            .withHeaders(buildHeaders)
            .withEntity[Json](VerifyMobileOtpRequest(phone, token, "sms"))
        }
      override def inviteUserByEmail(
          email: String,
          jwt: String,
          redirectTo: Option[String],
          metadata: Option[Map[String, String]]
      ): F[UserSession] =
        client.expect[UserSession] {
          Request[F](Method.POST, baseUrl / "invite" :? redirectTo)
            .withHeaders(createHeaders("Authorization" -> s"Bearer $jwt"))
            .withEntity[Json](InviteUserByEmailRequest(email, metadata))
        }

      override def resetPasswordForEmail(email: String, redirectTo: Option[String]): F[Status] =
        client
          .run {
            Request[F](Method.POST, baseUrl / "recover" :? redirectTo)
              .withHeaders(buildHeaders)
              .withEntity[Json](ResetPasswordForEmailRequest(email))
          }
          .use(resp => resp.status.pure[F])

      override def signOut(jwt: String): F[Status] =
        client
          .run {
            Request[F](Method.POST, baseUrl / "logout")
              .withHeaders(createHeaders("Authorization" -> s"Bearer $jwt"))
          }
          .use(resp => resp.status.pure[F])

      override def getUrlForProvider(
          provider: String,
          redirectTo: Option[String],
          scopes: Option[List[String]]
      ): F[Uri] = Async[F].delay {
        val scopeQueryParams: Map[String, String] =
          scopes.map(s => Map("scopes" -> s.mkString(","))).getOrElse(Map.empty)

        baseUrl / "authorize" :+? scopeQueryParams :? redirectTo
      }

      override def getUser(jwt: String): F[UserSession] =
        client.expect[UserSession] {
          Request[F](Method.GET, baseUrl / "user")
            .withHeaders(createHeaders("Authorization" -> s"Bearer $jwt"))
        }

      override def updateUser(
          jwt: String,
          attributes: UserAttributesRequest[Map[String, String]]
      ): F[UserSession] =
        client.expect[UserSession] {
          Request[F](Method.PUT, baseUrl / "user")
            .withHeaders(createHeaders("Authorization" -> s"Bearer $jwt"))
            .withEntity(attributes.asJson)

        }

      override def deleteUser(userId: UUID, jwt: String, shouldSoftDelete: Boolean): F[Status] =
        client
          .run {
            Request[F](Method.DELETE, baseUrl / "admin" / "users" / userId.toString)
              .withHeaders(createHeaders("Authorization" -> s"Bearer $jwt"))
              .withEntity(ShouldSoftDeleteRequest(shouldSoftDelete).asJson)
          }
          .use(resp => resp.status.pure[F])

    }
}

object Example extends IOApp {

  import SupabaseAuthAPI._

  def run(args: List[String]): IO[ExitCode] = {
//    val baseUrl: Uri = Uri.unsafeFromString("http://localhost:54321/auth/v1/")
    val baseUrl: Uri = Uri.unsafeFromString("https://rzactotqkibyymeotzui.supabase.co/auth/v1/")
    val apiKey: String =
//      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0"
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ6YWN0b3Rxa2lieXltZW90enVpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzcwNTkxOTMsImV4cCI6MjA1MjYzNTE5M30.knztz7okoUciM9kvcxu95ti-0qDwCLa5PbwjpP6peIM"
    val clientR: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
    val user =
      clientR.use { client =>
        implicit val c: Client[IO] = client
        val api                    = build[IO](baseUrl, apiKey)
        for {
          user <- api.signInWithEmail("test.4@eff3ct.com", "Admin123")
          t     = implicitly[As[Session, TokenSession]].as(user)
          token = t.get
          d <- api.signOut(token.accessToken)
          a = 0

        } yield d
      }

    for {
      user <- user
      _    <- IO(println(user))
    } yield ExitCode.Success
  }
}
