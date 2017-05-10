package tcd.android.com.howaboutthere;

/**
 * Created by ADMIN on 07/05/2017.
 */

public class FriendInfo {
    private String name;
    private String id;

    public FriendInfo(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
