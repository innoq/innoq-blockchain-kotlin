package com.innoq.chainy.miner

object Settings {
    val difficulty = System.getProperty("difficulty", "5").toInt()

    val port = System.getProperty("port", "8080")
}