import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'auth_remote_data_source.g.dart';

@Riverpod(keepAlive: true)
AuthRemoteDataSource authRemoteDataSource(AuthRemoteDataSourceRef ref) {
  return AuthRemoteDataSource(Dio(), const FlutterSecureStorage());
}

class AuthRemoteDataSource {
  final Dio _dio;
  final FlutterSecureStorage _storage;

  AuthRemoteDataSource(this._dio, this._storage) {
    _dio.options.baseUrl = 'http://localhost:8080/api'; // Adjust for emulator/device
    _dio.options.headers = {'Content-Type': 'application/json'};
  }

  Future<String> login(String username, String password) async {
    try {
      final response = await _dio.post('/auth/login', data: {
        'username': username,
        'password': password,
      });

      if (response.statusCode == 200) {
        final token = response.data['accessToken'];
        await _storage.write(key: 'auth_token', value: token);
        return token;
      } else {
        throw Exception('Login failed: ${response.statusMessage}');
      }
    } catch (e) {
      throw Exception('Login error: $e');
    }
  }

  Future<void> logout() async {
    await _storage.delete(key: 'auth_token');
  }

  Future<String?> getToken() async {
    return await _storage.read(key: 'auth_token');
  }
}
