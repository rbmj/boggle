#ifndef TRIE_H_INC
#define TRIE_H_INC

#include <vector>
#include <string>
#include <memory>
#include "optional.h"

/** A prefix Trie.
 * Note:  This Trie will only work on strings "[A-Z]*".  Any
 * other characters will cause failure conditions.
 *
 * This Trie is built to be fast, so it has minimal error
 * checking.  If you send it bad input you'll probably segfault.
 */
class Trie {
private:
    /** A low level struct with some helpers for use by Trie. */
    struct Node {

        /** This node's parent (null if root) */
        Node* parent;

        /** The character at this node */
        char char_here;

        /** Whether this node represents a string in the Trie */
        bool value_here;

        /** The child node array */
        //TODO:  Might be faster to actually have 26 children in line so
        //we don't have the extra indirection, at the cost of some code.
        //need to benchmark for that though.
        std::unique_ptr<Node> children[26];

        /** Create a node with a given character character and parent
         * @param c The character at this node
         * @param p The parent of this node
         */
        Node(char c, Node* p);

        /** Get the index corresponding to a character into the child
         *  array
         *  @param c The character key
         *  @return i The index into the array
         */ 
        static int index(char c);

        /** Get the proper child node corresponding to a character,
         *  creating the node if necessary.
         *  @param c The character key.
         */
        Node* get(char c);
        
        /** Get the string representaiton of this node, starting at the 
         * root.
         * @return The string representing the string at this node
         */
        const std::string& toString();
        
        /** Set the cached string that corresponds to this node.
         * Note:  This function does not check if s actually corresponds
         * to this node.  It trusts you - don't abuse its trust.
         * @param s The string to set the cached toString value to.
         * @return The cache string, or null if not cached.
         */
        std::string* getCacheString();
        /** Set the cached string that corresponds to this node.
         * Note:  This function does not check if s actually corresponds
         * to this node.  It trusts you - don't abuse its trust.
         * @param s The string to set the cached toString value to.
         */
        void setCacheString(std::string s);

    private:
        
        /** The current node represented as a std::string, starting at the
         * root.  This might be null - but it's private.  The fact that
         * a string isn't stored at every node is encapsulated by calling
         * toString(), which will generate one if necessary.
         */
        optional<std::string> str;
        
        /** Recursive helper for toString().
         * @param sb The string so far (i.e. the prefix)
         * @param n The current node being visited
         */
        static void buildString(std::string& s, Node* n);
    };

public:
    /** Enum to allow checking both the membership of a key k
     * and the existance of any keys prefixed by k */
    enum SearchResult {
        FOUND,
        NOTFOUND,
        NOPREFIX
    };

    /** Iterator for searching a Trie */
    class SearchIterator {
    private:
        /** The current node being visited */
        Node* curNode;
    public:

        /** Check membership of the current location
         * @return True if this iterator represents a string in the set
         */
        bool inSet();

        /** Check reachability of any keys from the current node.
         * @return True if some sequence of keys moves this iterator
         *         to a valid string in the Trie
         */
        bool reachable();

        /** Descend to the next level in the Trie.
         * @param c The character to append/the path to descend
         * 
         * Note:  It is possible to fall off the bottom of the tree
         * with this method.  If that is the case reachable() will
         * return false.
         */ 
        void next(char c);
        
        /** Construct a SearchIterator.
         * @param The root of the Trie to search
         */
        SearchIterator(Node* n);

        /** Copy a SearchIterator.
         * @param si The SearchIterator to copy
         */
        SearchIterator(const SearchIterator& si);
        
        /** Get the string corresponding to the node pointed to by the
         * iterator.
         * Note:  This may not be all uppercase - see Trie.enqueue(std::vector)
         * for details.
         * @return The string.
         */
        const std::string& toString();

        /** The current character at this node in the Trie.
         * @return The character
         */
        char charHere();

        /** Ascend this iterator to the parent node.
         *
         * It is possible to fall off the top of the trie using this
         * method (i.e. if the iterator points to the root).  In that
         * case, reachable() will return false.
         */
        void up();
        
        /** Get the current cached string.  This may be null.
         * @return The internal cache.
         */
        const std::string* getCacheString();
    };

    /** Create a Trie */
    Trie();

    /** Gets a SearchIterator for this Trie.
     * @return The SearchIterator.
     */
    SearchIterator beginSearch();

    /** Insert a string into the trie.
     * @param s The string to insert
     */
    void insert(std::string s);
    
    /** Insert a string into the trie, converting the string to uppercase.
     * @param s The string to insert
     */
    void insertCase(std::string s);

    /** Insert a string into the trie, converting the string to uppercase.
     * This method does not cache the string internally.  This is useful
     * if you need SearchIterator.toString() or enqueue() to be strings
     * in all caps.
     * @param s The string to insert
     */
    void insertCase_nocache(const std::string& s);
    
    /** Insert the string indicated by a SearchIterator into another Trie
     * into this Trie.
     * @param it An iterator into another Trie.
     */
    void insertForeignIt(SearchIterator it);
    
    /** Determine if a string is in this Trie.
     * @param s The string to search for
     * @return SearchResult.{NOPREFIX, FOUND, NOTFOUND} for the string
     *
     * NOPREFIX implies that s is not a prefix to any string in the trie.
     * NOTFOUND implies that s may be a prefix to one or more strings in
     *      the Trie, but s itself is not in the Trie.
     * FOUND implies that the string is in fact in the Trie.
     */
    SearchResult find(const std::string& s);

    /** Determine if a string is in this Trie.
     * @param s The string to search for
     * @return True if s is in the Trie, false otherwise
     *
     * This is exactly the same as find(s) == FOUND.
     */
    bool get(const std::string& s);
    
    /** enqueue all of the elements of this Trie.
     * See enqueue(std::vector) for caveats.
     * @return A vector containing all of the elements of this Trie
     */
    std::vector<std::string> enqueue();

    /** enqueue all of the elements of this Trie into a given vector.
     * There is no guarantee that the strings will be all caps - even if
     * they are stored that way internally - iff insertCase() has been
     * called.  If you require this guarantee, you may:
     * <ul>
     *  <li> Use Trie.insertCase_nocache() instead of insertCase() </li>
     *  <li> Use Trie.insert(s.toUpperCase()) instead of insertCase() </li>
     *  <li> Use std::vector.{peek(), poll()}.toUpperCase() </li>
     *  <li> Use Trie.enqueue_nocache() instead of enqueue() </li>
     * </ul>
     * @param q The vector to enqueue all of the elements into
     */
    void enqueue(std::vector<std::string>& q);
    
    /** enqueue all of the elements of this Trie, not using the cache.
     * @return A vector containing all of the elements of this Trie
     */
    std::vector<std::string> enqueue_nocache();

    /** enqueue all of the elements of this Trie into a given vector.
     * This verison does not use the internal string cache.
     * @param q The vector to enqueue all of the elements into
     */
    void enqueue_nocache(std::vector<std::string>& q);

    /** Get the number of elements in this Trie.
     * @return The number of elements in the Trie.
     */
    int size();

private:

    /** The root of the Trie */
    Node root;

    /** The number of elements in this Trie */
    int m_size;

    /** Recursive helper for insert(s).
     * This is basic tree traversal - nothing new here.
     * @param n The current node visited
     * @param sci An iterator to the next char in the string
     * @return The node that was created/updated by this insertion
     */
    static Node* insert(Node* n, std::string::const_iterator it,
                        std::string::const_iterator end);


    /** Recursive helper for insertCase(s).
     * This is basic tree traversal - nothing new here.
     * @param n The current node visited
     * @param sci An iterator to the next char in the string
     * @return The node that was created/updated by this insertion
     */
    static Node* insertCase(Node* n, std::string::const_iterator it,
                            std::string::const_iterator end);
    
    /** Recursive helper for insertForeignIt(it).
     * @param it The iterator into the foreign Trie.
     * @param r The root node of the local Trie.
     * @param c The character indicating the proper child to traverse to
     *          from the node in the local tree corresponding to it.
     * @return The child of the node in the local tree corresponding to
     *         it.next(c)
     */
    static Node* insertForeignIt(SearchIterator it, Node* r, char c);

    /** Recursive helper for find(s).
     * @param n The current node being visited
     * @param sci An iterator into the current position in the string
     */
    static SearchResult find(Node* n, std::string::const_iterator it,
                             std::string::const_iterator end);
    
    /** Helper method for enqueue()
     * @param n The current node being visited
     * @param q The vector to add items to
     */
    static void enqueue(Node* n, std::vector<std::string>& q);

    /** Helper method for enqueue_nocache()
     * @param n The current node being visited
     * @param prefix The prefix associated with the current node
     * @param q The vector to add items to
     */
    static void enqueue_nocache(Node* n, std::string prefix, 
                                std::vector<std::string>& q); 
};

#endif
