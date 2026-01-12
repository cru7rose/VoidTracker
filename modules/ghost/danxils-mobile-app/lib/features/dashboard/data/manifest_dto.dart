import 'package:json_annotation/json_annotation.dart';
import '../domain/manifest.dart';

part 'manifest_dto.g.dart';

@JsonSerializable()
class ManifestDto {
  final String id;
  final String driverId;
  final String date;
  final String status;
  final List<ManifestRouteDto> routes;

  ManifestDto({
    required this.id,
    required this.driverId,
    required this.date,
    required this.status,
    required this.routes,
  });

  factory ManifestDto.fromJson(Map<String, dynamic> json) => _$ManifestDtoFromJson(json);
  Map<String, dynamic> toJson() => _$ManifestDtoToJson(this);

  Manifest toDomain() {
    return Manifest(
      id: id,
      driverId: driverId,
      date: DateTime.parse(date),
      status: status,
      routes: routes.map((e) => e.toDomain()).toList(),
    );
  }
}

@JsonSerializable()
class ManifestRouteDto {
  final String id;
  final String orderId;
  final int sequence;
  final String address;
  final String timeWindow;
  final String estimatedArrival;
  final String status;

  ManifestRouteDto({
    required this.id,
    required this.orderId,
    required this.sequence,
    required this.address,
    required this.timeWindow,
    required this.estimatedArrival,
    required this.status,
  });

  factory ManifestRouteDto.fromJson(Map<String, dynamic> json) => _$ManifestRouteDtoFromJson(json);
  Map<String, dynamic> toJson() => _$ManifestRouteDtoToJson(this);

  ManifestRoute toDomain() {
    return ManifestRoute(
      id: id,
      orderId: orderId,
      sequence: sequence,
      address: address,
      timeWindow: timeWindow,
      estimatedArrival: estimatedArrival,
      status: status,
    );
  }
}
