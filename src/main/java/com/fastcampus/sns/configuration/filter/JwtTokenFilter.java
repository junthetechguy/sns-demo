package com.fastcampus.sns.configuration.filter;

import com.fastcampus.sns.model.User;
import com.fastcampus.sns.service.UserService;
import com.fastcampus.sns.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    // 매 reqeust 때마다 filter를 태울것이므로 OncePerReqeustFilter를 extends해서 override로 구현해주자.

    private final String key;
    private final UserService userService;
    private final static List<String> TOKEN_IN_PARAM_URLS = List.of("api/v1/users/alarm/subscribe"); // Path param에 token 정보가 담겨있는 API URL

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 매 reqeust마다 token을 헤더에 넣어서 request가 오므로 이 filter에서 header에서 해당 token을 뽑아서
        // 이 token에 들어있는 claims에 담긴 userName을 꺼내서 이 userName으로 실제 user가 유효한지 확인해주자.
        final String token; // 그냥 String이 아니라 좀 더 안전하게 하기 위해서 final String(반드시 초기화 진행해야 하는거)으로 선언해주자.
        try {
            if (TOKEN_IN_PARAM_URLS.contains(request.getRequestURI())) {
                log.info("Request with {} check the query param", request.getRequestURI());
                token = request.getQueryString().split("=")[1].trim();
            } else {
                // http request header의 Authorization에 필요한 정보가 들어있으므로 이것을 받아오자.
                final String header = request.getHeader(HttpHeaders.AUTHORIZATION); // 그냥 String이 아니라 좀 더 안전하게 하기 위해서 final String(반드시 초기화 진행해야 하는거)으로 선언해주자.
                if (header == null || !header.startsWith("Bearer ")) {
                    log.error("Error occurs while getting header. header is null or invalid {}", request.getRequestURL());
                    filterChain.doFilter(request, response); // Error가 났어도 일단 성공했으므로 filterChain으로 이제 뒤에 filter들로 작업을 넘긴 후 return으로 filter 작업을 스스로 종료해준다.
                    return;
                }
                token = header.split(" ")[1].trim(); // header에서 Bearer 때고 token만 뽑아오기
            }

            if(JwtTokenUtils.isExpired(token, key)){
                log.error("Key is expired");
                filterChain.doFilter(request, response);
                return;
            }


            // 만약 token이 valid하다면 user도 valid한지 확인 후 request context에 이 Authentication 정보를 넣어서 controller로 보내주자.
            String userName = JwtTokenUtils.getUserName(token, key);
            User user = userService.loadUserByUserName(userName); // 이 loadUserByUserName() 부분은 Cache를 통해서 줄여주 자.
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities()); // Principal param에는 user를 넣고, credentials param은 null을 넣고, authorities param에는 해당 user의 userRole을 List 형태로 집어넣는다.
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }catch (RuntimeException e) {
            log.error("Error occurs while validating, {}", e.toString());
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response); // 성공했으므로 filterChain으로 이제 뒤에 filter들로 작업을 넘긴다.
    }
}
