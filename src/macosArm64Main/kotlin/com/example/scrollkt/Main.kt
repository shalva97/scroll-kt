package com.example.scrollkt

import kotlinx.cinterop.*
import platform.AppKit.*
import platform.Foundation.*

private var strongDelegate: AppDelegate? = null

@OptIn(BetaInteropApi::class)
fun main() {
    autoreleasepool {
        val app = NSApplication.sharedApplication()
        app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyAccessory)
        
        strongDelegate = AppDelegate()
        app.delegate = strongDelegate
        app.run()
    }
}
