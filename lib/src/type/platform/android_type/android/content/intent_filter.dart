import 'package:foundation_fluttify/src/object/obejcts.dart';
import 'package:foundation_fluttify/src/type/platform/android_type/java/lang/object.dart';

import 'intent.dart';

// ignore_for_file: non_constant_identifier_names, camel_case_types, missing_return, unused_import
class android_content_IntentFilter extends java_lang_Object {
  Future<android_content_IntentFilter> create(String action) async {
    final result = await kMethodChannel.invokeMethod(
      'android.content.IntentFilter::create',
      {'refId': refId, 'action': action},
    );
    return android_content_IntentFilter()..refId = result;
  }
}
