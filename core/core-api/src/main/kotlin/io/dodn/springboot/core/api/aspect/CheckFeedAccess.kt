package io.dodn.springboot.core.api.aspect

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CheckFeedAccess(
    val permission: String,
    val feedIdParam: String = "feedId",
)
