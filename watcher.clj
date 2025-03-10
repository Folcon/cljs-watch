(use '[clojure.java.io :only [file]])
(require '[cljs.closure :as cljsc])

(do
  (import '[java.util Calendar])
  (import '[java.text SimpleDateFormat])

  (defn text-timestamp []
    (let [c (Calendar/getInstance)
          f (SimpleDateFormat. "HH:mm:ss")]
      (.format f (.getTime c))))
  
  (def default-opts {:optimizations :simple
                     :pretty-print true
                     :output-dir "resources/public/cljs/"
                     :output-to "resources/public/cljs/bootstrap.js"})

  (def last-compiled (atom 0))

  (defn ext-filter [coll ext]
    (filter (fn [f]
              (let [fname (.getName f)
                    fext (subs fname (inc (.lastIndexOf fname ".")))]
                (and (not= \. (first fname)) (.isFile f) (= fext ext))))
            coll))

  (defn find-cljs [dir]
    (let [dir-files (-> dir file file-seq)]
      (ext-filter dir-files "cljs")))

  (defn compile-cljs [src-dir opts]
    (try 
      (cljsc/build src-dir opts)
      (catch Throwable e
        (.printStackTrace e)))
    (reset! last-compiled (System/currentTimeMillis)))

  (defn newer? [f]
    (let [last-modified (.lastModified f)]
      (> last-modified @last-compiled)))

  (defn files-updated? [dir]
    (some newer? (find-cljs dir)))

  (defn watcher-print [& text]
    (print (str (text-timestamp) ":: watcher ::"))
    (apply print text)
    (flush))

  (defn status-print [text]
    (print "    " text "\n")
    (flush))

  (defn transform-cl-args
    [args]
    (let [source (first args)
          opts-string (apply str (interpose " " (rest args)))
          options (when (> (count opts-string) 1)
                    (try (read-string opts-string)
                      (catch Exception e (println e))))]
      {:source source :options options}))

  (let [{:keys [source options]} (transform-cl-args *command-line-args*)
        src-dir (or source "src/")
        opts (merge default-opts options)]
    (watcher-print "Building ClojureScript files in ::" src-dir)
    (compile-cljs src-dir opts)
    (status-print "[done]")
    (watcher-print "Waiting for changes\n")
    (while true
      (Thread/sleep 1000)
      (when (files-updated? src-dir)
        (watcher-print "Compilingupdated files...")
        (compile-cljs src-dir opts)
        (status-print "[done]")))))
