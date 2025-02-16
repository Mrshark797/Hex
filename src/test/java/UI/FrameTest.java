package UI;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FrameTest {

    @Test
    public void testInterpretData_2Bytes_returnsCorrectValue() {
        // Подготовка
        Frame frame = new Frame();
        byte[] data = {0x02, 0x01}; // 0x0102 = 258 (Little Endian)
        String type = "Int16 (signed 2 bytes)";

        // Выполнение
        Object result = frame.interpretData(data, type);

        // Проверки
        assertEquals((short) 258, result); // 258 как Short
    }

    @Test
    public void testInterpretData_invalidType_returnsNull() {
        // Подготовка
        Frame frame = new Frame();
        byte[] data = {0x01, 0x02};
        String type = "Invalid Type";

        // Выполнение
        Object result = frame.interpretData(data, type);

        // Проверки
        assertNull(result); // Или проверьте на исключение, в зависимости от вашей реализации
    }
}
