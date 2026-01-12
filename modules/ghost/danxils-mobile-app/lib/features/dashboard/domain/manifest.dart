import 'package:equatable/equatable.dart';

class Manifest extends Equatable {
  final String id;
  final String driverId;
  final DateTime date;
  final String status;
  final List<ManifestRoute> routes;

  const Manifest({
    required this.id,
    required this.driverId,
    required this.date,
    required this.status,
    required this.routes,
  });

  @override
  List<Object?> get props => [id, driverId, date, status, routes];
}

class ManifestRoute extends Equatable {
  final String id;
  final String orderId;
  final int sequence;
  final String address;
  final String timeWindow;
  final String estimatedArrival;
  final String status;

  const ManifestRoute({
    required this.id,
    required this.orderId,
    required this.sequence,
    required this.address,
    required this.timeWindow,
    required this.estimatedArrival,
    required this.status,
  });

  @override
  List<Object?> get props => [id, orderId, sequence, address, timeWindow, estimatedArrival, status];
}
