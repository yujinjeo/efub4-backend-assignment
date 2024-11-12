package efub.assignment.community.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import efub.assignment.community.account.AccountRepository;
import efub.assignment.community.account.domain.Account;
import efub.assignment.community.auth.OAuth2UserService;
import efub.assignment.community.auth.jwt.JwtFilter;
import efub.assignment.community.auth.jwt.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;
    private final OAuth2UserService oAuth2UserService;
    private final RedisTemplate<String , Object> redisTemplate;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/login/**").permitAll()
                        .anyRequest().hasRole("MEMBER")
                )
                .addFilterBefore(new JwtFilter(jwtUtils,accountRepository,objectMapper,redisTemplate), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2-> oauth2.userInfoEndpoint(o-> o.userService(oAuth2UserService))
                        .successHandler(successHandler()))
                ;
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("http://localhost:8080");

        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PATCH");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedHeader("*");
        // 헤더에 authorization항목이 있으므로 credential을 true로 설정합니다.
        configuration.setAllowCredentials(true);

        source.registerCorsConfiguration("/**",configuration);

        return source;
    }


    @Bean
    public AuthenticationSuccessHandler successHandler(){
        return ((request, response, authentication) -> {
            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

            String nickname = (String)defaultOAuth2User.getAttributes().get("nickname");

            String newRefreshToken = jwtUtils.createToken(nickname);
            Account account = accountRepository.findByNickname(nickname)
                    .orElseThrow(() -> new RuntimeException());

            account.updateRefreshToken(newRefreshToken);
            accountRepository.save(account);


            String token = "Bearer "+jwtUtils.createToken(nickname);


            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8.toString());
            System.out.println(encodedToken);

            // 쿠키 생성
            Cookie cookie = new Cookie("accessToken", encodedToken);
            cookie.setPath("/");
            cookie.setMaxAge(60*60*24);
            response.addCookie(cookie);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");


        });
    }


}
