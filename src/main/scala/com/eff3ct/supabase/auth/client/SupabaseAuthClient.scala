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

package com.eff3ct.supabase.auth.client

import com.eff3ct.supabase.auth.api.response.Session

trait SupabaseAuthClient[F[_]] {

  /**
   * Creates a new user. If both `email` and `phone` are provided, only `email` will be used.
   *
   * @param email The user's email address (optional).
   * @param phone The user's phone number (optional).
   * @param password The user's password (optional).
   * @param redirectTo A URL or mobile address to send the user to after they are confirmed (optional).
   * @param metadata Optional metadata to associate with the user.
   *
   * @return A `Session` if the server has "autoconfirm" enabled, or a `User` if "autoconfirm" is disabled.
   *
   * @throws APIError If an error occurs during the operation.
   */

  def signUp(
      email: Option[String],
      phone: Option[String],
      password: Option[String],
      redirectTo: Option[String],
      metadata: Option[Map[String, String]]
  ): F[Session]

}

object SupabaseAuthClient {
  def apply[F[_]: SupabaseAuthClient]: SupabaseAuthClient[F] = implicitly[SupabaseAuthClient[F]]
  sealed trait AuthChangeEvent
  object AuthChangeEvent {
    case object SignedIn       extends AuthChangeEvent
    case object SignedOut      extends AuthChangeEvent
    case object UserUpdated    extends AuthChangeEvent
    case object TokenRefreshed extends AuthChangeEvent
  }
}
