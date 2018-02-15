package com.icthh.xm.ms.balance.config.oauth2;

import static com.icthh.xm.ms.balance.config.Constants.AUTH_AE_API_TOKEN_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_CHARGE_API_TOKEN_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_TENANT_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_USER_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_COOKIE_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_LOCALE_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_TOKEN_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_USER_ID_KEY;
import static com.icthh.xm.ms.balance.config.Constants.AUTH_XM_USER_LOGIN_KEY;

import com.icthh.xm.ms.balance.security.oauth2.OAuth2SignatureVerifierClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Improved JwtAccessTokenConverter that can handle lazy fetching of public verifier keys.
 */
public class OAuth2JwtAccessTokenConverter extends JwtAccessTokenConverter {
    private final Logger log = LoggerFactory.getLogger(OAuth2JwtAccessTokenConverter.class);

    private final OAuth2Properties oAuth2Properties;
    private final OAuth2SignatureVerifierClient signatureVerifierClient;
    /**
     * When did we last fetch the public key?
     */
    private long lastKeyFetchTimestamp;

    public OAuth2JwtAccessTokenConverter(OAuth2Properties oAuth2Properties, OAuth2SignatureVerifierClient signatureVerifierClient) {
        this.oAuth2Properties = oAuth2Properties;
        this.signatureVerifierClient = signatureVerifierClient;
        tryCreateSignatureVerifier();
    }

    /**
     * Try to decode the token with the current public key.
     * If it fails, contact the OAuth2 server to get a new public key, then try again.
     * We might not have fetched it in the first place or it might have changed.
     *
     * @param token the JWT token to decode.
     * @return the resulting claims.
     * @throws InvalidTokenException if we cannot decode the token.
     */
    @Override
    protected Map<String, Object> decode(String token) {
        try {
            //check if our public key and thus SignatureVerifier have expired
            long ttl = oAuth2Properties.getSignatureVerification().getTtl();
            if (ttl > 0 && System.currentTimeMillis() - lastKeyFetchTimestamp > ttl) {
                throw new InvalidTokenException("public key expired");
            }
            return super.decode(token);
        } catch (InvalidTokenException ex) {
            if (tryCreateSignatureVerifier()) {
                return super.decode(token);
            }
            throw ex;
        }
    }


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

    /**
     * Fetch a new public key from the AuthorizationServer.
     *
     * @return true, if we could fetch it; false, if we could not.
     */
    private boolean tryCreateSignatureVerifier() {
        long t = System.currentTimeMillis();
        if (t - lastKeyFetchTimestamp < oAuth2Properties.getSignatureVerification().getPublicKeyRefreshRateLimit()) {
            return false;
        }
        try {
            SignatureVerifier verifier = signatureVerifierClient.getSignatureVerifier();
            if(verifier!=null) {
                setVerifier(verifier);
                lastKeyFetchTimestamp = t;
                log.debug("Public key retrieved from OAuth2 server to create SignatureVerifier");
                return true;
            }
        } catch (Throwable ex) {
            log.error("could not get public key from OAuth2 server to create SignatureVerifier", ex);
        }
        return false;
    }
}
