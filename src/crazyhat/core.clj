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

(defn new-extension
  [x]
  (case x
    "md" "html"
    x))

(defn new-filenames-and-extensions
  [files srcdir destdir]
  (map (fn [F]
         (let [path (.getPath F)
               ext (extension path)
               newdir (dirname (st/replace path srcdir destdir))
               basedir (basename path)
               newext (new-extension ext)
               newfile (str (pathjoin newdir basedir) "." newext)]
           [path newfile newext]))
       files))

(defn handle-copy!
  [oldfile newfile verbose]
  (if verbose (println oldfile "==copy==>" newfile))
  (copy-file oldfile newfile))

(defn handle-html!
  [oldfile newfile verbose]
  (if verbose (println oldfile "==HTML==>" newfile))
  (->> oldfile
       (slurp)
       (md/md-to-html-string)
       (spit newfile)))

(defn handle-update
  [files srcdir destdir & more]
  (let [verbose (:verbose (apply hash-map more))
        newfiles (new-filenames-and-extensions files srcdir destdir)]
    (doseq [[oldfile newfile ext] newfiles]
      (io/make-parents newfile)
      (case ext
        "html" (handle-html! oldfile newfile verbose)
        ("jpg" "png" "css") (handle-copy! oldfile newfile verbose)
        (println "Don't know what to do with" oldfile "!!!")))))        

(def extensions-to-update [:md :jpg :png :css])

(defn watcher
  [root]
  (let [src (pathjoin root "markup")
        dest (pathjoin root "site")]
    (w/watcher [src]
               (w/rate 100)
               (w/file-filter w/ignore-dotfiles)
               (w/file-filter (apply w/extensions extensions-to-update))
               (w/on-change (fn [fs] (handle-update fs src dest :verbose true))))))


(defn server
  [root]
  (letfn [(serve-stat [req]
            (print "Serving" (:uri req) ": ")
            (let [r (resp/file-response (:uri req)
                                        {:root (pathjoin root "site")})]
              (println r)
              r))]
    (future (j/run-jetty serve-stat {:port 8080 :join? false}))))


(defn -main
  [ & args ]
  (case (count args)
    0 (println "Please supply a directory for your site toplevel")
    1 (let [[path] args]
        (if (.exists (File. path))
          (do
            (watcher path)
            (server path))
          (println (format "Directory %s doesn't exist!" path))))
    (println "Please supply only one directory name")))
