package com.xeniac.warrantyroster_manager.utils

object Constants {
    //Web URLs
    const val URL_PRIVACY_POLICY = "https://warranty-roster.herokuapp.com/privacy-policy"

    //Tapsell Ads Constants
    const val TAPSELL_KEY =
        "phbftfiakptpjbkegafmqmmkdsjcjmkldcahhapfssfftdnbgpqeimkjiitfpcoingqkad"
    const val WARRANTIES_NATIVE_ZONE_ID = "61647159b2c8056d868b66c6"
    const val SETTINGS_NATIVE_ZONE_ID = "616b0d4cb2c8056d868b6a1a"
    const val DELETE_WARRANTY_Interstitial_ZONE_ID = "616476f7b2c8056d868b66cb"

    //SharedPreference Settings Constants
    const val PREFERENCE_SETTINGS = "preference_settings"
    const val PREFERENCE_THEME_KEY = "theme"
    const val PREFERENCE_LANGUAGE_KEY = "language"
    const val PREFERENCE_COUNTRY_KEY = "country"

    //SharedPreference Login Constants
    const val PREFERENCE_LOGIN = "preference_login"
    const val PREFERENCE_IS_LOGGED_IN_KEY = "is_logged_in"

    //Warranty Adapter View Type Constants
    const val VIEW_TYPE_WARRANTY = 0
    const val VIEW_TYPE_AD = 1

    //Add Warranty Activity Calendars Fragment Tag Constants
    const val FRAGMENT_TAG_ADD_CALENDAR_STARTING = "fragment_tag_add_calendar_starting"
    const val FRAGMENT_TAG_ADD_CALENDAR_EXPIRY = "fragment_tag_add_calendar_expiry"

    //Edit Warranty Activity Calendars Fragment Tag Constants
    const val FRAGMENT_TAG_EDIT_CALENDAR_STARTING = "fragment_tag_edit_calendar_starting"
    const val FRAGMENT_TAG_EDIT_CALENDAR_EXPIRY = "fragment_tag_edit_calendar_expiry"

    //Firestore Collections ID Constants
    const val COLLECTION_CATEGORIES = "categories"
    const val COLLECTION_WARRANTIES = "warranties"

    //Firestore Categories Collection Fields Constants
    const val CATEGORIES_TITLE = "title"
    const val CATEGORIES_ICON = "icon"

    //Firestore Warranties Collection Fields Constants
    const val WARRANTIES_TITLE = "title"
    const val WARRANTIES_BRAND = "brand"
    const val WARRANTIES_MODEL = "model"
    const val WARRANTIES_SERIAL_NUMBER = "serialNumber"
    const val WARRANTIES_STARTING_DATE = "startingDate"
    const val WARRANTIES_EXPIRY_DATE = "expiryDate"
    const val WARRANTIES_DESCRIPTION = "description"
    const val WARRANTIES_CATEGORY_ID = "categoryId"
    const val WARRANTIES_UUID = "uuid"

    //Response Errors
    const val ERROR_NETWORK_CONNECTION = "Unable to connect to the internet"
    const val ERROR_NETWORK_403 = "403"
    const val ERROR_FIREBASE_AUTH_ACCOUNT_EXISTS =
        "The email address is already in use by another account"
    const val ERROR_FIREBASE_AUTH_ACCOUNT_NOT_FOUND =
        "There is no user record corresponding to this identifier"
    const val ERROR_FIREBASE_AUTH_CREDENTIALS =
        "The password is invalid or the user does not have a password"
    const val ERROR_EMPTY_CATEGORY_LIST = "Category list is empty"
    const val ERROR_EMPTY_WARRANTY_LIST = "Warranty list is empty"

    //Login Fragment SaveInstanceState Keys
    const val SAVE_INSTANCE_LOGIN_EMAIL = "save_instance_login_email"
    const val SAVE_INSTANCE_LOGIN_PASSWORD = "save_instance_login_password"

    //Register Fragment SaveInstanceState Keys
    const val SAVE_INSTANCE_REGISTER_EMAIL = "save_instance_register_email"
    const val SAVE_INSTANCE_REGISTER_PASSWORD = "save_instance_register_password"
    const val SAVE_INSTANCE_REGISTER_RETYPE_PASSWORD = "save_instance_register_retype_password"

    //Forgot PW Fragment SaveInstanceState Keys
    const val SAVE_INSTANCE_FORGOT_PW_EMAIL = "save_instance_forgot_pw_email"

    //Change Email Fragment SaveInstanceState Keys
    const val SAVE_INSTANCE_CHANGE_EMAIL_PASSWORD = "save_instance_change_email_password"
    const val SAVE_INSTANCE_CHANGE_EMAIL_NEW_EMAIL = "save_instance_change_email_new_email"

    //Change Password Fragment SaveInstanceState Keys
    const val SAVE_INSTANCE_CHANGE_PASSWORD_CURRENT = "save_instance_change_password_current"
    const val SAVE_INSTANCE_CHANGE_PASSWORD_NEW = "save_instance_change_password_new"
    const val SAVE_INSTANCE_CHANGE_PASSWORD_RETYPE = "save_instance_change_password_retype"
}