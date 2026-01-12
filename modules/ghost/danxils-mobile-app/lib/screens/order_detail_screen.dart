import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:lucide_icons/lucide_icons.dart';

class OrderDetailScreen extends StatelessWidget {
  final String orderId;

  const OrderDetailScreen({super.key, required this.orderId});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Order $orderId'),
        leading: IconButton(
          icon: const Icon(LucideIcons.arrowLeft),
          onPressed: () => context.go('/'),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Status Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    const Icon(LucideIcons.package, size: 48, color: Colors.blue),
                    const SizedBox(height: 8),
                    const Text(
                      'PICKUP',
                      style: TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                        color: Colors.blue,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text('Assigned to you', style: TextStyle(color: Colors.grey[600])),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Actions
            ElevatedButton.icon(
              onPressed: () {
                // TODO: Implement confirm pickup
              },
              icon: const Icon(LucideIcons.scanLine),
              label: const Text('Scan & Confirm Pickup'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.orange,
                foregroundColor: Colors.white,
              ),
            ),
            const SizedBox(height: 8),
            ElevatedButton.icon(
              onPressed: () => context.go('/order/$orderId/epod'),
              icon: const Icon(LucideIcons.checkCircle),
              label: const Text('Complete Delivery (ePoD)'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.green,
                foregroundColor: Colors.white,
              ),
            ),
            
            const SizedBox(height: 24),
            
            // Details
            const Text(
              'Delivery Details',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            const Card(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: Column(
                  children: [
                    ListTile(
                      leading: Icon(LucideIcons.user),
                      title: Text('Customer Name'),
                      subtitle: Text('Warehouse A'),
                    ),
                    Divider(),
                    ListTile(
                      leading: Icon(LucideIcons.mapPin),
                      title: Text('Address'),
                      subtitle: Text('Logistics Way 1, Warsaw'),
                    ),
                    Divider(),
                    ListTile(
                      leading: Icon(LucideIcons.phone),
                      title: Text('Contact'),
                      subtitle: Text('+48 123 456 789'),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
