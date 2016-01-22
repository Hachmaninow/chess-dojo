(ns chessdojo.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [chessdojo.middleware :refer [wrap-middleware]]
            [environ.core :refer [env]]))

(def mount-target
  [:div#app
   [:h3 "ClojureScript has not been compiled!"]
   [:p "please run "
    [:b "lein figwheel"]
    " in order to start the compiler"]])

(def loading-page
  (html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      (include-css "css/normalize.css")
      (include-css "css/skeleton.css")
      (include-css "css/chessboard-0.3.0.min.css")
      (include-css (if (env :dev) "css/site.css" "css/site.min.css"))
      ]
     [:body
      [:div {:class "container"}
       [:div {:class "row"}
        [:div {:class "eight columns"}
         [:div {:id "board"}]
         ]
        [:div {:class "four columns"}
         [:div {:id "notation"}]
         mount-target
         ]
        ]
       [:div {:class "row"}
        [:div {:class "four columns"}
         [:span {:id "status"}]
         ]
        [:div {:class "four columns"}
         [:span {:id "fen"}]
         ]
        [:div {:class "four columns"}
         [:span {:id "pgn"}]
         ]
        ]
       ]
      (include-js "js/chess.min.js")
      (include-js "js/jquery-2.1.4.min.js")
      (include-js "js/chessboard-0.3.0.js")
      (include-js "js/chessdojo.js")
      (include-js "js/app.js")
      ]]))

(defroutes routes
           (GET "/" [] loading-page)
           (GET "/about" [] loading-page)

           (resources "/")
           (not-found "Not Found"))

(def app (wrap-middleware #'routes))