import 'package:equatable/equatable.dart';

class Order extends Equatable {
  final String id;
  final String customerName;
  final String pickupAddress;
  final String deliveryAddress;
  final String status;
  final int weight;
  final int volume;

  const Order({
    required this.id,
    required this.customerName,
    required this.pickupAddress,
    required this.deliveryAddress,
    required this.status,
    required this.weight,
    required this.volume,
  });

  @override
  List<Object?> get props => [id, customerName, pickupAddress, deliveryAddress, status, weight, volume];
}
