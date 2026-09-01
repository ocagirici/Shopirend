package com.shopirend.security

import com.shopirend.repository.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

data class AuthenticatedUser(val id: UUID, val email: String)

@Component
class JwtService(
    @Value("\${app.jwt.secret}") secret: String,
    @Value("\${app.jwt.expiration-minutes}") private val expirationMinutes: Long,
) {
    private val key = Keys.hmacShaKeyFor(secret.padEnd(32, '0').toByteArray(StandardCharsets.UTF_8))

    fun create(userId: UUID, email: String): String = Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .issuedAt(Date())
        .expiration(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
        .signWith(key)
        .compact()

    fun parse(token: String): AuthenticatedUser {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        return AuthenticatedUser(UUID.fromString(claims.subject), claims["email"] as String)
    }
}

@Component
class JwtAuthenticationFilter(private val jwtService: JwtService, private val users: UserRepository) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = request.getHeader("Authorization")?.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")
        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            runCatching { jwtService.parse(token) }.getOrNull()?.takeIf { users.existsById(it.id) }?.let { principal ->
                SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(principal, token, listOf(SimpleGrantedAuthority("ROLE_USER")))
            }
        }
        filterChain.doFilter(request, response)
    }
}

@Configuration
class SecurityConfig(private val jwtFilter: JwtAuthenticationFilter) {
    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }
        .cors { }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it.requestMatchers("/auth/**", "/actuator/health").permitAll()
                .anyRequest().authenticated()
        }
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()
}
