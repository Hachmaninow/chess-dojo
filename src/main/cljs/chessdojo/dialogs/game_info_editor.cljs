(ns chessdojo.dialogs.game-info-editor
  (:require [chessdojo.state :as cst]
            [chessdojo.game :as cg]
            [clojure.string :as string]
            [reagent.core :as reagent]))

(enable-console-print!)

; backing atoms for state

(def current-value
  (reagent/atom ""))

(def current-taxonomy-placement
  (reagent/atom nil))

; helpers to convert newline-separated key-values to a map and vice-versa

(defn str->game-info [game-info-str]
  (into {} (map #(string/split % "=") (string/split-lines game-info-str))))

(defn game-info->str [game-info]
  (string/join "\n" (map (fn [[k v]] (str (name k) "=" v)) game-info)))

; event handlers

(defn update-game-info []
  (do
    (cst/update-game (cg/with-game-info (cst/active-game) (str->game-info @current-value)))
    (println (cg/game-info (cst/active-game)))))

(defn update-current-value [event]
  (reset! current-value (-> event .-target .-value)))

(defn update-taxonomy-placement [event]
  (let [selected-taxon (-> event .-target .-value)]
    (println selected-taxon)
    (reset! current-taxonomy-placement selected-taxon))
  )

(defn- render-taxon-option [taxon]
  ^{:key (:_id taxon)} [:option {:value (:_id taxon)} (:name taxon)])

(defn flatten-taxonomy [taxon]
  (concat [taxon] (map flatten-taxonomy (:children taxon))))

(defn taxonomy-placement-select []
  [:select.form-control  {:value @current-taxonomy-placement :on-change update-taxonomy-placement}
   (map render-taxon-option (flatten (map flatten-taxonomy @cst/taxonomy)))])

(defn game-info-textarea []
  [:textarea.full-width {:rows 10 :value (str @current-value) :on-change update-current-value}])

(defn render []
  [:div#game-info-editor.modal.fade {:tab-index "-1" :role "dialog"}
   [:div.modal-dialog {:role "document"}
    [:div.modal-content
     [:div.modal-header
      [:button.close {:type "button" :data-dismiss "modal" :aria-label "Close"}
       [:span {:aria-hidden true} "×"]]
      [:h4.modal-title "Edit game info"]
      [taxonomy-placement-select]
      [game-info-textarea]
      [:div.modal-footer
       [:button.btn.btn-default {:type "button" :data-dismiss "modal"} "Cancel"]
       [:button.btn.btn-primary {:type "button" :data-dismiss "modal" :on-click update-game-info} "Ok"]]]]]])

(defn init-current-value []
  (reset! current-value (game-info->str (cg/game-info (cst/active-game))))
  (reset! current-taxonomy-placement (cg/taxonomy-placement (cst/active-game))))

(defn edit-game-info-button []
  [:button.btn.btn-default
   {:type "button" :data-toggle "modal" :data-target "#game-info-editor" :on-click init-current-value}
   [:span.glyphicon.glyphicon-tags]])
