import java.util.Date;

public class Message {
    private String text;
    private String senderId;
    private Date timestamp;

    public Message() {} // ضروري لـ Firestore

    public Message(String text, String senderId, Date timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getText() { return text; }
    public String getSenderId() { return senderId; }
    public Date getTimestamp() { return timestamp; }
}
