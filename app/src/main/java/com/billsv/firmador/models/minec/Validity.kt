package com.billsv.firmador.models.minec

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.Instant


class Validity {
    var notBefore: Instant
    var notAfter: Instant

    constructor(notBefore: Instant, notAfter: Instant) : super() {
        this.notBefore = notBefore
        this.notAfter = notAfter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    constructor() : super() {
        notBefore = Instant.now()
        notAfter = notBefore.plus(Duration.ofDays(1825))
    }
}

