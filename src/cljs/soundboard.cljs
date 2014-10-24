(ns soundboardloop)

(def $ js/$)

(defn log [text]
  (.log js/console (pr-str text)))
(defn timeout [frequency function]
  (.setTimeout js/window
               function
               frequency))

(defrecord Sound [el])

(defn audio-element [sound] 
  (-> ($ (:el sound)) (.children "audio") .first $))

(defn filename [sound]
  (-> ($ (:el sound)) (.data "filename")))

(defn play [sound] 
  (let [fp (fn [_ el] 
             (log (str "currentTime " (.-currentTime el)))
             ;(set! (.-currentTime el) 0)
             (.load el)
             (log "Playing")
             (.play el))]
    (-> sound audio-element (.each fp))))

(defprotocol SoundEnum
  "Returns the next sound"
  (nextSound [this] "Returns the next sound and new sound enum"))

(def pause (Sound. nil))

(defrecord Beat [sound]
  SoundEnum
  (nextSound [this] {:sound sound :enum nil}))
(def beat? #(instance? Beat %))

(defrecord Loop [beats]
  SoundEnum
  (nextSound [this]
    (let [[b & bs] beats
          {sound :sound} b]
      {:sound sound :enum (Loop. (concat bs [b]))})))

(def loop? #(instance? Loop %))

(defn play-loop [loop]
  (let [{s :sound n :enum} (nextSound loop)]
    (log s)
    (play s)
    (log (filename s))
    (log loop)
    (timeout 1000 #(play-loop n))))


(def all-sounds (->> ($ ".sounds > .sound")
                  .toArray
                  (map #(Sound. %))))
(log all-sounds)
(log (first all-sounds))
(def first-sound (first all-sounds))
;(log (audio-element first-sound))
;(play first-sound)

(def default-sounds (map #(Sound. %)
                         (take 20 (range))))
(def loop-all (Loop. (map #(Beat. %) all-sounds)))

;(def loop-1 (Loop. (->> default-sounds (drop 5) (take 3))))
;(def loop-2 (Loop. (->> default-sounds (drop 10) (take 5))))
;(def loop-3 (Loop. (list loop-1 loop-2)))

;(log pause)
;(log loop-1)
;(log loop-2)
;(log loop-3)
;(log (map loop? (:beats loop-3)))
;(log default-sounds)
;(timeout 1000 #(log "hello"))
(play-loop loop-all)

