(defproject crazyhat "0.1.0-SNAPSHOT"
  :description "Static Web site generator and test server"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"stuart" "http://stuartsierra.com/maven2"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.0.2"]
                 [ring/ring-jetty-adapter "1.0.2"]
                 [markdown-clj "0.9.13"]
                 [com.stuartsierra/lazytest "1.2.3"]
                 [midje "1.4.0"]
                 [watchtower "0.1.1"]]
  :main crazyhat.core
  :jar-name "crazyhat.jar"
  :uberjar-name "crazyhat-app.jar")
