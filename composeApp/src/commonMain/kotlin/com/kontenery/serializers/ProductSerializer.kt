package com.kontenery.serializers

import com.kontenery.model.Product
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val productSerializersModule = SerializersModule {
    polymorphic(Product::class) {
        subclass(Product.Container::class, Product.Container.serializer())
        subclass(Product.Yard::class, Product.Yard.serializer())
    }
}