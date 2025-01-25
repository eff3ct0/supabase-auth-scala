# Supabase Auth

[![Release](https://github.com/eff3ct0/supabase-auth-scala/actions/workflows/release.yml/badge.svg)](https://github.com/eff3ct0/supabase-auth-scala/actions/workflows/release.yml)
[![codecov](https://codecov.io/gh/eff3ct0/supabase-auth-scala/graph/badge.svg?token=eVnypuOLNu)](https://codecov.io/gh/eff3ct0/supabase-auth-scala)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Supabase Auth is a Scala library for interacting with the [Supabase Auth API](https://supabase.com/docs/guides/auth).

This Scala client follows the ideas of the official client [supabase/auth-py](https://github.com/supabase/auth-py)

## Getting Started

To get started, you'll need to create a Supabase account and obtain an API key. You can sign up for a free Supabase
account [here](https://app.supabase.io).

Once you have your API key, you can use the Supabase Auth library to interact with the Supabase Auth API. Here's an
example of how to create a user:

### Run Example: Sign Up with Email and Password

```scala
import cats.effect._
import com.eff3ct.supabase.auth.api._
import org.http4s.client._
import org.http4s.ember.client._
import org.http4s.implicits._

implicit val client: Resource[IO, Client[IO]] =
  EmberClientBuilder.default[IO].build

for {
  api <- SupabaseAuthAPI.create[IO](uri"https://your-supabase-url.supabase.co/auth/v1", "your-supabase-api-key")
  user <- api.signUpWithEmail("user@example.com", "password")
} yield user
```

### Unsafe Run Example: Sign Up with Email and Password

```scala
import cats.effect._
import cats.effect.unsafe.implicits.global
import com.eff3ct.supabase.auth.api._
import org.http4s.ember.client._
import org.http4s.implicits._

implicit val client: ClientR[IO] = EmberClientBuilder.default[IO].build
implicit val api: SupabaseAuthAPI[IO] =
  SupabaseAuthAPI.create[IO](uri"https://your-supabase-url.supabase.co/auth/v1", "your-supabase-api-key")
    .unsafeRunSync()

SupabaseAuthAPI[IO].signUpWithEmail("user@example.com", "password").unsafeRunSync()
```

## Contributing

Contributions are welcome! Please see the [contributing guidelines](CONTRIBUTING.md) for more information.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more information.
