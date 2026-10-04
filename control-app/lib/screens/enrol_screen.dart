import 'package:flutter/material.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../api/control_api.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';

/// Pairs this device with the stack.
///
/// Reuses the existing in-game handshake rather than inventing a second credential path: the
/// app registers a code, the operator runs /verifyapi (already STAFF-gated) on the server, and
/// only then is a device token issued.
class EnrolScreen extends StatefulWidget {
  final ControlApi api;
  final VoidCallback onEnrolled;

  const EnrolScreen({super.key, required this.api, required this.onEnrolled});

  @override
  State<EnrolScreen> createState() => _EnrolScreenState();
}

class _EnrolScreenState extends State<EnrolScreen> {
  final _urlController = TextEditingController(text: 'http://192.168.100.10:8080');
  final _deviceNameController = TextEditingController();

  String? _code;
  String? _sessionId;
  String? _error;
  String? _status;
  bool _busy = false;
  bool _recovering = false;
  bool _revokePrevious = true;

  String _version = '';

  @override
  void initState() {
    super.initState();
    PackageInfo.fromPlatform()
        .then((info) {
          if (mounted) setState(() => _version = 'v${info.version}');
        })
        .catchError((_) => null);
    _deviceNameController.text = 'control app';
    if (widget.api.baseUrl != null) _urlController.text = widget.api.baseUrl!;
  }

  @override
  void dispose() {
    _urlController.dispose();
    _deviceNameController.dispose();
    super.dispose();
  }

  Future<void> _requestCode() async {
    setState(() {
      _busy = true;
      _error = null;
      _status = null;
    });
    try {
      await widget.api.setBaseUrl(_urlController.text);
      final sessionId = ControlApi.generateSessionId();
      final code = ControlApi.generateAuthCode();
      await widget.api.initiateAuth(sessionId, code);
      setState(() {
        _sessionId = sessionId;
        _code = code;
        _status = 'Run /verifyapi $code in game, then press Complete pairing.';
      });
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      setState(() => _busy = false);
    }
  }

  Future<void> _complete() async {
    final sessionId = _sessionId;
    if (sessionId == null) return;
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      final name = _deviceNameController.text.trim();
      if (_recovering) {
        final result = await widget.api
            .recoverOwner(sessionId, name, revokePrevious: _revokePrevious);
        final revoked = (result['revokedDevices'] as List?)?.cast<String>() ?? const [];
        setState(() => _status = revoked.isEmpty
            ? 'Recovered as OWNER (${result['verdict']}).'
            : 'Recovered as OWNER. Revoked: ${revoked.join(', ')}.');
      } else {
        final result = await widget.api.enrol(sessionId, name);
        final firstDevice = result['firstDevice'] == true;
        setState(() => _status = firstDevice
            ? 'Paired as OWNER (first device).'
            : 'Paired as ${result['role']}. An owner must promote this device before it can control anything.');
      }
      widget.onEnrolled();
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 420),
            child: HudPanel(
              title: 'pair device',
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text('HYPIXEL CONTROL',
                      style: ReactorTheme.heading(size: 20, color: ReactorTheme.cyan)),
                  const SizedBox(height: 4),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text('Operations console', style: ReactorTheme.label(size: 10)),
                      if (_version.isNotEmpty) ...[
                        const SizedBox(width: 8),
                        Text(_version,
                            style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint)),
                      ],
                    ],
                  ),
                  const SizedBox(height: 20),
                  _field('SERVER ADDRESS', _urlController, 'http://192.168.100.10:8080'),
                  const SizedBox(height: 12),
                  _field('DEVICE NAME', _deviceNameController, 'pixel 8 / desktop'),
                  const SizedBox(height: 18),
                  if (_code != null) ...[
                    Container(
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      decoration: BoxDecoration(
                        color: ReactorTheme.ground,
                        border: Border.all(color: ReactorTheme.cyan.withValues(alpha: 0.5)),
                      ),
                      child: Column(
                        children: [
                          Text('RUN IN GAME', style: ReactorTheme.label(size: 9, spacing: 2)),
                          const SizedBox(height: 6),
                          SelectableText('/verifyapi $_code',
                              style: ReactorTheme.data(
                                  size: 17,
                                  color: ReactorTheme.cyan,
                                  weight: FontWeight.w600)),
                        ],
                      ),
                    ),
                    const SizedBox(height: 14),
                  ],
                  if (_status != null) ...[
                    Text(_status!,
                        style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary)),
                    const SizedBox(height: 12),
                  ],
                  if (_error != null) ...[
                    Text(_error!,
                        style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)),
                    const SizedBox(height: 12),
                  ],
                  _button(
                    _code == null
                        ? 'REQUEST CODE'
                        : (_recovering ? 'CLAIM OWNER' : 'COMPLETE PAIRING'),
                    _busy ? null : (_code == null ? _requestCode : _complete),
                  ),
                  if (_code != null) ...[
                    const SizedBox(height: 8),
                    TextButton(
                      onPressed: _busy ? null : () => setState(() => _code = null),
                      child: Text('START OVER',
                          style: ReactorTheme.label(size: 9, color: ReactorTheme.textFaint)),
                    ),
                  ],
                  const SizedBox(height: 4),
                  TextButton(
                    onPressed: _busy
                        ? null
                        : () => setState(() => _recovering = !_recovering),
                    child: Text(
                      _recovering ? 'PAIR NORMALLY INSTEAD' : 'LOST THE OWNER DEVICE?',
                      style: ReactorTheme.label(
                          size: 9,
                          color: _recovering
                              ? ReactorTheme.amber
                              : ReactorTheme.textFaint),
                    ),
                  ),
                  if (_recovering) _recoveryNotice(),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  /// Recovery is a privileged, audited claim, so the screen says plainly what it will do
  /// rather than letting it look like an ordinary pairing.
  Widget _recoveryNotice() {
    return Container(
      margin: const EdgeInsets.only(top: 4),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: ReactorTheme.ground,
        border: Border.all(color: ReactorTheme.amber.withValues(alpha: 0.5)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('OWNER RECOVERY', style: ReactorTheme.label(size: 9, color: ReactorTheme.amber)),
          const SizedBox(height: 6),
          Text(
            'Claims OWNER using your in-game identity. Refused while another '
            "operator's owner device is still active - ask them to promote you instead. "
            'Every attempt is recorded in the audit log.',
            style: ReactorTheme.data(size: 10, color: ReactorTheme.textSecondary),
          ),
          const SizedBox(height: 8),
          InkWell(
            onTap: _busy ? null : () => setState(() => _revokePrevious = !_revokePrevious),
            child: Row(
              children: [
                Icon(
                  _revokePrevious ? Icons.check_box_outlined : Icons.check_box_outline_blank,
                  size: 16,
                  color: ReactorTheme.amber,
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text('Revoke my previous owner devices',
                      style: ReactorTheme.data(size: 10, color: ReactorTheme.textSecondary)),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _field(String label, TextEditingController controller, String hint) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: ReactorTheme.label(size: 9, spacing: 1.8)),
        const SizedBox(height: 4),
        TextField(
          controller: controller,
          style: ReactorTheme.data(size: 12),
          cursorColor: ReactorTheme.cyan,
          decoration: InputDecoration(
            isDense: true,
            hintText: hint,
            hintStyle: ReactorTheme.data(size: 12, color: ReactorTheme.textFaint),
            filled: true,
            fillColor: ReactorTheme.ground,
            contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 10),
            border: const OutlineInputBorder(
                borderRadius: BorderRadius.zero,
                borderSide: BorderSide(color: ReactorTheme.hairline)),
            enabledBorder: const OutlineInputBorder(
                borderRadius: BorderRadius.zero,
                borderSide: BorderSide(color: ReactorTheme.hairline)),
            focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.zero,
                borderSide: BorderSide(color: ReactorTheme.cyan.withValues(alpha: 0.6))),
          ),
        ),
      ],
    );
  }

  Widget _button(String label, VoidCallback? onTap) {
    final enabled = onTap != null;
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 13),
        decoration: ShapeDecoration(
          color: enabled
              ? ReactorTheme.cyan.withValues(alpha: 0.12)
              : ReactorTheme.panelRaised,
          shape: ChamferBorder(
            cut: 6,
            color: enabled ? ReactorTheme.cyan.withValues(alpha: 0.6) : ReactorTheme.hairline,
          ),
        ),
        child: Center(
          child: _busy
              ? const SizedBox(
                  width: 14,
                  height: 14,
                  child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan))
              : Text(label,
                  style: ReactorTheme.label(
                      size: 11,
                      spacing: 2,
                      color: enabled ? ReactorTheme.cyan : ReactorTheme.textFaint)),
        ),
      ),
    );
  }
}
