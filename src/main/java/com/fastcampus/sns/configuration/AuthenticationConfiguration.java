package com.fastcampus.sns.configuration;

import com.fastcampus.sns.configuration.filter.JwtTokenFilter;
import com.fastcampus.sns.exception.CustomAuthenticationEntryPoint;
import com.fastcampus.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // Security 관련 Configuration을 정의하는 annotation으로 http 통신을 진행할때의 security 정책을 설정해준다.
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthenticationConfiguration extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    @Value("${jwt.secret-key}")
    private String key;

    // application을 띄울때 FE 코드와 함께 Jar file로 말아서 띄우는데 그렇게 되면 FE 코드에는 API Header 설정하는 부분이 들어있어서 FE 코드를 통해서 API call을 할때는 문제가 없는데
    // 내가 직접 localhost:8080으로 들어가서 view를 보는 동작(가령, favicon 가져 오는 요청)에 대해서 이때의 모든 reqeust에 대해서 token에서 user를 뽑아내는 동작이 걸리게 되므로
    // 따라서 어느 경우에만 filter를 태울건지를 정하자.
    @Override
    public void configure(WebSecurity web) throws Exception {
        // api가 path 정보에 들어있을때만 filter를 태운다. 나머지(가령, favicon 가져 오는 요청)은 filter를 아예 무시한다.
        web.ignoring().regexMatchers("^(?!/api/).*")
                .antMatchers(HttpMethod.POST, "/api/*/users/join", "/api/*/users/login"); // 정의해둔 WebSecurity에서 정의한 부분(join, login)은 permit해준다는 의미
        // 모든 version에 대해서 일단 join과 login api path는 csrf를 open해줘도 된다.
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception { // HttpSecurity를 가져와서 configure을 설정하는 WebSecurityConfigurerAdapter의 method override
        http.csrf().disable() // 일단 csrf를 disable하고 내가 원하는 match 조건에 맞는 api만 잠근상태로 open해준다.
                .authorizeRequests()
                .antMatchers("/api/**").authenticated()  // join, login 하는 부분을 제외한 모든 부분은 항상 authentication이 이루어져야 한다.
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // session을 따로 관리하지 않으므로 그냥 STATELESS로 session을 처리하자.
                .and()
                .addFilterBefore(new JwtTokenFilter(key, userService), UsernamePasswordAuthenticationFilter.class) // 매 request마다 filter를 하나 둬서 reqeust로 들어온 Token이 어떤 User를 가리키는지 체크하는 logic을 추가하기 위해서 Username과 Password Authenticaition Filter 이전에 이 JwtTokenFilter()를 태운다.
                .exceptionHandling() // spring security쪽에서 인증하다가 exception이 던저질 경우 아래의 entry point로 가라는 의미
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }
}
