package ml.guru.barcodescanner.model;

public class Items {
    private String itemId;
    private Boolean present;
    private Integer count;

    public Items(Boolean present, Integer count) {
        this.present = present;
        this.count = count;
    }

    public Items(String itemId, Boolean present, Integer count) {
        this.itemId = itemId;
        this.present = present;
        this.count = count;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Boolean getPresent() {
        return present;
    }

    public void setPresent(Boolean present) {
        this.present = present;
    }
}
