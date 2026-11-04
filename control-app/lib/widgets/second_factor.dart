import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../api/control_api.dart';
import '../api/ops_api.dart';
import '../theme/reactor_theme.dart';

/// Asks for a six-digit authenticator code. Returns null if cancelled.
Future<String?> promptSecondFactor(BuildContext context, {String? reason}) {
  final controller = TextEditingController();
  return showDialog<String>(
    context: context,
    builder: (context) => AlertDialog(
      backgroundColor: ReactorTheme.panel,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.zero),
      title: Text('Second factor', style: ReactorTheme.heading(size: 14, color: ReactorTheme.amber)),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(reason ?? 'Enter the 6-digit code from your authenticator app.',
              style: ReactorTheme.data(size: 11.5, color: ReactorTheme.textSecondary)),
          const SizedBox(height: 10),
          TextField(
            controller: controller,
            autofocus: true,
            keyboardType: TextInputType.number,
            maxLength: 6,
            inputFormatters: [FilteringTextInputFormatter.digitsOnly],
            style: ReactorTheme.data(size: 20, weight: FontWeight.w600),
            decoration: const InputDecoration(counterText: '', hintText: '000000'),
            onSubmitted: (value) => Navigator.pop(context, value.trim()),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: Text('CANCEL', style: ReactorTheme.label(size: 10)),
        ),
        TextButton(
          onPressed: () => Navigator.pop(context, controller.text.trim()),
          child: Text('CONFIRM', style: ReactorTheme.label(size: 10, color: ReactorTheme.amber)),
        ),
      ],
    ),
  );
}

/// Runs an OWNER [action], asking for a TOTP code first when the server has one enrolled.
///
/// Returns null without running the action when the operator cancels the prompt. With no
/// enrolment nothing is asked and the action runs exactly as before. If the server refuses
/// because a code was needed after all (enrolled since the status was cached), it asks once.
Future<T?> withSecondFactor<T>(
  BuildContext context,
  ControlApi api,
  Future<T> Function() action, {
  String? reason,
}) async {
  final ops = OpsApi(api);
  var enrolled = false;
  try {
    enrolled = (await ops.fetchTotpStatus()).enrolled;
  } catch (_) {
    // An older backend has no /control/totp; behave as if nothing is enrolled.
  }

  Future<T?> attempt(bool ask) async {
    if (ask) {
      if (!context.mounted) return null;
      final code = await promptSecondFactor(context, reason: reason);
      if (code == null || code.isEmpty) return null;
      api.secondFactorCode = code;
    }
    try {
      return await action();
    } finally {
      api.secondFactorCode = null;
    }
  }

  try {
    return await attempt(enrolled);
  } on ApiException catch (e) {
    if (!enrolled && e.message.startsWith('Second factor required')) {
      await ops.fetchTotpStatus(force: true);
      return attempt(true);
    }
    rethrow;
  }
}
