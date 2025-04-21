package io.dodn.springboot.core.support.auth

import io.dodn.springboot.storage.db.core.user.UserRole
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

class GominUserDetails(
    val id: Long,
    private val email: String,
    private val encodedPassword: String,
    val name: String?,
    val status: UserStatus,
    val role: UserRole,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return setOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    }

    override fun getPassword(): String = encodedPassword

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = status != UserStatus.LOCKED

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = status == UserStatus.ACTIVE
}