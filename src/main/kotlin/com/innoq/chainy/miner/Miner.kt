package com.innoq.chainy.miner

import com.innoq.chainy.model.Block
import com.innoq.chainy.model.Transaction
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset

object Miner {
    fun mine(previous: Block, transactions: List<Transaction>, difficulty: Int): Block {
        val seedBlock = Block(
                previous.index + 1,
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                0,
                transactions,
                hashBlock(previous)
        )

        // let the object serialize itself
        return generateSequence(seedBlock) { it.copy(proof = it.proof + 1) }
                .dropWhile { !hashPassesDifficulty(hashBlock(it), difficulty) }
                .first()
    }

    fun hashPassesDifficulty(hash: String, difficulty: Int): Boolean {
        return hash.take(difficulty).all { it == '0' }
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