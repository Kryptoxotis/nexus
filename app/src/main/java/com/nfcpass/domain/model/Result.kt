package com.nfcpass.domain.model

/**
 * A sealed class representing the result of an operation.
 * Used throughout the app to handle success and error states in a type-safe way.
 *
 * @param T The type of data returned on success
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with an error message
     */
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()

    /**
     * Returns true if this is a Success result
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if this is an Error result
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Returns the data if Success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the error message if Error, null otherwise
     */
    fun errorOrNull(): String? = when (this) {
        is Success -> null
        is Error -> message
    }
}
