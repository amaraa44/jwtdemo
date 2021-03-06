package com.davidhalma.jwtdemo.jwtframework.service;

import com.davidhalma.jwtdemo.jwtframework.exception.JwtTokenException;
import com.davidhalma.jwtdemo.jwtframework.property.AccessTokenProperty;
import com.davidhalma.jwtdemo.jwtframework.property.JwtProperty;
import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.PublicKey;

@Service
@Log4j2
public class JwtTokenValidator {

    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String TOKEN_PREFIX_WITH_WHITESPACE = "Bearer ";

    private final PublicKeyService publicKeyService;
    private final AccessTokenProperty accessTokenProperty;

    public JwtTokenValidator(PublicKeyService publicKeyService, AccessTokenProperty accessTokenProperty) {
        this.publicKeyService = publicKeyService;
        this.accessTokenProperty = accessTokenProperty;
    }

    public void validateToken(String token){
        validateRequestTokenHeader(token);
        parseClaimsJws(token);
    }

    public void parseClaimsJws(String token) {
        try {
            PublicKey publicKey = publicKeyService.getKey(getAccessTokenProperty());
            Jwts.parser().setSigningKey(publicKey).parseClaimsJws(getJwtToken(token));
        }catch (ExpiredJwtException e){
            throw new JwtTokenException("JWT token expired.");
        }catch (IllegalArgumentException e){
            throw new JwtTokenException("JWT token is missing.");
        }catch (MalformedJwtException e){
            throw new JwtTokenException("JWT token is not valid.");
        }catch (UnsupportedJwtException e){
            throw new JwtTokenException("JWT token has no claims.");
        }catch (SignatureException e){
            throw new JwtTokenException("Bad JWT token signature.");
        }
    }

    private JwtProperty getAccessTokenProperty() {
        return accessTokenProperty.getJwtProperty();
    }

    private void validateRequestTokenHeader(String requestTokenHeader) {
        if (StringUtils.isEmpty(requestTokenHeader)){
            throw new JwtTokenException(AUTHORIZATION_HEADER_KEY + " header is missing.");
        }
        if (!requestTokenHeader.startsWith(TOKEN_PREFIX_WITH_WHITESPACE)){
            throw new JwtTokenException(AUTHORIZATION_HEADER_KEY + " does not begin " + TOKEN_PREFIX_WITH_WHITESPACE.trim() + " prefix.");
        }
    }

    private String getJwtToken(String requestTokenHeader) {
        return requestTokenHeader.replace(TOKEN_PREFIX_WITH_WHITESPACE, "");
    }

}
