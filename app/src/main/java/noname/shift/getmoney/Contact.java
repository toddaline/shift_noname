package noname.shift.getmoney;

public class Contact {
        private String name = "";
        private String phone = "";
        private boolean isChecked = false;
        private int id;

    Contact(String name, String phone) {
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

    /*        public void setMyId(int my_id) {
            _my_id = my_id;
        }*/
}
