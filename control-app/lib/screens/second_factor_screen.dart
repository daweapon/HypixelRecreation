import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../api/control_api.dart';
import '../api/ops_api.dart';
import '../models/ops.dart';
import '../theme/reactor_theme.dart';
import '../widgets/chamfer.dart';
import '../widgets/hud_button.dart';
import '../widgets/second_factor.dart';

/// Enrols or removes the optional TOTP second factor on OWNER actions.
///
/// Enrolment shows the secret as an otpauth URI and as grouped text to type into any
/// authenticator app, then asks for one code to prove the app is in step before anything is
/// enforced. Until that confirmation nothing changes.
///
/// Constructor: `SecondFactorScreen(api: ControlApi, isOwner: bool)`.
class SecondFactorScreen extends StatefulWidget {
  final ControlApi api;
  final bool isOwner;

  const SecondFactorScreen({super.key, required this.api, required this.isOwner});

  @override
  State<SecondFactorScreen> createState() => _SecondFactorScreenState();
}

class _SecondFactorScreenState extends State<SecondFactorScreen> {
  late final OpsApi _ops = OpsApi(widget.api);
  final _code = TextEditingController();
  TotpStatus? _status;
  TotpEnrolment? _enrolment;
  String? _error;
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final status = await _ops.fetchTotpStatus(force: true);
      if (mounted) {
        setState(() {
          _status = status;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  Future<void> _run(Future<void> Function() action) async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await action();
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _start() => _run(() async {
        final enrolment = await withSecondFactor(context, widget.api, _ops.startTotpEnrolment,
            reason: 'Replacing the secret needs a code from the current one.');
        if (enrolment != null && mounted) setState(() => _enrolment = enrolment);
      });

  Future<void> _confirm() => _run(() async {
        await _ops.confirmTotp(_code.text);
        _code.clear();
        if (mounted) setState(() => _enrolment = null);
        await _load();
      });

  Future<void> _disable() => _run(() async {
        await withSecondFactor(context, widget.api, _ops.disableTotp,
            reason: 'Removing the second factor needs a current code.');
        await _load();
      });

  void _copy(String value, String what) {
    Clipboard.setData(ClipboardData(text: value));
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      backgroundColor: ReactorTheme.panelRaised,
      content: Text('$what copied', style: ReactorTheme.data(size: 11, color: ReactorTheme.cyan)),
    ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: hudAppBar('SECOND FACTOR'),
      body: Padding(padding: const EdgeInsets.all(10), child: _body()),
    );
  }

  Widget _body() {
    final status = _status;
    if (status == null) {
      return _error != null
          ? Center(
              child: Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)))
          : hudSpinner();
    }
    final enrolment = _enrolment;

    return ListView(
      children: [
        HudPanel(
          title: 'totp on owner actions',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              hudKeyValue('state', status.enrolled ? 'ENROLLED' : 'off',
                  colour: status.enrolled ? ReactorTheme.cyan : ReactorTheme.textSecondary),
              if (status.enrolled)
                hudKeyValue('by', '${status.enrolledBy ?? '?'} · ${formatStamp(status.enrolledAt)}'),
              const SizedBox(height: 4),
              Text(
                status.enrolled
                    ? 'Deploy, restore, device role changes, backup settings and owner recovery '
                        'ask for a code from your authenticator app.'
                    : 'Owner actions need only this paired device. Enrol to also require a code '
                        'from an authenticator app.',
                style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary),
              ),
              const SizedBox(height: 10),
              if (!widget.isOwner)
                Text('Only an OWNER device can change this.',
                    style: ReactorTheme.data(size: 10.5, color: ReactorTheme.amber))
              else
                Row(children: [
                  Expanded(
                    child: HudButton(
                      label: status.enrolled ? 'REPLACE SECRET' : 'ENROL',
                      busy: _busy,
                      onTap: _start,
                    ),
                  ),
                  if (status.enrolled) ...[
                    const SizedBox(width: 8),
                    Expanded(
                      child: HudButton(
                        label: 'REMOVE',
                        colour: ReactorTheme.critical,
                        busy: _busy,
                        onTap: _disable,
                      ),
                    ),
                  ],
                ]),
            ],
          ),
        ),
        if (enrolment != null) ...[
          const SizedBox(height: 10),
          HudPanel(
            title: 'add to your authenticator app',
            borderColor: ReactorTheme.amber.withValues(alpha: 0.5),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text('Secret key (time-based, 6 digits, 30s) for account ${enrolment.account}:',
                    style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary)),
                const SizedBox(height: 6),
                SelectableText(enrolment.groupedSecret,
                    style: ReactorTheme.data(size: 16, weight: FontWeight.w600)),
                const SizedBox(height: 6),
                Row(children: [
                  Expanded(
                      child: HudButton(
                          label: 'COPY SECRET', onTap: () => _copy(enrolment.secret, 'Secret'))),
                  const SizedBox(width: 8),
                  Expanded(
                      child: HudButton(
                          label: 'COPY URI', onTap: () => _copy(enrolment.otpauthUri, 'otpauth URI'))),
                ]),
                const SizedBox(height: 8),
                SelectableText(enrolment.otpauthUri,
                    style: ReactorTheme.data(size: 9.5, color: ReactorTheme.textFaint)),
                const SizedBox(height: 10),
                Text('Then enter the code it shows:',
                    style: ReactorTheme.data(size: 11, color: ReactorTheme.textSecondary)),
                TextField(
                  controller: _code,
                  keyboardType: TextInputType.number,
                  maxLength: 6,
                  inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                  style: ReactorTheme.data(size: 20, weight: FontWeight.w600),
                  decoration: const InputDecoration(counterText: '', hintText: '000000'),
                ),
                const SizedBox(height: 8),
                HudButton(label: 'CONFIRM AND ENFORCE', colour: ReactorTheme.amber, busy: _busy, onTap: _confirm),
              ],
            ),
          ),
        ],
        if (_error != null) ...[
          const SizedBox(height: 10),
          Text(_error!, style: ReactorTheme.data(size: 11, color: ReactorTheme.critical)),
        ],
        const SizedBox(height: 10),
        Text(
          'Lost the authenticator? Delete the "install" document from the Mongo collection '
          'api-control-totp with a shell on the NAS; that switches the factor off.',
          style: ReactorTheme.data(size: 10, color: ReactorTheme.textFaint),
        ),
      ],
    );
  }
}
