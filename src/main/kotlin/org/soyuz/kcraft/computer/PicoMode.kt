package org.soyuz.kcraft.computer

class PicoMode(
    val path: String,
    contents: String
) : ComputerMode {

    companion object {

        /*
         * Maximum length of one REAL line in the file.
         *
         * This is deliberately different from
         * ComputerMode.DISPLAY_WIDTH.
         *
         * A 100-character Kotlin line remains one line in the
         * saved file, even though Pico displays it across
         * several visual rows.
         */
        const val MAX_LINE_LENGTH = 128
    }


    // -------------------------------------------------------------------------
    // Document
    // -------------------------------------------------------------------------

    /*
     * The actual contents of the file.
     *
     * Each entry corresponds to a real line separated by '\n'.
     * Visual wrapping NEVER changes this list.
     */
    private val lines: MutableList<String> =
        contents
            .split('\n')
            .toMutableList()
            .ifEmpty {
                mutableListOf("")
            }


    // -------------------------------------------------------------------------
    // Logical cursor
    // -------------------------------------------------------------------------

    /*
     * Cursor position in the actual document.
     *
     * cursorRow:
     *     index of the real file line
     *
     * cursorColumn:
     *     character offset within that line
     */
    private var cursorRow = 0

    private var cursorColumn = 0


    // -------------------------------------------------------------------------
    // Scrolling
    // -------------------------------------------------------------------------

    /*
     * Unlike the old Pico implementation, scrolling is measured
     * in VISUAL rows.
     *
     * A single long source line may therefore occupy multiple
     * rows in the viewport.
     */
    private var scrollOffset = 0


    // -------------------------------------------------------------------------
    // Visual model
    // -------------------------------------------------------------------------

    /*
     * One row as it appears on the KCraft display.
     *
     * logicalRow tells us which actual file line this belongs to.
     *
     * startColumn tells us where this visual chunk begins within
     * that logical line.
     */
    private data class VisualLine(
        val logicalRow: Int,
        val startColumn: Int,
        val text: String
    )


    /*
     * Cursor position in the visually wrapped document.
     */
    private data class VisualCursor(
        val row: Int,
        val column: Int
    )


    /**
     * Convert the real document into display rows.
     *
     * Pico deliberately uses HARD wrapping rather than word wrapping.
     *
     * Source code is not prose, and hard wrapping means character
     * positions map predictably onto visual rows.
     *
     * Importantly, this is display-only. No '\n' is inserted into
     * the actual document.
     */
    private fun visualLines(): List<VisualLine> =
        buildList {

            lines.forEachIndexed {
                    logicalRow,
                    line ->

                /*
                 * Even an empty logical line occupies one row
                 * on the display.
                 */
                if (line.isEmpty()) {
                    add(
                        VisualLine(
                            logicalRow =
                                logicalRow,

                            startColumn =
                                0,

                            text =
                                ""
                        )
                    )

                    return@forEachIndexed
                }


                var startColumn =
                    0

                while (
                    startColumn <
                    line.length
                ) {
                    val endColumn =
                        minOf(
                            startColumn +
                                    ComputerMode.DISPLAY_WIDTH,

                            line.length
                        )

                    add(
                        VisualLine(
                            logicalRow =
                                logicalRow,

                            startColumn =
                                startColumn,

                            text =
                                line.substring(
                                    startColumn,
                                    endColumn
                                )
                        )
                    )

                    startColumn +=
                        ComputerMode.DISPLAY_WIDTH
                }
            }
        }


    /**
     * Translate the logical document cursor into a visual
     * row/column after wrapping.
     */
    private fun visualCursor(): VisualCursor {
        val line =
            lines[cursorRow]

        /*
         * Normally:
         *
         *     column 0..39  -> visual segment 0
         *     column 40..79 -> visual segment 1
         *     ...
         *
         * There is one edge case:
         *
         * if the cursor is at the END of a line whose length is
         * exactly divisible by DISPLAY_WIDTH, there is no following
         * visual row.
         *
         * For example:
         *
         *     [exactly 40 characters]|
         *
         * should place the cursor at column 40 of that row,
         * not at column 0 of a nonexistent second row.
         */
        val segment =
            if (
                cursorColumn > 0 &&
                cursorColumn ==
                line.length &&
                cursorColumn %
                ComputerMode.DISPLAY_WIDTH ==
                0
            ) {
                cursorColumn /
                        ComputerMode.DISPLAY_WIDTH -
                        1
            } else {
                cursorColumn /
                        ComputerMode.DISPLAY_WIDTH
            }


        /*
         * Find the first visual row belonging to this logical line.
         */
        val allVisualLines =
            visualLines()

        val firstVisualRow =
            allVisualLines.indexOfFirst {
                it.logicalRow ==
                        cursorRow
            }


        /*
         * Every logical line always has at least one visual row,
         * including empty lines.
         */
        check(
            firstVisualRow >= 0
        ) {
            "Could not find visual row for logical row $cursorRow"
        }


        val visualRow =
            firstVisualRow +
                    segment


        val visualColumn =
            cursorColumn -
                    segment *
                    ComputerMode.DISPLAY_WIDTH


        return VisualCursor(
            row =
                visualRow,

            column =
                visualColumn
        )
    }


    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    override fun handleInput(
        input: ComputerInput,
        runtime: ComputerRuntime
    ) {
        when (input) {

            is ComputerInput.Character -> {
                Character.toChars(
                    input.codepoint
                )
                    .concatToString()
                    .forEach(
                        ::insertCharacter
                    )
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


    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    override fun displayState():
            ComputerDisplayState {

        val visualLines =
            visualLines()

        val cursor =
            visualCursor()


        return ComputerDisplayState(

            /*
             * Only send the visible portion of the visually
             * wrapped document to the client.
             */
            lines =
                List(
                    ComputerMode.VISIBLE_LINES
                ) { index ->

                    visualLines
                        .getOrNull(
                            scrollOffset +
                                    index
                        )
                        ?.text
                        ?: ""
                },


            /*
             * ComputerDisplayState expects the cursor position
             * relative to the visible viewport.
             */
            cursorRow =
                cursor.row -
                        scrollOffset,

            cursorColumn =
                cursor.column
        )
    }


    // -------------------------------------------------------------------------
    // Character insertion
    // -------------------------------------------------------------------------

    private fun insertCharacter(
        char: Char
    ) {
        val line =
            lines[cursorRow]


        /*
         * MAX_LINE_LENGTH applies to the actual source line,
         * not one visual row.
         */
        if (
            line.length >=
            MAX_LINE_LENGTH
        ) {
            return
        }


        lines[cursorRow] =
            line.substring(
                0,
                cursorColumn
            ) +
                    char +
                    line.substring(
                        cursorColumn
                    )


        cursorColumn++


        ensureCursorVisible()
    }


    // -------------------------------------------------------------------------
    // Newlines
    // -------------------------------------------------------------------------

    private fun insertNewLine() {
        val line =
            lines[cursorRow]


        val beforeCursor =
            line.substring(
                0,
                cursorColumn
            )


        val afterCursor =
            line.substring(
                cursorColumn
            )


        /*
         * This is a REAL newline, unlike visual wrapping.
         */
        lines[cursorRow] =
            beforeCursor


        lines.add(
            cursorRow + 1,
            afterCursor
        )


        cursorRow++

        cursorColumn =
            0


        ensureCursorVisible()
    }


    // -------------------------------------------------------------------------
    // Backspace
    // -------------------------------------------------------------------------

    private fun backspace() {

        /*
         * Normal deletion:
         *
         *     hel|lo
         *
         * becomes:
         *
         *     he|lo
         */
        if (
            cursorColumn > 0
        ) {
            val line =
                lines[cursorRow]


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
         * At the beginning of a REAL file line, backspace joins
         * it onto the previous logical line.
         *
         * Visual wrapping boundaries do not count as newlines.
         */
        if (
            cursorRow > 0
        ) {
            val previousLine =
                lines[cursorRow - 1]


            val currentLine =
                lines[cursorRow]


            /*
             * Joining must still respect MAX_LINE_LENGTH.
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
                previousLine +
                        currentLine


            lines.removeAt(
                cursorRow
            )


            cursorRow--

            cursorColumn =
                newCursorColumn


            ensureCursorVisible()
        }
    }


    // -------------------------------------------------------------------------
    // Horizontal movement
    // -------------------------------------------------------------------------

    private fun moveLeft() {

        /*
         * Because cursorColumn refers to the logical line,
         * crossing a visual wrapping boundary requires no
         * special handling.
         */
        if (
            cursorColumn > 0
        ) {
            cursorColumn--
        } else if (
            cursorRow > 0
        ) {
            cursorRow--

            cursorColumn =
                lines[cursorRow]
                    .length
        }


        ensureCursorVisible()
    }


    private fun moveRight() {
        val line =
            lines[cursorRow]


        if (
            cursorColumn <
            line.length
        ) {
            cursorColumn++
        } else if (
            cursorRow <
            lines.lastIndex
        ) {
            cursorRow++

            cursorColumn =
                0
        }


        ensureCursorVisible()
    }


    // -------------------------------------------------------------------------
    // Vertical movement
    // -------------------------------------------------------------------------

    /*
     * For this first wrapped implementation, Up and Down retain
     * Pico's existing LOGICAL-line semantics.
     *
     * In other words:
     *
     *     long logical line
     *     visually wraps here
     *     and here
     *
     *     next logical line
     *
     * pressing Down moves to "next logical line", not to the next
     * wrapped visual row.
     *
     * This keeps editing behaviour simple while wrapping itself is
     * introduced.
     *
     * We can make vertical movement operate on visual rows later
     * without changing the file representation.
     */

    private fun moveUp() {
        if (
            cursorRow <= 0
        ) {
            return
        }


        cursorRow--


        cursorColumn =
            cursorColumn.coerceAtMost(
                lines[cursorRow]
                    .length
            )


        ensureCursorVisible()
    }


    private fun moveDown() {
        if (
            cursorRow >=
            lines.lastIndex
        ) {
            return
        }


        cursorRow++


        cursorColumn =
            cursorColumn.coerceAtMost(
                lines[cursorRow]
                    .length
            )


        ensureCursorVisible()
    }


    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    private fun save(
        runtime: ComputerRuntime
    ) {
        runtime.fileSystem.writeFile(
            path,
            contents()
        )
    }


    /**
     * Reconstruct the actual file.
     *
     * Only REAL logical lines produce '\n'.
     * Visual wrapping has no effect here.
     */
    private fun contents(): String =
        lines.joinToString(
            "\n"
        )


    // -------------------------------------------------------------------------
    // Scrolling
    // -------------------------------------------------------------------------

    /**
     * Keep the visually wrapped cursor inside the viewport.
     */
    private fun ensureCursorVisible() {
        val cursor =
            visualCursor()


        if (
            cursor.row <
            scrollOffset
        ) {
            /*
             * Cursor moved above the viewport.
             */
            scrollOffset =
                cursor.row
        } else if (
            cursor.row >=
            scrollOffset +
            ComputerMode.VISIBLE_LINES
        ) {
            /*
             * Cursor moved below the viewport.
             */
            scrollOffset =
                cursor.row -
                        ComputerMode.VISIBLE_LINES +
                        1
        }


        /*
         * Document edits can reduce the total number of visual
         * rows, so make sure the viewport hasn't been left beyond
         * the end of the document.
         */
        val maxScrollOffset =
            (
                    visualLines().size -
                            ComputerMode.VISIBLE_LINES
                    )
                .coerceAtLeast(
                    0
                )


        scrollOffset =
            scrollOffset.coerceIn(
                0,
                maxScrollOffset
            )
    }
}