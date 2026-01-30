package com.example.arweld.core.drawing2d.validation

internal fun MutableList<ViolationV1>.addError(
    code: String,
    path: String,
    message: String,
    hint: String? = null,
    refs: List<String> = emptyList()
) {
    add(
        ViolationV1(
            code = code,
            severity = SeverityV1.ERROR,
            path = path,
            message = message,
            hint = hint,
            refs = refs
        )
    )
}

internal fun MutableList<ViolationV1>.addWarn(
    code: String,
    path: String,
    message: String,
    hint: String? = null,
    refs: List<String> = emptyList()
) {
    add(
        ViolationV1(
            code = code,
            severity = SeverityV1.WARN,
            path = path,
            message = message,
            hint = hint,
            refs = refs
        )
    )
}
