(ns chess.fen)

;
; board to fen
;

(defn- run-length-encode [vector]
  (map #(list (count %) (first %)) (partition-by identity vector)))

(defn- piece->fen [[count piece]]
  (if (nil? piece)
    (str count)
    (apply str (repeat count (name piece)))))

(defn- rank->fen [rank]
  (apply str (map piece->fen rank)))

(defn board->fen [board]
  (clojure.string/join "/" (map rank->fen (map run-length-encode (reverse (partition 8 board))))))

(defn position->fen [{board :board} ]
  (clojure.string/join "/" (map rank->fen (map run-length-encode (reverse (partition 8 board))))))

;
; fen to board
;

(defn- char->piece [piece-str]
  (let [piece (read-string (str piece-str))]
    (if (number? piece) (repeat piece nil) (keyword piece))))

(defn- digits->space [rank]
  (flatten (map char->piece rank)))

(def fen-castling-availability-map {:white {\K :O-O \Q :O-O-O} :black {\k :O-O \q :O-O-O}})

(defn parse-castling-availability [castling-availability-str]
  {
   :white (set (remove nil? (map (:white fen-castling-availability-map) castling-availability-str)))
   :black (set (remove nil? (map (:black fen-castling-availability-map) castling-availability-str)))
   })

(defn fen->board [fen]
          (mapcat #(digits->space %) (reverse (clojure.string/split fen #"/"))))


; fen to game

(defn fen->game [fen]
  (let [parts (clojure.string/split fen #"\s+")]
    {
     :board                     (fen->board (get parts 0))
     :turn                      (if (= (get parts 1) "w") :white :black)
     :castling-availability     (parse-castling-availability (get parts 2))
     :ep-info                   (if (= (get parts 3) "-") nil (keyword (get parts 3)))
     :fifty-rule-halfmove-clock (read-string (get parts 4))
     :move-no                   (read-string (get parts 5))
     }))
