package efub.assignment.community.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private Key key;

    @Autowired
    public JwtUtils(@Value("${spring.jwt.secret}") String secretKey){
        byte[] decode = Decoders.BASE64.decode(secretKey);

        key= Keys.hmacShaKeyFor(decode);
    }

    public String createToken(String nickname){
        Claims claims = Jwts.claims();

        claims.put("nickname",nickname);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+60*60*12*1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

    }

    public String getNickname(String token){
        try {
            String nickname = Jwts.parserBuilder().setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("nickname", String.class);
            return nickname;
        }catch (Exception e){
            throw e;
        }

    }

    public boolean isExpired(String token) {
        return Jwts.parserBuilder().setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());

    }

}
