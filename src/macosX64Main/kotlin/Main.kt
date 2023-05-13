import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import platform.CoreFoundation.*
import platform.CoreGraphics.*
import kotlin.math.sign

fun cgEventCallback(
    proxy: CGEventTapProxy?, type: CGEventType, event: CGEventRef?, refcon: COpaquePointer?
): CGEventRef? {
    if (CGEventGetIntegerValueField(event, kCGScrollWheelEventIsContinuous) != 1.toLong()) {
        val deltaY = CGEventGetIntegerValueField(event, kCGScrollWheelEventPointDeltaAxis1)
        val deltaX = CGEventGetIntegerValueField(event, kCGScrollWheelEventPointDeltaAxis2)

        CGEventSetIntegerValueField(event, kCGScrollWheelEventDeltaAxis1, deltaY.sign * LINES_Y)
        CGEventSetIntegerValueField(event, kCGScrollWheelEventDeltaAxis2, deltaX.sign * LINES_X)
    }
    return event
}

fun main() {
    val eventTap = CGEventTapCreate(
        tap = kCGSessionEventTap,
        place = kCGHeadInsertEventTap,
        options = 0,
        eventsOfInterest = (1uL shl kCGEventScrollWheel.toInt()),
        callback = staticCFunction(::cgEventCallback),
        userInfo = null
    )
    val runLoopSource = CFMachPortCreateRunLoopSource(kCFAllocatorDefault, eventTap, 0)

    CFRunLoopAddSource(CFRunLoopGetCurrent(), runLoopSource, kCFRunLoopCommonModes)
    CGEventTapEnable(eventTap, true)
    CFRunLoopRun()

    CFRelease(eventTap)
    CFRelease(runLoopSource)
}

private const val LINES_Y = 3L
private const val LINES_X = 5L
