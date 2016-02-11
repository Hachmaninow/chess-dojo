(ns chessdojo.pgn-test
  (:require [clojure.test :refer :all]
            [chessdojo.pgn :refer :all]
            [chessdojo.rules :refer :all]
            [chessdojo.game :refer [game->board-fen game->str]]
            [chessdojo.data :as cd]
            [instaparse.core :as insta]))

(deftest test-parse-move
  (testing "simple pawn move"
    (is (= {:to-file "e" :to-rank "6"} (parse-move "e6"))))
  (testing "capturing pawn move"
    (is (= {:from-file "e" :capture "x" :to-file "f" :to-rank "6"} (parse-move "exf6"))))
  (testing "simple piece move"
    (is (= {:piece "N" :to-file "e" :to-rank "6"} (parse-move "Ne6")))
    (is (= {:piece "N" :from-file "g" :to-file "e" :to-rank "6"} (parse-move "Nge6")))
    (is (= {:piece "N" :from-rank "4" :to-file "e" :to-rank "6"} (parse-move "N4e6")))
    (is (= {:piece "N" :from-file "g" :from-rank "5" :to-file "e" :to-rank "6"} (parse-move "Ng5e6")))
    ; (is (= {:piece "N" :from-file "b" :from-rank "8" :to-file "c" :to-rank "6"} (parse-move "Nb8c6")))  # oh, oh! does not work currently
    )
  (testing "capturing piece move"
    (is (= {:piece "N" :capture "x" :to-file "e" :to-rank "6"} (parse-move "Nxe6")))
    (is (= {:piece "Q" :capture "x" :to-file "f" :to-rank "7" :call "#"} (parse-move "Qxf7#")))
    (is (= {:piece "N" :from-file "f" :capture "x" :to-file "e" :to-rank "6"} (parse-move "Nfxe6")), "ambiguous from square"))
  (testing "promotion"
    (is (= {:to-file "e" :to-rank "8" :promote-to "R"} (parse-move "e8=R")))
    (is (= {:from-file "e" :capture "x" :to-file "f" :to-rank "8" :promote-to "N"} (parse-move "exf8=N"))))
  (testing "castling"
    (is (= {:castling "O-O"} (parse-move "O-O")))
    (is (= {:castling "O-O-O"} (parse-move "O-O-O")))
    (is (= {:castling "O-O-O" :call "+"} (parse-move "O-O-O+")))))

(deftest test-parse-tags
  (is (= [[:tag "White" "Kasparov, Garry"]] (filter #(and (= :tag (first %)) (= "White" (second %))) (pgn (slurp "src/test/cljc/test-pgns/tags.pgn"))))))

(deftest test-parse-comments
  (is (= [:comment "Topalov is a\nSicilian player, but against Kasparov he prefers to spring a slight surprise\non his well prepared opponent as soon as possible."] (last (filter #(= :comment (first %)) (pgn (slurp "src/test/cljc/test-pgns/comments.pgn")))))))

(deftest test-parse-variations
  (is (= [:move-number :move :move :move-number :move :move :move-number :move :variation :black-move-number :move] (map first (pgn (slurp "src/test/cljc/test-pgns/variations.pgn"))))))

(deftest test-parse-annotations
  (is (= [[:annotation "132"] [:annotation "6"]] (filter #(= :annotation (first %)) (pgn (slurp "src/test/cljc/test-pgns/annotations.pgn"))))))

(deftest test-complete-game
  (is (= 9160 (count (flatten (pgn (slurp "src/test/cljc/test-pgns/complete.pgn")))))))

(deftest test-parse-error
  (is (true? (insta/failure? (pgn (slurp "src/test/cljc/test-pgns/invalid.pgn"))))))


;
; pgn to event seq
;

(deftest test-pgn->events
  (is (= [{:piece :P :to 27} {:piece :P :to 35}] (pgn->events "d4 d5")))
  (is (= [{:piece :P :to 27} {:piece :P :to 35} :back {:piece :N :to 45} :out :forward {:piece :N :to 21}]
         (pgn->events "d4 d5 (Nf6) Nf3")))
  (is (= [{:piece :P :to 27} {:piece :P :to 35} :back
          {:piece :N :to 45} {:piece :P :to 26} :back
          {:piece :P :to 22} :out :forward :out :forward
          {:piece :N :to 21}]
         (pgn->events "d4 d5 (Nf6 c4 (g3)) Nf3")))
  (is (= [{:piece :P :to 28} {:piece :P :to 36} {:piece :N :to 21}
          :back {:piece :N :to 18} :out :forward
          {:piece :N :to 42} {:piece :B :to 33} {:piece :P :to 40}
          {:piece :B :to 42 :capture :X}]
         (pgn->events "e4 e5 Nf3 (Nc3) Nc6 Bb5 a6 Bxc6")))
  (is (= [{:piece :N :from-file 6 :to 21} {:piece :N :from-rank 0 :to 18}]
         (pgn->events "Ngf3 N1c3")))
  (testing "comments"
    (is (= [{:piece :P :to 27} {:piece :P :to 35} {:comment "a closed game"}]
           (pgn->events "d4 d5 {a closed game}"))))

  )

;(time
;  (pgn->events (slurp "src/test/cljc/test-pgns/complete.pgn")))

(deftest test-error
  (is (thrown? Exception (pgn->events (slurp "src/test/cljc/test-pgns/invalid.pgn")))))

(deftest test-load-pgn
  (are [pgn fen game-str]
    (let [game (load-pgn pgn)]
      (is (= fen (game->board-fen game)))
      (is (= game-str (game->str game))))
    "e4 e5 Nf3 Nc6 Bb5 a6 Bxc6" "r1bqkbnr/1ppp1ppp/p1B5/4p3/4P3/5N2/PPPP1PPP/RNBQK2R" "e2-e4 e7-e5 Ng1-f3 Nb8-c6 Bf1-b5 a7-a6 >Bb5xc6"
    "e4 e5 Nf3 (Nc3) Nc6 Bb5 a6 Bxc6" "r1bqkbnr/1ppp1ppp/p1B5/4p3/4P3/5N2/PPPP1PPP/RNBQK2R" "e2-e4 e7-e5 Ng1-f3 (Nb1-c3) Nb8-c6 Bf1-b5 a7-a6 >Bb5xc6"
    "d4 d5 (Nf6 c4 (g3)) Nf3" "rnbqkbnr/ppp1pppp/8/3p4/3P4/5N2/PPP1PPPP/RNBQKB1R" "d2-d4 d7-d5 (Ng8-f6 c2-c4 (g2-g3)) >Ng1-f3"))

(deftest load-complex-pgn
  (is (= "8/Q6p/6p1/5p2/5P2/2p3P1/3r3P/2K1k3" (game->board-fen (load-pgn (slurp "src/test/cljc/test-pgns/complete.pgn"))))))

;(cd/deflate (load-pgn (slurp "src/test/cljc/test-pgns/complete.pgn")))

