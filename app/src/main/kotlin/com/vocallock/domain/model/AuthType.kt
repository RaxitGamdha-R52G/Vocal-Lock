package com.vocallock.domain.model

/**
 * Defines the type of lock applied to an App or Group.
 */
enum class AuthType {
    TEXT_PASSWORD,
    PIN,
    PATTERN
}