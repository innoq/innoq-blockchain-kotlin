package com.innoq.chainy.miner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.innoq.chainy.model.Block
import com.innoq.chainy.model.Transaction
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset

object Miner {
    fun mine(previous: Block, transactions: List<Transaction>): Block {
        val seedBlock = Block(
                previous.index,
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                0,
                transactions,
                hashBlock(previous)
        )

        // split json string to inject changing proof value (much faster than jackson)
        val hash = jacksonObjectMapper().writer().writeValueAsString(seedBlock);
        val start: Int = hash.indexOf("\"proof\":") + 8
        val end: Int = start + hash.substring(start, hash.length).indexOf(",")
        val prefix = hash.substring(0, start)
        val postfix = hash.substring(end, hash.length)

        return generateSequence(seedBlock) { it.copy(proof = it.proof + 1) }
                .dropWhile { !hashStringWithSha256(prefix + it.proof + postfix).startsWith("0000") }
                .first()

        // let the object serialize itself
//        return generateSequence(seedBlock) { it.copy(proof = it.proof + 1) }
//                .dropWhile { !hashBlock(it).startsWith("0000") }
//                .first()
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