(ns chessdojo.game-test
  #?(:clj
     (:require [clojure.test :refer :all]
               [clojure.zip :as zip :refer [vector-zip up down left lefts right rights rightmost insert-right branch? node path root]]
               [chessdojo.game :as cg :refer [navigate]]
               [chessdojo.rules :as cr :refer [to-idx to-sqr]]
               [chessdojo.fen :as cf]
               [chessdojo.notation :as cn]
               ))
  #?(:cljs (:require [cljs.test :refer-macros [deftest is testing run-tests]]
             [clojure.zip :as zip :refer [vector-zip up down left lefts right rights rightmost insert-right branch? node path root]]
             [chessdojo.game :as cg :refer [navigate]]
             [chessdojo.rules :as cr :refer [to-idx to-sqr]]
             [chessdojo.fen :as cf]
             [chessdojo.notation :as cn])))

;
; verify zipper basics
;

(deftest test-zipper-learning
  (testing "vector-zipper"
    (let [zipper (vector-zip [1 2 3 4 [:a :b] [:c :d] 5])]
      (is (= [1 2 3 4 [:a :b] [:c :d] 5] (-> zipper root)))
      (is (= 1 (-> zipper down node)))
      (is (= 2 (-> zipper down right node)))
      (is (= 2 (-> zipper down right node)))
      (is (= :a (-> zipper down right right right right down node)))
      (testing "down"
        (is (= :a (-> zipper down right right right right down node)))
        (is (= [:a :b] (-> zipper down right right right right down up node))))))
  (testing "zip/node"
    (is (= {
            :move     {:ep-info [20 28] :from 12 :piece :P :to 28}
            :position {:board [:R :N :B :Q :K :B :N :R :P :P :P :P nil :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r] :castling-availability {:black #{:O-O :O-O-O} :white #{:O-O :O-O-O}} :ep-info [20 28] :turn :black :ply 2}
            }
           (node (cg/insert-move cg/new-game (cr/parse-move :e4))))))
  (testing "zip/root"
    (is (= [
            {
             :position {:board [:R :N :B :Q :K :B :N :R :P :P :P :P :P :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r], :turn :white, :castling-availability {:white #{:O-O-O :O-O}, :black #{:O-O-O :O-O}} :ply 1}
             }
            {
             :position {:board [:R :N :B :Q :K :B :N :R :P :P :P :P nil :P :P :P nil nil nil nil nil nil nil nil nil nil nil nil :P nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil :p :p :p :p :p :p :p :p :r :n :b :q :k :b :n :r], :turn :black, :castling-availability {:white #{:O-O-O :O-O}, :black #{:O-O-O :O-O}}, :ep-info [20 28] :ply 2},
             :move     {:piece :P, :from 12, :to 28, :ep-info [20 28]}
             }
            ] (root (cg/insert-move cg/new-game (cr/parse-move :e4))))))
  (testing "zip/next"
    (is (= [:a :b] (-> (vector-zip [1 2 [:a :b] [:c :d] 3]) down zip/next zip/next node)))
    (is (= :a (-> (vector-zip [1 2 [:a :b] [:c :d] 3]) down zip/next zip/next zip/next node)))))

(deftest test-new-game
  (is (= cr/start-position (:position (zip/node cg/new-game)))))

; can be tested using simple vector-zippers
(deftest test-find-insert-loc
  (let [zipper (vector-zip [1 2 3])]
    (is (= 2 (-> zipper down right node)))
    (is (= 3 (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 3 4 [:a :b] [:c :d] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= 3 (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:c :d] (-> zipper down right right cg/find-anchor node)))
    (is (= 5 (-> zipper down right rightmost node)))
    (is (= 5 (-> zipper down right rightmost cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 [:a :b] [:c :d]])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:c :d] (-> zipper down right cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 3 [:a :b] 4 [:c :d] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:a :b] (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:c :d] (-> zipper down right right cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 3 [:a :b] [:c :d] 4 [:e :f [:g :h [:i [:j]]]] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:c :d] (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:e :f [:g :h [:i [:j]]]] (-> zipper down right right cg/find-anchor node))))
  (let [zipper (vector-zip [1 2 3 [:a :b] [:c :d] 4 [:e :f] [:g :h] 5])]
    (is (= 2 (-> zipper down right node)))
    (is (= [:c :d] (-> zipper down right cg/find-anchor node)))
    (is (= 3 (-> zipper down right right node)))
    (is (= [:g :h] (-> zipper down right right cg/find-anchor node))))
  )

(deftest test-end-of-variation?
  (let [zipper (vector-zip [1 2 3 4 [:a :b] [:c :d]])]
    (is (= 3 (-> zipper down right right node)))
    (is (false? (-> zipper down cg/end-of-variation?)))
    (is (= 4 (-> zipper down right right right node)))
    (is (true? (-> zipper down right right right cg/end-of-variation?))))
  (let [zipper (vector-zip [1])]
    (is (= 1 (-> zipper down node)))
    (is (true? (-> zipper down cg/end-of-variation?)))))


(deftest test-soak
  (testing "supports moves as keywords or symbols"
    (is (= "e4 c5 >Nf3" (cn/notation (cg/soak :e4 :c5 :Nf3))))
    (is (= "e4 c5 >Nf3" (cn/notation (cg/soak 'e4 'c5 'Nf3)))))
  (testing "supports move criteria"
    (is (= "e4 e5 Nf3 (Nc3) Nc6 Bb5 a6 >Bxc6"
           (cn/notation (cg/soak {:piece :P :to 28} {:piece :P :to 36} {:piece :N :to 21}
                                 :back {:piece :N :to 18} :out :forward
                                 {:piece :N :to 42} {:piece :B :to 33} {:piece :P :to 40}
                                 {:piece :B :capture :X :to 42})))))
  (testing "comments"
    (is (= "the sicilian defence" (:comment (zip/node (cg/soak {:piece :P :to 28} {:piece :P :to 34} "the sicilian defence"))))))
  (testing "annotations"
    (is (= {:move-assessment :$1} (:annotations (zip/node (cg/soak {:piece :P :to 28} {:piece :P :to 34} :$1)))))
    (is (= {:move-assessment :$1} (:annotations (zip/node (cg/soak {:piece :P :to 28} {:piece :P :to 34} :$1)))))
    (is (= {:move-assessment :$5 :positional-assessment :$18} (:annotations (zip/node (cg/soak {:piece :P :to 28} {:piece :P :to 34} :$5 :$18)))))
    (is (= {:move-assessment :$5 :positional-assessment :$18} (:annotations (zip/node (cg/soak {:piece :P :to 28} {:piece :P :to 34} :$1 :$19 :$5 :$18))))))
  (testing "unknown annotations are ignored"
    (is (false? (contains? (zip/node (cg/soak {:piece :P :to 28} {:piece :P :to 34} :$0)) :annotations)))))

(deftest test-soak-variations
  (testing "variations"
    (is (= ">e4" (cn/notation (cg/soak :e4))))
    (is (= "e4 >c5" (cn/notation (cg/soak :e4 :c5))))
    (is (= "e4 c5 >Nf3" (cn/notation (cg/soak :e4 :c5 :Nf3))))
    (is (= "e4 c5 Nf3 (>Nc3)" (cn/notation (cg/soak :e4 :c5 :Nf3 :back :Nc3))))
    (is (= "e4 c5 Nf3 (Nc3 >Nc6)" (cn/notation (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6))))
    (is (= "e4 >c5 Nf3 (Nc3 Nc6)" (cn/notation (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out))))
    (is (= "e4 c5 Nf3 (Nc3 Nc6) (>c3)" (cn/notation (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3))))
    (is (= "e4 c5 Nf3 (Nc3 Nc6) (c3 >Nf6)" (cn/notation (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6))))
    (is (= "e4 c5 >Nf3 (Nc3 Nc6) (c3 Nf6)" (cn/notation (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward))))
    (is (= "e4 c5 Nf3 (Nc3 Nc6) (c3 Nf6) Nc6 >d4" (cn/notation (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6 :d4)))))

  (testing "subsequent moves with variations"
    (is (= "e4 c5 (e5) Nf3 (>Nc3)" (cn/notation (cg/soak :e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3))))
    (is (= "e4 >c5 (e5) Nf3 (Nc3)" (cn/notation (cg/soak :e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3 :out))))
    (is (= "e4 c5 (e5) Nf3 (Nc3) (c3) >Nc6" (cn/notation (cg/soak :e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3 :out :c3 :out :forward :Nc6)))))

  (testing "nested variations"
    (is (= "d4 d5 (Nf6 c4 (c3 g6 g3 (>a3))) Nf3" (cn/notation (cg/soak :d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :back :a3))))
    (is (= "e4 e5 (c5 Nc3 (g3 g6 Bg2 (a3 Bg7 (>h5)))) Nf3" (cn/notation (cg/soak :e4 :e5 :Nf3 :back :back :c5 :Nc3 :back :g3 :g6 :Bg2 :back :a3 :Bg7 :back :h5))))
    (is (= "d4 d5 (Nf6 c4 (c3 g6 g3) (>h4)) Nf3" (cn/notation (cg/soak :d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :out :h4)))))

  (testing "nested variation in very first move"
    (is (= "d4 (e4 e5 (>c5)) d5" (cn/notation (cg/soak :d4 :d5 :back :back :e4 :e5 :back :c5))))))

(deftest test-soak-updates-position
  (is (= "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 1 1" (cf/fen (cg/soak))))
  (is (= "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 1 1" (cf/fen (cg/soak :e4))))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 1 2" (cf/fen (cg/soak :e4 :c5))))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2" (cf/fen (cg/soak :e4 :c5 :Nf3))))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 1 2" (cf/fen (cg/soak :e4 :c5 :Nf3 :back))))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR b KQkq - 1 2" (cf/fen (cg/soak :e4 :c5 :Nf3 :back :Nc3))))
  (is (= "r1bqkbnr/pp1ppppp/2n5/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR w KQkq - 1 3" (cf/fen (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6))))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 1 2" (cf/fen (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out))))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/2P5/PP1P1PPP/RNBQKBNR b KQkq - 1 2" (cf/fen (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3))))
  (is (= "rnbqkb1r/pp1ppppp/5n2/2p5/4P3/2P5/PP1P1PPP/RNBQKBNR w KQkq - 1 3" (cf/fen (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6))))
  (is (= "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2" (cf/fen (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward))))
  (is (= "r1bqkbnr/pp1ppppp/2n5/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 1 3" (cf/fen (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6))))
  (is (= "r1bqkbnr/pp1ppppp/2n5/2p5/3PP3/5N2/PPP2PPP/RNBQKB1R b KQkq d3 1 3" (cf/fen (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward :Nc6 :d4)))))

(deftest test-soak-updates-game-path
  (testing "basics"
    (is (= [0 0 nil] (cg/game-path (cg/soak))))
    (is (= [1 0 nil] (cg/game-path (cg/soak :e4))))
    (is (= [2 0 nil] (cg/game-path (cg/soak :e4 :c5))))
    (is (= [3 0 nil] (cg/game-path (cg/soak :e4 :c5 :Nf3))))
    (is (= [2 0 nil] (cg/game-path (cg/soak :e4 :c5 :Nf3 :back))))
    (is (= [3 1 [2 0 nil]] (cg/game-path (cg/soak :e4 :c5 :Nf3 :back :Nc3))))
    (is (= [4 1 [2 0 nil]] (cg/game-path (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6))))
    (is (= [2 0 nil] (cg/game-path (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out))))
    (is (= [3 2 [2 0 nil]] (cg/game-path (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3))))
    (is (= [4 2 [2 0 nil]] (cg/game-path (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6))))
    (is (= [2 0 nil] (cg/game-path (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out))))
    (is (= [3 0 nil] (cg/game-path (cg/soak :e4 :c5 :Nf3 :back :Nc3 :Nc6 :out :c3 :Nf6 :out :forward)))))
  (testing "subsequent moves with variations"
    (is (= [3 1 [2 0 nil]] (cg/game-path (cg/soak :e4 :c5 :back :e5 :out :forward :Nf3 :back :Nc3)))))
  (testing "multiple variations"
    (is (= [2 3 [1 0 nil]] (cg/game-path (cg/soak :e4 :c5 :back :e5 :out :e6 :out :c6))))) ; third variation
  (testing "nested variations"
    (is (= [5 1 [4 1 [2 1 [1 0 nil]]]] (cg/game-path (cg/soak :d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :back :a3)))) ; "d2-d4 d7-d5 (Ng8-f6 c2-c4 (c2-c3 g7-g6 g2-g3 (>a2-a3))) Ng1-f3"
    (is (= [3 2 [2 1 [1 0 nil]]] (cg/game-path (cg/soak :d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :out :h4))))
    (is (= [6 1 [5 1 [4 1 [2 1 [1 0 nil]]]]] (cg/game-path (cg/soak :e4 :e5 :Nf3 :back :back :c5 :Nc3 :back :g3 :g6 :Bg2 :back :a3 :Bg7 :back :h5)))))
  (testing "nested variation in very first move"
    (is (= [2 1 [1 1 [0 0 nil]]] (cg/game-path (cg/soak :d4 :d5 :back :back :e4 :e5 :back :c5)))))
  (testing "paths of variation node"
    (is (= [1 [4 1 [2 1 [1 0 nil]]]] (cg/game-path (zip/up (cg/soak :d4 :d5 :Nf3 :back :back :Nf6 :c4 :back :c3 :g6 :g3 :back :a3))))) ; third variation of that parent
    (is (= [3 [1 0 nil]] (cg/game-path (zip/up (cg/soak :e4 :c5 :back :e5 :out :e6 :out :c6))))) ; third variation of that parent
    )
  )

;
; navigation
;

(deftest test-navigate
  (testing "simple zipper"
    (let [zipper (down (vector-zip [1 2 3]))]
      (is (= 1 (-> zipper node)))
      (is (= 2 (-> zipper (navigate :forward) node)))
      (is (= 3 (-> zipper (navigate :forward) (navigate :forward) node)))
      (testing "navigatation beyond last move is ignored"
        (is (= 3 (-> zipper (navigate :forward) (navigate :forward) (navigate :forward) node))))))
  (testing "nested zipper"
    (let [zipper (down (vector-zip [1 2 [:a :b] [:c [:c1 :c2 :c3 [:c3a]] :d] 3]))]
      (is (= 1 (-> zipper node)))
      (is (= 2 (-> zipper (navigate :forward) node)))
      (is (= 3 (-> zipper (navigate :forward) (navigate :forward) (navigate :forward) node)))
      (is (= 2 (-> zipper (navigate :forward) (navigate :forward) (navigate :forward) (navigate :back) node)))
      (is (= 1 (-> zipper (navigate :forward) (navigate :forward) (navigate :forward) (navigate :back) (navigate :back) (navigate :back) (navigate :back) node)))))
  (testing "start"
    (let [zipper (down (vector-zip [1 2 [:a :b] [:c [:c1 :c2 :c3 [:c3a]] :d] 3]))]
      (is (= 1 (-> zipper (navigate :start) node)))
      (is (= 2 (-> zipper (navigate :start) (navigate :forward) node))))))

(deftest test-jump
  (let [game (cg/soak :e4 :e5 :Nf3 :back :Nc3 :out :g3 :g6 :Bg2 :Bg7 :back :Bh6 :start)]
    (testing "jump to existing paths return game with this path"
      (is (= "e4" (cn/san (:move (node (cg/jump game [1 0 nil]))))))
      (is (= "Nf3" (cn/san (:move (node (cg/jump game [3 0 nil]))))))
      (is (= "Nc3" (cn/san (:move (node (cg/jump game [3 1 [2 0 nil]]))))))
      (is (= "Bg7" (cn/san (:move (node (cg/jump game [6 2 [2 0 nil]]))))))
      (is (= "Bh6" (cn/san (:move (node (cg/jump game [6 1 [5 2 [2 0 nil]]]))))))
      (testing "non-existing path return original game"
        (is (= game (cg/jump game [6 3 [2 0 nil]])))))))
