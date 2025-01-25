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

import com.eff3ct.supabase.auth.test.resources.TestUserMetadata
import org.scalacheck.Gen
import org.scalatest.{Assertion, Assertions}

trait CustomGenerators {
  self: Assertions =>

  def forAll[A](gen: Gen[A])(f: A => Assertion): Unit = {
    val sample = Vector.fill(5)(gen.sample).flatten
    sample.foreach(f)
  }

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

  val userMetadataGen: Gen[TestUserMetadata] = for {
    profileImage <- Gen.alphaNumStr
    website      <- Gen.alphaNumStr
    twitter      <- Gen.alphaNumStr
    bio          <- Gen.alphaNumStr
    fullName     <- Gen.alphaNumStr
  } yield TestUserMetadata(profileImage, website, twitter, bio, fullName)

}
