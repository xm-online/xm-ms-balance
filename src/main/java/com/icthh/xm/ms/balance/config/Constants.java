package com.icthh.xm.ms.balance.config;

/**
 * Application constants.
 */
public final class Constants {

    //Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_'.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";

    public static final String AUTH_TENANT_KEY = "tenant";
    public static final String AUTH_USER_KEY = "user_key";
    public static final String AUTH_XM_TOKEN_KEY = "xmToken";
    public static final String AUTH_XM_COOKIE_KEY = "xmCookie";
    public static final String AUTH_XM_USER_ID_KEY = "xmUserId";
    public static final String AUTH_XM_LOCALE_KEY = "xmLocale";
    public static final String AUTH_AE_API_TOKEN_KEY = "aeApiToken";
    public static final String AUTH_XM_USER_LOGIN_KEY = "xmUserLogin";
    public static final String AUTH_CHARGE_API_TOKEN_KEY = "chargeApiToken";

    public static final String HEADER_TENANT = "x-tenant";

    public static final String DDL_CREATE_SCHEMA = "CREATE SCHEMA IF NOT EXISTS %s";
    public static final String CHANGE_LOG_PATH = "classpath:config/liquibase/master.xml";

    public static final String CERTIFICATE = "X.509";
    public static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----%n%s%n-----END PUBLIC KEY-----";

    private Constants() {
    }
}
