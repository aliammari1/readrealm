import 'dart:html' as html;
import 'dart:convert';
import 'dart:typed_data';
import 'package:http/http.dart' as http;

class WebFaceRecognitionService {
  static const String _apiKey = 'YOUR_AZURE_API_KEY';
  static const String _endpoint = 'YOUR_AZURE_ENDPOINT';
  static const String _personGroupId = 'library_users';
  html.VideoElement? _videoElement;
  html.CanvasElement? _canvasElement;

  Future<bool> isFaceAuthAvailable() async {
    try {
      final mediaDevices = await html.window.navigator.mediaDevices?.enumerateDevices();
      return mediaDevices?.any((device) => device.kind == 'videoinput') ?? false;
    } catch (e) {
      print('Error checking camera availability: $e');
      return false;
    }
  }

  Future<String?> startCamera() async {
    try {
      final stream = await html.window.navigator.mediaDevices?.getUserMedia({
        'video': {
          'facingMode': 'user',
        }
      });

      if (stream != null) {
        _videoElement = html.VideoElement()
          ..srcObject = stream
          ..autoplay = true;

        _canvasElement = html.CanvasElement();
        return 'success';
      }
      return null;
    } catch (e) {
      print('Error starting camera: $e');
      return null;
    }
  }

  Future<String?> captureImage() async {
    if (_videoElement == null || _canvasElement == null) return null;

    try {
      _canvasElement!
        ..width = _videoElement!.videoWidth
        ..height = _videoElement!.videoHeight;

      _canvasElement!.context2D.drawImage(_videoElement!, 0, 0);
      final dataUrl = _canvasElement!.toDataUrl('image/jpeg');
      return dataUrl;
    } catch (e) {
      print('Error capturing image: $e');
      return null;
    }
  }

  Future<String?> detectFace(String imageData) async {
    try {
      final bytes = base64Decode(imageData.split(',').last);
      
      final response = await http.post(
        Uri.parse('$_endpoint/face/v1.0/detect'),
        headers: {
          'Content-Type': 'application/octet-stream',
          'Ocp-Apim-Subscription-Key': _apiKey,
        },
        body: bytes,
      );

      if (response.statusCode == 200) {
        final List<dynamic> faces = jsonDecode(response.body);
        if (faces.isNotEmpty) {
          return faces.first['faceId'];
        }
      }
      return null;
    } catch (e) {
      print('Error detecting face: $e');
      return null;
    }
  }

  Future<String?> registerFace(String imageData) async {
    final faceId = await detectFace(imageData);
    if (faceId == null) return null;

    try {
      final response = await http.post(
        Uri.parse('$_endpoint/face/v1.0/persongroups/$_personGroupId/persons'),
        headers: {
          'Content-Type': 'application/json',
          'Ocp-Apim-Subscription-Key': _apiKey,
        },
        body: jsonEncode({
          'name': 'User_$faceId',
        }),
      );

      if (response.statusCode == 200) {
        final personId = jsonDecode(response.body)['personId'];
        return personId;
      }
      return null;
    } catch (e) {
      print('Error registering face: $e');
      return null;
    }
  }

  Future<bool> authenticate(String storedPersonId, String imageData) async {
    final faceId = await detectFace(imageData);
    if (faceId == null) return false;

    try {
      final response = await http.post(
        Uri.parse('$_endpoint/face/v1.0/identify'),
        headers: {
          'Content-Type': 'application/json',
          'Ocp-Apim-Subscription-Key': _apiKey,
        },
        body: jsonEncode({
          'personGroupId': _personGroupId,
          'faceIds': [faceId],
          'maxNumOfCandidatesReturned': 1,
          'confidenceThreshold': 0.6,
        }),
      );

      if (response.statusCode == 200) {
        final List<dynamic> results = jsonDecode(response.body);
        if (results.isNotEmpty && results[0]['candidates'].isNotEmpty) {
          return results[0]['candidates'][0]['personId'] == storedPersonId;
        }
      }
      return false;
    } catch (e) {
      print('Error authenticating: $e');
      return false;
    }
  }

  void dispose() {
    _videoElement?.srcObject?.getTracks().forEach((track) => track.stop());
    _videoElement = null;
    _canvasElement = null;
  }
}
