(ns crazyhat.core
  (:gen-class)
  (:require [watchtower.core :as w]
            [ring.adapter.jetty :as j]
            [ring.util.response :as resp]
            [clojure.string :as st]
            [markdown.core :as md]
            [clojure.java.io :as io]))

(def source-files "../markup")
(def site-dest    "/Users/jacobsen/websites/johnj12/site")


(defn newpath [p]
  (-> p
      (st/replace-first source-files site-dest)
      (st/replace-first #"\.[^\.]+?$" ".html")))


(defn handle-update [files]
  (doseq [f files]
    (let [path (.getPath f)
          new-path (newpath path)]
      ;; Make parents if needed
      (if (io/make-parents new-path)
        (println "Made parent dir(s) for" new-path))
      (println "Updating" path "--->" new-path)
      ;; Actually make the required HTML file
      (let [parents (io/make-parents new-path)
            html (md/md-to-html-string (slurp f))]
        (spit new-path html)))))


(defn watcher []
  (w/watcher [source-files]
             (w/rate 100)
             (w/file-filter w/ignore-dotfiles)
             (w/file-filter (w/extensions :md))
             (w/on-change handle-update)))


(defn server []
  (letfn [(serve-stat [req]
            (println "Serving" (:uri req))
            (let [r (resp/file-response (:uri req) {:root site-dest})]
              (println r)
              r))]
    (future (j/run-jetty serve-stat {:port 8080 :join? false}))))


(defn -main []
  (watcher)
  (server))
