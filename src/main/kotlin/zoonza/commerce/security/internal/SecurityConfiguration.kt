package zoonza.commerce.security.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(restAuthenticationEntryPoint) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/orders",
                        "/api/orders/{orderId}/payments",
                        "/api/payments/{paymentId}/confirm",
                        "/api/payments/{paymentId}/cancel",
                        "/api/products/{productId}/likes",
                        "/api/products/{productId}/likes/cancel",
                        "/api/products/{productId}/reviews",
                        "/api/orders/items/{orderItemId}/purchase-confirmation",
                    ).authenticated()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/orders",
                        "/api/orders/{orderId}",
                        "/api/payments/{paymentId}",
                        "/api/products/{productId}/reviews/me",
                    ).authenticated()
                    .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/orders/{orderId}",
                    ).authenticated()
                    .requestMatchers(
                        HttpMethod.PUT,
                        "/api/products/{productId}/reviews/me",
                    ).authenticated()
                    .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/orders/{orderId}",
                        "/api/products/{productId}/reviews/me",
                    ).authenticated()
                    .anyRequest().permitAll()
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java,
            )
            .build()
    }
}
