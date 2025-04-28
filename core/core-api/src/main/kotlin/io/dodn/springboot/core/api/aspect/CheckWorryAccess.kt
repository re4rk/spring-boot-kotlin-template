package io.dodn.springboot.core.api.aspect

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CheckWorryAccess(
    val permission: String,
    val worryIdParam: String = "worryId",
)
