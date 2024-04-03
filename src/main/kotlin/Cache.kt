package org.example

import java.util.concurrent.ConcurrentHashMap

class Cache<K, V>(
    private val clock: Clock, private val ttl: Long
) {
    private class Value<V>(val expireTime: Long, val v: V)

    interface Clock {
        fun currentTimeMillis(): Long
    }

    private val data = ConcurrentHashMap<K, Value<V>>()

    fun put(key: K, value: V) {
        data[key] = Value(clock.currentTimeMillis() + ttl, value)
    }

    fun get(key: K): V? {
        val value = data[key] ?: return null

        if (value.expireTime < clock.currentTimeMillis()) {
            data.remove(key)
            return null
        }

        return value.v
    }

    fun cleanup() {
        val now = clock.currentTimeMillis()
        val iterator = data.entries.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.expireTime > now) iterator.remove()
        }
    }
}
