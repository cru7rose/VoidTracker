import 'package:json_annotation/json_annotation.dart';
import '../domain/order.dart';

part 'order_dto.g.dart';

@JsonSerializable()
class OrderDto {
  final String id;
  final String customerName;
  final String pickupAddress;
  final String deliveryAddress;
  final String status;
  final int weight;
  final int volume;

  OrderDto({
    required this.id,
    required this.customerName,
    required this.pickupAddress,
    required this.deliveryAddress,
    required this.status,
    required this.weight,
    required this.volume,
  });

  factory OrderDto.fromJson(Map<String, dynamic> json) => _$OrderDtoFromJson(json);
  Map<String, dynamic> toJson() => _$OrderDtoToJson(this);

  Order toDomain() {
    return Order(
      id: id,
      customerName: customerName,
      pickupAddress: pickupAddress,
      deliveryAddress: deliveryAddress,
      status: status,
      weight: weight,
      volume: volume,
    );
  }
}
