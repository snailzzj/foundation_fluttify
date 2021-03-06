import 'package:foundation_fluttify/foundation_fluttify.dart';

class CGSize extends Ref {
  static Future<CGSize> create(
    double width,
    double height,
  ) async {
    final refId = await kMethodChannel.invokeMethod('CGSize::create', {
      'width': width,
      'height': height,
    });
    return CGSize()
      ..refId = refId
      ..tag__ = 'platform';
  }

  Future<double> get width {
    return kMethodChannel.invokeMethod('CGSize::getWidth', {'refId': refId});
  }

  Future<double> get height {
    return kMethodChannel.invokeMethod('CGSize::getHeight', {'refId': refId});
  }
}
