package noname.shift.getmoney.views;

import java.util.ArrayList;

import noname.shift.getmoney.models.Contact;


public interface ContactsView {
    public void goTargetContact();
    public void setVisibility();
    public void showMessage(String message);
    public void resetAdapter(ArrayList<Contact> items);
}
