import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../domain/manifest.dart';
import '../data/manifest_repository.dart';

part 'manifest_controller.g.dart';

@riverpod
class ManifestController extends _$ManifestController {
  @override
  FutureOr<Manifest?> build() async {
    return await ref.read(manifestRepositoryProvider).getTodayManifest();
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => ref.read(manifestRepositoryProvider).getTodayManifest());
  }
}
