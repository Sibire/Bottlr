package com.example.bottlr;

import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;

// Isolating NFC Functionality in compliance with some of the documentation I've read

public class NFCCode {
    public static void writeToTag(Tag tag, Context context) {
        // Adjust as needed, placeholder text for now
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{NdefRecord.createTextRecord(null, "Test Data")});

        try {
            // NDEF Format Verification
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                Log.d("NFC Code", "Tag Not Null, NDEF");
                ndef.connect();
                Log.d("NFC Code", "Tag Connected");
                if (ndef.isWritable()) {
                    ndef.writeNdefMessage(ndefMessage);
                    Log.d("NFC Code", "Tag Written");
                    Toast.makeText(context, "Write successful.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "NFC Tag is read-only.", Toast.LENGTH_LONG).show();
                }
                ndef.close();
            } else {
                // Try to format the tag with the NDEF message
                Ndef ndefFormattable = Ndef.get(tag);
                Log.d("NFC Code", "Tag Not Null, not NDEF");
                if (ndefFormattable != null) {
                    Log.d("NFC Code", "Tag NDEF Formattable");
                    ndefFormattable.connect();
                    Log.d("NFC Code", "Connected to Formattable Tag");
                    ndefFormattable.writeNdefMessage(ndefMessage);
                    Log.d("NFC Code", "Formatted and written.");
                    Toast.makeText(context, "Tag formatted and written.", Toast.LENGTH_LONG).show();
                    ndefFormattable.close();
                } else {
                    Log.d("NFC Code", "Tag does not support NDEF.");
                    Toast.makeText(context, "NFC Tag does not support NDEF.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException | FormatException e) {
            Toast.makeText(context, "Write error.", Toast.LENGTH_LONG).show();
            Log.d("NFC Code", "Unspecified Error, Check for Details");
            e.printStackTrace();
        }
    }
    public static void readFromTag() {
    //TODO: This
    }
}
