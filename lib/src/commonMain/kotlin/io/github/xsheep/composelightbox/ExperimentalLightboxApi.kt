package io.github.xsheep.composelightbox

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This api is experimental and may be changed at any time.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class ExperimentalLightboxApi
