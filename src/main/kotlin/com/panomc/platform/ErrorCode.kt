package com.panomc.platform

enum class ErrorCode {
    INVALID_DATA,

    REGISTER_USERNAME_EMPTY,
    REGISTER_EMAIL_EMPTY,
    REGISTER_PASSWORD_EMPTY,

    REGISTER_USERNAME_TOO_SHORT,
    REGISTER_USERNAME_TOO_LONG,

    REGISTER_PASSWORD_TOO_SHORT,
    REGISTER_PASSWORD_TOO_LONG,

    REGISTER_INVALID_USERNAME,
    REGISTER_INVALID_EMAIL,

    REGISTER_CANT_VERIFY_ROBOT,

    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_1,
    REGISTER_EMAIL_NOT_AVAILABLE,

    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_2,

    FINISH_API_CANT_CONNECT_DATABASE_PLEASE_CHECK_YOUR_INFO,
    FINISH_API_SOMETHING_WENT_WRONG_IN_DATABASE,

    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_3,

    LOGIN_EMAIL_EMPTY,
    LOGIN_PASSWORD_EMPTY,

    LOGIN_INVALID_EMAIL,
    LOGIN_INVALID_PASSWORD,

    LOGIN_CANT_VERIFY_ROBOT,

    LOGIN_WRONG_EMAIL_OR_PASSWORD,

    LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_4,
    LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_5,
    LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_6,

    CANT_CONNECT_DATABASE,

    BASIC_DATA_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_7,
    BASIC_DATA_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_8,

    PLATFORM_CODE_GENERATOR_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_9,
    PLATFORM_CODE_GENERATOR_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_10,
    PLATFORM_CODE_GENERATOR_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_11,

}