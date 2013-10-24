#include "Board.h"
#include <fstream>
#include <algorithm>

#include <iostream>

Board::Board() : Board(time(nullptr)) {}
Board::Board(long seed) {
    newGame(seed);
    std::ifstream f("american-english");
    for (std::string line; std::getline(f, line);) {
        englishWords.insertCase(std::move(line));
    }
    f.close();
}


std::string Board::toString() {
    std::string s = "";

    for (int r = 0; r < 5; r++) {
        for (int c = 0; c < 5; c++) {
            s += board[r][c];
            if (board[r][c] == 'Q') {
                s += "u";
            }
            else {
                s += " ";
            }
        }
        if (r < 4) {
            s += "\n";
        }
    }
    return s;
}

int Board::countPoints(std::vector<std::string>& q) {
    int pts = 0;
    for (const auto& s : q) {
        switch (s.length()) {
        case 1:
        case 2:
            break;
        case 3:
        case 4:
            pts += 1;
            break;
        case 5:
            pts += 2;
            break;
        case 6:
            pts += 3;
            break;
        case 7:
            pts += 5;
            break;
        default:
            pts += 11;
            break;
        }
    }
    return pts;
}
    
std::vector<std::string> Board::allWords() {
    bool used[5][5] = {};
    Trie foundwords;

    //basically, we just do the recursive search starting at each node
    for (int r = 0; r < 5; r++) {
        for (int c = 0; c < 5; c++) {
            used[r][c] = true;
            char ch = board[r][c];

            auto it = englishWords.beginSearch();
            it.next(ch);
            //handle Qu
            if (ch == 'Q') {
                it.next('U');
            }
            //perform search
            allWords(it, r, c, used, foundwords);
            used[r][c] = false;
        }
    }
    return foundwords.enqueue();
}

void Board::newGame(long seed) {
    srand(seed);
    char dieRolls[25];

    dieRolls[0] = "AAAFRS"[rand() % 6];
    dieRolls[1] = "AAEEEE"[rand() % 6];
    dieRolls[2] = "AAFIRS"[rand() % 6];
    dieRolls[3] = "ADENNN"[rand() % 6];
    dieRolls[4] = "AEEEEM"[rand() % 6];
    dieRolls[5] = "AEEGMU"[rand() % 6];
    dieRolls[6] = "AEGMNN"[rand() % 6];
    dieRolls[7] = "AFIRSY"[rand() % 6];
    dieRolls[8] = "BJKQXZ"[rand() % 6];
    dieRolls[9] = "CCENST"[rand() % 6];
    dieRolls[10] = "CEIILT"[rand() % 6];
    dieRolls[11] = "CEILPT"[rand() % 6];
    dieRolls[12] = "CEIPST"[rand() % 6];
    dieRolls[13] = "DDHNOT"[rand() % 6];
    dieRolls[14] = "DHHLOR"[rand() % 6];
    dieRolls[15] = "DHLNOR"[rand() % 6];
    dieRolls[16] = "DHLNOR"[rand() % 6];
    dieRolls[17] = "EIIITT"[rand() % 6];
    dieRolls[18] = "EMOTTT"[rand() % 6];
    dieRolls[19] = "ENSSSU"[rand() % 6];
    dieRolls[20] = "FIPRSY"[rand() % 6];
    dieRolls[21] = "GORRVW"[rand() % 6];
    dieRolls[22] = "IPRRRY"[rand() % 6];
    dieRolls[23] = "NOOTUW"[rand() % 6];
    dieRolls[24] = "OOOTTU"[rand() % 6];

    std::random_shuffle(std::begin(dieRolls), std::end(dieRolls));
    
    int pos = 0;
    for (int row = 0; row < 5; row++) {
        for (int c = 0; c < 5; c++) {
            board[row][c] = dieRolls[pos++];
        }
    }
}

void Board::allWords(Trie::SearchIterator si, int r, int c,
                     bool (&used)[5][5], Trie& foundwords)
{
    if (!si.reachable()) {
        //no reachable strings from this prefix
        return;
    }
    if (si.inSet()) {
        foundwords.insertForeignIt(si);
    }

    //try to append all adjacent nodes to current prefix
    for (int y : (int[]){r - 1, r, r + 1}) {
        for (int x : (int[]){c - 1, c, c + 1}) {
            //check if we're out of bounds
            if (x < 0 || y < 0 || y >= 5 || x >= 5) 
            {
                continue;
            }
            //only append if we haven't already used this node
            if (!used[y][x]) {
                used[y][x] = true;
                char ch = board[y][x];

                auto it = si;
                it.next(ch);
                if (ch == 'Q') {
                    it.next('U');
                }
                //append and check
                allWords(it, y, x, used, foundwords);
                //unset now that we've handled that node
                used[y][x] = false;
            }
        }
    }
}

