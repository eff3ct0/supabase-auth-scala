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

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.eff3ct.supabase.auth.api.response.{Session, TokenSession, UserSession}
import com.eff3ct.supabase.auth.implicits.UserMetadataOps
import com.eff3ct.supabase.auth.test._
import com.eff3ct.supabase.auth.test.resources._
import org.http4s.Status
import org.http4s.ember.client.EmberClientBuilder
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SupabaseAuthAPISpec
    extends AnyFlatSpecLike
    with Matchers
    with FancyCatsEffectSuiteTest
    with ScalaCheckPropertyChecks
    with CustomGenerators
    with resources {

  implicit val client: ClientR[IO] = EmberClientBuilder.default[IO].build
  implicit val api: SupabaseAuthAPI[IO] =
    SupabaseAuthAPI
      .create[IO](localhostUri, Map("Authorization" -> s"Bearer $ServiceRoleJwt"))
      .unsafeRunSync()

  "SupabaseAuthAPI" should "sign up a new user with email and password" in
    forAll(emailPasswordGen) { case (email, password) =>
      val result: Session = SupabaseAuthAPI[IO].signUpWithEmail(email, password).unsafeRunSync()
      result match {
        case TokenSession(
              accessToken,
              tokenType,
              expiresIn,
              expiresAt,
              refreshToken,
              providerToken,
              providerRefreshToken,
              user
            ) =>
          user.email shouldBe email
          user.role shouldBe "authenticated"
          user.appMetadata.provider shouldBe "email"
          user.userMetadata.email shouldBe email
          assert(user.identities.nonEmpty)
          assert(user.createdAt.nonEmpty)
          assert(user.updatedAt.nonEmpty)
          assert(!user.isAnonymous)
          assert(accessToken.nonEmpty)
          tokenType shouldBe "bearer"
          assert(expiresIn > 0)
          assert(expiresAt.nonEmpty)
          assert(refreshToken.nonEmpty)
          assert(providerToken.isEmpty)
          assert(providerRefreshToken.isEmpty)
        case _ => fail("Unexpected result. Expected TokenSession")
      }
    }

  it should "sign up a new user with email, password and metadata" in
    forAll(emailPasswordGen, userMetadataGen) { case ((email, password), metadata) =>
      val specMetadata: IO[TestUserMetadata] =
        for {
          user <- SupabaseAuthAPI[IO].signUpWithEmail(email, password, metadata)
          userMetadata <- user match {
            case u: UserSession  => u.userMetadata.asTypedF[IO, TestUserMetadata]
            case t: TokenSession => t.user.userMetadata.asTypedF[IO, TestUserMetadata]
          }
          metadata <- IO.fromOption(userMetadata.metadata)(
            fail("User metadata should not be empty")
          )
        } yield metadata

      specMetadata shouldBe metadata
    }

  it should "sign in a user with email and password" in
    forAll(emailPasswordGen) { case (email, password) =>
      val signUpResult: Session =
        SupabaseAuthAPI[IO].signUpWithEmail(email, password).unsafeRunSync()
      val result: Session = SupabaseAuthAPI[IO].signInWithEmail(email, password).unsafeRunSync()
      result match {
        case TokenSession(
              accessToken,
              tokenType,
              expiresIn,
              expiresAt,
              refreshToken,
              providerToken,
              providerRefreshToken,
              user
            ) =>
          user.email shouldBe email
          user.role shouldBe "authenticated"
          user.appMetadata.provider shouldBe "email"
          user.userMetadata.email shouldBe email
          assert(user.identities.nonEmpty)
          assert(user.createdAt.nonEmpty)
          assert(user.updatedAt.nonEmpty)
          assert(!user.isAnonymous)
          assert(accessToken.nonEmpty)
          tokenType shouldBe "bearer"
          assert(expiresIn > 0)
          assert(expiresAt.nonEmpty)
          assert(refreshToken.nonEmpty)
          assert(providerToken.isEmpty)
          assert(providerRefreshToken.isEmpty)
        case _ => fail("Unexpected result. Expected TokenSession")
      }

      (signUpResult, result) match {
        case (s: TokenSession, r: TokenSession) =>
          assert(s.user.id == r.user.id)
          assert(s.user.email == r.user.email)

        case _ =>
          fail(
            "Unexpected. Both results should be TokenSession and should have the same user id and email"
          )

      }
    }

  it should "sign out an active session" in
    forAll(emailPasswordGen) { case (email, password) =>
      val response: IO[Status] = for {
        _      <- SupabaseAuthAPI[IO].signUpWithEmail(email, password)
        result <- SupabaseAuthAPI[IO].signInWithEmail(email, password)
        res = result match {
          case t: TokenSession => t
          case _               => fail("Unexpected result. Expected TokenSession")
        }
        signOutResult: Status <- SupabaseAuthAPI[IO].signOut(res.accessToken)
      } yield signOutResult

      response shouldBe Status.NoContent
    }

}
