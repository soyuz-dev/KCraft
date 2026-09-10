package org.soyuz.kcraft.computer

class Terminal {

    companion object {
        const val VISIBLE_LINES = 12
        const val MAX_LINE_LENGTH = 128
        const val MAX_LINES = 64

        const val PROMPT = "> "
    }

    data class CursorPosition(
        val row: Int,
        val column: Int
    )

    // Fixed-size circular buffer for committed terminal history.
    private val _lines = Array(MAX_LINES) { "" }

    private var _lineCount = 0
    private var _startIndex = 0

    // Editable input is separate from history.
    private var _input = ""

    // Cursor within the input string.
    private var _inputCursor = 0

    // 0 = oldest possible viewport position.
    // maxScrollOffset() = newest possible viewport position.
    private var _scrollOffset = 0

    val lineCount: Int
        get() = _lineCount

    val lines: List<String>
        get() = List(_lineCount) { getLineAt(it) }

    val input: String
        get() = _input

    val inputCursor: Int
        get() = _inputCursor

    /**
     * Full display contents:
     *
     * history...
     * > current input
     *
     * The prompt/input line is derived state and is NOT part of history.
     */
    private val displayLines: List<String>
        get() = lines + "$PROMPT$_input"

    val visibleLines: Array<String>
        get() = Array(VISIBLE_LINES) { index ->
            val displayIndex = _scrollOffset + index
            displayLines.getOrElse(displayIndex) { "" }
        }

    /**
     * Cursor position relative to the currently visible viewport.
     *
     * The cursor always lives on the prompt/input line.
     */
    val visibleCursorPosition: CursorPosition?
        get() {
            val inputRow = displayLines.lastIndex

            if (
                inputRow < _scrollOffset ||
                inputRow >= _scrollOffset + VISIBLE_LINES
            ) {
                return null
            }

            return CursorPosition(
                row = inputRow - _scrollOffset,
                column = PROMPT.length + _inputCursor
            )
        }

    fun appendChar(char: Char) {
        if (_input.length >= MAX_LINE_LENGTH - PROMPT.length) {
            return
        }

        _input =
            _input.substring(0, _inputCursor) +
                    char +
                    _input.substring(_inputCursor)

        _inputCursor++

        scrollToBottom()
    }

    fun popChar(): Char? {
        if (_inputCursor <= 0 || _input.isEmpty()) {
            return null
        }

        val removed = _input[_inputCursor - 1]

        _input =
            _input.removeRange(
                _inputCursor - 1,
                _inputCursor
            )

        _inputCursor--

        scrollToBottom()

        return removed
    }

    fun appendLine(text: String = "") {
        if (text.isEmpty()) {
            appendRawLine("")
        } else {
            text.chunked(MAX_LINE_LENGTH)
                .forEach(::appendRawLine)
        }

        scrollToBottom()
    }

    /**
     * Commits the current input into history.
     *
     * Returns the raw command text without the prompt.
     */
    fun commitInput(): String {
        val command = _input

        appendLine("$PROMPT$command")

        _input = ""
        _inputCursor = 0

        scrollToBottom()

        return command
    }

    fun clearInput() {
        _input = ""
        _inputCursor = 0
        scrollToBottom()
    }

    fun setInput(text: String) {
        _input = text.take(MAX_LINE_LENGTH - PROMPT.length)
        _inputCursor = _input.length
        scrollToBottom()
    }

    fun moveCursorLeft(amount: Int = 1) {
        require(amount >= 0) {
            "Cursor movement amount cannot be negative"
        }

        _inputCursor =
            (_inputCursor - amount)
                .coerceAtLeast(0)
    }

    fun moveCursorRight(amount: Int = 1) {
        require(amount >= 0) {
            "Cursor movement amount cannot be negative"
        }

        _inputCursor =
            (_inputCursor + amount)
                .coerceAtMost(_input.length)
    }

    fun popLine(): String {
        if (_lineCount == 0) {
            return ""
        }

        val lastLogicalIndex = _lineCount - 1
        val physicalIndex =
            physicalIndex(lastLogicalIndex)

        val removed = _lines[physicalIndex]

        _lines[physicalIndex] = ""
        _lineCount--

        clampScrollOffset()

        return removed
    }

    fun scrollUp(amount: Int = 1) {
        require(amount >= 0) {
            "Scroll amount cannot be negative"
        }

        _scrollOffset =
            (_scrollOffset - amount)
                .coerceAtLeast(0)
    }

    fun scrollDown(amount: Int = 1) {
        require(amount >= 0) {
            "Scroll amount cannot be negative"
        }

        _scrollOffset =
            (_scrollOffset + amount)
                .coerceAtMost(maxScrollOffset())
    }

    fun scrollToBottom() {
        _scrollOffset = maxScrollOffset()
    }

    fun clear() {
        _lines.fill("")

        _lineCount = 0
        _startIndex = 0

        _input = ""
        _inputCursor = 0

        _scrollOffset = 0
    }

    private fun appendRawLine(line: String) {
        require(line.length <= MAX_LINE_LENGTH) {
            "Line exceeds maximum length of $MAX_LINE_LENGTH"
        }

        if (_lineCount == MAX_LINES) {
            _lines[_startIndex] = ""

            _startIndex =
                (_startIndex + 1) % MAX_LINES

            _lineCount--
        }

        val index =
            physicalIndex(_lineCount)

        _lines[index] = line
        _lineCount++
    }

    private fun getLineAt(row: Int): String {
        require(row in 0 until _lineCount) {
            "Row $row is outside terminal contents"
        }

        return _lines[physicalIndex(row)]
    }

    private fun physicalIndex(
        logicalIndex: Int
    ): Int =
        (_startIndex + logicalIndex) % MAX_LINES

    private fun maxScrollOffset(): Int {
        val displayLineCount =
            _lineCount + 1 // +1 for prompt/input line

        return (
                displayLineCount - VISIBLE_LINES
                ).coerceAtLeast(0)
    }

    private fun clampScrollOffset() {
        _scrollOffset =
            _scrollOffset.coerceIn(
                0,
                maxScrollOffset()
            )
    }
}