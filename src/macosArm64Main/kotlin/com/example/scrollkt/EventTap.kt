package com.example.scrollkt

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.CoreGraphics.*
import kotlin.math.sign

private const val MULTIPLIER_Y = 4L
private const val MULTIPLIER_X = 4L

@OptIn(ExperimentalForeignApi::class)
fun cgEventCallback(
    proxy: CGEventTapProxy?, type: CGEventType, event: CGEventRef?, refcon: COpaquePointer?
): CGEventRef? {
    if (CGEventGetIntegerValueField(event, kCGScrollWheelEventIsContinuous) == 0L) {
        val deltaY = CGEventGetIntegerValueField(event, kCGScrollWheelEventPointDeltaAxis1)
        val deltaX = CGEventGetIntegerValueField(event, kCGScrollWheelEventPointDeltaAxis2)

        CGEventSetIntegerValueField(event, kCGScrollWheelEventDeltaAxis1, -deltaY.sign * MULTIPLIER_Y)
        CGEventSetIntegerValueField(event, kCGScrollWheelEventDeltaAxis2, -deltaX.sign * MULTIPLIER_X)
    }
    return event
}

@OptIn(ExperimentalForeignApi::class)
fun setupEventTap(): Boolean {
    val eventTap = CGEventTapCreate(
        tap = kCGSessionEventTap,
        place = kCGHeadInsertEventTap,
        options = 0u,
        eventsOfInterest = (1uL shl kCGEventScrollWheel.toInt()),
        callback = staticCFunction(::cgEventCallback),
        userInfo = null
    )
    if (eventTap == null) {
        println("Failed to create event tap - check Accessibility permissions")
        return false
    }
    val runLoopSource = CFMachPortCreateRunLoopSource(kCFAllocatorDefault, eventTap, 0)
    CFRunLoopAddSource(CFRunLoopGetCurrent(), runLoopSource, kCFRunLoopCommonModes)
    CGEventTapEnable(eventTap, true)
    return true
}
