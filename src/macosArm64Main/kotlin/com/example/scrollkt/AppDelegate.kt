package com.example.scrollkt

import kotlinx.cinterop.*
import platform.AppKit.*
import platform.Foundation.*
import platform.ServiceManagement.*
import platform.objc.*
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class AppDelegate : NSObject(), NSApplicationDelegateProtocol {
    private var statusItem: NSStatusItem? = null
    private val defaults = NSUserDefaults.standardUserDefaults
    private val launchAtLoginKey = "launchAtLogin"
    private var isAccessibilityEnabled = true
    private var lastError: String? = null
    private var retryTimer: NSTimer? = null

    override fun applicationDidFinishLaunching(notification: NSNotification) {
        isAccessibilityEnabled = setupEventTap()
        updateMenu()
        
        if (!isAccessibilityEnabled) {
            startRetryTimer()
        }
    }

    private fun startRetryTimer() {
        retryTimer?.invalidate()
        retryTimer = NSTimer.scheduledTimerWithTimeInterval(
            ti = 2.0,
            target = this,
            selector = NSSelectorFromString("checkPermissions:"),
            userInfo = null,
            repeats = true
        )
    }

    @ObjCAction
    fun checkPermissions(timer: NSTimer) {
        if (!isAccessibilityEnabled) {
            isAccessibilityEnabled = setupEventTap()
            if (isAccessibilityEnabled) {
                timer.invalidate()
                retryTimer = null
                updateMenu()
            }
        } else {
            timer.invalidate()
            retryTimer = null
        }
    }

    private fun updateMenu() {
        val menu = NSMenu()
        
        if (!isAccessibilityEnabled) {
            val errorItem = NSMenuItem(
                title = "⚠️ Permission Denied",
                action = NSSelectorFromString("openAccessibilitySettings:"),
                keyEquivalent = ""
            )
            errorItem.target = this
            menu.addItem(errorItem)
            
            val hintItem = NSMenuItem(
                title = "Grant Accessibility permission",
                action = null,
                keyEquivalent = ""
            )
            hintItem.enabled = false
            menu.addItem(hintItem)
            
            val retryItem = NSMenuItem(
                title = "Retry Now",
                action = NSSelectorFromString("retryAccessibility:"),
                keyEquivalent = ""
            )
            retryItem.target = this
            menu.addItem(retryItem)
            
            menu.addItem(NSMenuItem.separatorItem())
        }

        if (lastError != null) {
            val errorItem = NSMenuItem(
                title = "❌ $lastError",
                action = null,
                keyEquivalent = ""
            )
            errorItem.enabled = false
            menu.addItem(errorItem)
            menu.addItem(NSMenuItem.separatorItem())
        }

        val isLaunchAtLoginEnabled = checkLoginItemExists()
        
        // Sync defaults with actual system state if they differ
        if (defaults.boolForKey(launchAtLoginKey) != isLaunchAtLoginEnabled) {
            defaults.setBool(isLaunchAtLoginEnabled, launchAtLoginKey)
        }

        val launchAtLoginItem = NSMenuItem(
            title = "Start at Login",
            action = NSSelectorFromString("toggleLaunchAtLogin:"),
            keyEquivalent = ""
        )
        launchAtLoginItem.target = this
        launchAtLoginItem.state = if (isLaunchAtLoginEnabled) NSControlStateValueOn else NSControlStateValueOff
        menu.addItem(launchAtLoginItem)

        menu.addItem(NSMenuItem.separatorItem())

        val quitItem = NSMenuItem(title = "Quit", action = NSSelectorFromString("terminate:"), keyEquivalent = "q")
        menu.addItem(quitItem)
        
        if (statusItem == null) {
            val statusBar = NSStatusBar.systemStatusBar
            statusItem = statusBar.statusItemWithLength(NSVariableStatusItemLength)
            statusItem?.button?.title = "Scroll-KT"
        }
        statusItem?.setMenu(menu)
    }

    @ObjCAction
    fun openAccessibilitySettings(sender: NSMenuItem) {
        val url = NSURL.URLWithString("x-apple.systempreferences:com.apple.preference.security?Privacy_Accessibility")
        if (url != null) {
            NSWorkspace.sharedWorkspace().openURL(url)
        }
    }

    @ObjCAction
    fun retryAccessibility(sender: NSMenuItem) {
        isAccessibilityEnabled = setupEventTap()
        if (isAccessibilityEnabled) {
            retryTimer?.invalidate()
            retryTimer = null
        } else {
            startRetryTimer()
        }
        updateMenu()
    }

    private fun checkLoginItemExists(): Boolean {
        val service = SMAppService.mainAppService
        return service.status == SMAppServiceStatus.SMAppServiceStatusEnabled
    }

    @ObjCAction
    fun toggleLaunchAtLogin(sender: NSMenuItem) {
        val newState = if (sender.state == NSControlStateValueOn) NSControlStateValueOff else NSControlStateValueOn
        sender.state = newState
        val launchAtLogin = newState == NSControlStateValueOn
        defaults.setBool(launchAtLogin, launchAtLoginKey)
        
        setLaunchAtLogin(launchAtLogin)
        updateMenu()
    }

    private fun setLaunchAtLogin(enabled: Boolean) {
        val service = SMAppService.mainAppService
        lastError = null
        
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            if (enabled) {
                if (!service.registerAndReturnError(error.ptr)) {
                    val errorDescription = error.value?.localizedDescription
                    println("Failed to register login item: $errorDescription")
                    lastError = "Login Item registration failed"
                    updateMenu()
                    showAlert("Failed to update Login Items", "Scroll-KT could not enable 'Start at Login'. ${errorDescription ?: ""}")
                }
            } else {
                if (!service.unregisterAndReturnError(error.ptr)) {
                    val errorDescription = error.value?.localizedDescription
                    println("Failed to unregister login item: $errorDescription")
                    lastError = "Login Item removal failed"
                    updateMenu()
                    showAlert("Failed to update Login Items", "Scroll-KT could not disable 'Start at Login'. ${errorDescription ?: ""}")
                }
            }
        }
    }

    private fun showAlert(title: String, informativeText: String) {
        val alert = NSAlert()
        alert.setMessageText(title)
        alert.setInformativeText(informativeText)
        alert.addButtonWithTitle("OK")
        NSApplication.sharedApplication().activateIgnoringOtherApps(true)
        alert.runModal()
    }
}
