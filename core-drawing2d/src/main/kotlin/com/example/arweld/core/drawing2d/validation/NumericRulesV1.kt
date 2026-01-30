package com.example.arweld.core.drawing2d.validation

import com.example.arweld.core.drawing2d.v1.PointV1

internal fun Double.isFiniteStrict(): Boolean = !isNaN() && !isInfinite()

internal fun PointV1.isFiniteStrict(): Boolean = x.isFiniteStrict() && y.isFiniteStrict()
