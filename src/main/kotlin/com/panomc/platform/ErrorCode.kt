package com.panomc.platform

enum class ErrorCode {
    INVALID_DATA,
    UNKNOWN,

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

    PANEL_BASIC_DATA_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_7,
    PANEL_BASIC_DATA_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_8,
    PANEL_BASIC_DATA_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_68,

    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_12,
    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_13,
    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_14,
    REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_15,

    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_16,
    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_17,
    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_18,
    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_19,
    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_20,
    DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_112,

    CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_22,
    CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_23,
    CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_24,

    CLOSE_CONNECT_SERVER_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_25,
    CLOSE_CONNECT_SERVER_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_26,
    CLOSE_CONNECT_SERVER_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_27,

    LOGOUT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_28,

    CONNECT_NEW_SERVER_API_PLATFORM_CODE_WRONG,
    CONNECT_NEW_SERVER_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_29,

    PANEL_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_65,
    PANEL_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_66,
    PANEL_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_110,
    PANEL_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_111,

    PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_69,
    PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_70,
    PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_71,
    PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_72,

    PANEL_QUICK_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_73,
    PANEL_QUICK_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_74,
    PANEL_QUICK_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_67,

    PAGE_NOT_FOUND,

    TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_75,
    TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_76,
    TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_77,
    TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_78,

    TICKET_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_79,
    TICKET_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_80,

    POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_81,
    POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_82,
    POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_83,
    POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_84,

    POST_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_85,
    POST_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_86,

    POST_CATEGORY_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_87,
    POST_CATEGORY_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_88,

    TICKET_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_89,
    TICKET_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_90,

    NOT_EXISTS,

    TICKET_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_91,

    TICKET_CATEGORY_UPDATE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_92,

    POST_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_93,
    POST_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_94,

    POST_CATEGORY_UPDATE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_95,
    POST_CATEGORY_UPDATE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_96,

    POST_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_97,
    POST_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_98,

    POST_NOT_FOUND,

    EDIT_POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_99,
    EDIT_POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_100,

    DELETE_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_101,
    DELETE_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_102,

    PUBLISH_ONLY_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_103,
    PUBLISH_ONLY_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_104,
    PUBLISH_ONLY_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_105,

    MOVE_TRASH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_106,
    MOVE_TRASH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_107,

    MOVE_DRAFT_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_108,
    MOVE_DRAFT_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_109,

    TICKET_CLOSE_TICKETS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_113,

    PUBLISH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_114,
    PUBLISH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_115,
    PUBLISH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_116

    // last ID 116
}