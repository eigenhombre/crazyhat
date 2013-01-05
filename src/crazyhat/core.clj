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


;; FIXME: decompose/decomplect this more functionally, into the following stages:
;; - determine destination paths
;; - process files individually based on extension
(defn handle-update [files srcdir destdir & more]
  (doseq [f files]
    (let [verbose (:verbose (apply hash-map more))
          path (.getPath f)
          ext (extension path)
          newdir (dirname (st/replace path srcdir destdir))
          bn (basename path)
          get-new-path (fn [ext] (str (pathjoin newdir bn) "." ext))]
      (case ext
        "md" (let [new-path (get-new-path "html")]
               (io/make-parents new-path)
               (if verbose (println path "==HTML==>" new-path))
               ;; Actually make the required HTML file
               (let [html (md/md-to-html-string (slurp f))]
                 (spit new-path html)))
        ("jpg" "png" "css") (let [new-path (str (pathjoin newdir bn) "." ext)]
                              (if verbose (println path "==copy==>" new-path))
                              (copy-file path new-path))
        (println "Don't know what to do with" path "!!!")))))
        

(def extensions-to-update [:md :jpg :png :css])

(defn watcher [root]
  (let [src (pathjoin root "markup")
        dest (pathjoin root "site")]
    (w/watcher [src]
               (w/rate 100)
               (w/file-filter w/ignore-dotfiles)
               (w/file-filter (apply w/extensions extensions-to-update))
               (w/on-change (fn [fs] (handle-update fs src dest :verbose true))))))


(defn server [root]
  (letfn [(serve-stat [req]
            (print "Serving" (:uri req) ": ")
            (let [r (resp/file-response (:uri req)
                                        {:root (pathjoin root "site")})]
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
