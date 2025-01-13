import 'package:flutter/material.dart';
import '../services/face_recognition_service.dart';
import 'package:camera/camera.dart';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter/foundation.dart' show kIsWeb;

enum FaceRecognitionMode { registration, verification }

class FaceRecognitionScreen extends StatefulWidget {
  final FaceRecognitionMode mode;

  const FaceRecognitionScreen({
    Key? key,
    this.mode = FaceRecognitionMode.verification,
  }) : super(key: key);

  @override
  _FaceRecognitionScreenState createState() => _FaceRecognitionScreenState();
}

class _FaceRecognitionScreenState extends State<FaceRecognitionScreen> {
  final FaceRecognitionService _faceService = FaceRecognitionService();
  late CameraController _cameraController;
  bool _isInitialized = false;
  bool _isProcessing = false;
  bool _isChecking = true;
  String _status = 'Initializing face recognition...';
  final _secureStorage = const FlutterSecureStorage();

  @override
  void initState() {
    super.initState();
    _initializeCamera();
  }

  Future<void> _initializeCamera() async {
    final cameras = await availableCameras();
    final front = cameras.firstWhere(
      (camera) => camera.lensDirection == CameraLensDirection.front,
      orElse: () => cameras.first,
    );

    _cameraController = CameraController(front, ResolutionPreset.high);
    await _cameraController.initialize();
    
    if (mounted) {
      setState(() {
        _isInitialized = true;
      });
    }
  }

  Future<String?> _getStoredPersonId() async {
    return await _secureStorage.read(key: 'azure_person_id');
  }

  Future<void> _captureAndProcess() async {
    if (_isProcessing) return;
    setState(() => _isProcessing = true);

    try {
      String? imagePath;
      
      if (kIsWeb) {
        // For web, we'll use base64 image data
        final imageData = await _cameraController.takePicture();
        imagePath = imageData.path;
      } else {
        // For mobile platforms, use file system
        final tempDir = await getTemporaryDirectory();
        imagePath = '${tempDir.path}/face_capture.jpg';
        final image = await _cameraController.takePicture();
        await File(image.path).copy(imagePath);
      }

      // Process with Azure
      if (widget.mode == FaceRecognitionMode.registration) {
        final personId = await _faceService.registerFace(imagePath);
        if (mounted && personId != null) {
          Navigator.pop(context, {'personId': personId, 'imagePath': imagePath});
        }
      } else {
        final storedPersonId = await _getStoredPersonId();
        if (storedPersonId != null) {
          final success = await _faceService.authenticate(storedPersonId, imagePath);
          if (mounted) {
            Navigator.pop(context, success);
          }
        } else {
          throw Exception('No stored face data found');
        }
      }
    } catch (e) {
      print('Error processing face: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Face recognition failed: $e')),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isProcessing = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!_isInitialized) {
      return Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    return Scaffold(
      appBar: AppBar(title: Text('Face Recognition')),
      body: Stack(
        children: [
          CameraPreview(_cameraController),
          if (_isProcessing)
            Container(
              color: Colors.black45,
              child: Center(
                child: CircularProgressIndicator(),
              ),
            ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _isProcessing ? null : _captureAndProcess,
        child: Icon(Icons.camera),
      ),
    );
  }

  @override
  void dispose() {
    _cameraController.dispose();
    super.dispose();
  }
}
