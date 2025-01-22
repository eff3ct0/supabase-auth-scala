package com.eff3ct.supabase.auth.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.eff3ct.supabase.auth.api.response.{Session, TokenSession}
import com.eff3ct.supabase.auth.test.FancyCatsEffectSuiteTest
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.{Status, Uri}
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
    SupabaseAuthAPI.create[IO](baseUrl, apiKey).unsafeRunSync()

  // Define a generator for valid email addresses
  val emailGen: Gen[String] = for {
    user   <- Gen.alphaNumStr
    domain <- Gen.oneOf("email.com", "example.com", "supabase.com")
  } yield s"${user.take(5)}@$domain"

  // Define a generator for valid passwords
  val passwordGen: Gen[String] =
    Gen.alphaNumStr.retryUntil(password => password.length >= 6).map(_.take(10))

  // Define a generator for valid phone numbers
  val phoneGen: Gen[String] = for {
    countryCode <- Gen.oneOf("+1", "+2", "+3", "+4", "+5", "+6", "+7", "+8", "+9")
    takeNumber  <- Gen.pick(6, List(1, 2, 3, 4, 5, 6, 7, 8, 9))
  } yield s"$countryCode${takeNumber.mkString}"

  val phonePasswordGen: Gen[(String, String)] = for {
    phone    <- phoneGen
    password <- passwordGen
  } yield (phone, password)

  val emailPasswordGen: Gen[(String, String)] = for {
    email    <- emailGen
    password <- passwordGen
  } yield (email.toLowerCase, password)

  "SupabaseAuthAPI" should "sign up a new user with email and password" in {

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
  }

  it should "sign in a user with email and password" in {
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
  }

  it should "signOut an active session" in {
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
}
