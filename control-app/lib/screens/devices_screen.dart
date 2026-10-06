import 'package:flutter/material.dart';

import '../api/control_api.dart';
import '../models/fleet.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';

/// The roster of paired devices, and the only place a role can be changed.
///
/// Without this screen the server's device endpoints are unreachable from the app, which left
/// owner recovery - a break-glass path that revokes the existing owner - as the only way to
/// change anything about a device. Revoking a lost phone should not require that.
class DevicesScreen extends StatefulWidget {
  final ControlApi api;

  const DevicesScreen({super.key, required this.api});

  @override
  State<DevicesScreen> createState() => _DevicesScreenState();
}

class _DevicesScreenState extends State<DevicesScreen> {
  static const List<String> _roles = ['READ_ONLY', 'OPERATOR', 'OWNER'];

  List<DeviceEntry>? _devices;
  String? _error;
  String? _busyDeviceId;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final devices = await widget.api.fetchDevices();
      if (mounted) {
        setState(() {
          _devices = devices;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  Future<void> _apply(DeviceEntry device, Future<void> Function() action, String success) async {
    setState(() => _busyDeviceId = device.deviceId);
    try {
      await action();
      await _load();
      if (mounted) _notify(success, ReactorTheme.cyan);
    } catch (e) {
      if (mounted) _notify(e.toString(), ReactorTheme.critical);
    } finally {
      if (mounted) setState(() => _busyDeviceId = null);
    }
  }

  void _notify(String message, Color colour) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      backgroundColor: ReactorTheme.panelRaised,
      content: Text(message, style: ReactorTheme.data(size: 11, color: colour)),
    ));
  }

  Future<bool> _confirm(String title, String body, String verb) async {
    final answer = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: ReactorTheme.panelRaised,
        title: Text(title, style: ReactorTheme.heading(size: 12)),
        content: Text(body,
            style: ReactorTheme.data(size: 12, color: ReactorTheme.textSecondary)),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: Text('CANCEL', style: ReactorTheme.label(size: 10)),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: Text(verb,
                style: ReactorTheme.label(size: 10, color: ReactorTheme.amber)),
          ),
        ],
      ),
    );
    return answer ?? false;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: ReactorTheme.panel,
        elevation: 0,
        shape: const Border(bottom: BorderSide(color: ReactorTheme.hairline)),
        title: Text('DEVICES', style: ReactorTheme.heading(size: 13)),
        iconTheme: const IconThemeData(color: ReactorTheme.textSecondary, size: 18),
        actions: [
          IconButton(
            iconSize: 16,
            icon: const Icon(Icons.refresh, color: ReactorTheme.textSecondary),
            onPressed: _load,
          ),
        ],
      ),
      body: Padding(padding: const EdgeInsets.all(10), child: _body()),
    );
  }

  Widget _body() {
    if (_error != null) {
      return Center(
          child: Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)));
    }
    final devices = _devices;
    if (devices == null) {
      return const Center(
          child: SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan)));
    }
    if (devices.isEmpty) {
      return Center(
          child: Text('no devices enrolled',
              style: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint)));
    }

    final active = devices.where((d) => !d.revoked).length;
    return HudPanel(
      title: '$active active  ·  ${devices.length} enrolled',
      child: ListView.builder(
        itemCount: devices.length,
        itemBuilder: (context, index) => _row(devices[index]),
      ),
    );
  }

  Widget _row(DeviceEntry device) {
    final isSelf = device.deviceId == widget.api.deviceId;
    final busy = _busyDeviceId == device.deviceId;
    final colour = device.revoked
        ? ReactorTheme.textFaint
        : device.isOwner
            ? ReactorTheme.amber
            : ReactorTheme.cyan;

    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(width: 2, height: 42, color: colour),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Flexible(
                      child: Text(device.deviceName,
                          overflow: TextOverflow.ellipsis,
                          style: ReactorTheme.data(
                              size: 12, color: colour, weight: FontWeight.w600)),
                    ),
                    const SizedBox(width: 8),
                    Text(device.role,
                        style: ReactorTheme.label(size: 9, color: colour, spacing: 1.2)),
                    if (isSelf) ...[
                      const SizedBox(width: 6),
                      Text('THIS DEVICE',
                          style: ReactorTheme.label(
                              size: 9, color: ReactorTheme.textFaint, spacing: 1.2)),
                    ],
                    if (device.revoked) ...[
                      const SizedBox(width: 6),
                      Text('REVOKED',
                          style: ReactorTheme.label(
                              size: 9, color: ReactorTheme.critical, spacing: 1.2)),
                    ],
                  ],
                ),
                const SizedBox(height: 2),
                Text(
                  '${device.playerName}'
                  '${device.lastSeenAt == null ? '' : ' · last seen ${_stamp(device.lastSeenAt!)}'}',
                  style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary),
                ),
                Text(device.deviceId,
                    style: ReactorTheme.data(size: 9, color: ReactorTheme.textFaint)),
                if (!device.revoked) ...[
                  const SizedBox(height: 6),
                  busy
                      ? const SizedBox(
                          width: 12,
                          height: 12,
                          child: CircularProgressIndicator(
                              strokeWidth: 1.5, color: ReactorTheme.cyan))
                      : _controls(device, isSelf),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _controls(DeviceEntry device, bool isSelf) {
    return Wrap(
      spacing: 6,
      runSpacing: 6,
      children: [
        for (final role in _roles)
          if (role != device.role)
            _chip(role.replaceAll('_', ' '), ReactorTheme.cyan, () => _changeRole(device, role),
                // The server refuses to demote the calling device; say so rather than fail.
                enabled: !(isSelf && role != 'OWNER')),
        _chip('REVOKE', ReactorTheme.critical, () => _revoke(device), enabled: !isSelf),
      ],
    );
  }

  Widget _chip(String label, Color colour, VoidCallback onTap, {bool enabled = true}) {
    final shade = enabled ? colour : ReactorTheme.textFaint;
    return InkWell(
      onTap: enabled ? onTap : null,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(
          border: Border.all(color: shade.withValues(alpha: enabled ? 0.6 : 0.25)),
        ),
        child: Text(label, style: ReactorTheme.label(size: 9, color: shade, spacing: 1.2)),
      ),
    );
  }

  Future<void> _changeRole(DeviceEntry device, String role) async {
    if (role == 'OWNER') {
      final ok = await _confirm(
        'Promote to owner?',
        '${device.deviceName} (${device.playerName}) will be able to deploy arbitrary code and '
            'revoke other devices, including this one.',
        'PROMOTE',
      );
      if (!ok) return;
    }
    await _apply(device, () => widget.api.setDeviceRole(device.deviceId, role),
        '${device.deviceName} is now ${role.replaceAll('_', ' ').toLowerCase()}');
  }

  Future<void> _revoke(DeviceEntry device) async {
    final ok = await _confirm(
      'Revoke this device?',
      '${device.deviceName} (${device.playerName}) loses access immediately and permanently. '
          'It would have to enrol again from scratch.',
      'REVOKE',
    );
    if (!ok) return;
    await _apply(device, () => widget.api.revokeDevice(device.deviceId),
        '${device.deviceName} revoked');
  }

  static String _stamp(DateTime at) {
    final local = at.toLocal();
    String two(int v) => v.toString().padLeft(2, '0');
    return '${two(local.month)}/${two(local.day)} ${two(local.hour)}:${two(local.minute)}';
  }
}
