package noname.shift.getmoney.data;

import android.provider.BaseColumns;

public class ListContract {

    private ListContract() {}

    public static final class ListEntry implements BaseColumns {

        public final static String TABLE_NAME = "contacts";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_PHONE = "phone_number";
        public final static String COLUMN_PAID = "paid";

        public final static Integer TRUE = 1;
        public final static Integer FALSE = 0;
    }
}
