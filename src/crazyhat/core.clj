(ns crazyhat.core
  (:gen-class)
  (:import java.io.File)
  (:require [watchtower.core :as w]
            [ring.adapter.jetty :as j]
            [ring.util.response :as resp]
            [clojure.string :as st]
            [markdown.core :as md]
            [clojure.java.io :as io]))


(defn new-file-name [p srcdir destdir]
  (-> p
      (st/replace-first srcdir destdir)
      (st/replace-first #"\.[^\.]+?$" ".html")))

(defn dirname [path]
  (if (.contains path "/")
    (st/join "/" (drop-last (.split path "\\/")))
    ""))

(defn extension [path]
  (let [no-dir (last (.split path "\\/"))]
    (if (.contains no-dir ".")
      (last (.split no-dir "\\.")))))


(defn handle-update [files srcdir destdir]
  (doseq [f files]
    (let [path (.getPath f)]
      (case (extension path)
        "md" (let [new-path (new-file-name path srcdir destdir)]
               ;; Make parents if needed
               (if (io/make-parents new-path)
                 (println "Made parent dir(s) for" new-path))
               (println "Updating" path "--->" new-path)
               ;; Actually make the required HTML file
               (let [html (md/md-to-html-string (slurp f))]
                 (spit new-path html)))
        "jpg" (println "Found a jpg file" path)
        (println "Don't know what to do with" path "!!!")))))
        

(defn watcher [root]
  (let [src (str root "/markup")
        dest (str root "/site")]
    (w/watcher [src]
               (w/rate 100)
               (w/file-filter w/ignore-dotfiles)
               (w/file-filter (w/extensions :md :jpg))
               (w/on-change (fn [fs] (handle-update fs src dest))))))


(defn server [root]
  (letfn [(serve-stat [req]
            (print "Serving" (:uri req) ": ")
            (let [r (resp/file-response (:uri req) {:root (str root "/site")})]
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
