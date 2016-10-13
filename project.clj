(defproject chess-dojo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.22" :scope "provided"]

                 [instaparse "1.4.3"]
                 [spyscope "0.1.5"]
                 [com.taoensso/timbre "4.7.4"]

                 [ring-server "0.4.0"]
                 [reagent "0.6.0" :exclusions [org.clojure/tools.reader]]
                 [reagent-forms "0.5.26"]
                 [reagent-utils "0.2.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [compojure "1.5.1"]
                 [metosin/compojure-api "1.1.8"]
                 [clj-time "0.12.0"]
                 [hiccup "1.0.5"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.1.6" :exclusions [org.clojure/tools.reader]]

                 [environ "1.0.2"]
                 [com.novemberain/monger "3.0.2"]
                 [cheshire "5.6.3"]
                 ]

  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.2"]
            [lein-asset-minifier "0.2.4" :exclusions [org.clojure/clojure]]
            [lein-doo "0.1.6"]
            ]

  :ring {:handler chessdojo.app/api-and-site :uberwar-name "chessdojo.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "chessdojo.jar"

  :main chessdojo.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/main/clj" "src/main/cljc"]
  :test-paths ["src/test/clj" "src/test/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app          {:source-paths ["src/main/cljs" "src/main/cljc"]
                                      :compiler     {:output-to     "target/cljsbuild/public/js/app.js"
                                                     :output-dir    "target/cljsbuild/public/js/out"
                                                     :asset-path    "js/out"
                                                     :optimizations :none
                                                     :pretty-print  true}}
                       :browser-test {:source-paths ["src/main/cljs" "src/main/cljc" "src/test/cljs" "src/test/cljc"]
                                      :compiler     {:output-dir    "target/doo"
                                                     :output-to     "target/browser_tests.js"
                                                     :main          "chessdojo.browser-test"
                                                     :optimizations :none}}}}

  :doo {
        :paths {:karma "/Users/hman/Tools/node_modules/karma/bin/karma"}
        }

  :test-selectors {:default    (complement :functional)
                   :functional :functional
                   :all        (constantly true)}

  :profiles {:dev     {:repl-options {:init-ns chessdojo.repl}
                       :dependencies [[ring/ring-mock "0.3.0"]
                                      [ring/ring-devel "1.4.0"]
                                      [prone "0.8.3"]
                                      [lein-figwheel "0.5.8" :exclusions [org.clojure/core.memoize
                                                                            ring/ring-core
                                                                            org.clojure/clojure
                                                                            org.ow2.asm/asm-all
                                                                            org.clojure/data.priority-map
                                                                            org.clojure/tools.reader
                                                                            org.clojure/clojurescript
                                                                            org.clojure/core.async
                                                                            org.clojure/tools.analyzer.jvm]]
                                      [org.clojure/clojurescript "1.9.229" :exclusions [org.clojure/clojure org.clojure/tools.reader]]
                                      [org.clojure/tools.nrepl "0.2.12"]
                                      [com.cemerick/piggieback "0.2.1"]
                                      [pjstadig/humane-test-output "0.7.1"]
                                      ]

                       :source-paths ["env/dev/clj"]
                       :plugins      [[lein-figwheel "0.5.8" :exclusions [org.clojure/core.memoize
                                                                            ring/ring-core
                                                                            org.clojure/clojure
                                                                            org.ow2.asm/asm-all
                                                                            org.clojure/data.priority-map
                                                                            org.clojure/tools.reader
                                                                            org.clojure/clojurescript
                                                                            org.clojure/core.async
                                                                            org.clojure/tools.analyzer.jvm]]
                                      [org.clojure/clojurescript "1.9.229"]
                                      ]

                       :injections   [(require 'pjstadig.humane-test-output)
                                      (pjstadig.humane-test-output/activate!)]

                       :figwheel     {:http-server-root "public"
                                      :server-port      3449
                                      :nrepl-port       7002
                                      :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
                                      :css-dirs         ["resources/public/css"]
                                      :ring-handler     chessdojo.app/api-and-site}

                       :env          {
                                      :mongo-database-name   "chessdojo_test"
                                      :mongo-collection-name "games"
                                      }

                       :cljsbuild    {:builds {:app {:source-paths ["env/dev/cljs"]
                                                     :compiler     {:main       "chessdojo.dev"
                                                                    :source-map true}}}}}
             :test    {
                       :env {
                             :mongo-database-name   "chessdojo_test"
                             :mongo-collection-name "games"
                             }
                       }

             :prod    {
                       :env {
                             :mongo-database-name   "chessdojo_test"
                             :mongo-collection-name "games"
                             }
                       }

             :uberjar {:hooks        [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks   ["compile" ["cljsbuild" "once"]]
                       :env          {:production true}
                       :aot          :all
                       :omit-source  true
                       :cljsbuild    {:jar    true
                                      :builds {:app
                                               {:source-paths ["env/prod/cljs"]
                                                :compiler
                                                              {:optimizations :advanced
                                                               :pretty-print  false}}}}}

             }

  )
