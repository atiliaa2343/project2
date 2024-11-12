package quadtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** 
 * This class represents a quadtree data structure that paritions a 
 * two-dimensionl space by recursively subdividing it into four qudrants.
 * 
 * 
 * */
public class QuadTree {
    private Node root;
    private static final int MAX_CAPACITY = 5;
    private static final int MAX_HEIGHT = 20;

    /** 
     * This constructs new quadtree with the specified boundaries.
     * 
     * @param x The x-coordinate of the top-left corner of the box. 
     * @param y The y-coordinate of the top-left corner of the box. 
     * 
     * @param width The width of the quadtree's bounding box. 
     * @param height The height of the quadtree's bounding box.
     * 
     * 
     * */
    public QuadTree(double x, double y, double width, double height) {
        root = new Node(x, y, width, height);
    } 
    
    /**
     * Represents a node in the QuadTree. 
     * Each node can either be a leaf node containing rectangles, 
     * or an internal ndoe with four child nodes.
     * 
     * */

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
     * */
    public class Rectangle {
        double x, y, width, height;

        public Rectangle(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    /** 
     * Inserts a new rectangle into the quadtree.
     * 
     * @param x The x-coordinate of the top-left corner of the rectangle. 
     * @param y The y-coordinate of the top-left corner of the rectangle. 
     * 
     * @param width The width of the rectangle. 
     * @param height The height of the rectangle.
     * @return true if the rectangle was inserted, false otherwise.
     * */
    public boolean insert(double x, double y, double width, double height) {
        if (find(x, y) != null) {
            System.out.printf("You can not double insert at a position (%.2f, %.2f).\n", x, y);
            System.exit(1);
        }
        Rectangle rectangle = new Rectangle(x, y, width, height);
        boolean inserted = insert(root, rectangle);
        if (inserted) {
            System.out.printf("Rectangle inserted at (%.2f, %.2f): %.2fx%.2f\n", x, y, width, height);
        }
        return inserted;
    } 
    
    /**
     * Recursively inserts a rectangle into the QuadTree. 
     * 
     * @param node The current node in the QuadTree.
     * @param rectangle The rectangle to be inserted.
     * @returns true if the rectangle was inserted.
     * 
     * 
     * */

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

    /**
     * Subdivides a leaf node into four child nodes. 
     * This method is called when a leaf node reaches it cpacity and needs to be split. 
     * 
     * @param node The leaf node to be subdivided.
     * 
     * 
     * */
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
     * 
     * @param width The width of the rectangle. 
     * @param height The height of the rectangle.
     * @return true if the rectangle was removed, false otherwise.
     * */
    public boolean remove(double x, double y) {
        Rectangle toRemove = find(x, y);
        if (toRemove == null) {
            System.out.printf("Nothing to delete at [%.2f], [%.2f]\n", x, y);
            return false;
        }
        boolean removed = remove(root, x, y);
        if (removed) {
            System.out.printf("Rectangle deleted at (%.2f, %.2f): %.2fx%.2f\n", x, y, toRemove.width, toRemove.height);
        }
        return removed;
    } 
    
    /**
     * Recursively removes a rectangle from the QuadTree based on its top-left corner coordinates. 
     * 
     * @param node The current node in the QuadTree. 
     * @param x The x-coordinate of the rectangle's top-left corner to be removed. 
     * @param y The y-coordinate fo the rectangle's top-left corner to be removed. 
     * @return true if a rectangle was successfully removed, false otherwise.
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

        if (remove(node.NW, x, y)) return true;
        if (remove(node.NE, x, y)) return true;
        if (remove(node.SW, x, y)) return true;
        if (remove(node.SE, x, y)) return true;

        return false;
    } 
    
    /** 
     * Finds a rectangle at the specified coordinates.
     * 
     * @param x The x-coordinate to search for 
     * @param y The y-coordinate to search for. 
     * 
     * @return The rectangle object if found.
     * */

    public Rectangle find(double x, double y) {
        return find(root, x, y);
    } 
    
    /**
     * Recursively searches for a rectangle in the QuadTree based on its top-left corner coordinates.
     *
     * @param node The current node in the QuadTree.
     * @param x    The x-coordinate of the rectangle's top-left corner to find.
     * @param y    The y-coordinate of the rectangle's top-left corner to find.
     * @return The Rectangle object if found, null otherwise.
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
     * Updates a rectangle in the quadtree.
     * 
     * @param x The x-coordinate of the rectangle to update. 
     * @param y The y-coordinate of the rectangle to update. 
     * 
     * @param newWidth The new width of the rectangle. 
     * @param newHeight The new height of the rectangle.
     * @return true if the rectangle was updated, false otherwise.
     * */

    public boolean update(double x, double y, double newWidth, double newHeight) {
        Rectangle r = find(x, y);
        if (r == null) {
            System.out.printf("Nothing to update at [%.2f], [%.2f].\n", x, y);
            System.exit(1);
        }
        r.width = newWidth;
        r.height = newHeight;
        System.out.printf("Rectangle updated at (%.2f, %.2f): %.2fx%.2f\n", x, y, newWidth, newHeight);
        return true;
    }

    /** 
     * Prints a representation of the quadtree structure
     * 
     * */
    public void dump() {
        System.out.println("QuadTree Dump:");
        dump(root, 0);
    } 
    
    /**
     * Recursively prints the structure of the QuadTree, including all nodes and rectangles.
     *
     * @param node  The current node in the QuadTree.
     * @param level The current depth level in the tree, used for indentation.
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
     * Checks if a rectangle intersects with the area covered by a node.
     *
     * @param node The node in the QuadTree.
     * @param r    The rectangle to check for intersection.
     * @return true if the rectangle intersects with the node's area, false otherwise.
     */

    private boolean intersects(Node node, Rectangle r) {
        return !(r.x > node.x + node.width || r.x + r.width < node.x ||
                 r.y > node.y + node.height || r.y + r.height < node.y);
    } 
    
    /**
     * This is the main method used to run the program. 
     * 
     * @param args Comman line arguments
     * */

    public static void main(String[] args) {
        QuadTree quadTree = new QuadTree(-50, -50, 100, 100);
        Scanner scanner = new Scanner(System.in);

        System.out.println("QuadTree initialized. Enter commands (type 'exit' to quit):");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            processCommand(quadTree, input);
        }

        scanner.close();
        System.out.println("QuadTree program terminated.");
    }

    private static void processCommand(QuadTree quadTree, String command) {
        String[] parts = command.split("\\s+");
        try {
            switch (parts[0].toLowerCase()) {
                case "insert":
                    if (parts.length != 5) throw new IllegalArgumentException("Insert command requires 4 parameters");
                    quadTree.insert(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
                                    Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
                    break;
                case "remove":
                case "delete":
                    if (parts.length != 3) throw new IllegalArgumentException("Remove/Delete command requires 2 parameters");
                    quadTree.remove(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                    break;
                case "find":
                    if (parts.length != 3) throw new IllegalArgumentException("Find command requires 2 parameters");
                    Rectangle found = quadTree.find(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                    if (found != null) {
                        System.out.printf("Found: Rectangle at (%.2f, %.2f): %.2fx%.2f\n", 
                                          found.x, found.y, found.width, found.height);
                    } else {
                        System.out.printf("Nothing is at [%.2f], [%.2f].\n", 
                                          Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                        System.exit(1);
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