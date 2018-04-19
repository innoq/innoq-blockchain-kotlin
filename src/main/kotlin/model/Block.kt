package model

import java.time.LocalDateTime

/**
 * Model for Block and its transactions
 *
 * Example:
 *   {
 *       "index": 1,
 *       "timestamp": 152352542,
 *       "proof": 124451,
 *       "previousBlockHash": "dsafsavew4d1as0adf001...",
 *       "transactions": [
 *           {
 *           "id": "ff09d94d-dd51-4df6-809f-ee50b2df3eff",
 *           "payload": "Arnulf Beckenbauer",
 *           "timestamp":15235254
 *           }]
 *   }
 */

data class Block(
        val index: Int,
        val timestamp: LocalDateTime,
        val proof: Int,
        val previousBlockHash: String,
        val transactions: List<Transaction>)

data class Transaction(
        val id: String,
        val payload: String,
        val timestamp: LocalDateTime)
