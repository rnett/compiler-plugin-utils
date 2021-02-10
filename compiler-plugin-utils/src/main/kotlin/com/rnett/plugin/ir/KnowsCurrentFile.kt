package com.rnett.plugin.ir

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocationWithRange
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path

/**
 * Utility methods for working with a [MessageCollector] when the current file is available
 */
public interface KnowsCurrentFile {
    /**
     * Gets the current file
     */
    public val file: IrFile

    /**
     * Get the message location for an element
     */
    public fun IrElement.messageLocation(): CompilerMessageSourceLocation? {
        val range = file.fileEntry.getSourceRangeInfo(this.startOffset, this.endOffset)
        return CompilerMessageLocationWithRange.create(
            file.path,
            range.startLineNumber,
            range.startColumnNumber,
            range.endLineNumber,
            range.endColumnNumber,
            null
        )
    }

    /**
     * Report a message
     */
    public fun MessageCollector.report(
        message: String,
        level: CompilerMessageSeverity,
        location: CompilerMessageSourceLocation?
    ): Unit =
        report(level, message, location)

    /**
     * Report a message at the given element
     */
    public fun MessageCollector.reportAt(message: String, level: CompilerMessageSeverity, location: IrElement): Unit =
        report(message, level, location.messageLocation())

    public fun MessageCollector.reportError(message: String, location: CompilerMessageSourceLocation?): Unit =
        report(message, CompilerMessageSeverity.ERROR, location)

    public fun MessageCollector.reportError(message: String, location: IrElement): Unit =
        reportError(message, location.messageLocation())

    public fun MessageCollector.reportWarning(message: String, location: CompilerMessageSourceLocation?): Unit =
        report(message, CompilerMessageSeverity.WARNING, location)

    public fun MessageCollector.reportWarning(message: String, location: IrElement): Unit =
        reportWarning(message, location.messageLocation())

    public fun MessageCollector.reportInfo(message: String, location: CompilerMessageSourceLocation?): Unit =
        report(message, CompilerMessageSeverity.INFO, location)

    public fun MessageCollector.reportInfo(message: String, location: IrElement): Unit =
        reportInfo(message, location.messageLocation())
}