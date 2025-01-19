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

trait SupabaseAuthAPI[F[_]] {

  def createUser(
      email: String,
      password: String,
      metadata: Option[Map[String, String]]
  ): F[UserSession]

  def listUsers(): F[List[UserSession]]

  def signUpWithEmail(
      email: String,
      password: String,
      redirectTo: Option[String],
      metadata: Option[Map[String, String]]
  ): F[Session]

  def signInWithEmail(email: String, password: String, redirectTo: Option[String]): F[Session]

  def signUpWithPhone(
      phone: String,
      password: String,
      metadata: Option[Map[String, String]]
  ): F[Session]

  def signInWithPhone(phone: String, password: String): F[Session]

  def sendMagicLinkEmail(email: String, createUser: Boolean, redirectTo: Option[String]): F[Unit]

  def sendMobileOtp(phone: String, createUser: Boolean): F[Unit]

  def verifyMobileOtp(phone: String, token: String, redirectTo: Option[String]): F[Session]

  def inviteUserByEmail(
      email: String,
      redirectTo: Option[String],
      metadata: Option[Map[String, String]]
  ): F[UserSession]

  def requestPasswordForEmail(email: String, redirectTo: Option[String]): F[Unit]

  def signOut(jwt: String): F[Unit]

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

      def createUser(
          email: String,
          password: String,
          metadata: Option[Map[String, String]]
      ): F[UserSession] = {
        val uri: Uri = baseUrl / "admin" / "users"
        val attributes: UserAttributesRequest =
          UserAttributesRequest(email, password, metadata = metadata)
        val request: Request[F] = Request[F](Method.POST, uri)
          .withHeaders(buildHeaders)
          .withEntity(attributes.asJson)

        client.expect[UserSession](request)
      }

      def listUsers(): F[List[UserSession]] = {
        val uri: Uri            = baseUrl / "admin" / "users"
        val request: Request[F] = Request[F](Method.GET, uri).withHeaders(buildHeaders)

        client.expect[List[UserSession]](request)
      }

      /** Sign Up */
      def signUpWithEmail(
          email: String,
          password: String,
          redirectTo: Option[String],
          metadata: Option[Map[String, String]]
      ): F[Session] = postF("signup", EmailPasswordRequest(email, password, metadata), redirectTo)

      override def signUpWithPhone(
          phone: String,
          password: String,
          metadata: Option[Map[String, String]]
      ): F[Session] = postF("signup", PhonePasswordRequest(phone, password, metadata))

      /** Sign In */
      override def signInWithEmail(
          email: String,
          password: String,
          redirectTo: Option[String]
      ): F[Session] = postF("token", EmailPasswordRequest(email, password), redirectTo)

      override def signInWithPhone(phone: String, password: String): F[Session] =
        postF("token", PhonePasswordRequest(phone, password))

      def sendMagicLinkEmail(
          email: String,
          createUser: Boolean,
          redirectTo: Option[String]
      ): F[Unit] = postF("magiclink", SendMagicLinkRequest(email, createUser), redirectTo)

      override def sendMobileOtp(phone: String, createUser: Boolean): F[Unit] =
        postF("otp", SendMobileOtpRequest(phone, createUser))

      /** POST F */
      private def postF[T: Encoder, O: EntityDecoder[F, *]](
          endpoint: String,
          payload: T,
          redirectTo: Option[String] = None,
          headers: => Headers = buildHeaders
      ): F[O] = {
        val base: Uri = baseUrl / endpoint
        val uri: Uri = redirectTo.fold(base)(redirectTo =>
          base.withQueryParam("redirect_to", Uri.encode(redirectTo))
        )
        val request: Request[F] = Request[F](Method.POST, uri)
          .withHeaders(headers)
          .withEntity(payload.asJson)

        client.expect[O](request)
      }

      override def verifyMobileOtp(
          phone: String,
          token: String,
          redirectTo: Option[String]
      ): F[Session] =
        postF("verify", VerifyMobileOtpRequest(phone, token, "sms"), redirectTo)

      override def inviteUserByEmail(
          email: String,
          redirectTo: Option[String],
          metadata: Option[Map[String, String]]
      ): F[UserSession] =
        postF("invite", InviteUserByEmailRequest(email, metadata), redirectTo)

      override def requestPasswordForEmail(email: String, redirectTo: Option[String]): F[Unit] =
        postF("recover", RequestPasswordForEmailRequest(email), redirectTo)

      override def signOut(jwt: String): F[Unit] =
        postF("logout", None, None, Headers(Header.Raw(CIString("Authorization"), s"Bearer $jwt")))
    }
}
