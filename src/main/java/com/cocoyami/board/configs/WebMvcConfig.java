package com.cocoyami.board.configs;

import com.cocoyami.board.interceptors.CommonInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration //이걸 붙여야 설정하는 클래스가 됨
public class WebMvcConfig implements WebMvcConfigurer { // config : 설정

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.commonInterceptor()).addPathPatterns("/**")
                .excludePathPatterns("/**/resources/**"); //css,js는 인터셉터를 거쳐갈 필요가 없음, 그래서 이거 추가해서 안받게 해줌
        // 이거 추가해서 만약에 /read?aid=.. 페이지면 read 랑 댓글만 받음
        //    /* 랑 /** 차이
        //  주소가 /hello 면 둘 다 적용 가능, 근데 /hello/login 이면 /*는 적용이 안됨
    }



    @Bean
    public HandlerInterceptor commonInterceptor() {
        return new CommonInterceptor(); //commonInterceptor 객체화한거
    }
}
