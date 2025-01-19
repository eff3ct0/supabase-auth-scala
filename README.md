# Supabase Auth

[![Build Status](https://github.com/eff3ct/supabase-auth/actions/workflows/ci.yml/badge.svg)](https://github.com/eff3ct0/supabase-auth/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/eff3ct/supabase-auth/branch/main/graph/badge.svg?token=0S0Z6Z1Z8Y)](https://codecov.io/gh/eff3ct0/supabase-auth)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Supabase Auth is a Scala library for interacting with the [Supabase Auth API](https://supabase.com/docs/guides/auth).

## Getting Started

To get started, you'll need to create a Supabase account and obtain an API key. You can sign up for a free Supabase account [here](https://app.supabase.io).

Once you have your API key, you can use the Supabase Auth library to interact with the Supabase Auth API. Here's an example of how to create a user:

```scala
import com.eff3ct.supabase.auth.client.SupabaseAuthClient
import com.eff3ct.supabase.auth.model.User

val client = SupabaseAuthClient.create[IO]("https://your-supabase-url.supabase.co", "your-supabase-api-key")

val user = User(
  email = "user@example.com",
  password = "password",
  roles = List("authenticated")
)

client.createUser(user).unsafeRunSync()
```

## Contributing

Contributions are welcome! Please see the [contributing guidelines](CONTRIBUTING.md) for more information.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more information.