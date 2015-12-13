(ns chess.pgn-test
  (:require [clojure.test :refer :all]
            [chess.rules :refer :all]
            [chess.pgn :refer :all]))

(deftest test-parse-move
  (testing "simple pawn move"
    (is (= {:to-file "e" :to-rank "6"} (parse-move "e6"))))
  (testing "capturing pawn move"
    (is (= {:from-file "e" :capture "x" :to-file "f" :to-rank "6"} (parse-move "exf6"))))
  (testing "simple piece move"
    (is (= {:piece "N" :to-file "e" :to-rank "6"} (parse-move "Ne6")))
    (is (= {:piece "N" :from-file "g" :to-file "e" :to-rank "6"} (parse-move "Nge6")))
    (is (= {:piece "N" :from-rank "4" :to-file "e" :to-rank "6"} (parse-move "N4e6")))
    (is (= {:piece "N" :from-file "g" :from-rank "5" :to-file "e" :to-rank "6"} (parse-move "Ng5e6"))))
  (testing "capturing piece move"
    (is (= {:piece "N" :capture "x" :to-file "e" :to-rank "6"} (parse-move "Nxe6")))
    (is (= {:piece "Q" :capture "x" :to-file "f" :to-rank "7" :call "#"} (parse-move "Qxf7#")))
    (is (= {:piece "N" :from-file "f" :capture "x" :to-file "e" :to-rank "6"} (parse-move "Nfxe6")), "ambiguous from square"))
  (testing "promotion"
    (is (= {:to-file "e" :to-rank "8" :promote-to "R"} (parse-move "e8=R"))))
  (is (= {:from-file "e" :capture "x" :to-file "f" :to-rank "8" :promote-to "N"} (parse-move "exf8=N"))))
(testing "castling"
  (is (= {:castling "O-O"} (parse-move "O-O")))
  (is (= {:castling "O-O-O"} (parse-move "O-O-O")))
  (is (= {:castling "O-O-O" :call "+"} (parse-move "O-O-O+"))))

(deftest test-parse-tags
  (is (= [[:tag "White" "Kasparov, Garry"]] (filter #(and (= :tag (first %)) (= "White" (second %))) (pgn (slurp "test/test-pgns/tags.pgn"))))))

(deftest test-parse-comments
  (is (= [:comment "Topalov is a\nSicilian player, but against Kasparov he prefers to spring a slight surprise\non his well prepared opponent as soon as possible."] (last (filter #(= :comment (first %)) (pgn (slurp "test/test-pgns/comments.pgn")))))))

(deftest test-parse-variations
  (is (= [:move-number :move :move :move-number :move :move :move-number :move :variation :black-move-number :move] (map first (pgn (slurp "test/test-pgns/variations.pgn"))))))

(deftest test-parse-annotations
  (is (= [[:annotation "132"] [:annotation "6"]] (filter #(= :annotation (first %)) (pgn (slurp "test/test-pgns/annotations.pgn"))))))

(deftest test-complete
  (is (= 9160 (count (flatten (pgn (slurp "test/test-pgns/complete.pgn")))))))


