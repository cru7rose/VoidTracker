import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'order_controller.dart';

class OrderDetailsScreen extends ConsumerWidget {
  final String orderId;

  const OrderDetailsScreen({super.key, required this.orderId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final orderState = ref.watch(orderControllerProvider(orderId));

    return Scaffold(
      appBar: AppBar(title: const Text('Order Details')),
      body: orderState.when(
        data: (order) => Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Order ID: ${order.id}', style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 16),
              Text('Customer: ${order.customerName}'),
              const SizedBox(height: 8),
              Text('Pickup: ${order.pickupAddress}'),
              const SizedBox(height: 8),
              Text('Delivery: ${order.deliveryAddress}'),
              const SizedBox(height: 16),
              Text('Status: ${order.status}', style: const TextStyle(fontWeight: FontWeight.bold)),
              const SizedBox(height: 32),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  ElevatedButton(
                    onPressed: () => ref.read(orderControllerProvider(orderId).notifier).updateStatus('PICKUP'),
                    child: const Text('PICKUP'),
                  ),
                  ElevatedButton(
                    onPressed: () => ref.read(orderControllerProvider(orderId).notifier).updateStatus('DELIVERED'),
                    child: const Text('DELIVERED'),
                  ),
                ],
              ),
            ],
          ),
        ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, stack) => Center(child: Text('Error: $err')),
      ),
    );
  }
}
