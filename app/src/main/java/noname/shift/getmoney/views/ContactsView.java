package noname.shift.getmoney.views;

import java.util.ArrayList;

import noname.shift.getmoney.models.Contact;


public interface ContactsView {
    void goTargetContact();
    void setVisibility();
    void showMessage(String message);
    void resetAdapter(ArrayList<Contact> items);
}
