package com.innoq.chainy.miner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innoq.chainy.model.Block
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset

object Miner {
    fun mine(previous: Block): Block {
        val seedBlock = Block(
                previous.index,
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                0,
                emptyList(),
                hashBlock(previous)
        )

        return generateSequence(seedBlock) { it.copy(proof = it.proof + 1) }
                .dropWhile { !hashBlock(it).startsWith("0000") }
                .first()
    }

    fun hashBlock(block: Block): String {
        return hashStringWithSha256(block.serialize())
    }

    private fun hashStringWithSha256(input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
                .getInstance("SHA-256")
                .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}