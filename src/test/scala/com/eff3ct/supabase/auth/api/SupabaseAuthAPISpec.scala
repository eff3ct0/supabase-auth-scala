package com.eff3ct.supabase.auth.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.eff3ct.supabase.auth.api.response.TokenSession
import com.eff3ct.supabase.auth.test.FancyCatsEffectSuiteTest
import org.http4s.Uri
import org.http4s.ember.client.EmberClientBuilder
import org.scalacheck._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SupabaseAuthAPISpec
    extends AnyFlatSpecLike
    with Matchers
    with FancyCatsEffectSuiteTest
    with ScalaCheckPropertyChecks {

  // Define a resource that provides an instance of SupabaseAuthAPI
  val baseUrl: Uri = Uri.unsafeFromString("http://localhost:54321/auth/v1/")
  val apiKey: String =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0"
  implicit val client: ClientR[IO] = EmberClientBuilder.default[IO].build
  implicit val api: SupabaseAuthAPI[IO] =
    SupabaseAuthAPI.create[IO](baseUrl, apiKey).use(a => IO(a)).unsafeRunSync()

  // Define a generator for valid email addresses
  val emailGen: Gen[String] = for {
    user   <- Gen.alphaNumStr
    domain <- Gen.oneOf("email.com", "example.com", "supabase.com")
  } yield s"${user.take(5)}@$domain"

  // Define a generator for valid passwords
  val passwordGen: Gen[String] =
    Gen.alphaNumStr.retryUntil(password => password.length >= 6).map(_.take(10))

  "SupabaseAuthAPI" should "sign up a new user" in {

    val emailPasswordGen: Gen[(String, String)] = for {
      email    <- emailGen
      password <- passwordGen
    } yield (email, password)

    forAll(emailPasswordGen) { case (email, password) =>
      val result = SupabaseAuthAPI[IO].signUpWithEmail(email, password).unsafeRunSync()

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
          user.email shouldBe email.toLowerCase
          user.role shouldBe "authenticated"
          user.appMetadata.provider shouldBe "email"
          user.userMetadata.email shouldBe email.toLowerCase
          assert(user.identities.isEmpty)
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
  }

}
