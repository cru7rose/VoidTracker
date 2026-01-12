import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'auth_remote_data_source.dart';

part 'auth_repository.g.dart';

@Riverpod(keepAlive: true)
AuthRepository authRepository(AuthRepositoryRef ref) {
  return AuthRepository(ref.watch(authRemoteDataSourceProvider));
}

class AuthRepository {
  final AuthRemoteDataSource _remoteDataSource;

  AuthRepository(this._remoteDataSource);

  Future<void> login(String username, String password) async {
    await _remoteDataSource.login(username, password);
  }

  Future<void> logout() async {
    await _remoteDataSource.logout();
  }

  Future<bool> isLoggedIn() async {
    final token = await _remoteDataSource.getToken();
    return token != null;
  }
}
