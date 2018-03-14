package me.glatteis.unichat

enum class ErrorCode(val value: Int) {
    NOT_LOGGED_IN(0),
    USERNAME_EMPTY(1),
    USERNAME_BLANK(2),
    ROOM_DOES_NOT_EXIST(3),
    USERNAME_TOO_LONG(4),
    INVALID_CHALLENGE_RESPONSE(5),
    DUPLICATE_USER(6),
    USER_ID_EMPTY(7),
    INVALID_KEY(8),
    IMAGE_EMPTY(9),
    MESSAGE_EMPTY(10),
    NONEXISTENT_IMAGE(12),
}