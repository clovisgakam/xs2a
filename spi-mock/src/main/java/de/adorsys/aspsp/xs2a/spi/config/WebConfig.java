/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.spi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@RequiredArgsConstructor
public class WebConfig extends de.adorsys.psd2.xs2a.web.config.WebConfig {
    private final CorsConfigProperties corsConfigProperties;

    @Bean
    public FilterRegistrationBean corsFilterRegistrationBean() {
        CorsConfiguration config = new CorsConfiguration();
        config.applyPermitDefaultValues();
        config.setAllowCredentials(corsConfigProperties.getAllowCredentials());
        config.setAllowedOrigins(corsConfigProperties.getAllowedOrigins());
        config.setAllowedHeaders(corsConfigProperties.getAllowedHeaders());
        config.setAllowedMethods(corsConfigProperties.getAllowedMethods());
        config.setMaxAge(corsConfigProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new FilterRegistrationBean(new CorsFilter(source));
    }}
