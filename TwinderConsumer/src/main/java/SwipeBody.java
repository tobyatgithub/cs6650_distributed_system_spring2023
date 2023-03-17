import com.google.gson.Gson;

public class SwipeBody {
    private int swiper;
    private int swipee;
    private String comment;
    private String leftOrRight;

    public int getSwiper() {
        return swiper;
    }
    public void setSwiper(int swiper) {
        this.swiper = swiper;
    }
    public int getSwipee() {
        return swipee;
    }
    public void setSwipee(int swipee) {
        this.swipee = swipee;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getLeftOrRight() {return leftOrRight;}
    public void setLeftOrRight(String leftOrRight) {this.leftOrRight = leftOrRight;}
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
