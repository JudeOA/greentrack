package com.greentrack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1) Uploaded files (unchanged)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);

        // 2) Serve the single-page app from classpath:/static/.
        //    Any request that isn't a real file (and isn't an /api/ call)
        //    falls back to index.html, so the SPA loads on every route.
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // Don't hijack API routes
                        if (resourcePath.startsWith("api/")) {
                            return null;
                        }
                        // Fallback: the SPA entry point
                        Resource index = new ClassPathResource("/static/index.html");
                        return index.exists() ? index : null;
                    }
                });
    }
}
