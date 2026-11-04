import 'package:flutter/material.dart';

import '../theme/reactor_theme.dart';

/// Shared chrome and dialogs for the game-data admin screens, in the Reactor HUD style.
class AdminScaffold extends StatelessWidget {
  final String title;
  final Widget body;
  final VoidCallback? onRefresh;
  final PreferredSizeWidget? bottom;

  const AdminScaffold({
    super.key,
    required this.title,
    required this.body,
    this.onRefresh,
    this.bottom,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: ReactorTheme.panel,
        elevation: 0,
        shape: const Border(bottom: BorderSide(color: ReactorTheme.hairline)),
        title: Text(title, style: ReactorTheme.heading(size: 13)),
        iconTheme: const IconThemeData(color: ReactorTheme.textSecondary, size: 18),
        bottom: bottom,
        actions: [
          if (onRefresh != null)
            IconButton(
              iconSize: 16,
              icon: const Icon(Icons.refresh, color: ReactorTheme.textSecondary),
              onPressed: onRefresh,
            ),
        ],
      ),
      body: Padding(padding: const EdgeInsets.all(10), child: body),
    );
  }
}

class AdminSpinner extends StatelessWidget {
  const AdminSpinner({super.key});

  @override
  Widget build(BuildContext context) => const Center(
      child: SizedBox(
          width: 16,
          height: 16,
          child: CircularProgressIndicator(strokeWidth: 1.5, color: ReactorTheme.cyan)));
}

class AdminMessage extends StatelessWidget {
  final String text;
  final Color colour;

  const AdminMessage(this.text, {super.key, this.colour = ReactorTheme.textFaint});

  @override
  Widget build(BuildContext context) =>
      Center(child: Text(text, textAlign: TextAlign.center, style: ReactorTheme.data(size: 11, color: colour)));
}

class AdminChip extends StatelessWidget {
  final String label;
  final Color colour;
  final VoidCallback? onTap;

  const AdminChip(this.label, {super.key, this.colour = ReactorTheme.cyan, this.onTap});

  @override
  Widget build(BuildContext context) {
    final enabled = onTap != null;
    final shade = enabled ? colour : ReactorTheme.textFaint;
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(
          border: Border.all(color: shade.withValues(alpha: enabled ? 0.6 : 0.25)),
        ),
        child: Text(label, style: ReactorTheme.label(size: 9, color: shade, spacing: 1.2)),
      ),
    );
  }
}

/// A label and value on one line, the value in the data face.
class AdminField extends StatelessWidget {
  final String label;
  final String value;
  final Color colour;

  const AdminField(this.label, this.value, {super.key, this.colour = ReactorTheme.textPrimary});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 110,
            child: Text(label.toUpperCase(), style: ReactorTheme.label(size: 9, spacing: 1.2)),
          ),
          Expanded(child: Text(value, style: ReactorTheme.data(size: 11, color: colour))),
        ],
      ),
    );
  }
}

void adminNotify(BuildContext context, String message, {Color colour = ReactorTheme.cyan}) {
  ScaffoldMessenger.of(context).showSnackBar(SnackBar(
    backgroundColor: ReactorTheme.panelRaised,
    content: Text(message, style: ReactorTheme.data(size: 11, color: colour)),
  ));
}

Future<bool> adminConfirm(BuildContext context, String title, String body, String verb) async {
  final answer = await showDialog<bool>(
    context: context,
    builder: (context) => AlertDialog(
      backgroundColor: ReactorTheme.panelRaised,
      title: Text(title, style: ReactorTheme.heading(size: 12)),
      content: Text(body, style: ReactorTheme.data(size: 12, color: ReactorTheme.textSecondary)),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context, false),
          child: Text('CANCEL', style: ReactorTheme.label(size: 10)),
        ),
        TextButton(
          onPressed: () => Navigator.pop(context, true),
          child: Text(verb, style: ReactorTheme.label(size: 10, color: ReactorTheme.amber)),
        ),
      ],
    ),
  );
  return answer ?? false;
}

/// Second step of a destructive action: the operator must type [expected] exactly.
Future<bool> adminConfirmByTyping(BuildContext context, String title, String body, String expected) async {
  final controller = TextEditingController();
  final answer = await showDialog<bool>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setState) => AlertDialog(
        backgroundColor: ReactorTheme.panelRaised,
        title: Text(title, style: ReactorTheme.heading(size: 12)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(body, style: ReactorTheme.data(size: 12, color: ReactorTheme.textSecondary)),
            const SizedBox(height: 10),
            Text('Type $expected to confirm',
                style: ReactorTheme.label(size: 9, color: ReactorTheme.amber, spacing: 1.2)),
            TextField(
              controller: controller,
              autofocus: true,
              style: ReactorTheme.data(size: 12),
              onChanged: (_) => setState(() {}),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: Text('CANCEL', style: ReactorTheme.label(size: 10)),
          ),
          TextButton(
            onPressed: controller.text.trim() == expected ? () => Navigator.pop(context, true) : null,
            child: Text('CONFIRM',
                style: ReactorTheme.label(
                    size: 10,
                    color: controller.text.trim() == expected ? ReactorTheme.critical : ReactorTheme.textFaint)),
          ),
        ],
      ),
    ),
  );
  return answer ?? false;
}

Future<String?> adminPick(BuildContext context, String title, List<String> options) {
  return showDialog<String>(
    context: context,
    builder: (context) => SimpleDialog(
      backgroundColor: ReactorTheme.panelRaised,
      title: Text(title, style: ReactorTheme.heading(size: 12)),
      children: [
        SizedBox(
          width: 320,
          height: options.length > 8 ? 360 : null,
          child: ListView(
            shrinkWrap: true,
            children: [
              for (final option in options)
                SimpleDialogOption(
                  onPressed: () => Navigator.pop(context, option),
                  child: Text(option, style: ReactorTheme.data(size: 12)),
                ),
            ],
          ),
        ),
      ],
    ),
  );
}

Future<String?> adminPrompt(BuildContext context, String title, String hint, {String initial = ''}) async {
  final controller = TextEditingController(text: initial);
  final answer = await showDialog<String>(
    context: context,
    builder: (context) => AlertDialog(
      backgroundColor: ReactorTheme.panelRaised,
      title: Text(title, style: ReactorTheme.heading(size: 12)),
      content: TextField(
        controller: controller,
        autofocus: true,
        style: ReactorTheme.data(size: 12),
        decoration: InputDecoration(
          hintText: hint,
          hintStyle: ReactorTheme.data(size: 11, color: ReactorTheme.textFaint),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: Text('CANCEL', style: ReactorTheme.label(size: 10)),
        ),
        TextButton(
          onPressed: () => Navigator.pop(context, controller.text.trim()),
          child: Text('OK', style: ReactorTheme.label(size: 10, color: ReactorTheme.cyan)),
        ),
      ],
    ),
  );
  return answer;
}

String adminStamp(DateTime at) {
  final local = at.toLocal();
  String two(int v) => v.toString().padLeft(2, '0');
  return '${two(local.month)}/${two(local.day)} ${two(local.hour)}:${two(local.minute)}';
}

String adminCoins(double coins) {
  if (coins >= 1e9) return '${(coins / 1e9).toStringAsFixed(2)}B';
  if (coins >= 1e6) return '${(coins / 1e6).toStringAsFixed(2)}M';
  if (coins >= 1e3) return '${(coins / 1e3).toStringAsFixed(1)}k';
  return coins.toStringAsFixed(0);
}
