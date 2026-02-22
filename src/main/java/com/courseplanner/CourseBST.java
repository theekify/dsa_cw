package com.courseplanner;

import java.util.*;


/**
 * CourseBST: Handles the Binary Search Tree logic for Course management.
 * Optimized for O(log n) search and insertion.
 * Contribution by: Hiruna
 */
public class CourseBST {
    
    private CourseNode root;// The starting point (root) of the Binary Search Tree

    private HashMap<String, CourseNode> courseMap;// Used for fast O(1) lookups by course code

    /**
     * Constructor initializes empty tree
     */
    public CourseBST() {
        this.root = null;
        this.courseMap = new HashMap<>();
    }

    /**
     * Public method to insert a course into the BST
     * @param course Course to insert
     */
    
   public void insert(Course course) {
    if (course == null || course.getCode() == null) {
        System.err.println("Error: Attempted to insert a null course.");
        return;
    }

    // Pass the course to the recursive logic
    root = insertRec(root, course);
    
    // Optimization: Instead of searching the tree again, 
    // we leverage the fact that insertion is complete.
    // Note: To make this even faster in the future, we could 
    // modify insertRec to return the created node.
    CourseNode node = searchNode(course.getCode());
    if (node != null) {
        courseMap.put(course.getCode(), node);
    }
}

    /**
     * Recursive helper for insertion with AVL balancing
     * @param node Current node in recursion
     * @param course Course to insert
     * @return Updated node after insertion and balancing
     */
    private CourseNode insertRec(CourseNode node, Course course) {
        // Base case: create new node
        if (node == null) {
            return new CourseNode(course);
        }

        // Compare course codes alphabetically
        String courseCode = course.getCode();
        String nodeCode = node.getCourse().getCode();

        if (courseCode.compareTo(nodeCode) < 0) {
            // Insert in left subtree
            node.setLeft(insertRec(node.getLeft(), course));
        } else if (courseCode.compareTo(nodeCode) > 0) {
            // Insert in right subtree
            node.setRight(insertRec(node.getRight(), course));
        } else {
            // Course already exists
            return node;
        }

        // Update height of current node
        updateHeight(node);

        // Check balance factor and perform rotations if needed
        return balanceNode(node, courseCode);
    }

   /**
 * Search for a course by code using HashMap (O(1)).
 * Optimization: Added case-insensitivity to handle varied user input.
 * @param code Course code to search
 * @return Course object if found, null otherwise
 */
public Course search(String code) {
    if (code == null) return null;
    
    // Convert input to uppercase to match the standard format stored in the map
    CourseNode node = courseMap.get(code.toUpperCase().trim());
    return (node != null) ? node.getCourse() : null;
}

    
    public List<Course> inOrderTraversal() {
        List<Course> courses = new ArrayList<>();
        inOrderRec(root, courses);
        return courses;
    }

    /**
     * Recursive in-order traversal
     * @param node Current node
     * @param courses List to store courses
     */
    private void inOrderRec(CourseNode node, List<Course> courses) {
        if (node != null) {
            // Traverse left subtree
            inOrderRec(node.getLeft(), courses);

            // Visit current node
            courses.add(node.getCourse());

            // Traverse right subtree
            inOrderRec(node.getRight(), courses);
        }
    }

    /**
     * Get height of a node
     * @param node Node to get height of
     * @return Height value, 0 if node is null
     */
    private int height(CourseNode node) {
        return (node == null) ? 0 : node.getHeight();
    }

    /**
     * Update height of a node based on children
     * @param node Node to update
     */
    private void updateHeight(CourseNode node) {
        if (node != null) {
            int leftHeight = height(node.getLeft());
            int rightHeight = height(node.getRight());
            node.setHeight(1 + Math.max(leftHeight, rightHeight));
        }
    }

    /**
     * Get balance factor of a node
     * @param node Node to check
     * @return Balance factor (left height - right height)
     */
    private int getBalance(CourseNode node) {
        if (node == null) return 0;
        return height(node.getLeft()) - height(node.getRight());
    }

    /**
     * Perform right rotation (AVL balancing)
     * @param y Node to rotate around
     * @return New root after rotation
     */
    private CourseNode rightRotate(CourseNode y) {
        CourseNode x = y.getLeft();
        CourseNode T2 = x.getRight();

        // Perform rotation
        x.setRight(y);
        y.setLeft(T2);

        // Update heights
        updateHeight(y);
        updateHeight(x);

        return x;
    }

    /**
     * Perform left rotation (AVL balancing)
     * @param x Node to rotate around
     * @return New root after rotation
     */
    private CourseNode leftRotate(CourseNode x) {
        CourseNode y = x.getRight();
        CourseNode T2 = y.getLeft();

        // Perform rotation
        y.setLeft(x);
        x.setRight(T2);

        // Update heights
        updateHeight(x);
        updateHeight(y);

        return y;
    }

    /**
     * Balance a node using AVL rotations
     * @param node Node to balance
     * @param insertedCode Code of newly inserted course
     * @return Balanced node
     */
    private CourseNode balanceNode(CourseNode node, String insertedCode) {
        int balance = getBalance(node);
        String nodeCode = node.getCourse().getCode();

        // Left Left Case
        if (balance > 1 && insertedCode.compareTo(node.getLeft().getCourse().getCode()) < 0) {
            return rightRotate(node);
        }

        // Right Right Case
        if (balance < -1 && insertedCode.compareTo(node.getRight().getCourse().getCode()) > 0) {
            return leftRotate(node);
        }

        // Left Right Case
        if (balance > 1 && insertedCode.compareTo(node.getLeft().getCourse().getCode()) > 0) {
            node.setLeft(leftRotate(node.getLeft()));
            return rightRotate(node);
        }

        // Right Left Case
        if (balance < -1 && insertedCode.compareTo(node.getRight().getCourse().getCode()) < 0) {
            node.setRight(rightRotate(node.getRight()));
            return leftRotate(node);
        }

        return node;
    }

    /**
     * Search for a node by course code (used internally)
     * @param code Course code to search
     * @return CourseNode if found, null otherwise
     */
    private CourseNode searchNode(String code) {
        return searchNodeRec(root, code);
    }

    /**
     * Recursive helper for node search
     */
    private CourseNode searchNodeRec(CourseNode node, String code) {
        if (node == null || node.getCourse().getCode().equals(code)) {
            return node;
        }

        if (code.compareTo(node.getCourse().getCode()) < 0) {
            return searchNodeRec(node.getLeft(), code);
        }

        return searchNodeRec(node.getRight(), code);
    }

    /**
     * Get the course map (for other classes to use)
     * @return HashMap of course codes to nodes
     */
    public HashMap<String, CourseNode> getCourseMap() {
        return courseMap;
    }

    /**
     * Display tree structure (for debugging)
     */
    public void displayTree() {
        System.out.println("\nBST Structure:");
        displayTreeRec(root, "", true);
    }

    private void displayTreeRec(CourseNode node, String indent, boolean last) {
        if (node != null) {
            System.out.print(indent);
            if (last) {
                System.out.print("└── ");
                indent += "    ";
            } else {
                System.out.print("├── ");
                indent += "│   ";
            }
            System.out.println(node.getCourse().getCode() + " (h=" + node.getHeight() + ")");
            displayTreeRec(node.getLeft(), indent, false);
            displayTreeRec(node.getRight(), indent, true);
        }
    }

}

