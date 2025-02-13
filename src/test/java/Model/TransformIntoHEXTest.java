package Model;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.table.DefaultTableModel;


public class TransformIntoHEXTest {

    private static File testFile;

    @BeforeAll
    static void setUp() throws IOException{
        testFile = new File("test.bin");
        try(FileWriter fw = new FileWriter(testFile)){
            fw.write("Какой-то текст для тестирования \n");
            fw.write("Вторая строчка текста для тестирования\n");
        }

    }
    @AfterAll
    static void tearDown(){
        if(testFile != null && testFile.exists()){
            testFile.delete();
        }
    }
    @Test
    public void testGetHexPage_validFileAndPageNumber_returnsTableModel() throws IOException {

        DefaultTableModel model = TransformIntoHEX.getHexPage(testFile, 0);
        assertNotNull(model);
        assertTrue(model.getRowCount()>0);
        assertTrue(model.getColumnCount()>0);
    }

    @Test
    public void testGetHexPage_invalidFile_throwsIOException(){
        File file = new File("nonexistent.bin");

        assertThrows(IOException.class, () -> TransformIntoHEX.getHexPage(file, 0));
    }
}
