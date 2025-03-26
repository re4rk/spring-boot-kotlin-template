package io.dodn.springboot.storage.db.core.user

enum class UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    INACTIVE,
    LOCKED,
    DELETED,
}
