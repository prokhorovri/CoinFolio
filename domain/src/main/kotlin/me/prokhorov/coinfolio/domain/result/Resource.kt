package me.prokhorov.coinfolio.domain.result

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Failure(val message: String) : Resource<Nothing>
}
