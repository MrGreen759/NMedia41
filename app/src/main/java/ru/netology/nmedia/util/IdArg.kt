package ru.netology.nmedia.util

import android.os.Bundle
import ru.netology.nmedia.dto.Post
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object IdArg: ReadWriteProperty<Bundle, Long?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Long?) {
        if (value != null) {
            thisRef.putLong(property.name, value)
        }
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): Long? =
        thisRef.getLong(property.name)

}