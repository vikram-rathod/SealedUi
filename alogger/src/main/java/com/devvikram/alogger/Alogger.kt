package com.devvikram.alogger

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Log levels for filtering output
 */
enum class LogLevel(val value: Int, val symbol: String) {
    VERBOSE(Log.VERBOSE, "V"),
    DEBUG(Log.DEBUG, "D"),
    INFO(Log.INFO, "I"),
    WARN(Log.WARN, "W"),
    ERROR(Log.ERROR, "E"),
    NONE(Int.MAX_VALUE, "X")
}

/**
 * Interface for custom log implementations
 */
interface LogAdapter {
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Default Android logcat adapter with colorized output
 */
class LogcatAdapter : LogAdapter {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, message, throwable)
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARN -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
            LogLevel.NONE -> {}
        }
    }
}

/**
 * File logging adapter with automatic rotation
 */
class FileLogAdapter(
    private val logDir: File,
    private val maxFileSizeBytes: Long = 5 * 1024 * 1024,
    private val maxFiles: Int = 5
) : LogAdapter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val lock = ReentrantReadWriteLock()

    init {
        logDir.mkdirs()
    }

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        lock.write {
            try {
                val logFile = File(logDir, "app_${getCurrentDate()}.log")
                if (logFile.length() > maxFileSizeBytes) rotateFiles()

                FileWriter(logFile, true).use { writer ->
                    val timestamp = dateFormat.format(System.currentTimeMillis())
                    val thread = Thread.currentThread().name
                    val logLine = "[$timestamp] [${level.symbol}] [$tag] [$thread] $message"
                    writer.append("$logLine\n")
                    throwable?.let { writer.append(it.stackTraceToString() + "\n") }
                }
            } catch (e: Exception) {
                Log.e("FileLogAdapter", "Failed to write log", e)
            }
        }
    }

    private fun getCurrentDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(System.currentTimeMillis())

    private fun rotateFiles() {
        logDir.listFiles { file ->
            file.name.startsWith("app_") && file.name.endsWith(".log")
        }?.sortedByDescending { it.lastModified() }?.drop(maxFiles - 1)?.forEach { it.delete() }
    }
}

/**
 * Custom formatter for log output
 */
interface LogFormatter {
    fun format(level: LogLevel, tag: String, message: String): String
}

/**
 * Pretty formatter with caller info and timestamps
 */
class PrettyFormatter : LogFormatter {
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    override fun format(level: LogLevel, tag: String, message: String): String {
        val time = timeFormat.format(System.currentTimeMillis())
        val caller = getCallerInfo()
        val thread = Thread.currentThread().name.take(15)
        return "┌─ [$time] [${level.symbol}] $tag\n" +
                "├─ $caller • $thread\n" +
                "└─ $message"
    }

    private fun getCallerInfo(): String {
        val element = Thread.currentThread().stackTrace.firstOrNull {
            !it.className.contains("com.devvikram.alogger") &&
                    !it.className.contains("java.lang.Thread")
        } ?: return "Unknown"

        val cls = element.className.substringAfterLast(".")
        return "$cls.${element.methodName}:${element.lineNumber}"
    }
}

/**
 * Compact formatter for simple output
 */
class CompactFormatter : LogFormatter {
    override fun format(level: LogLevel, tag: String, message: String): String {
        return "[${level.symbol}|$tag] $message"
    }
}

/**
 * Main logger with multiple adapter support and easy configuration
 */
class Alogger(
    val tag: String = "Alogger",
    val minLogLevel: LogLevel = LogLevel.DEBUG,
    val formatter: LogFormatter = PrettyFormatter()
) {
    private val adapters = mutableListOf<LogAdapter>()
    private val lock = ReentrantReadWriteLock()
    private val history = mutableListOf<String>()
    private val historyLimit = 1000

    init {
        adapters.add(LogcatAdapter())
    }

    // ============ Adapter Management ============

    fun addAdapter(adapter: LogAdapter) = lock.write { adapters.add(adapter) }
    fun removeAdapter(adapter: LogAdapter) = lock.write { adapters.remove(adapter) }
    fun clearAdapters() = lock.write { adapters.clear() }

    // ============ Logging Methods ============

    fun v(msg: () -> String) = logAt(LogLevel.VERBOSE, msg)
    fun d(msg: () -> String) = logAt(LogLevel.DEBUG, msg)
    fun i(msg: () -> String) = logAt(LogLevel.INFO, msg)
    fun w(msg: () -> String) = logAt(LogLevel.WARN, msg)
    fun e(msg: () -> String, err: Throwable? = null) = logAt(LogLevel.ERROR, msg, err)

    // Overloads for direct strings
    fun v(msg: String) = v { msg }
    fun d(msg: String) = d { msg }
    fun i(msg: String) = i { msg }
    fun w(msg: String) = w { msg }
    fun e(msg: String, err: Throwable? = null) = e({ msg }, err)

    // ============ History & Debug ============

    fun getHistory(): List<String> = lock.read { history.toList() }
    fun clearHistory() = lock.write { history.clear() }

    // ============ Internal ============

    private fun logAt(level: LogLevel, msgBlock: () -> String, err: Throwable? = null) {
        if (level.value < minLogLevel.value) return

        lock.read {
            val msg = msgBlock()
            val formatted = formatter.format(level, tag, msg)
            addToHistory(formatted)

            adapters.forEach { adapter ->
                try {
                    adapter.log(level, tag, formatted, err)
                } catch (e: Exception) {
                    Log.e("Alogger", "Adapter error", e)
                }
            }
        }
    }

    private fun addToHistory(msg: String) {
        lock.write {
            history.add(msg)
            if (history.size > historyLimit) history.removeAt(0)
        }
    }

    // ============ Singleton ============

    companion object {
        private var instance: Alogger? = null
        private val lock = ReentrantReadWriteLock()

        fun get(): Alogger = lock.read { instance } ?: lock.write {
            Alogger().also { instance = it }
        }

        fun init(
            tag: String = "Alogger",
            level: LogLevel = LogLevel.DEBUG,
            formatter: LogFormatter = PrettyFormatter()
        ): Alogger = lock.write {
            Alogger(tag, level, formatter).also { instance = it }
        }

        fun reset() = lock.write { instance = null }
    }
}

/**
 * Easy-to-use extension functions
 */

// For classes: MyClass.log { "message" }
inline fun <reified T> T.log(crossinline msg: () -> String) {
    val name = T::class.simpleName ?: "Unknown"
    Alogger.get().d { "[$name] ${msg()}" }
}

// For shorter syntax: logD { "message" }
fun logV(msg: () -> String) = Alogger.get().v(msg)
fun logD(msg: () -> String) = Alogger.get().d(msg)
fun logI(msg: () -> String) = Alogger.get().i(msg)
fun logW(msg: () -> String) = Alogger.get().w(msg)
fun logE(msg: () -> String, err: Throwable? = null) = Alogger.get().e(msg, err)

// Direct string versions
fun logD(msg: String) = Alogger.get().d(msg)
fun logI(msg: String) = Alogger.get().i(msg)
fun logW(msg: String) = Alogger.get().w(msg)
fun logE(msg: String, err: Throwable? = null) = Alogger.get().e(msg, err)