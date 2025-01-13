import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:camera/camera.dart';
import 'package:path_provider/path_provider.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import '../services/web_face_recognition_service.dart';

class FaceRecognitionService {
  static const String _apiKey = 'YOUR_AZURE_API_KEY';
  static const String _endpoint = 'YOUR_AZURE_ENDPOINT';
  static const String _personGroupId = 'library_users';
  final _webService = kIsWeb ? WebFaceRecognitionService() : null;

  Future<bool> isFaceAuthAvailable() async {
    if (kIsWeb) {
      return _webService!.isFaceAuthAvailable();
    }
    final cameras = await availableCameras();
    return cameras.any((camera) => camera.lensDirection == CameraLensDirection.front);
  }

  Future<String?> detectFace(String imagePath) async {
    if (kIsWeb) {
      return _webService!.detectFace(imagePath);
    }
    try {
      final url = '$_endpoint/face/v1.0/detect?returnFaceId=true';
      final imageFile = File(imagePath);
      final bytes = await imageFile.readAsBytes();

      final response = await http.post(
        Uri.parse(url),
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
      print('Face detection error: $e');
      return null;
    }
  }

  Future<bool> verifyFace(String faceId1, String faceId2) async {
    try {
      final url = '$_endpoint/face/v1.0/verify';
      
      final response = await http.post(
        Uri.parse(url),
        headers: {
          'Content-Type': 'application/json',
          'Ocp-Apim-Subscription-Key': _apiKey,
        },
        body: jsonEncode({
          'faceId1': faceId1,
          'faceId2': faceId2,
        }),
      );

      if (response.statusCode == 200) {
        final result = jsonDecode(response.body);
        return result['isIdentical'] == true && result['confidence'] > 0.6;
      }
      return false;
    } catch (e) {
      print('Face verification error: $e');
      return false;
    }
  }

  Future<String?> registerFace(String imagePath) async {
    if (kIsWeb) {
      return _webService!.registerFace(imagePath);
    }
    final faceId = await detectFace(imagePath);
    if (faceId == null) return null;

    try {
      // Create a unique person ID for the user
      final createPersonUrl = '$_endpoint/face/v1.0/persongroups/$_personGroupId/persons';
      final createPersonResponse = await http.post(
        Uri.parse(createPersonUrl),
        headers: {
          'Content-Type': 'application/json',
          'Ocp-Apim-Subscription-Key': _apiKey,
        },
        body: jsonEncode({
          'name': 'User_$faceId',
        }),
      );

      if (createPersonResponse.statusCode != 200) return null;
      
      final personId = jsonDecode(createPersonResponse.body)['personId'];

      // Add face to person
      final addFaceUrl = '$_endpoint/face/v1.0/persongroups/$_personGroupId/persons/$personId/persistedFaces';
      final imageFile = File(imagePath);
      final bytes = await imageFile.readAsBytes();

      final addFaceResponse = await http.post(
        Uri.parse(addFaceUrl),
        headers: {
          'Content-Type': 'application/octet-stream',
          'Ocp-Apim-Subscription-Key': _apiKey,
        },
        body: bytes,
      );

      if (addFaceResponse.statusCode == 200) {
        // Train the person group after adding face
        await http.post(
          Uri.parse('$_endpoint/face/v1.0/persongroups/$_personGroupId/train'),
          headers: {
            'Ocp-Apim-Subscription-Key': _apiKey,
          },
        );

        return personId;
      }
      return null;
    } catch (e) {
      print('Face registration error: $e');
      return null;
    }
  }

  Future<bool> authenticate(String storedPersonId, String imagePath) async {
    if (kIsWeb) {
      return _webService!.authenticate(storedPersonId, imagePath);
    }
    final faceId = await detectFace(imagePath);
    if (faceId == null) return false;

    try {
      final url = '$_endpoint/face/v1.0/identify';
      
      final response = await http.post(
        Uri.parse(url),
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
          final candidate = results[0]['candidates'][0];
          return candidate['personId'] == storedPersonId;
        }
      }
      return false;
    } catch (e) {
      print('Face authentication error: $e');
      return false;
    }
  }
}