package me.prokhorov.coinfolio.domain.result

sealed interface AppError {
    /** No response at all: offline, DNS failure, refused connection, timeout. */
    data object Network : AppError

    /** Rate limited. Failed calls count toward the limit too, so retrying now makes it worse. */
    data object RateLimited : AppError

    /** 404: no such coin id, so retrying won't help. */
    data object NotFound : AppError

    data class Server(val code: Int) : AppError

    /** The app could not read or write the local database: no free space, or a corrupt file. */
    data object Storage : AppError

    /** Anything not recognised: a response we cannot read, or a bug on our side. */
    data object Unexpected : AppError
}
