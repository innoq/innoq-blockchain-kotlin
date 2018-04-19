package com.innoq.chainy.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

/**
 * Model for Block and its transactions
 *
 * Example:
 *   {
 *       "index": 1,
 *       "timestamp": 152352542,
 *       "proof": 124451,
 *       "transactions": [
 *           {
 *           "id": "ff09d94d-dd51-4df6-809f-ee50b2df3eff",
 *           "timestamp":15235254,
 *           "payload": "Arnulf Beckenbauer"
 *           }],
 *       "previousBlockHash": "dsafsavew4d1as0adf001..."
 *   }
 */

data class Block(
        val index: Int,
        val timestamp: Long,
        val proof: Int,
        val transactions: List<Transaction>,
        val previousBlockHash: String) {

    /**
     * Manual serialization is about 10 times faster than generic jackson serialization
     */
    fun serialize(): String {
        val output = StringBuffer("{")
                .append("\"index\":")
                .append(index)
                .append(",\"timestamp\":")
                .append(timestamp)
                .append(",\"proof\":")
                .append(proof)
                .append(",\"transactions\":[")

        transactions.forEach { transaction ->
            output.append("{")
                    .append("\"id\":\"")
                    .append(transaction.id)
                    .append("\",\"timestamp\":")
                    .append(transaction.timestamp)
                    .append(",\"payload\":\"")
                    .append(transaction.payload)
                    .append("\"}")
        }

        output.append("],\"previousBlockHash\":\"")
                .append(previousBlockHash)
                .append("\"}")

        return output.toString()
    }
}

data class Transaction(
        val id: UUID,
        val timestamp: Long,
        val payload: String)
