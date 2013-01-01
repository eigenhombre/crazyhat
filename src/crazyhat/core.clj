(ns crazyhat.core
  (:gen-class)
  (:import [java.io File])
  (:require [watchtower.core :as w]
            [ring.adapter.jetty :as j]
            [ring.util.response :as resp]
            [clojure.string :as st]
            [markdown.core :as md]
            [clojure.java.io :as io]
            crazyhat.util)
  (:use crazyhat.util))


(defn handle-update [files srcdir destdir]
  (doseq [f files]
    (let [path (.getPath f)
          ext (extension path)
          newdir (dirname (st/replace path srcdir destdir))
          bn (basename path)]
      (case ext
        "md" (let [new-path (str (appendpath newdir bn) ".html")]
               (io/make-parents new-path)
               (println path "==HTML==>" new-path)
               ;; Actually make the required HTML file
               (let [html (md/md-to-html-string (slurp f))]
                 (spit new-path html)))
        "jpg" (let [new-path (str (appendpath newdir bn) ".jpg")]
                (println path "==JPG==>" new-path)
                (copy-file path new-path))
        (println "Don't know what to do with" path "!!!")))))
        

(defn watcher [root]
  (let [src (appendpath root "markup")
        dest (appendpath root "site")]
    (w/watcher [src]
               (w/rate 100)
               (w/file-filter w/ignore-dotfiles)
               (w/file-filter (w/extensions :md :jpg))
               (w/on-change (fn [fs] (handle-update fs src dest))))))


(defn server [root]
  (letfn [(serve-stat [req]
            (print "Serving" (:uri req) ": ")
            (let [r (resp/file-response (:uri req)
                                        {:root (appendpath root "site")})]
              (println r)
              r))]
    (future (j/run-jetty serve-stat {:port 8080 :join? false}))))


(defn -main [ & args ]
  (case (count args)
    0 (println "Please supply a directory for your site toplevel")
    1 (let [[path] args]
        (if (.exists (File. path))
          (do
            (watcher path)
            (server path))
          (println (format "Directory %s doesn't exist!" path))))
    (println "Please supply only one directory name")))
