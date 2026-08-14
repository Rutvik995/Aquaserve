package com.example.aquaserve.util

fun isValidEmails(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.isNotBlank() && email.matches(Regex(emailPattern))
}