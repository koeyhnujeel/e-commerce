package zoonza.commerce.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import zoonza.commerce.adapter.`in`.auth.JwtAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(restAuthenticationEntryPoint) }
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/error",
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/auth/refresh",
                        "/api/members/signup",
                        "/api/members/signup/**",
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
