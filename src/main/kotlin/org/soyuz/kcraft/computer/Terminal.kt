package org.soyuz.kcraft.computer


class Terminal : ComputerMode {

    companion object {
        /*
         * Commands may be longer than one visual row.
         */
        const val MAX_INPUT_LENGTH =
            128

        /*
         * Number of logical history entries retained.
         *
         * A logical entry may occupy several visual rows after wrapping.
         */
        const val MAX_LINES =
            64

        const val PROMPT =
            "> "
    }


    data class CursorPosition(
        val row: Int,
        val column: Int
    )


    // -------------------------------------------------------------------------
    // Logical terminal state
    // -------------------------------------------------------------------------

    /*
     * Fixed-size circular buffer of LOGICAL history lines.
     *
     * Wrapping is deliberately not stored here.
     *
     * For example:
     *
     *   "deploy: failed to deploy Ruby Golem"
     *
     * remains one history entry even if the display later wraps it.
     */
    private val _lines =
        Array(MAX_LINES) {
            ""
        }

    private var _lineCount =
        0

    private var _startIndex =
        0


    /*
     * Editable command input is separate from committed history.
     */
    private var _input =
        ""

    /*
     * Cursor position within the raw input string.
     *
     * This does NOT include the "> " prompt.
     */
    private var _inputCursor =
        0


    /*
     * Offset into VISUAL rows, not logical history lines.
     *
     * Once wrapping exists, those are no longer the same thing.
     */
    private var _scrollOffset =
        0


    // -------------------------------------------------------------------------
    // Public state
    // -------------------------------------------------------------------------

    val lineCount: Int
        get() =
            _lineCount

    val lines: List<String>
        get() =
            List(_lineCount) {
                getLineAt(it)
            }

    val input: String
        get() =
            _input

    val inputCursor: Int
        get() =
            _inputCursor


    // -------------------------------------------------------------------------
    // Output wrapping
    // -------------------------------------------------------------------------

    /**
     * Wrap ordinary terminal output.
     *
     * Prefer breaking at whitespace so prose and diagnostics remain readable.
     * If no suitable whitespace exists, hard-wrap at DISPLAY_WIDTH.
     */
    private fun wrapOutputLine(
        line: String
    ): List<String> {

        if (line.isEmpty()) {
            return listOf("")
        }

        val result =
            mutableListOf<String>()

        var remaining =
            line

        while (
            remaining.length >
            ComputerMode.DISPLAY_WIDTH
        ) {

            val candidate =
                remaining.take(
                    ComputerMode.DISPLAY_WIDTH
                )

            /*
             * Prefer the final space within the available width.
             *
             * Do not split at character 0, because that could make no
             * progress for lines beginning with whitespace.
             */
            val splitAt =
                candidate
                    .lastIndexOf(' ')
                    .takeIf {
                        it > 0
                    }
                    ?: ComputerMode.DISPLAY_WIDTH

            result +=
                remaining.take(
                    splitAt
                )

            remaining =
                remaining
                    .drop(splitAt)
                    .trimStart()
        }

        result +=
            remaining

        return result
    }


    // -------------------------------------------------------------------------
    // Input wrapping
    // -------------------------------------------------------------------------

    /**
     * The input is hard-wrapped rather than word-wrapped.
     *
     * This is intentional.
     *
     * If input were word-wrapped, cursor position would depend on every
     * previous whitespace break. Hard wrapping means:
     *
     *   visualIndex = PROMPT.length + inputCursor
     *   row         = visualIndex / DISPLAY_WIDTH
     *   column      = visualIndex % DISPLAY_WIDTH
     *
     * which makes editing predictable.
     */
    private val inputDisplayLines: List<String>
        get() {
            val text =
                "$PROMPT$_input"

            val rows =
                text.chunked(
                    ComputerMode.DISPLAY_WIDTH
                )
                    .toMutableList()

            /*
             * If the cursor lands exactly after the final character of a
             * completely full row, it belongs at column 0 of the NEXT row.
             *
             * Example with width 5:
             *
             *   abcde
             *   _
             *
             * chunked() would otherwise only produce ["abcde"], leaving no
             * visual row for the cursor.
             */
            if (
                text.length %
                ComputerMode.DISPLAY_WIDTH ==
                0
            ) {
                rows += ""
            }

            return rows
        }


    // -------------------------------------------------------------------------
    // Complete visual display
    // -------------------------------------------------------------------------

    /**
     * Convert logical terminal state into actual rows displayed by the GUI.
     *
     * History:
     *     word-wrapped
     *
     * Current command:
     *     hard-wrapped
     */
    private val displayLines: List<String>
        get() =
            buildList {

                for (line in lines) {
                    addAll(
                        wrapOutputLine(
                            line
                        )
                    )
                }

                addAll(
                    inputDisplayLines
                )
            }


    /**
     * The twelve rows currently visible in the terminal viewport.
     */
    val visibleLines: Array<String>
        get() {
            val display =
                displayLines

            return Array(
                ComputerMode.VISIBLE_LINES
            ) { index ->

                val displayIndex =
                    _scrollOffset +
                            index

                display.getOrElse(
                    displayIndex
                ) {
                    ""
                }
            }
        }


    // -------------------------------------------------------------------------
    // Cursor
    // -------------------------------------------------------------------------

    /**
     * Cursor position relative to the currently visible viewport.
     */
    val visibleCursorPosition:
            CursorPosition?
        get() {

            /*
             * Count how many visual rows the history occupies.
             */
            val historyVisualRows =
                lines.sumOf { line ->
                    wrapOutputLine(
                        line
                    ).size
                }


            /*
             * Cursor index within:
             *
             *     "> " + input
             */
            val visualInputIndex =
                PROMPT.length +
                        _inputCursor

            val inputRowOffset =
                visualInputIndex /
                        ComputerMode.DISPLAY_WIDTH

            val inputColumn =
                visualInputIndex %
                        ComputerMode.DISPLAY_WIDTH


            /*
             * Absolute visual row of the cursor.
             */
            val cursorRow =
                historyVisualRows +
                        inputRowOffset


            /*
             * Cursor may be outside the currently visible viewport if the
             * user has manually scrolled upwards.
             */
            if (
                cursorRow <
                _scrollOffset ||
                cursorRow >=
                _scrollOffset +
                ComputerMode.VISIBLE_LINES
            ) {
                return null
            }


            return CursorPosition(
                row =
                    cursorRow -
                            _scrollOffset,

                column =
                    inputColumn
            )
        }


    // -------------------------------------------------------------------------
    // Input editing
    // -------------------------------------------------------------------------

    fun appendChar(
        char: Char
    ) {

        if (
            _input.length >=
            MAX_INPUT_LENGTH
        ) {
            return
        }

        _input =
            _input.substring(
                0,
                _inputCursor
            ) +
                    char +
                    _input.substring(
                        _inputCursor
                    )

        _inputCursor++

        scrollToBottom()
    }


    fun popChar(): Char? {

        if (
            _inputCursor <= 0 ||
            _input.isEmpty()
        ) {
            return null
        }

        val removed =
            _input[
                _inputCursor - 1
            ]

        _input =
            _input.removeRange(
                _inputCursor - 1,
                _inputCursor
            )

        _inputCursor--

        scrollToBottom()

        return removed
    }


    fun clearInput() {

        _input =
            ""

        _inputCursor =
            0

        scrollToBottom()
    }


    fun setInput(
        text: String
    ) {

        _input =
            text.take(
                MAX_INPUT_LENGTH
            )

        _inputCursor =
            _input.length

        scrollToBottom()
    }


    fun moveCursorLeft(
        amount: Int = 1
    ) {

        require(
            amount >= 0
        ) {
            "Cursor movement amount cannot be negative"
        }

        _inputCursor =
            (
                    _inputCursor -
                            amount
                    )
                .coerceAtLeast(
                    0
                )

        /*
         * For now, editing always returns the viewport to the active command.
         *
         * Later we could instead scroll only enough to reveal the cursor.
         */
        scrollToBottom()
    }


    fun moveCursorRight(
        amount: Int = 1
    ) {

        require(
            amount >= 0
        ) {
            "Cursor movement amount cannot be negative"
        }

        _inputCursor =
            (
                    _inputCursor +
                            amount
                    )
                .coerceAtMost(
                    _input.length
                )

        scrollToBottom()
    }


    // -------------------------------------------------------------------------
    // History
    // -------------------------------------------------------------------------

    /**
     * Append one LOGICAL line to terminal history.
     *
     * The line is not pre-wrapped here.
     * Wrapping belongs to display generation.
     */
    fun appendLine(
        text: String = ""
    ) {

        appendRawLine(
            text
        )

        scrollToBottom()
    }


    /**
     * Commit the current command into terminal history.
     *
     * Returns the raw command text without the prompt.
     */
    fun commitInput(): String {

        val command =
            _input

        appendLine(
            "$PROMPT$command"
        )

        _input =
            ""

        _inputCursor =
            0

        scrollToBottom()

        return command
    }


    fun popLine(): String {

        if (
            _lineCount == 0
        ) {
            return ""
        }

        val lastLogicalIndex =
            _lineCount - 1

        val physicalIndex =
            physicalIndex(
                lastLogicalIndex
            )

        val removed =
            _lines[
                physicalIndex
            ]

        _lines[
            physicalIndex
        ] =
            ""

        _lineCount--

        clampScrollOffset()

        return removed
    }


    private fun appendRawLine(
        line: String
    ) {

        /*
         * If the history buffer is full, forget the oldest logical line.
         */
        if (
            _lineCount ==
            MAX_LINES
        ) {

            _lines[
                _startIndex
            ] =
                ""

            _startIndex =
                (
                        _startIndex +
                                1
                        ) %
                        MAX_LINES

            _lineCount--
        }


        val index =
            physicalIndex(
                _lineCount
            )

        _lines[
            index
        ] =
            line

        _lineCount++
    }


    private fun getLineAt(
        row: Int
    ): String {

        require(
            row in
                    0 until
                    _lineCount
        ) {
            "Row $row is outside terminal contents"
        }

        return _lines[
            physicalIndex(
                row
            )
        ]
    }


    private fun physicalIndex(
        logicalIndex: Int
    ): Int =
        (
                _startIndex +
                        logicalIndex
                ) %
                MAX_LINES


    // -------------------------------------------------------------------------
    // Scrolling
    // -------------------------------------------------------------------------

    fun scrollUp(
        amount: Int = 1
    ) {

        require(
            amount >= 0
        ) {
            "Scroll amount cannot be negative"
        }

        _scrollOffset =
            (
                    _scrollOffset -
                            amount
                    )
                .coerceAtLeast(
                    0
                )
    }


    fun scrollDown(
        amount: Int = 1
    ) {

        require(
            amount >= 0
        ) {
            "Scroll amount cannot be negative"
        }

        _scrollOffset =
            (
                    _scrollOffset +
                            amount
                    )
                .coerceAtMost(
                    maxScrollOffset()
                )
    }


    fun scrollToBottom() {

        _scrollOffset =
            maxScrollOffset()
    }


    private fun maxScrollOffset(): Int {

        /*
         * IMPORTANT:
         *
         * This must use VISUAL rows.
         *
         * A single logical history entry may now occupy multiple display rows.
         */
        return (
                displayLines.size -
                        ComputerMode.VISIBLE_LINES
                )
            .coerceAtLeast(
                0
            )
    }


    private fun clampScrollOffset() {

        _scrollOffset =
            _scrollOffset.coerceIn(
                0,
                maxScrollOffset()
            )
    }


    // -------------------------------------------------------------------------
    // Clearing
    // -------------------------------------------------------------------------

    fun clear() {

        _lines.fill(
            ""
        )

        _lineCount =
            0

        _startIndex =
            0

        _input =
            ""

        _inputCursor =
            0

        _scrollOffset =
            0
    }


    // -------------------------------------------------------------------------
    // ComputerMode
    // -------------------------------------------------------------------------

    override fun handleInput(
        input: ComputerInput,
        runtime: ComputerRuntime
    ) {

        when (input) {

            is ComputerInput.Character -> {

                Character
                    .toChars(
                        input.codepoint
                    )
                    .concatToString()
                    .forEach(
                        ::appendChar
                    )
            }


            ComputerInput.Backspace ->
                popChar()


            ComputerInput.Enter ->
                runtime
                    .submitCurrentCommand()


            ComputerInput.Up ->
                scrollUp()


            ComputerInput.Down ->
                scrollDown()


            ComputerInput.Left ->
                moveCursorLeft()


            ComputerInput.Right ->
                moveCursorRight()


            else ->
                Unit
        }
    }


    override fun displayState():
            ComputerDisplayState {

        val cursor =
            visibleCursorPosition

        return ComputerDisplayState(
            lines =
                visibleLines.toList(),

            cursorRow =
                cursor?.row,

            cursorColumn =
                cursor?.column
        )
    }
}