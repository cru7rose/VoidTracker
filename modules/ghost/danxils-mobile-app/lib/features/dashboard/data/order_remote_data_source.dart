import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'order_dto.dart';
import '../../auth/data/auth_remote_data_source.dart';

part 'order_remote_data_source.g.dart';

@Riverpod(keepAlive: true)
OrderRemoteDataSource orderRemoteDataSource(OrderRemoteDataSourceRef ref) {
  return OrderRemoteDataSource(Dio(), ref.read(authRemoteDataSourceProvider));
}

class OrderRemoteDataSource {
  final Dio _dio;
  final AuthRemoteDataSource _authDataSource;

  OrderRemoteDataSource(this._dio, this._authDataSource) {
    _dio.options.baseUrl = 'http://localhost:8080/api';
    _dio.options.headers = {'Content-Type': 'application/json'};
  }

  Future<OrderDto> getOrder(String id) async {
    try {
      final token = await _authDataSource.getToken();
      final response = await _dio.get(
        '/orders/$id',
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );
      return OrderDto.fromJson(response.data);
    } catch (e) {
      throw Exception('Failed to fetch order: $e');
    }
  }

  Future<void> updateStatus(String id, String status) async {
    try {
      final token = await _authDataSource.getToken();
      await _dio.put(
        '/mobile/orders/$id/status',
        data: {'status': status},
        options: Options(headers: {'Authorization': 'Bearer $token'}),
      );
    } catch (e) {
      throw Exception('Failed to update status: $e');
    }
  }
}
