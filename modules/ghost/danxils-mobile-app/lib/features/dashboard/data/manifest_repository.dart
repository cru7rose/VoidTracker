import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../domain/manifest.dart';
import 'manifest_remote_data_source.dart';

part 'manifest_repository.g.dart';

@Riverpod(keepAlive: true)
ManifestRepository manifestRepository(ManifestRepositoryRef ref) {
  return ManifestRepository(ref.watch(manifestRemoteDataSourceProvider));
}

class ManifestRepository {
  final ManifestRemoteDataSource _remoteDataSource;

  ManifestRepository(this._remoteDataSource);

  Future<Manifest?> getTodayManifest() async {
    final dto = await _remoteDataSource.getTodayManifest();
    return dto?.toDomain();
  }
}
