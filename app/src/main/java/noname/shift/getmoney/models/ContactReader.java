package noname.shift.getmoney.models;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by denis on 02.02.2018.
 */

public class ContactReader {

    private static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private static final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;

    public ArrayList<Contact> readAll(ContentResolver contentResolver) {
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor pCur = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{PHONE_NUMBER, PHONE_CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER},
                null,
                null,
                null
        );

        if (pCur != null) {
            if (pCur.getCount() > 0) {
                HashMap<String, ArrayList<String>> phones = new HashMap<>();
                while (pCur.moveToNext()) {
                    String contactName = pCur.getString(pCur.getColumnIndex(DISPLAY_NAME));
                    ArrayList<String> curPhones = new ArrayList<>();

                    if (phones.containsKey(contactName)) {
                        continue;
                    }

                    if (pCur.getInt(pCur.getColumnIndex(HAS_PHONE_NUMBER)) > 0) {
                        curPhones.add(pCur.getString(pCur.getColumnIndex(PHONE_NUMBER)));
                        Log.i("cur1", contactName);
                        Log.i("cur", pCur.getString(pCur.getColumnIndex(PHONE_NUMBER)));
                    }

                    phones.put(contactName, curPhones);
                    contacts.add(new Contact(contactName, curPhones.get(0)));
                }
                Collections.sort(contacts, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            }
            pCur.close();
            return contacts;
        } else{
            return null;
        }

    }
}
