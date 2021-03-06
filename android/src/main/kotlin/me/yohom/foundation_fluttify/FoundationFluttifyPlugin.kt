package me.yohom.foundation_fluttify

import android.app.Activity
import android.content.IntentFilter
import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import me.yohom.foundation_fluttify.android.app.ActivityHandler
import me.yohom.foundation_fluttify.android.app.ApplicationHandler
import me.yohom.foundation_fluttify.android.app.NotificationHandler
import me.yohom.foundation_fluttify.android.app.PendingIntentHandler
import me.yohom.foundation_fluttify.android.content.BroadcastReceiverHandler
import me.yohom.foundation_fluttify.android.content.ContextHandler
import me.yohom.foundation_fluttify.android.content.IntentFilterHandler
import me.yohom.foundation_fluttify.android.content.IntentHandler
import me.yohom.foundation_fluttify.android.graphics.BitmapHandler
import me.yohom.foundation_fluttify.android.graphics.PointHandler
import me.yohom.foundation_fluttify.android.location.LocationHandler
import me.yohom.foundation_fluttify.android.os.BundleHandler
import me.yohom.foundation_fluttify.android.util.PairHandler
import me.yohom.foundation_fluttify.android.view.ViewHandler
import me.yohom.foundation_fluttify.java.io.FileHandler

// The stack that exists on the Dart side for a method call is enabled only when the MethodChannel passing parameters are limited
val STACK = mutableMapOf<String, Any>()

// Container for Dart side random access objects
val HEAP = mutableMapOf<Int, Any>()

// whether enable log or not
var enableLog: Boolean = true

// method channel for foundation
lateinit var gMethodChannel: MethodChannel

// broadcast event channel for foundation
lateinit var gBroadcastEventChannel: EventChannel

class FoundationFluttifyPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {
    private var applicationContext: Context? = null
    private var activity: Activity? = null
    private var activityBinding: ActivityPluginBinding? = null
    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var registrar: Registrar? = null
    private var broadcastReceiver: FluttifyBroadcastReceiver? = null

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val plugin = FoundationFluttifyPlugin()
            plugin.registrar = registrar
            plugin.activity = registrar.activity()
            plugin.broadcastReceiver = FluttifyBroadcastReceiver()

            gMethodChannel = MethodChannel(registrar.messenger(), "com.fluttify/foundation_method")
            gMethodChannel.setMethodCallHandler(plugin)

            registrar.activity().registerReceiver(plugin.broadcastReceiver, IntentFilter())
            gBroadcastEventChannel = EventChannel(registrar.messenger(), "com.fluttify/foundation_broadcast_event")
            gBroadcastEventChannel.setStreamHandler(plugin.broadcastReceiver)
        }
    }

    override fun onMethodCall(methodCall: MethodCall, methodResult: Result) {
        val rawArgs = methodCall.arguments ?: mapOf<String, Any>()
        methodCall.method.run {
            when {
                startsWith("android.app.Application::") -> ApplicationHandler(methodCall.method, rawArgs, methodResult, applicationContext)
                startsWith("android.app.Activity::") -> ActivityHandler(methodCall.method, rawArgs, methodResult, activity)
                startsWith("android.app.PendingIntent::") -> PendingIntentHandler(methodCall.method, rawArgs, methodResult)
                startsWith("android.app.Notification::") -> NotificationHandler(methodCall.method, rawArgs, methodResult, activity)
                startsWith("android.os.Bundle::") -> BundleHandler(methodCall.method, rawArgs, methodResult)
                startsWith("android.content.Intent::") -> IntentHandler(methodCall.method, rawArgs, methodResult)
                startsWith("android.content.Context::") -> ContextHandler(methodCall.method, rawArgs, methodResult)
                startsWith("android.content.BroadcastReceiver::") -> BroadcastReceiverHandler(methodCall.method, rawArgs, registrar?.messenger(), methodResult)
                startsWith("android.content.IntentFilter::") -> IntentFilterHandler(methodCall.method, rawArgs, methodResult)
                startsWith("android.graphics.Bitmap::") -> BitmapHandler(methodCall.method, rawArgs, methodResult, activity)
                startsWith("android.graphics.Point::") -> PointHandler(methodCall.method, rawArgs, methodResult)
                startsWith("android.location.Location::") -> LocationHandler(methodCall.method, rawArgs, methodResult)
                startsWith("android.util.Pair::") -> PairHandler(methodCall.method, rawArgs, methodResult)
                startsWith("android.view.View::") -> ViewHandler(methodCall.method, rawArgs, methodResult)
                startsWith("java.io.File::") -> FileHandler(methodCall.method, rawArgs, methodResult)
                startsWith("PlatformService::") -> PlatformService(methodCall.method, rawArgs as Map<String, Any>, methodResult, activityBinding, pluginBinding, registrar)
                else -> methodResult.notImplemented()
            }
        }
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = binding.applicationContext
        pluginBinding = binding

        gMethodChannel = MethodChannel(binding.binaryMessenger, "com.fluttify/foundation_method")
        gMethodChannel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = null
        activity = null
        activityBinding = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        activityBinding = binding

        broadcastReceiver = FluttifyBroadcastReceiver()
        binding.activity.registerReceiver(broadcastReceiver, IntentFilter())
        gBroadcastEventChannel = EventChannel(pluginBinding?.binaryMessenger, "com.fluttify/foundation_broadcast_event")
        gBroadcastEventChannel.setStreamHandler(broadcastReceiver)
    }

    override fun onDetachedFromActivity() {
        activity?.unregisterReceiver(broadcastReceiver)
        activity = null
        activityBinding = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        activityBinding = binding
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
        activityBinding = null
    }
}

fun <T> T.jsonable(): Boolean {
    return when (this) {
        is Byte, is Short, is Int, is Long, is Float, is Double -> true
        is String -> true
        is List<*> -> true
        is Map<*, *> -> true
        else -> false
    }
}