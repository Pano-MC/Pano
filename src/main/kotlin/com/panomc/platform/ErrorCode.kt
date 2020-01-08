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

    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_12,
    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_13,
    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_14,
    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_15,

    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_16,
    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_17,
    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_18,
    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_19,
    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_20,

    CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_22,
    CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_23,
    CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_24,

    CLOSE_CONNECT_SERVER_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_25,
    CLOSE_CONNECT_SERVER_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_26,
    CLOSE_CONNECT_SERVER_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_27,

    LOGOUT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_28,

    CONNECT_NEW_SERVER_API_PLATFORM_CODE_WRONG,
    CONNECT_NEW_SERVER_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_29,
    CONNECT_NEW_SERVER_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_30,

    PAGE_NOT_FOUND,

    TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_31,
    TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_32,
    TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_33,
    TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_34,

    TICKET_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_35,
    TICKET_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_36,

    POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_37,
    POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_38,
    POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_39,
    POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_40,

    POST_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_41,
    POST_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_42,

    POST_CATEGORY_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_42,
    POST_CATEGORY_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_43,

    TICKET_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_44,
    TICKET_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_45,

    NOT_EXISTS,

    TICKET_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_46,

    TICKET_CATEGORY_UPDATE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_47,

    POST_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_48,
    POST_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_49


}