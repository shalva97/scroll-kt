import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import platform.CoreFoundation.*
import platform.CoreGraphics.*
import kotlin.math.sign

fun cgEventCallback(
    proxy: CGEventTapProxy?, type: CGEventType, event: CGEventRef?, refcon: COpaquePointer?
): CGEventRef? {
    if (CGEventGetIntegerValueField(event, kCGScrollWheelEventIsContinuous) != 1.toLong()) {
        val deltaY = CGEventGetIntegerValueField(event, kCGScrollWheelEventPointDeltaAxis1)
        val deltaX = CGEventGetIntegerValueField(event, kCGScrollWheelEventPointDeltaAxis2)

        CGEventSetIntegerValueField(event, kCGScrollWheelEventDeltaAxis1, deltaY.sign * multiplier_y.toLong())
        CGEventSetIntegerValueField(event, kCGScrollWheelEventDeltaAxis2, deltaX.sign * multiplier_x.toLong())
    }
    return event
}

fun main(args: Array<String>) {
    val parser = ArgParser("Scroll-kt")
    val verticalMultiplier by parser.option(
        ArgType.Int, shortName = "e", description = "Increase vertical scroll multiplier. Default is $multiplier_y"
    ).default(multiplier_y)
    val horizontalMultiplier by parser.option(
        ArgType.Int, shortName = "o", description = "Increase horizontal scroll multiplier. Default is $multiplier_x"
    ).default(multiplier_x)
    parser.parse(args)

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

private var multiplier_y = 7
private var multiplier_x = 4
