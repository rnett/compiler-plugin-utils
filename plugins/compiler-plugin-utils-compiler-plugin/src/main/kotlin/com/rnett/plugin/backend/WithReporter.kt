package com.rnett.plugin.backend

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocationWithRange
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path

interface WithReporter {
    val messageCollector: MessageCollector

    val file: IrFile

    fun IrElement.messageLocation(): CompilerMessageSourceLocation? {
        val loc = file.fileEntry.getSourceRangeInfo(startOffset, endOffset)
        return CompilerMessageLocationWithRange.create(file.path, loc.startLineNumber, loc.startColumnNumber, loc.endLineNumber, loc.endColumnNumber, null)
    }

    fun IrElement.reportErrorAt(
            message: String,
            level: CompilerMessageSeverity = CompilerMessageSeverity.ERROR
    ) = messageCollector.report(
            level,
            message,
            this.messageLocation()
    )

    fun report(message: String, level: CompilerMessageSeverity, location: CompilerMessageSourceLocation?) =
            messageCollector.report(level, message, location)

    fun reportAt(message: String, level: CompilerMessageSeverity, location: IrElement) = report(message, level, location.messageLocation())

    fun reportError(message: String, location: CompilerMessageSourceLocation?) = report(message, CompilerMessageSeverity.ERROR, location)
    fun reportErrorAt(message: String, location: IrElement) = reportError(message, location.messageLocation())
}