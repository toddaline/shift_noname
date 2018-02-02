package noname.shift.getmoney.models;

public class Contact {
        private String name = "";
        private String phone = "";
        private boolean isChecked = false;

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setChecked(int checked) {
        if (checked == 0) {
            isChecked = false;
        } else {
            isChecked = true;
        }
    }

}
