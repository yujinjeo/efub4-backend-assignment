package efub.assignment.community.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import efub.assignment.community.account.AccountRepository;
import efub.assignment.community.account.domain.Account;
import efub.assignment.community.auth.OAuth2Principal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private JwtUtils jwtUtils;
    private AccountRepository accountRepository;
    private ObjectMapper objectMapper;
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        try {
            // 1. 토큰 유무 확인
            if (authorization==null || !authorization.startsWith("Bearer")){

                throw new JwtException("토큰이 존재하지 않습니다.");

            }

            String token = authorization.split("\\+")[1];

            // 2. 토큰 기한 만료 여부 확인
            if (jwtUtils.isExpired(token)){

                throw new JwtException("토큰 기한이 만료되었습니다.");
            }

            // 3. context authentication에 저장하기
            String nickname = jwtUtils.getNickname(token);
            Account account = accountRepository.findByNickname(nickname)
                    .orElse(null);

            // 3-1 * : 로그아웃된 JWT인지 확인
            Set<String> keys = redisTemplate.keys("token:" + nickname + ":*");

            if (keys!=null){
                for (String key:keys){
                    String logoutToken = (String)redisTemplate.opsForValue().get(key);

                    if (token.equals(logoutToken)){

                        throw new JwtException("로그아웃된 토큰입니다. 다시 로그인해주세요.");                    }

                }
            }


            if (account==null ||!nickname.equals(account.getNickname())){
                throw new JwtException("회원가입되어있지 않습니다.");
            }

            OAuth2Principal principal = new OAuth2Principal(account);

            SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_MEMBER");

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(principal,"kakao", Collections.singleton(grantedAuthority));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);


        }catch (JwtException e){
            throw e;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        filterChain.doFilter(request, response);
    }


}
