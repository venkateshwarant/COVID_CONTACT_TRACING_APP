package cash.z.ecc.android.sdk.demoapp.pojo;

public class User {
    public String viewingKey;
    public String saplingKeyAddress;
    public String spendingKey;
    public byte[] seedByteArr;

    public String getViewingKey() {
        return viewingKey;
    }

    public void setViewingKey(String viewingKey) {
        this.viewingKey = viewingKey;
    }

    public String getSaplingKeyAddress() {
        return saplingKeyAddress;
    }

    public void setSaplingKeyAddress(String saplingKeyAddress) {
        this.saplingKeyAddress = saplingKeyAddress;
    }

    public String getSpendingKey() {
        return spendingKey;
    }

    public void setSpendingKey(String spendingKey) {
        this.spendingKey = spendingKey;
    }

    public byte[] getSeedByteArr() {
        return seedByteArr;
    }

    public void setSeedByteArr(byte[] seedByteArr) {
        this.seedByteArr = seedByteArr;
    }
}
