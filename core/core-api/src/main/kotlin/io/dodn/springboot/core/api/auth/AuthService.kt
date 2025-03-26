package io.dodn.springboot.core.api.auth

import io.dodn.springboot.core.domain.user.AuthResponse
import io.dodn.springboot.core.domain.user.RefreshTokenRequest
import io.dodn.springboot.core.domain.user.UserInfo
import io.dodn.springboot.core.domain.user.UserLoginRequest
import io.dodn.springboot.core.domain.user.UserRegisterRequest
import io.dodn.springboot.core.domain.user.UserService
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService,
    private val authenticationManager: AuthenticationManager,
) {

    @Transactional
    fun register(request: UserRegisterRequest): AuthResponse {
        // Register user
        val userInfo = userService.register(request)

        // Generate tokens
        val userDetails = userDetailsService.loadUserByUsername(userInfo.email)
        val accessToken = jwtService.generateToken(userDetails)
        val refreshToken = jwtService.generateRefreshToken(userDetails)

        // Save refresh token
        val refreshExpiryDate = jwtService.extractClaim(refreshToken) { claims ->
            LocalDateTime.ofInstant(claims.expiration.toInstant(), java.time.ZoneId.systemDefault())
        }
        userService.updateRefreshToken(userInfo.email, refreshToken, refreshExpiryDate)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = userInfo,
        )
    }

    @Transactional
    fun login(request: UserLoginRequest): AuthResponse {
        // Authenticate user through Spring Security
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password),
        )

        // Verify credentials and update last login time
        val userInfo = userService.verifyCredentials(request.email, request.password)

        // Generate tokens
        val userDetails = userDetailsService.loadUserByUsername(userInfo.email)
        val accessToken = jwtService.generateToken(userDetails)
        val refreshToken = jwtService.generateRefreshToken(userDetails)

        // Save refresh token
        val refreshExpiryDate = jwtService.extractClaim(refreshToken) { claims ->
            LocalDateTime.ofInstant(claims.expiration.toInstant(), java.time.ZoneId.systemDefault())
        }
        userService.updateRefreshToken(userInfo.email, refreshToken, refreshExpiryDate)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = userInfo,
        )
    }

    @Transactional
    fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val username = jwtService.extractUsername(request.refreshToken)

        // Check if refresh token is valid in database
        if (!userService.validateRefreshToken(username, request.refreshToken)) {
            throw CoreException(ErrorType.INVALID_CREDENTIALS)
        }

        val userDetails = userDetailsService.loadUserByUsername(username)
        val userInfo = userService.findByEmail(username)

        // Generate new tokens
        val accessToken = jwtService.generateToken(userDetails)
        val refreshToken = jwtService.generateRefreshToken(userDetails)

        // Save new refresh token
        val refreshExpiryDate = jwtService.extractClaim(refreshToken) { claims ->
            LocalDateTime.ofInstant(claims.expiration.toInstant(), java.time.ZoneId.systemDefault())
        }
        userService.updateRefreshToken(username, refreshToken, refreshExpiryDate)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = userInfo,
        )
    }

    @Transactional
    fun logout() {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated) {
            val userDetails = authentication.principal as UserDetails
            userService.invalidateRefreshToken(userDetails.username)
        }
    }

    fun getCurrentUser(): UserInfo {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || !authentication.isAuthenticated) {
            throw CoreException(ErrorType.UNAUTHORIZED)
        }

        val userDetails = authentication.principal as UserDetails
        return userService.findByEmail(userDetails.username)
    }
}
