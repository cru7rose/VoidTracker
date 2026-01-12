import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:go_router/go_router.dart';
import '../../auth/presentation/auth_controller.dart';
import 'manifest_controller.dart';

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Dashboard'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () {
              ref.read(authControllerProvider.notifier).logout();
              context.go('/login');
            },
          ),
        ],
      ),
      body: Consumer(
        builder: (context, ref, child) {
          final manifestState = ref.watch(manifestControllerProvider);

          return manifestState.when(
            data: (manifest) {
              if (manifest == null) {
                return const Center(child: Text('No manifest assigned for today.'));
              }
              return RefreshIndicator(
                onRefresh: () => ref.read(manifestControllerProvider.notifier).refresh(),
                child: ListView(
                  padding: const EdgeInsets.all(16.0),
                  children: [
                    Card(
                      elevation: 4,
                      child: Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Today\'s Manifest',
                              style: Theme.of(context).textTheme.titleLarge,
                            ),
                            const SizedBox(height: 8),
                            Text('Status: ${manifest.status}'),
                            Text('Date: ${manifest.date.toLocal().toString().split(' ')[0]}'),
                            const SizedBox(height: 16),
                            Text(
                              '${manifest.routes.length} Stops',
                              style: Theme.of(context).textTheme.titleMedium,
                            ),
                            const SizedBox(height: 8),
                            ...manifest.routes.map((route) => ListTile(
                                  leading: CircleAvatar(child: Text('${route.sequence}')),
                                  title: Text(route.address),
                                  subtitle: Text(route.timeWindow),
                                  trailing: Text(route.status),
                                  onTap: () {
                                    context.push('/order/${route.orderId}');
                                  },
                                )),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              );
            },
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (err, stack) => Center(child: Text('Error: $err')),
          );
        },
      ),
    );
  }
}
