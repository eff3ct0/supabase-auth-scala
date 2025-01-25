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

import cats.implicits._
import cats.{MonadError, MonadThrow}
import com.eff3ct.supabase.auth.api.response.{UserMetadata, UserMetadataType}
import io.circe.Decoder.Result
import io.circe.{Decoder, DecodingFailure, Json}

object implicits {

  implicit def monadThrowResult[E <: DecodingFailure](implicit
      m: MonadError[Result, DecodingFailure]
  ): MonadThrow[Result] = new MonadThrow[Result] {
    override def pure[A](x: A): Result[A] = m.pure(x)

    override def map[A, B](fa: Result[A])(f: A => B): Result[B] = fa.map(f)

    override def flatMap[A, B](fa: Result[A])(f: A => Result[B]): Result[B] = fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => Result[Either[A, B]]): Result[B] = m.tailRecM(a)(f)

    override def raiseError[A](e: Throwable): Result[A] =
      m.raiseError(DecodingFailure.fromThrowable(e, Nil))

    override def handleErrorWith[A](fa: Result[A])(f: Throwable => Result[A]): Result[A] =
      m.handleErrorWith(fa)(f)
  }

  implicit class UserMetadataOps(val um: UserMetadata) {
    def asTyped[T: Decoder]: Result[UserMetadataType[T]] = um.asTypedF[Result, T]

    def asTypedF[F[_]: MonadThrow, T: Decoder]: F[UserMetadataType[T]] = {
      val metadata: F[Option[T]] = um.metadata match {
        case Json.Null => MonadThrow[F].pure(None)
        case m         => MonadThrow[F].fromEither(m.as[T].map(Option(_)))
      }
      metadata.map { m =>
        UserMetadataType(um.email, um.emailVerified, um.phoneVerified, um.sub, m)
      }
    }

    def asTypedOrElse[T: Decoder](default: => T): UserMetadataType[T] =
      um.asTyped[T]
        .getOrElse(
          UserMetadataType(um.email, um.emailVerified, um.phoneVerified, um.sub, Option(default))
        )
  }

}
