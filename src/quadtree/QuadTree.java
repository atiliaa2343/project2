package quadtree;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** 
 * This class represents a quadtree data structure that partitions a 
 * two-dimensional space by recursively subdividing it into four quadrants.
 */
public class QuadTree {
    private Node root;
    private static final int MAX_CAPACITY = 5;
    private static final int MAX_HEIGHT = 20;

    /** 
     * Constructs a new quadtree with the specified boundaries.
     * 
     * @param x The x-coordinate of the top-left corner of the box. 
     * @param y The y-coordinate of the top-left corner of the box. 
     * @param width The width of the quadtree's bounding box. 
     * @param height The height of the quadtree's bounding box.
     */
    public QuadTree(double x, double y, double width, double height) {
        root = new Node(x, y, width, height);
    }

    /**
     * Represents a node in the QuadTree. 
     * Each node can either be a leaf node containing rectangles, 
     * or an internal node with four child nodes.
     */
    private class Node {
        double x, y, width, height;
        List<Rectangle> rectangles;
        Node NW, NE, SE, SW;
        int nodeHeight;

        Node(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.rectangles = new ArrayList<>();
            this.nodeHeight = 1;
        }

        boolean isLeaf() {
            return NW == null && NE == null && SE == null && SW == null;
        }
    }

    /**
     * Represents a rectangle in the QuadTree.
     */
    public class Rectangle {
        double x, y, width, height;

        public Rectangle(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        } 
        
        // Getter methods for encapsulated fields
        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }
    }

    /** 
     * Inserts a new rectangle into the quadtree.
     * 
     * @param x The x-coordinate of the top-left corner of the rectangle. 
     * @param y The y-coordinate of the top-left corner of the rectangle. 
     * @param width The width of the rectangle. 
     * @param height The height of the rectangle.
     * @return true if the rectangle was inserted, false otherwise.
     */
    public boolean insert(double x, double y, double width, double height) {
        if (find(x, y) != null) {
            System.out.printf("You cannot double insert at position (%.2f, %.2f).\n", x, y);
            System.exit(1);
        }
        Rectangle rectangle = new Rectangle(x, y, width, height);
        boolean inserted = insert(root, rectangle);
        if (inserted) {
            System.out.printf("Rectangle inserted at (%.2f, %.2f): %.2fx%.2f\n", x, y, width, height);
        }
        return inserted;
    }

    private boolean insert(Node node, Rectangle rectangle) {
        if (!intersects(node, rectangle)) {
            return false;
        }

        if (node.isLeaf() && node.rectangles.size() < MAX_CAPACITY) {
            node.rectangles.add(rectangle);
            return true;
        }

        if (node.isLeaf() && node.nodeHeight < MAX_HEIGHT) {
            subdivide(node);
        }

        if (node.isLeaf()) {
            node.rectangles.add(rectangle);
            return true;
        }

        if (insert(node.NW, rectangle)) return true;
        if (insert(node.NE, rectangle)) return true;
        if (insert(node.SW, rectangle)) return true;
        if (insert(node.SE, rectangle)) return true;

        return false;
    }

    private void subdivide(Node node) {
        double w = node.width / 2;
        double h = node.height / 2;

        node.NW = new Node(node.x, node.y, w, h);
        node.NE = new Node(node.x + w, node.y, w, h);
        node.SW = new Node(node.x, node.y + h, w, h);
        node.SE = new Node(node.x + w, node.y + h, w, h);

        node.NW.nodeHeight = node.NE.nodeHeight = node.SW.nodeHeight = node.SE.nodeHeight = node.nodeHeight + 1;

        for (Rectangle r : node.rectangles) {
            insert(node.NW, r);
            insert(node.NE, r);
            insert(node.SW, r);
            insert(node.SE, r);
        }
        node.rectangles.clear();
    }

    /** 
     * Removes a rectangle from the quadtree.
     * 
     * @param x The x-coordinate of the top-left corner of the rectangle. 
     * @param y The y-coordinate of the top-left corner of the rectangle. 
     * @return true if the rectangle was removed, false otherwise.
     */
    public boolean remove(double x, double y) {
        Rectangle toRemove = find(x, y);
        if (toRemove == null) {
            System.out.printf("Nothing to delete at (%.2f, %.2f).\n", x, y);
            System.exit(1);
        }
        boolean removed = remove(root, x, y);
        if (removed) {
            System.out.printf("Rectangle deleted at (%.2f, %.2f): %.2fx%.2f\n", x, y, toRemove.width, toRemove.height);
        }
        return removed;
    }
 
    /**
     * Removes a rectangle from the QuadTree based on its coordinates. 
     * 
     * @param node The current node being examined. 
     * @param x The x-coordinate of the rectangle to remove. 
     * @param y The y-coordinate of the rectangle to remove. 
     * @return true if a rectangle was removed
     * 
     * */
    private boolean remove(Node node, double x, double y) {
        if (node == null) return false;

        if (node.isLeaf()) {
            for (int i = 0; i < node.rectangles.size(); i++) {
                Rectangle r = node.rectangles.get(i);
                if (r.x == x && r.y == y) {
                    node.rectangles.remove(i);
                    return true;
                }
            }
            return false;
        }

        if (remove(node.NW, x, y)) checkAndRevertToLeaf(node);
        if (remove(node.NE, x, y)) checkAndRevertToLeaf(node);
        if (remove(node.SW, x, y)) checkAndRevertToLeaf(node);
        if (remove(node.SE, x, y)) checkAndRevertToLeaf(node);

        return false;
    }
    
    /**
     * Checks if a node can be converted back to a leaf node after removal. 
     * If the total number of rectangles in all child nodes is less than or equal to MAX_CAPACITY, 
     * the node is converted back to a leaf node.
     * 
     * @param node The node to check and potentially convert.
     * 
     * */

    private void checkAndRevertToLeaf(Node node) {
        if (node.isLeaf()) return;

        List<Rectangle> allRectangles = new ArrayList<>();
        collectAllRectangles(node.NW, allRectangles);
        collectAllRectangles(node.NE, allRectangles);
        collectAllRectangles(node.SW, allRectangles);
        collectAllRectangles(node.SE, allRectangles);

        if (allRectangles.size() <= MAX_CAPACITY) {
            node.rectangles = allRectangles;
            node.NW = node.NE = node.SW = node.SE = null;
        }
    }

    /**
     * Recursively collects all rectangles from a node and its children.
     * 
     * @param node The current node being examined. 
     * @param allRectangles The list to store all collected rectangles.
     * 
     * 
     * */
    private void collectAllRectangles(Node node, List<Rectangle> allRectangles) {
        if (node == null) return;

        if (node.isLeaf()) {
            allRectangles.addAll(node.rectangles);
        } else {
            collectAllRectangles(node.NW, allRectangles);
            collectAllRectangles(node.NE, allRectangles);
            collectAllRectangles(node.SW, allRectangles);
            collectAllRectangles(node.SE, allRectangles);
        }
    } 
    
    /**
     * Finds a rectangle in the QuadTree based on its coordinates. 
     * 
     * @param x The x-coordinate of the rectangle to find. 
     * @param y The y-coordinate of the rectangle to find. 
     * @return The found Rectangle object, or null if not found.
     * 
     * 
     * */

    public Rectangle find(double x, double y) {
        return find(root, x, y);
    }
    
    /**
     * Recursive helper method for finding a rectangle.
     *
     * @param node The current node being examined.
     * @param x The x-coordinate of the rectangle to find.
     * @param y The y-coordinate of the rectangle to find.
     * @return The found Rectangle object, or null if not found.
     */

    private Rectangle find(Node node, double x, double y) {
        if (node == null) return null;

        if (node.isLeaf()) {
            for (Rectangle r : node.rectangles) {
                if (r.x == x && r.y == y) {
                    return r;
                }
            }
            return null;
        }

        if (x < node.x + node.width / 2) {
            if (y < node.y + node.height / 2) return find(node.NW, x, y);
            else return find(node.SW, x, y);
        } else {
            if (y < node.y + node.height / 2) return find(node.NE, x, y);
            else return find(node.SE, x, y);
        }
    }

    /**
     * Updates the dimensions of a rectangle in the QuadTree.
     *
     * @param x The x-coordinate of the rectangle to update.
     * @param y The y-coordinate of the rectangle to update.
     * @param newWidth The new width for the rectangle.
     * @param newHeight The new height for the rectangle.
     * @return true if the rectangle was updated, false if not found.
     */
    public boolean update(double x, double y, double newWidth, double newHeight) {
        Rectangle rectangle = find(x, y);
        if (rectangle == null) {
            System.out.printf("Nothing to update at (%.2f, %.2f).\n", x, y);
            return false;
        }
        rectangle.width = newWidth;
        rectangle.height = newHeight;
        System.out.printf("Rectangle updated at (%.2f, %.2f): %.2fx%.2f\n", x, y, newWidth, newHeight);
        return true;
    }
    
    /**
     * Prints a hierarchical representation of the QuadTree structure.
     */

    public void dump() {
        System.out.println("QuadTree Dump:");
        dump(root, 0);
    }
    
    /**
     * Recursive helper method for dumping the QuadTree structure.
     *
     * @param node The current node being examined.
     * @param level The current depth level in the tree.
     */


    private void dump(Node node, int level) {
        if (node == null) return;

        String indent = "    ".repeat(level);
        String nodeType = node.isLeaf() ? "Leaf Node" : "Internal Node";
        System.out.printf("%s%s - Rectangle at (%.2f, %.2f): %.2fx%.2f\n",
                          indent, nodeType, node.x, node.y, node.width, node.height);

        if (node.isLeaf()) {
            for (Rectangle r : node.rectangles) {
                System.out.printf("%s    Rectangle at (%.2f, %.2f): %.2fx%.2f\n",
                                  indent, r.x, r.y, r.width, r.height);
            }
        } else {
            dump(node.NW, level + 1);
            dump(node.NE, level + 1);
            dump(node.SW, level + 1);
            dump(node.SE, level + 1);
        }
    }
    
    /**
     * Checks if a rectangle intersects with a node's bounding box.
     *
     * @param node The node to check for intersection.
     * @param r The rectangle to check for intersection.
     * @return true if the rectangle intersects with the node's bounding box, false otherwise.
     */

    private boolean intersects(Node node, Rectangle r) {
        return !(r.x > node.x + node.width || r.x + r.width < node.x ||
                 r.y > node.y + node.height || r.y + r.height < node.y);
    }
    
    /**
     * Main method to run the QuadTree from command-line arguments.
     *
     * @param args Command-line arguments. Expects a single argument with the path to a command file.
     */

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java QuadTree <command_file>");
            System.exit(1);
        }

        String filePath = args[0];
        QuadTree quadTree = new QuadTree(-50, -50, 100, 100);

        try (Scanner fileScanner = new Scanner(new java.io.File(filePath))) {
            System.out.println("Processing commands from file: " + filePath);

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    processCommand(quadTree, line);
                }
            }

            System.out.println("Finished processing commands.");
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Error: File not found - " + filePath);
            System.exit(1);
        }
    } 
    
    /**
     * Processes a single command for the QuadTree.
     *
     * @param quadTree The QuadTree instance to operate on.
     * @param command The command string to process.
     */

    private static void processCommand(QuadTree quadTree, String command) {
        // Remove trailing semicolons, if present
        command = command.replaceAll(";$", "").trim();

        String[] parts = command.split("\\s+");
        try {
            switch (parts[0].toLowerCase()) {
                case "insert":
                    if (parts.length != 5) throw new IllegalArgumentException("Insert command requires 4 parameters");
                    quadTree.insert(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
                                    Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
                    break;
                case "delete":
                    if (parts.length != 3) throw new IllegalArgumentException("Delete command requires 2 parameters");
                    quadTree.remove(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                    break;
                case "find":
                    if (parts.length != 3) throw new IllegalArgumentException("Find command requires 2 parameters");
                    Rectangle found = quadTree.find(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                    if (found != null) {
                        System.out.printf("Found: Rectangle at (%.2f, %.2f): %.2fx%.2f\n",
                                          found.x, found.y, found.width, found.height);
                    } else {
                        System.out.printf("Nothing is at (%.2f, %.2f).\n",
                                          Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                    }
                    break;
                case "update":
                    if (parts.length != 5) throw new IllegalArgumentException("Update command requires 4 parameters");
                    quadTree.update(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
                                    Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
                    break;
                case "dump":
                    quadTree.dump();
                    break;
                default:
                    System.out.println("Unknown command: " + parts[0]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number in command: " + command);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage() + " in command: " + command);
        }
    }
} 
