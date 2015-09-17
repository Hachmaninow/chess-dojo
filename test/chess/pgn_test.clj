(ns chess.pgn-test
  (:require [clojure.test :refer :all] [chess.pgn :refer :all]))

(deftest test-parse-move
  (testing "simple pawn move"
    (is (= {:to-file "e", :to-rank "8"} (parse-move "e8"))))
  (testing "simple piece move"
    (is (= {:piece "N", :to-file "e", :to-rank "6"} (parse-move "Ne6")))
    (is (= {:piece "N", :to-file "e", :to-rank "6", :from-file "g"} (parse-move "Nge6")))
    (is (= {:piece "N", :to-file "e", :to-rank "6", :from-rank "4"} (parse-move "N4e6")))
    (is (= {:piece "N", :to-file "e", :to-rank "6", :from-file "g" :from-rank "5"} (parse-move "Ng5e6")))
    
    )
  (testing "capturing piece move"
    (is (= {:piece "N", :capture "x", :to-file "e", :to-rank "6"} (parse-move "Nxe6")))
    (is (= {:piece "Q", :capture "x", :to-file "f", :to-rank "7", :call "#"} (parse-move "Qxf7#"))))
  (testing "capturing piece move"
    (is (= {:castling "O-O"} (parse-move "O-O")))
    (is (= {:castling "O-O-O"} (parse-move "O-O-O"))))
    (is (= {:castling "O-O-O" :call "+"} (parse-move "O-O-O+"))))

(deftest test-matches-parsed-move
  (testing "pawn moves"
    (is (true? (matches-parsed-move? (parse-move "a6") {:piece :P, :to :a6})))
    (is (false? (matches-parsed-move? (parse-move "a5") {:piece :p, :to :a6})))
    (is (true? (matches-parsed-move? (parse-move "axb5") {:piece :p, :to :b5, :from :a6, :capture :P})))
    (is (false? (matches-parsed-move? (parse-move "axb5") {:piece :p, :to :b5, :from :a6})), "capture missing")
    (is (false? (matches-parsed-move? (parse-move "axb5") {:piece :p, :to :b5, :from :c6, :capture :P})), "wrong file"))
  (testing "piece moves"
    (is (true? (matches-parsed-move? (parse-move "Ne7") {:piece :N, :to :e7})))
    (is (true? (matches-parsed-move? (parse-move "Ne7") {:piece :n, :to :e7})))
    (is (false? (matches-parsed-move? (parse-move "Ne7") {:piece :B, :to :e7})))
    (is (true? (matches-parsed-move? (parse-move "Nxe7") {:piece :n, :to :e7, :capture :b})))
    (is (false? (matches-parsed-move? (parse-move "Nxe7") {:piece :n, :to :e7})), "missing capture")
    (is (true? (matches-parsed-move? (parse-move "Ngxe7") {:piece :n, :to :e7, :from :g6, :capture :B})))
    (is (true? (matches-parsed-move? (parse-move "N3xe5") {:piece :n, :to :e5, :from :d3, :capture :B})))
    (is (false? (matches-parsed-move? (parse-move "Ncxe7") {:piece :n, :to :e7, :from :g6, :capture :B})), "wrong file")
    (is (false? (matches-parsed-move? (parse-move "N7xe5") {:piece :n, :to :e5, :from :d3, :capture :B})), "wrong rank"))
  (testing "castlings"
    (is (true? (matches-parsed-move? (parse-move "O-O") {:to :g1, :from :e1, :castling :O-O})))
    (is (false? (matches-parsed-move? (parse-move "O-O-O") {:to :g1, :from :e1, :castling :O-O}))))


  
)

