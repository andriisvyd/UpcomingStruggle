package com.svyd.upcomingweather.core.designsystem.primitive

/**
 * The mark a screen shows when it has nothing else to show.
 *
 * Not copy: these are the same kind of thing as the `.` and `=` of the range bars — design marks
 * that carry meaning by shape, never translated and never read aloud. Every string the user
 * actually reads lives in `strings.xml`.
 */
enum class NoirStateMark(val mark: String) {
    Empty("(?)"),
    Error("(x)"),
}
