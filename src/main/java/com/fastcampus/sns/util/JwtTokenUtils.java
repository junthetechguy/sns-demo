package com.fastcampus.sns.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtTokenUtils {
    /*
    자바에서 메소드에 static을 붙이는 주된 이유 중 하나는 유틸리티(utility) 메소드를 다른 클래스에서 객체 인스턴스를 생성하지 않고도 손쉽게 사용할 수 있도록 하기 위함이다.
    static 메소드는 클래스 레벨에서 호출되기 때문에 클래스의 인스턴스를 생성할 필요가 없으므로 static 메소드는 유틸리티 기능을 제공하는 클래스에서 자주 사용됨.
    static 메소드를 사용하는 이유와 장단점은 다음과 같습니다:
    장점
    인스턴스 생성 불필요: static 메소드는 클래스의 인스턴스 없이 호출할 수 있어 메모리 효율적입니다.
    편리성: 인스턴스를 생성하지 않고 바로 클래스 이름을 통해 메소드에 접근할 수 있어, 유틸리티 함수를 쉽게 사용할 수 있습니다.
     */
    public static String getUserName(String token, String key) {
        return extractClaims(token, key).get("userName", String.class);
    }

    public static boolean isExpired(String token, String key) {
        Date expiredDate = extractClaims(token, key).getExpiration();
        return expiredDate.before(new Date()); // 지금 현재보다 더 이전인지 체크
    }

    private static Claims extractClaims(String token, String key) { // Token에서 Claims를 빼오는 method
        return Jwts.parserBuilder().setSigningKey(getKey(key))
                .build().parseClaimsJws(token).getBody();
    }

    public static String generateToken(String userName, String key, long expiredTimeMs) {
        Claims claims = Jwts.claims(); // 이 Claims 안에 userName을 넣은 후 이를 이용하여 body를 만들게 된다.
        claims.put("userName", userName);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredTimeMs))
                .signWith(getKey(key), SignatureAlgorithm.HS256) // 이 key를 가지고 HS256 알고리즘(256bit hash 알고리즘)으로 암호화를 한다
                .compact();
    }

    private static Key getKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8); // key를 byte로 변환
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
