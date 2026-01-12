import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'manifest_dto.dart';
import '../../auth/data/auth_remote_data_source.dart';

part 'manifest_remote_data_source.g.dart';

@Riverpod(keepAlive: true)
ManifestRemoteDataSource manifestRemoteDataSource(ManifestRemoteDataSourceRef ref) {
  // We can reuse the Dio instance from AuthRemoteDataSource or create a new one with interceptors
  // For simplicity, we'll assume a shared Dio provider or similar setup in a real app
  // Here we just instantiate a new one for the example, but in prod we'd share it
  return ManifestRemoteDataSource(Dio(), ref.read(authRemoteDataSourceProvider));
}

class ManifestRemoteDataSource {
  final Dio _dio;
  final AuthRemoteDataSource _authDataSource;

  ManifestRemoteDataSource(this._dio, this._authDataSource) {
    _dio.options.baseUrl = 'http://localhost:8080/api';
    _dio.options.headers = {'Content-Type': 'application/json'};
  }

  Future<ManifestDto?> getTodayManifest() async {
    try {
      final token = await _authDataSource.getToken();
      if (token == null) throw Exception('Not authenticated');

      final response = await _dio.get(
        '/mobile/manifest',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );

      if (response.statusCode == 200 && response.data != null) {
        return ManifestDto.fromJson(response.data);
      } else if (response.statusCode == 404) {
        return null; // No manifest for today
      } else {
        throw Exception('Failed to load manifest: ${response.statusMessage}');
      }
    } catch (e) {
      throw Exception('Error fetching manifest: $e');
    }
  }
}
