package com.wg.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -4101601621108974821L;
    private Clock clock = DefaultClock.INSTANCE;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.header}")
    private String tokenHeader;

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    private Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(clock.now());
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    private Boolean ignoreTokenExpiration(String token) {
        // here you specify tokens, for that the expiration is ignored
        return false;
    }

    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userName);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = clock.now();
        final Date expirationDate = calculateExpirationDate(createdDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)//sub(Subject)：代表这个JWT的主体，即它的所有人  username
                .setIssuedAt(createdDate)//iat: jwt的签发时间
                .setExpiration(expirationDate) //设置过期时间
                //生成签名的时候使用的秘钥secret,这个方法本地封装了的，一般可以从本地配置文件中读取，
                // 切记这个秘钥不能外露哦。它就是你服务端的私钥，在任何场景都不应该流露出去。
                // 一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。
                //指定签名的时候使用的签名算法
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
        final Date created = getIssuedAtDateFromToken(token);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
                && (!isTokenExpired(token) || ignoreTokenExpiration(token));
    }

    public String refreshToken(String token) {
        final Date createdDate = clock.now();
        final Date expirationDate = calculateExpirationDate(createdDate);

        final Claims claims = getAllClaimsFromToken(token);
        claims.setIssuedAt(createdDate);
        claims.setExpiration(expirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getToken(HttpServletRequest request){
        final String requestHeader = request.getHeader(tokenHeader);
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            return requestHeader.substring(7);
        }
        return null;
    }



    private Date calculateExpirationDate(Date createdDate) {
        return new Date(createdDate.getTime() + expiration);
    }
}

