package com.icthh.xm.ms.balance.security;

import static com.icthh.xm.ms.balance.config.Constants.AUTH_AE_API_TOKEN_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_CHARGE_API_TOKEN_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_TENANT_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_USER_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_COOKIE_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_LOCALE_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_TOKEN_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_USER_ID_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_USER_LOGIN_KEY;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * Overrides to get token tenant.
 */
public class DomainJwtAccessTokenConverter extends JwtAccessTokenConverter {

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        final OAuth2Authentication authentication = super.extractAuthentication(map);
        final Map<String, String> details = new HashMap<>();
        details.put(AUTH_TENANT_KEY, (String) map.get(AUTH_TENANT_KEY));
        details.put(AUTH_USER_KEY, (String) map.get(AUTH_USER_KEY));
        details.put(AUTH_XM_TOKEN_KEY, (String) map.get(AUTH_XM_TOKEN_KEY));
        details.put(AUTH_XM_COOKIE_KEY, (String) map.get(AUTH_XM_COOKIE_KEY));
        details.put(AUTH_XM_USER_ID_KEY, (String) map.get(AUTH_XM_USER_ID_KEY));
        details.put(AUTH_XM_USER_LOGIN_KEY, (String) map.get(AUTH_XM_USER_LOGIN_KEY));
        details.put(AUTH_XM_LOCALE_KEY, (String) map.get(AUTH_XM_LOCALE_KEY));
        details.put(AUTH_AE_API_TOKEN_KEY, (String) map.get(AUTH_AE_API_TOKEN_KEY));
        details.put(AUTH_CHARGE_API_TOKEN_KEY, (String) map.get(AUTH_CHARGE_API_TOKEN_KEY));
        authentication.setDetails(details);

        return authentication;
    }
}
