// ignore_for_file: non_constant_identifier_names, camel_case_types, missing_return, unused_import
import 'dart:typed_data';

import 'package:foundation_fluttify/src/constants.dart';
import 'package:foundation_fluttify/src/type/platform/android_type/java/lang/object.dart';

class android_graphics_Point extends java_lang_Object {
  android_graphics_Point._();

  static Future<android_graphics_Point> create(int x, int y) async {
    final refId = await kMethodChannel.invokeMethod(
        'PlatformFactory::createandroid_graphics_Point', {'x': x, 'y': y});
    return android_graphics_Point._()
      ..refId = refId
      ..tag = 'platform';
  }

  Future<int> get x {
    return kMethodChannel
        .invokeMethod('android.graphics.Point::getX', {'refId': refId});
  }

  Future<int> get y {
    return kMethodChannel
        .invokeMethod('android.graphics.Point::getY', {'refId': refId});
  }
}
