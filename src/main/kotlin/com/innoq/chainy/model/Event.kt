package com.innoq.chainy.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

enum class EventType(val type: String) {
    NEW_BLOCK("new_block"),
    NEW_TRANSACTION("new_transaction"),
    NEW_NODE("new_node")
}

@JsonSerialize(using = EventSerializer::class)
@JsonDeserialize(using = EventDeserializer::class)
sealed class Event

data class NewBlockEvent(val block: Block) : Event()

data class NewTransactionEvent(val transaction: Transaction) : Event()

data class NewNodeEvent(val node: RemoteNode) : Event()

object EventSerializer : JsonSerializer<Event>() {
    override fun serialize(value: Event?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let {
            gen!!.writeStartObject()
            when (it) {
                is NewBlockEvent -> {
                    gen.writeStringField("type", "new_block")
                    gen.writeObjectField("data", it.block)
                }
                is NewTransactionEvent -> {
                    gen.writeStringField("type", "new_transaction")
                    gen.writeObjectField("data", it.transaction)
                }
                is NewNodeEvent -> {
                    gen.writeStringField("type", "new_node")
                    gen.writeObjectField("data", it.node)
                }
            }
            gen.writeEndObject()
        }
    }
}

object EventDeserializer : JsonDeserializer<Event>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Event {
        
    }

}