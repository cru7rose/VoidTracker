class Order {
  final String orderId;
  final String status;
  final String customerName;
  final String deliveryAddress;
  final DateTime? sla;
  final String? assignedDriver;

  Order({
    required this.orderId,
    required this.status,
    required this.customerName,
    required this.deliveryAddress,
    this.sla,
    this.assignedDriver,
  });

  factory Order.fromJson(Map<String, dynamic> json) {
    return Order(
      orderId: json['orderId'],
      status: json['status'],
      customerName: json['delivery']['customerName'],
      deliveryAddress: '${json['delivery']['street']} ${json['delivery']['streetNumber']}, ${json['delivery']['city']}',
      sla: json['delivery']['sla'] != null ? DateTime.parse(json['delivery']['sla']) : null,
      assignedDriver: json['assignedDriver'],
    );
  }
}
