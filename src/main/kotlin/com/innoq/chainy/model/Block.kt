package com.innoq.chainy.model

import com.fasterxml.jackson.annotation.JsonIgnore

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
        val previousBlockHash: String,
        @JsonIgnore
        val next: Block? = null,
        @JsonIgnore
        val previous: Block? = null)

data class Transaction(
        val id: String,
        val timestamp: Long,
        val payload: String)
