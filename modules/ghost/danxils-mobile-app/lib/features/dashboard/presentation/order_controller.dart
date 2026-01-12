import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../domain/order.dart';
import '../data/order_repository.dart';

part 'order_controller.g.dart';

@riverpod
class OrderController extends _$OrderController {
  @override
  FutureOr<Order> build(String orderId) async {
    return await ref.read(orderRepositoryProvider).getOrder(orderId);
  }

  Future<void> updateStatus(String status) async {
    final orderId = arg;
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      await ref.read(orderRepositoryProvider).updateStatus(orderId, status);
      return await ref.read(orderRepositoryProvider).getOrder(orderId);
    });
  }
}
