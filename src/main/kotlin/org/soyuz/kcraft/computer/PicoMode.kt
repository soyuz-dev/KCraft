package org.soyuz.kcraft.computer

class PicoMode(
    val path: String,
    contents: String
) : ComputerMode {

    companion object {
        const val VISIBLE_LINES = 12
        const val MAX_LINE_LENGTH = 128
    }

    /*
     * Pico always contains at least one editable line.
     *
     * Keeping the document as lines instead of one giant String makes
     * rendering and 2D cursor movement much simpler.
     */
    private val lines: MutableList<String> =
        contents
            .split('\n')
            .toMutableList()
            .ifEmpty {
                mutableListOf("")
            }

    private var cursorRow = 0
    private var cursorColumn = 0

    /*
     * Index of the first document line currently visible.
     */
    private var scrollOffset = 0

    override fun handleInput(
        input: ComputerInput,
        runtime: ComputerRuntime
    ) {
        when (input) {
            is ComputerInput.Character -> {
                Character.toChars(input.codepoint)
                    .concatToString()
                    .forEach(::insertCharacter)
            }

            ComputerInput.Backspace ->
                backspace()

            ComputerInput.Enter ->
                insertNewLine()

            ComputerInput.Left ->
                moveLeft()

            ComputerInput.Right ->
                moveRight()

            ComputerInput.Up ->
                moveUp()

            ComputerInput.Down ->
                moveDown()

            ComputerInput.Save ->
                save(runtime)

            ComputerInput.Exit ->
                runtime.returnToTerminal()
        }
    }

    override fun displayState(): ComputerDisplayState =
        ComputerDisplayState(
            lines = List(VISIBLE_LINES) { index ->
                lines.getOrElse(
                    scrollOffset + index
                ) {
                    ""
                }
            },
            cursorRow = cursorRow - scrollOffset,
            cursorColumn = cursorColumn
        )

    private fun insertCharacter(char: Char) {
        val line = lines[cursorRow]

        if (line.length >= MAX_LINE_LENGTH) {
            return
        }

        lines[cursorRow] =
            line.substring(0, cursorColumn) +
                    char +
                    line.substring(cursorColumn)

        cursorColumn++

        ensureCursorVisible()
    }

    private fun insertNewLine() {
        val line = lines[cursorRow]

        val beforeCursor =
            line.substring(0, cursorColumn)

        val afterCursor =
            line.substring(cursorColumn)

        lines[cursorRow] = beforeCursor

        lines.add(
            cursorRow + 1,
            afterCursor
        )

        cursorRow++
        cursorColumn = 0

        ensureCursorVisible()
    }

    private fun backspace() {
        /*
         * Normal backspace:
         *
         * hel|lo
         *   ↓
         * he|lo
         */
        if (cursorColumn > 0) {
            val line = lines[cursorRow]

            lines[cursorRow] =
                line.removeRange(
                    cursorColumn - 1,
                    cursorColumn
                )

            cursorColumn--

            ensureCursorVisible()
            return
        }

        /*
         * At the beginning of a line, backspace joins it
         * onto the previous line:
         *
         * hello
         * |world
         *
         * becomes
         *
         * hello|world
         */
        if (cursorRow > 0) {
            val previousLine =
                lines[cursorRow - 1]

            val currentLine =
                lines[cursorRow]

            /*
             * Don't join them if doing so would violate the
             * line-length invariant.
             */
            if (
                previousLine.length +
                currentLine.length >
                MAX_LINE_LENGTH
            ) {
                return
            }

            val newCursorColumn =
                previousLine.length

            lines[cursorRow - 1] =
                previousLine + currentLine

            lines.removeAt(cursorRow)

            cursorRow--
            cursorColumn = newCursorColumn

            ensureCursorVisible()
        }
    }

    private fun moveLeft() {
        if (cursorColumn > 0) {
            cursorColumn--
        } else if (cursorRow > 0) {
            cursorRow--
            cursorColumn =
                lines[cursorRow].length
        }

        ensureCursorVisible()
    }

    private fun moveRight() {
        val line = lines[cursorRow]

        if (cursorColumn < line.length) {
            cursorColumn++
        } else if (cursorRow < lines.lastIndex) {
            cursorRow++
            cursorColumn = 0
        }

        ensureCursorVisible()
    }

    private fun moveUp() {
        if (cursorRow <= 0) {
            return
        }

        cursorRow--

        cursorColumn =
            cursorColumn.coerceAtMost(
                lines[cursorRow].length
            )

        ensureCursorVisible()
    }

    private fun moveDown() {
        if (cursorRow >= lines.lastIndex) {
            return
        }

        cursorRow++

        cursorColumn =
            cursorColumn.coerceAtMost(
                lines[cursorRow].length
            )

        ensureCursorVisible()
    }

    private fun save(runtime: ComputerRuntime) {
        runtime.fileSystem.writeFile(
            path,
            contents()
        )
    }

    private fun contents(): String =
        lines.joinToString("\n")

    private fun ensureCursorVisible() {
        if (cursorRow < scrollOffset) {
            scrollOffset =
                cursorRow
        } else if (
            cursorRow >=
            scrollOffset + VISIBLE_LINES
        ) {
            scrollOffset =
                cursorRow - VISIBLE_LINES + 1
        }
    }
}