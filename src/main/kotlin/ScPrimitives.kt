package org.example

import java.lang.invoke.VarHandle
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.Volatile

class SafePublication<T> {
    @Volatile private var ref: T? = null

    /** @param value более не должен изменяться на опубликовавшей стороне */
    fun set(value: T) { // T1
        ref = value
    }

    fun get(): T? {       // T2
        return ref
    }

    fun await(): T {     // T2
        var res: T? = null
        do { res = get() } while(res == null)
        return res
    }
}

/** Non-SC внутри, но SC снаружи */
class SafePublicationRelaxed<T> {
    private val ref = AtomicReference<T>()

    /** @param value более не должен изменяться на опубликовавшей стороне */
    fun set(value: T) {   // T1
        VarHandle.storeStoreFence()
        ref.opaque = value
    }

    fun get(): T? {             // T2
        val local = ref.opaque
        if (local != null) {
            VarHandle.loadLoadFence()
            return local
        } else return null
    }

    fun await(): T {           // T2
        var res: T? = null
        do { res = get() } while(res == null)
        return res
    }
}

class BalanceManager(// lock-protected shared data
    private var balance1: BigDecimal, private var balance2: BigDecimal
) {
    @Synchronized
    fun transfer1to2(amount: BigDecimal?) {
        balance1 = balance1.subtract(amount)
        balance2 = balance2.add(amount)
    }

    @Synchronized
    fun transfer2to1(amount: BigDecimal?) {
        balance2 = balance2.subtract(amount)
        balance1 = balance1.add(amount)
    }
}