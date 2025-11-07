package com.y11i.springcommddd.config;

import com.y11i.springcommddd.iam.api.support.CurrentMemberIdArgumentResolver;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebMvcConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
    private final CurrentMemberIdArgumentResolver resolver;

    public WebMvcConfig(CurrentMemberIdArgumentResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void addArgumentResolvers(java.util.List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }
}
