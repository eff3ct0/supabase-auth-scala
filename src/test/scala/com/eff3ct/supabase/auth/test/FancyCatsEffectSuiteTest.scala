package com.eff3ct.supabase.auth.test

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalacheck.Gen
import org.scalactic.source
import org.scalatest.{Assertion, Assertions}

trait FancyCatsEffectSuiteTest {
  self: Assertions =>

  def assertIO[A, B](
      obtained: IO[A],
      expected: B
  )(implicit pos: source.Position, ev: B <:< A): Assertion =
    assertIO(obtained, IO(expected))

  def assertIO[A, B](
      obtained: IO[A],
      expected: IO[B]
  )(implicit pos: source.Position, ev: B <:< A): Assertion =
    (for {
      a <- obtained
      b <- expected
    } yield assert(a == ev(b))).unsafeRunSync()

  def forAll[A](gen: Gen[A])(f: A => Assertion): Unit = {
    val sample = Vector.fill(5)(gen.sample).flatten
    sample.foreach(f)
  }

  implicit class AssertionOps[A](val a: IO[A]) {
    def shouldBe[B](expected: B)(implicit pos: source.Position, ev: B <:< A): Assertion =
      assertIO(a, expected)

    def shouldBe[B](expected: IO[B])(implicit pos: source.Position, ev: B <:< A): Assertion =
      assertIO(a, expected)
  }
}
