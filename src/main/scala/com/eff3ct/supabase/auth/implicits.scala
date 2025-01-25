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
import io.circe.{Decoder, Error, Json}

object implicits {

  implicit def monadErrorResult[F[_], E <: Error](implicit
      m: MonadError[F, E]
  ): MonadError[F, Throwable] = new MonadError[F, Throwable] {
    def pure[B](x: B): F[B] = m.pure(x)

    override def raiseError[A](e: Throwable): F[A] = m.raiseError(throw e)

    override def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]): F[A] = m.handleErrorWith(fa)(f)

    override def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = m.flatMap(fa)(f)

    override def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = m.tailRecM(a)(f)
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
