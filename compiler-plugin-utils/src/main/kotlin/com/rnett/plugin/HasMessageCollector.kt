package com.rnett.plugin

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import java.io.File

interface HasMessageCollector {
    val messageCollector: MessageCollector

    val file: File
    val fileText: String

    fun IrElement.messageLocation(): CompilerMessageLocation? {
        val beforeText = fileText.replace("\r\n", "\n").substring(0, this.startOffset)
        val line = beforeText.count { it == '\n' } + 1
        val beforeLine = beforeText.substringBeforeLast('\n').length
        val offsetOnLine = this.startOffset - beforeLine
        return CompilerMessageLocation.create(file.path, line, offsetOnLine, null)
    }

    fun IrElement.reportErrorAt(
        message: String,
        level: CompilerMessageSeverity = CompilerMessageSeverity.ERROR
    ) = messageCollector.report(
        level,
        message,
        this.messageLocation()
    )

    fun report(message: String, level: CompilerMessageSeverity, location: CompilerMessageLocation?) =
        messageCollector.report(level, message, location)

    fun reportAt(message: String, level: CompilerMessageSeverity, location: IrElement) = report(message, level, location.messageLocation())

    fun reportError(message: String, location: CompilerMessageLocation?) = report(message, CompilerMessageSeverity.ERROR, location)
    fun reportErrorAt(message: String, location: IrElement) = reportError(message, location.messageLocation())
}