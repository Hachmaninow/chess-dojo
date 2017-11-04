(ns chessdojo.views.browser
  (:require
    [chessdojo.gateway :as gateway]
    [chessdojo.state :as cst]))

(defn listed-game-view [game]
  (let [id (:_id game)
        {white :White black :Black result :Result} (:game-info game)]
    ^{:key id} [:tr {:on-click #(gateway/load-game id)}
                [:td white]
                [:td black]
                [:td result]]))

(defn inbox-view []
  [:table.table.table-striped.table-hover.table-condensed.small
   [:tbody
    (for [game @cst/game-list]
      (listed-game-view game))]])

(defn browser []
  [:div.panel.panel-default
   [:div.panel-heading "Inbox"]
   [inbox-view]])
