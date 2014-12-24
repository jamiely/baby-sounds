(ns soundboardloop)

(def $ js/$)

(defn log [text]
  (.log js/console (pr-str text)))
(defn timeout [frequency function]
  (.setTimeout js/window
               function
               frequency))

(defrecord Sound [el])

(defn copy-sound [sound]
  (let [copy (-> sound :el $ (.clone true) (.appendTo "body"))]
    (Sound. copy)))

(defn audio-element [sound] 
  (-> ($ (:el sound)) (.children "audio") .first $))

(defn filename [sound]
  (-> ($ (:el sound)) (.data "filename")))

(defn play [sound] 
  (let [fp (fn [_ el] 
             (.load el)
             (.play el))]
    (-> sound audio-element (.each fp))))

(defprotocol SoundEnum
  "Returns the next sound"
  (next-sound [this] "Returns the next sound and new sound enum"))

(def pause (Sound. nil))

(defrecord Beat [sound]
  SoundEnum
  (next-sound [this] {:sound sound :enum nil}))
(def beat? #(instance? Beat %))

(defrecord Loop [beats]
  SoundEnum
  (next-sound [this]
    (let [[b & bs] beats
          {sound :sound} b]
      {:sound sound :enum (Loop. (concat bs [b]))})))

(defn element-sounds
  "Extracts the sounds from an element"
  [parent]
  (let [els (-> parent $ (.children ".touch-sound") .toArray)
        sounds (map #(Sound. %) els)]
    sounds))

; "Plays a loop based on a container element, keeping track of the current index"
(defrecord ElementLoop 
  [element index]
  SoundEnum
  (next-sound [this]
    (let [sounds (element-sounds element)]
      (if (empty? sounds)
        {:sound nil :enum (ElementLoop. element 0)}
        (let [nth-sound (try 
                          (nth sounds index)
                          (catch :default e nil))
              [sound next-index] (if (nil? nth-sound)
                                   [nil 0]
                                   [nth-sound (+ 1 index)])]
          (.log js/console sounds)
          (log next-index)
          {:sound sound :enum (ElementLoop. element next-index)})))))

; we need a loop that can be based on a loop element.
; when calling next sound, it should check to see if
; the loop definition is the same. Sameness should be
; based on the sequence of sound filenames defined in
; the loop. If the loop detects a change, then it will
; restart from the beginning.

(defn copy-beat [{sound :sound}]
  (Beat. (copy-sound sound)))

(defn copy-loop [{beats :beats}]
  (Loop. (map copy-beat beats)))

(def loop? #(instance? Loop %))

(defn play-loop [loop]
  (if (nil? loop)
    (log "Loop is nil")
    (let [{s :sound n :enum} (next-sound loop)]
      (if (nil? s)
        (log "No sound available")
        (play s))
      ;(log (filename s))
      (timeout 1000 #(play-loop n)))))

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
(def loop-all (Loop. (map #(Beat. (copy-sound %)) all-sounds)))
(def loop-all2 (copy-loop loop-all))

(defn loop1 [] (ElementLoop. ".loop:first" 0))

(defn setup-drag-drop []
  (log "setting up drag and drop")
  (defn prevent [e]
    (if (.-preventDefault e)
      (.preventDefault e)))
  (defn stop [e]
    (if (.-stopPropagation e)
      (.stopPropagation e)))

  (defn sortables []
    (-> ".loop" $))

  (defn refresh-sortables []
    (-> (sortables) (.sortable "refresh")))

  (defn cancel-button []
    (-> "<div>X</div>" $ (.addClass "delete")))

  (defn on-drop [event ui]
    ;(prevent event)
    ;(stop event)
    (.log js/console ui)
    (.log js/console event)
    (let [t (-> event .-target $)
          p (-> ui .-draggable .parent $)]
      ;(refresh-sortables)
      (log "on drop")
      (if (-> p (.is ".loop"))
        (log "parent is loop so don't do anything")
        (let [h (-> ui .-helper $ .clone 
                  (.removeAttr "style") (.removeClass "draggable")
                  (.removeClass "drag-helper")
                  (.append (cancel-button)))]
          (-> t (.append h)))))
    (log "loop1")
    (log (loop1))
    (.log js/console (loop1)))

  ;(.log js/console (.-draggable ($ ".touch-sound")))
  (-> ".loop" $ (.droppable #js {:drop on-drop}))
  (-> "#soundboard .touch-sound" $ 
    (.draggable #js {
                     :helper (fn [e] 
                               (.log js/console e)
                               (-> e .-target $ .clone (.addClass "drag-helper")))
                     :start (fn []
                              (log "start")
                              (-> ".loop" $ (.addClass "drop-target")))
                     :stop (fn []
                             (log "stop")
                             (-> ".loop" $ (.removeClass "drop-target")))}))
  (let [s (sortables)]
    (.sortable s)
    (.disableSelection s))

  (defn delete-loop-sound [e]
    (-> e .-target $ .parent .remove)
    (.log js/console e))

  (-> js/document $ (.on "click" ".touch-sound > .delete" delete-loop-sound))

  ;(on "drop" ".touch-sound-placeholder" handle-drop)
  ;(on-loop "drop" handle-drop)
  ;(on-loop "dragover" handle-dragover)
  ;(on-loop "dragend" handle-dragend)
  ;(on "dragstart" ".touch-sound" handle-dragstart)
  )

(defn play-all-element-loops []
  (doall (map play-loop (all-element-loops))))

(defn all-element-loops []
  (->> ".loop" $ .toArray (map #(ElementLoop. % 0))))

(defn hide-loop-headers []
  (->> ".loop-container h2" $ .hide))

(defn ready []
  (setup-drag-drop)
  (log "ready")
  (play-all-element-loops)
  (hide-loop-headers))

($ ready)

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
;(play-loop loop-all)
;(timeout 1000 #(play-loop loop-all2))

