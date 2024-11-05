import 'package:flutter/material.dart';
import 'package:flutter_nfc_kit/flutter_nfc_kit.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Virtual NFC Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const NFCPage(),
    );
  }
}

class NFCPage extends StatelessWidget {
  const NFCPage({super.key});

  // Set the URL to be shared
  final String virtualNfcData =
      "https://www.kryptoxotis.com"; // Replace with your desired link

  Future<void> pushNFCMessage(BuildContext context) async {
    try {
      // Attempt to push the virtual NFC tag message
      await FlutterNfcKit.writeNDEFRecords([
        NDEFRawRecord(
          typeNameFormat: TypeNameFormat.nfcWellKnown,
          type:
              Uint8List.fromList([0x55]), // 'U' in ASCII, which stands for URI
          payload: Uint8List.fromList(
            [0x03] + utf8.encode(virtualNfcData), // 0x03 stands for 'https://'
          ),
        ),
      ]);

      // Show success message if NFC push was successful
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text("NFC Message Sent!"),
          content: Text("NFC Data: $virtualNfcData"),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text("Close"),
            ),
          ],
        ),
      );
    } catch (e) {
      // Show error if NFC push fails
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text("Error"),
          content: Text("Failed to push NFC message: $e"),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text("Close"),
            ),
          ],
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Virtual NFC Emulator"),
      ),
      body: Center(
        child: ElevatedButton(
          onPressed: () => pushNFCMessage(context),
          child: const Text("Send NFC Message"),
        ),
      ),
    );
  }
}
