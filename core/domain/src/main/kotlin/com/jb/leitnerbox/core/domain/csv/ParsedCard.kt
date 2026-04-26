package com.jb.leitnerbox.core.domain.csv

data class ParsedCard(
    val lineNumber: Int,
    val recto: String,
    val verso: String,
    val needsInput: Boolean = false
)
