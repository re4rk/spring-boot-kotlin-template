package io.dodn.springboot.core.domain.user

import io.dodn.springboot.storage.db.core.user.UserEntity

fun UserEntity.toUserInfo(): UserInfo = UserInfo(
    id = this.id,
    email = this.email,
    password = this.password,
    name = this.name,
    status = this.status,
    role = this.role,
    lastLoginAt = this.lastLoginAt,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)
