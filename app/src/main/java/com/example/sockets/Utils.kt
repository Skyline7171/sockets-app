package com.example.sockets

import java.util.regex.Pattern

val EMAIL_ADDRESS_REGEX: Pattern = Pattern.compile(
    "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

fun isEmailValid(email: String): Boolean {
    return EMAIL_ADDRESS_REGEX.matcher(email).matches()
}