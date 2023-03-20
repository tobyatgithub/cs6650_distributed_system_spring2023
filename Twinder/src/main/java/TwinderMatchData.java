import java.sql.Timestamp;

public class TwinderMatchData {
    private final int id;
    private final int swiper;
    private final int swipee;
    private final String comment;
    private final String leftOrRight;
    private final Timestamp timestamp;

    public TwinderMatchData(int ID, int Swiper, int Swipee, String Comment, String LeftOrRight, Timestamp timestamp){
        this.id = ID;
        this.swiper = Swiper;
        this.swipee = Swipee;
        this.comment = Comment;
        this.leftOrRight = LeftOrRight;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getSwiper() {
        return swiper;
    }

    public int getSwipee() {
        return swipee;
    }

    public String getComment() {
        return comment;
    }

    public String getLeftOrRight() {
        return leftOrRight;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
