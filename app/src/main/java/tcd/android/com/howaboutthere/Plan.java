package tcd.android.com.howaboutthere;

/**
 * Created by ADMIN on 07/05/2017.
 */

public class Plan {
    private String mName;
    private String mAddress;
    private String mDatetime;
    private int mNumPerson;
    private int mGoing;
    private int mBusy;

    public Plan(String name, String address, String dateTime, int numPerson, int going, int busy) {
        this.mName = name;
        this.mAddress = address;
        this.mDatetime = dateTime;
        this.mNumPerson = numPerson;
        this.mGoing = going;
        this.mBusy = busy;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getDatetime() {
        return mDatetime;
    }

    public void setDatetime(String mDatetime) {
        this.mDatetime = mDatetime;
    }

    public int getNumPerson() {
        return mNumPerson;
    }

    public void setNumPerson(int mNumPerson) {
        this.mNumPerson = mNumPerson;
    }

    public int getGoing() {
        return mGoing;
    }

    public void setGoing(int mGoing) {
        this.mGoing = mGoing;
    }

    public int getBusy() {
        return mBusy;
    }

    public void setBusy(int mBusy) {
        this.mBusy = mBusy;
    }
}
