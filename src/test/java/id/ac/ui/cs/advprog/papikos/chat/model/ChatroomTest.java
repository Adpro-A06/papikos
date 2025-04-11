import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomTest {

    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        chatRoom = new ChatRoom();
        chatRoom.setBuyerId(1L);
        chatRoom.setSellerId(2L);
        chatRoom.setUnitId(100L);
        chatRoom.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testChatRoomFields() {
        assertEquals(1L, chatRoom.getBuyerId());
        assertEquals(2L, chatRoom.getSellerId());
        assertEquals(100L, chatRoom.getUnitId());
        assertNotNull(chatRoom.getCreatedAt());
    }

    @Test
    void testMessagesCanBeAddedToChatRoom() {
        Message msg = new Message();
        msg.setContent("Hai! Unitnya masih tersedia?");
        msg.setSenderId(1L);
        msg.setReceiverId(2L);

        chatRoom.setMessages(List.of(msg));

        assertEquals(1, chatRoom.getMessages().size());
        assertEquals("Hai! Unitnya masih tersedia?", chatRoom.getMessages().get(0).getContent());
    }
}