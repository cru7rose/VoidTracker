import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../domain/order.dart';
import 'order_remote_data_source.dart';

part 'order_repository.g.dart';

@Riverpod(keepAlive: true)
OrderRepository orderRepository(OrderRepositoryRef ref) {
  return OrderRepository(ref.watch(orderRemoteDataSourceProvider));
}

class OrderRepository {
  final OrderRemoteDataSource _remoteDataSource;

  OrderRepository(this._remoteDataSource);

  Future<Order> getOrder(String id) async {
    final dto = await _remoteDataSource.getOrder(id);
    return dto.toDomain();
  }

  Future<void> updateStatus(String id, String status) async {
    await _remoteDataSource.updateStatus(id, status);
  }
}
