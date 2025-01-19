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

object request {
  case class EmailPasswordRequest(
      email: String,
      password: String,
      metadata: Option[Map[String, String]] = None
  )
  case class PhonePasswordRequest(
      phone: String,
      password: String,
      metadata: Option[Map[String, String]] = None
  )
  case class SendMagicLinkRequest(email: String, createUser: Boolean)
  case class SendMobileOtpRequest(phone: String, createUser: Boolean)
  case class VerifyMobileOtpRequest(phone: String, token: String, `type`: String)
  case class InviteUserByEmailRequest(email: String, metadata: Option[Map[String, String]])
  case class RequestPasswordForEmailRequest(email: String)

  case class UserAttributesRequest(
      email: String,
      password: String,
      redirectTo: Option[String] = None,
      metadata: Option[Map[String, String]] = None
  )

}
