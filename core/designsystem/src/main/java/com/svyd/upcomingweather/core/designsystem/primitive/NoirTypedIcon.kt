package com.svyd.upcomingweather.core.designsystem.primitive

/**
 * The mark a screen shows when it has nothing else to show.
 *
 * Not copy: these are the same kind of thing as the `.` and `=` of the range bars — design marks
 * that carry meaning by shape, never translated and never read aloud. Every string the user
 * actually reads lives in `strings.xml`.
 */
sealed class NoirTypedIcon(val mark: String) {
    data object Empty: NoirTypedIcon(mark = "(?)")
    data object Search: NoirTypedIcon(mark = "[?]")
    data object Error: NoirTypedIcon("(x)")
    data object CloudOff: NoirTypedIcon("{~/~}")
    data object Clear: NoirTypedIcon(" >< ")
    data object Clock: NoirTypedIcon(mark = "( <)")
    data object Gps: NoirTypedIcon(mark = "[x]")
    data object Back: NoirTypedIcon(mark = "[<]")
    class Condition(condition: NoirCondition): NoirTypedIcon(mark = condition.mark)
}
