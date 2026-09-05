package org.soyuz.kcraft.computer.terminal

class Terminal {

    companion object {
        const val VISIBLE_LINES = 8
        const val MAX_LINE_LENGTH = 64
        const val MAX_LINES = 32
    }

    data class CursorPosition(
        val row: Int,
        val column: Int
    )

    // Fixed-size circular buffer.
    // Every slot always contains a String.
    private val _lines = Array(MAX_LINES) { "" }

    // Number of currently occupied slots.
    private var _lineCount = 0

    // Physical index of the oldest logical line.
    private var _startIndex = 0

    // Cursor position within the logical terminal contents.
    private var _cursorRow = 0
    private var _cursorCol = 0

    // 0 = showing the oldest available page.
    // maxScroll = showing the newest available page.
    private var _scrollOffset = 0

    private fun isAtBottom(): Boolean =
        _scrollOffset >= maxScrollOffset()

    val lineCount: Int
        get() = _lineCount

    val lines: List<String>
        get() = List(_lineCount) { getLineAt(it) }

    val cursorPosition: CursorPosition
        get() = CursorPosition(_cursorRow, _cursorCol)

    val visibleLines: Array<String>
        get() = Array(VISIBLE_LINES) { index ->
            getLineAtOrEmpty(_scrollOffset + index)
        }

    fun appendChar(char: Char) {
        if (_lineCount == 0) {
            appendRawLine("")
        }

        var line = getLineAt(_cursorRow)

        if (line.length >= MAX_LINE_LENGTH) {
            appendRawLine("")
            _cursorRow = _lineCount - 1
            _cursorCol = 0
            line = ""
        }

        setLineAt(_cursorRow, line + char)
        _cursorCol++
        ensureCursorVisible()
    }


    fun popChar(): Char? {
        // If we are at the very beginning of the terminal, nothing to pop
        if (_lineCount == 0) {
            return null
        }

        var line = getLineAt(_cursorRow)

        // Backward wrapping logic: if current row is empty and there's a previous line, move up
        if (line.isEmpty() && _cursorRow > 0) {
            // Remove the empty line we are leaving behind
            popLine()
            // Update local variables to point to the preceding line
            _cursorRow = _lineCount - 1
            line = getLineAt(_cursorRow)
            _cursorCol = line.length
        }

        // Double-check if the target line is still empty after stepping up
        if (line.isEmpty()) {
            return null
        }

        val removed = line.last()

        setLineAt(
            _cursorRow,
            line.dropLast(1)
        )

        _cursorCol = line.length - 1
        ensureCursorVisible()

        return removed
    }

    fun appendLine(text: String = "") {
        if (text.isEmpty()) {
            appendRawLine("")
        } else {
            text.chunked(MAX_LINE_LENGTH)
                .forEach(::appendRawLine)
        }

        _cursorRow = _lineCount - 1
        _cursorCol = getLineAt(_cursorRow).length

        scrollToBottom()
    }

    fun popLine(): String {
        if (_lineCount == 0) {
            return ""
        }

        val lastIndex = _lineCount - 1
        val physicalIndex = physicalIndex(lastIndex)

        val removed = _lines[physicalIndex]

        _lines[physicalIndex] = ""
        _lineCount--

        _cursorRow = (_lineCount - 1).coerceAtLeast(0)
        _cursorCol = getLineAt(_cursorRow).length

        clampScrollOffset()

        return removed
    }

    fun scrollUp(amount: Int = 1) {
        require(amount >= 0) { "Scroll amount cannot be negative" }

        _scrollOffset = (
                _scrollOffset + amount
                ).coerceAtMost(maxScrollOffset())
    }

    fun scrollDown(amount: Int = 1) {
        require(amount >= 0) { "Scroll amount cannot be negative" }

        _scrollOffset = (
                _scrollOffset - amount
                ).coerceAtLeast(0)
    }

    fun clear() {
        _lines.fill("")

        _lineCount = 0
        _startIndex = 0

        _cursorRow = 0
        _cursorCol = 0

        _scrollOffset = 0
    }


    fun scrollToBottom() {
        _scrollOffset = maxScrollOffset()
    }

    private fun appendRawLine(line: String) {
        check(line.length <= MAX_LINE_LENGTH)

        if (_lineCount == MAX_LINES) {
            // Discard oldest line.
            _lines[_startIndex] = ""

            _startIndex = (_startIndex + 1) % MAX_LINES
            _lineCount--
        }

        val index = physicalIndex(_lineCount)

        _lines[index] = line
        _lineCount++
    }

    private fun getLineAt(row: Int): String {
        require(row in 0 until _lineCount) {
            "Row $row is outside terminal contents"
        }

        return _lines[physicalIndex(row)]
    }

    private fun getLineAtOrEmpty(row: Int): String {
        if (row !in 0 until _lineCount) {
            return ""
        }

        return getLineAt(row)
    }

    private fun setLineAt(row: Int, value: String) {
        require(row in 0 until _lineCount) {
            "Row $row is outside terminal contents"
        }

        require(value.length <= MAX_LINE_LENGTH) {
            "Line exceeds maximum length of $MAX_LINE_LENGTH"
        }

        _lines[physicalIndex(row)] = value
    }

    private fun physicalIndex(logicalIndex: Int): Int =
        (_startIndex + logicalIndex) % MAX_LINES

    private fun maxScrollOffset(): Int =
        (_lineCount - VISIBLE_LINES).coerceAtLeast(0)

    private fun clampScrollOffset() {
        _scrollOffset = _scrollOffset.coerceIn(
            0,
            maxScrollOffset()
        )
    }

    private fun ensureCursorVisible() {
        if (_cursorRow < _scrollOffset) {
            _scrollOffset = _cursorRow
        } else if (_cursorRow >= _scrollOffset + VISIBLE_LINES) {
            _scrollOffset = _cursorRow - VISIBLE_LINES + 1
        }

        clampScrollOffset()
    }
}