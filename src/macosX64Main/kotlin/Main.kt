import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.CoreGraphics.*
import kotlin.math.sign

fun cgEventCallback(
    proxy: CGEventTapProxy?, type: CGEventType, event: CGEventRef?, refcon: COpaquePointer?
): CGEventRef? {
    if (CGEventGetIntegerValueField(event, kCGScrollWheelEventIsContinuous) != 1.toLong()) {
        val delta = CGEventGetIntegerValueField(event, kCGScrollWheelEventPointDeltaAxis1)

        CGEventSetIntegerValueField(event, kCGScrollWheelEventDeltaAxis1, delta.sign * LINES)
    }
    return event
}

fun main() {
    val eventOfInterest = (1uL shl kCGEventScrollWheel.toInt())
    val eventTap = CGEventTapCreate(
        kCGSessionEventTap, kCGHeadInsertEventTap, 0, eventOfInterest, staticCFunction(::cgEventCallback), null
    )
    val runLoopSource = CFMachPortCreateRunLoopSource(kCFAllocatorDefault, eventTap, 0)

    CFRunLoopAddSource(CFRunLoopGetCurrent(), runLoopSource, kCFRunLoopCommonModes)
    CGEventTapEnable(eventTap, true)
    CFRunLoopRun()

    CFRelease(eventTap)
    CFRelease(runLoopSource)
}

private const val LINES = 3L