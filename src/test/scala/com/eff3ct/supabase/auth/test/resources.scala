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

package com.eff3ct.supabase.auth.test

import io.circe.generic.extras.{AutoDerivation, Configuration}
import io.circe.syntax._
import org.http4s.Uri
import pdi.jwt.{JwtAlgorithm, JwtCirce}

trait resources extends AutoDerivation {
  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames.withDefaults
}

object resources extends resources {

  // Define a resource that provides an instance of SupabaseAuthAPI
  val localhostUri: Uri = Uri.unsafeFromString("http://localhost:54321/auth/v1/")

  val JwtSecret: String = "37c304f8-51aa-419a-a1af-06154e63707a"

  case class AuthAdmin(sub: Option[String], role: String)

  val AuthAdmingJwt: String =
    JwtCirce.encode(
      AuthAdmin(sub = Option("1234567890"), role = "supabase_admin").asJson.noSpaces,
      JwtSecret,
      JwtAlgorithm.HS256
    )

  val ServiceRoleJwt: String =
    JwtCirce.encode(
      AuthAdmin(sub = None, role = "service_role").asJson.dropNullValues.noSpaces,
      JwtSecret,
      JwtAlgorithm.HS256
    )

  case class TestUserMetadata(
      profileImage: String,
      website: String,
      twitter: String,
      bio: String,
      fullName: String
  )

}
