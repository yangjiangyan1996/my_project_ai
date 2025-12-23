package com.example.filter;

import com.example.config.Config;
import com.example.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;


@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String phone = authentication.getName();
        String code = authentication.getCredentials().toString();
        UserDetails user = accountService.loadUserByUsername(phone);
        String codeOfRedis = accountService.getPhoneVerifyCode(phone);

        if (passwordEncoder.matches( code, user.getPassword())) { // 密码登录
//        if (code.equals(codeOfRedis)) { //验证码登录
            return new UsernamePasswordAuthenticationToken(
                    user, user.getAuthorities());
        } else {
            throw new BadCredentialsException("用户名或者密码错误！");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private boolean isNoAuthRequest() {
        try {
            // 获取当前请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return false;
            }
            HttpServletRequest request = attributes.getRequest();

            // 检查SecurityContext中是否已有认证信息
            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
            if (existingAuth != null && existingAuth.isAuthenticated()) {
                return false;
            }

            // 这里可以添加其他逻辑，例如检查请求路径是否在白名单中
            String requestURI = request.getRequestURI();
            if (Arrays.stream(Config.WHITE_URL).anyMatch(requestURI::startsWith)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}

