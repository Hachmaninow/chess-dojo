(defproject chess-dojo "0.1.0-SNAPSHOT"
  :description "Training place for chess"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-environ "1.1.0" :hooks false]
            [lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.10"]                             ; Clojurescript testing
            [lein-figwheel "0.5.19"]                        ; Builds Clojurescript and hot load
            [com.jakemccrary/lein-test-refresh "0.24.1"]]   ; Automatically refresh and run test

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]

                 [environ "1.1.0"]                          ; Library for managing environment variables in Clojure

                 [ring "1.8.0"]                             ; Clojure HTTP server abstraction
                 [ring-server "0.5.0"]                      ; Web server to serve a Ring handler
                 [ring/ring-defaults "0.3.2"]               ; A library to provide sensible Ring middleware defaults
                 [ring/ring-json "0.5.0"]                   ; Ring middleware for handling JSON
                 [ring-middleware-format "0.7.2"]           ; Ring middleware for parsing parameters and emitting responses in JSON or other formats
                 [compojure "1.6.1"]                        ; A concise routing library for Ring/Clojure
                 [hiccup "1.0.5"]                           ; Fast library for rendering HTML in Clojure

                 [cljsjs/react "16.12.0-1"]                 ; React packaged up with Google Closure externs
                 [reagent "0.8.1"]                          ; Minimalistic React for ClojureScript
                 [reagent-forms "0.5.43"]                   ; Bootstrap form components for Reagent

                 [instaparse "1.4.10"]                      ; Build parsers in Clojure
                 [cheshire "5.9.0"]                         ; JSON support

                 [com.novemberain/monger "3.5.0"]           ; MongoDB client

                 [cljs-http "0.1.46"]                       ; A ClojureScript HTTP library

                 [spyscope "0.1.6"]                         ; Debugging
                 [com.taoensso/timbre "4.10.0"]]            ; Profiling

  :managed-dependencies [[org.flatland/ordered "1.5.7"]     ; Explicitly manage because of problems in Java 9
                         [com.cognitect/transit-cljs "0.8.243"]]

  :min-lein-version "2.9.1"

  :source-paths ["src/main/clj" "src/main/cljc"]

  :test-paths ["src/test/clj" "src/test/cljc"]

  :resource-paths ["resources" "target/cljsbuild"]

  :clean-targets ^{:protect false} [:target-path            ; otherwise "target/cljsbuild/public/js/out" cannot be cleaned
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :uberjar-name "chessdojo.jar"

  :ring {:handler chessdojo.app/api-and-site :uberwar-name "chessdojo.war"}

  :cljsbuild {
              :builds {:app  {:source-paths ["src/main/cljs" "src/main/cljc"]
                              :compiler     {:output-to     "target/cljsbuild/public/js/app.js"
                                             :output-dir    "target/cljsbuild/public/js/out"
                                             :asset-path    "js/out"
                                             :optimizations :none
                                             :pretty-print  true}}
                       :test {:source-paths ["src/main/cljs" "src/main/cljc" "src/test/cljs" "src/test/cljc"]
                              :compiler     {:output-dir    "target/doo"
                                             :output-to     "target/browser_tests.js"
                                             :main          "chessdojo.test-suite"
                                             :optimizations :none}}}}

  :test-selectors {:default    (complement :functional)
                   :functional :functional
                   :all        (constantly true)}

  :profiles {:dev  {:dependencies [[ring/ring-mock "0.3.0"]
                                   [ring/ring-devel "1.4.0"]]

                    :source-paths ["env/dev/clj"]

                    :figwheel     {:css-dirs       ["resources/public/css"] ; watch and update CSS
                                   :server-logfile false
                                   :ring-handler   chessdojo.app/api-and-site} ; embed ring handler into the figwheel http-kit

                    :env          {:mongo-database-name "chessdojo_test"}

                    :cljsbuild    {:builds {:app {:source-paths ["env/dev/cljs"]
                                                  :compiler     {:main       "chessdojo.dev"
                                                                 :source-map true}}}}}
             :test {:env {:mongo-database-name "chessdojo_test"}}
             :prod {:env {:mongo-database-name "chessdojo_test"}}}
  )