#include "Trie.h"
#include <ctype.h>

bool Trie::SearchIterator::inSet() {
    if (!curNode) {
        return false;
    }
    return curNode->value_here;
}

bool Trie::SearchIterator::reachable() {
    if (!curNode) {
        return false;
    }
    return true;
    //this is semantically more correct than return true,
    //but I don't know if it's worth the extra work.
    //Benchmark.
    /*
       for (Node * child : curNode->children) {
           if (child) return true;
       }
       return curNode->value_here;
     */
}

void Trie::SearchIterator::next(char c) {
    if (!curNode) {
        return;
    }
    curNode = curNode->children[Node::index(c)].get();
}

Trie::SearchIterator::SearchIterator(Node* n) {
    curNode = n;
}

Trie::SearchIterator::SearchIterator(const SearchIterator& si) {
    curNode = si.curNode;
}

const std::string& Trie::SearchIterator::toString() {
    return curNode->toString();
}

char Trie::SearchIterator::charHere() {
    return curNode->char_here;
}

void Trie::SearchIterator::up() {
    if (curNode) {
        curNode = curNode->parent;
    }
}

const std::string* Trie::SearchIterator::getCacheString() {
    return (curNode) ? curNode->getCacheString() : nullptr;
}
    
Trie::Trie() : root(0, nullptr), m_size(0) {}

Trie::SearchIterator Trie::beginSearch() {
    return SearchIterator(&root);
}

void Trie::insert(std::string s) {
    //insert the string, and while we're at it, since we have the
    //string, we might as well update the cache.
    insert(&root, s.begin(), s.end())->setCacheString(std::move(s));
    ++m_size;
}

void Trie::insertCase(std::string s) {
    //insert the string, and while we're at it, since we have the
    //string, we might as well update the cache.
    insertCase(&root, s.begin(), s.end())->setCacheString(std::move(s));
    ++m_size;
}

void Trie::insertCase_nocache(const std::string& s) {
    insertCase(&root, s.begin(), s.end());
    ++m_size;
}

void Trie::insertForeignIt(SearchIterator it) {
    Node* n = insertForeignIt(it, &root, (char) 0);
    n->value_here = true;
    const std::string* s = it.getCacheString();
    if (s) {
        n->setCacheString(*s);
    }
    ++m_size;
}

Trie::SearchResult Trie::find(const std::string& s) {
    return find(&root, s.begin(), s.end());
}

bool Trie::get(const std::string& s) {
    return find(s) == FOUND;
}

std::vector<std::string> Trie::enqueue() {
    std::vector<std::string> q;
    q.reserve(m_size);
    enqueue(q);
    return q;
}

void Trie::enqueue(std::vector<std::string>& q) {
    for (int i = 0; i < 26; ++i) {
        enqueue(root.children[i].get(), q);
    }
}

std::vector<std::string> Trie::enqueue_nocache() {
    std::vector<std::string> q;
    q.reserve(m_size);
    enqueue_nocache(q);
    return q;
}

void Trie::enqueue_nocache(std::vector<std::string>& q) {
    for (int i = 0; i < 26; ++i) {
        enqueue_nocache(root.children[i].get(), std::string(1, 'A' + i), q);
    }
}

int Trie::size() {
    return m_size;
}


Trie::Node::Node(char c, Node* p) {
    parent = p;
    char_here = c;
    value_here = false;
}

int Trie::Node::index(char c) {
    return (int) (c - 'A');
}

Trie::Node* Trie::Node::get(char c) {
    int i = index(c);
    Node* n = children[i].get();

    if (!n) {
        n = new Node(c, this);
        children[i].reset(n);
    }
    return n;
}

const std::string& Trie::Node::toString() {
    if (str) {
        return *str; //cached
    }
    std::string s;
    buildString(s, this);
    str.emplace(std::move(s));
    return *str;
}

std::string* Trie::Node::getCacheString() {
    return str ? &(*str) : nullptr;
}

void Trie::Node::setCacheString(std::string s) {
    str.emplace(std::move(s));
}

void Trie::Node::buildString(std::string& s, Node* n) {
    if (!n) {
        return;
    }
    char c = n->char_here;

    if (c == 0) {
        return;  //also done - root node may not have a valid char
    }
    buildString(s, n->parent);
    s.push_back(c);
}

Trie::Node* Trie::insert(Node* n, std::string::const_iterator it,
                   std::string::const_iterator end)
{
    if (it == end) {
        n->value_here = true;
        return n;
    }
    char c = *it;
    return insert(n->get(c), ++it, end);
}

Trie::Node* Trie::insertCase(Node* n, std::string::const_iterator it,
                         std::string::const_iterator end) 
{
    if (it == end) {
        n->value_here = true;
        return n;
    }
    char c = *it;
    return insertCase(n->get(toupper(c)), ++it, end);
}

/* The recursion here is not the simplest to grok.  As an illustration:
 *
 * Foreign Trie:  root(0) - node0(b) - node1(a) - node2(r)
 * Local Trie:  same nodes, but names are prefixed with L (e.g. Lroot)
 * 
 * insertForeignIt([iterator corresponding to "bar"]):
 *      calls insertForeignIt(node2, Lroot, 0)
 *          calls insertForeignIt(node1, Lroot, r)
 *              calls insertForeignIt(node0, Lroot, a)
 *                  calls insertForeignIt(root, Lroot, b)
 *                      calls insertForeignIt(root, Lroot, 0)
 *                          returns Lroot
 *                      returns Lroot['b'] (Lnode0)
 *                  returns Lroot['b']['a'] (Lnode1)
 *              returns Lroot['b']['a']['r'] (Lnode2)
 *          returns Lroot['b']['a']['r'] (Lnode2)
 *      Lroot['b']['a']['r'].value_here = true;
 * done
 *
 * Basically, we walk up the first trie, pushing characters onto the
 * call stack as we go, and then once we reach the top, we walk down
 * the local trie, popping characters off the call stack as we go.
 * 
 * It's a fancy version of a recursive implementation of a stack based
 * string reversal algorithm, applied to a linked list and a trie.
 *
 * And everyone thought that reversing strings was a silly problem...
 */
Trie::Node* Trie::insertForeignIt(SearchIterator it, Node* r, char c) {
    if (!it.reachable()) {  //fallen off the top of the tree
        return r;
    }
    char tmp = it.charHere();
    it.up();
    Node* n = insertForeignIt(it, r, tmp);

    return (c == 0) ? n : n->get(c);
}

Trie::SearchResult Trie::find(Node* n, std::string::const_iterator it,
                         std::string::const_iterator end) {
    if (!n) {
        return NOPREFIX;
    }
    if (it == end) {
        return n->value_here ? FOUND : NOTFOUND;
    }
    char c = *it;
    return find(n->children[Node::index(c)].get(), ++it, end);
}

void Trie::enqueue(Node* n, std::vector<std::string>& q) {
    if (!n) {
        return;
    }
    if (n->value_here) {
        q.push_back(n->toString());
    }
    for (int i = 0; i < 26; ++i) {
        enqueue(n->children[i].get(), q);
    }
}

void Trie::enqueue_nocache(Node* n, std::string prefix, 
                            std::vector<std::string>& q) 
{
    if (!n) {
        return;
    }
    if (n->value_here) {
        q.push_back(prefix);
    }
    prefix.push_back('X'); //add a dummy char
    for (int i = 0; i < 26; ++i) {
        *(prefix.rbegin()) = (char)('A' + i); //set last char
        enqueue_nocache(n->children[i].get(), prefix, q);
    }
}

