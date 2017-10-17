(ns chessdojo.views.layout
  (:require
    [cljsjs.react-bootstrap]
    [reagent.core :as reagent :refer [atom]]))

(def Grid (reagent/adapt-react-class (aget js/ReactBootstrap "Grid")))
(def Row (reagent/adapt-react-class (aget js/ReactBootstrap "Row")))
(def Col (reagent/adapt-react-class (aget js/ReactBootstrap "Col")))

(defn grid-layout []
  [Grid
   [Row
    [Col {:md 2}]
    [Col {:md 8}]
    [Col {:md 2}]]])

(defn mount-grid []
  (reagent/render [grid-layout] (.getElementById js/document "mount"))

  ;(reagent/render [inbox-view] (.getElementById js/document "inbox"))
  ;(reagent/render [browser-view] (.getElementById js/document "browser"))
  ;(reagent/render [editor-view] (.getElementById js/document "editor"))
  )
