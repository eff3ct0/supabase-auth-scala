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

package com.eff3ct.supabase.auth

import cats.effect.Resource
import io.circe.generic.extras.Configuration
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import org.http4s.Uri
import org.http4s.client.Client

package object api {
  type ClientR[F[_]] = Resource[F, Client[F]]

  private[api] implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  private[api] implicit def asJson[T](t: T)(implicit enc: Encoder[T]): Json = t.asJson

  private[api] implicit class ImplicitURI(uri: Uri) {
    def :?(redirectTo: Option[String]): Uri =
      redirectTo.fold(uri)(redirectTo => uri.withQueryParam("redirect_to", Uri.encode(redirectTo)))

    def :+?(params: Map[String, String]): Uri =
      params.foldLeft(uri)((acc, param) => acc.withQueryParam(param._1, param._2))
  }

}
