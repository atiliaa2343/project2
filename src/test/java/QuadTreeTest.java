package test.java;

import quadtree.QuadTree;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuadTreeTest {

    private QuadTree quadTree;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        // Redirect standard output to capture console output for assertions
        System.setOut(new PrintStream(outputStream));
        quadTree = new QuadTree(-50, -50, 100, 100);
    }

    @After
    public void tearDown() {
        // Restore the original standard output
        System.setOut(originalOut);
    }

    @Test
    public void testInsert() {
        assertTrue("Insert failed for (10, 10).", quadTree.insert(10, 10, 5, 5));
        assertTrue("Insert failed for (-10, -10).", quadTree.insert(-10, -10, 5, 5));
        assertFalse("Double insert allowed for (10, 10).", quadTree.insert(10, 10, 5, 5));
    }

    @Test
    public void testFind() {
        quadTree.insert(10, 10, 5, 5);
        QuadTree.Rectangle found = quadTree.find(10, 10);
        assertNotNull("Rectangle not found at (10, 10).", found);
        assertEquals("Incorrect x-coordinate.", 10, found.getX(), 0.001);
        assertEquals("Incorrect y-coordinate.", 10, found.getY(), 0.001);
        assertEquals("Incorrect width.", 5, found.getWidth(), 0.001);
        assertEquals("Incorrect height.", 5, found.getHeight(), 0.001);

        assertNull("Non-existent rectangle found.", quadTree.find(-20, -20));
    }

    @Test
    public void testDelete() {
        quadTree.insert(10, 10, 5, 5);
        assertTrue("Failed to delete rectangle at (10, 10).", quadTree.remove(10, 10));
        assertNull("Rectangle not deleted properly.", quadTree.find(10, 10));
        assertFalse("Deleted non-existent rectangle.", quadTree.remove(10, 10));
    }

    @Test
    public void testUpdate() {
        quadTree.insert(10, 10, 5, 5);
        assertTrue("Failed to update rectangle at (10, 10).", quadTree.update(10, 10, 8, 8));
        QuadTree.Rectangle updated = quadTree.find(10, 10);
        assertEquals("Width not updated.", 8, updated.getWidth(), 0.001);
        assertEquals("Height not updated.", 8, updated.getHeight(), 0.001);
        assertFalse("Updated non-existent rectangle.", quadTree.update(-10, -10, 5, 5));
    }

    @Test
    public void testDump() {
        quadTree.insert(10, 10, 5, 5);
        quadTree.insert(-10, -10, 8, 8);
        quadTree.dump();

        String dumpOutput = outputStream.toString();
        assertTrue("Dump output missing rectangle at (10, 10).", dumpOutput.contains("Rectangle at (10.00, 10.00): 5.00x5.00"));
        assertTrue("Dump output missing rectangle at (-10, -10).", dumpOutput.contains("Rectangle at (-10.00, -10.00): 8.00x8.00"));
    }
}
